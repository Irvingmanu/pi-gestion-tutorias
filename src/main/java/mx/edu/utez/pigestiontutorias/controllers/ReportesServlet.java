package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.ReportesDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Chunk;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;

@WebServlet("/ReportesServlet")
public class ReportesServlet extends HttpServlet {

    private final TutorDao tutorDao = new TutorDao();
    private final ReportesDao reportesDao = new ReportesDao();

    private static final LocalDate FECHA_DEFAULT_DESDE = LocalDate.of(2000, 1, 1);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_ARCHIVO = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        Integer idTutorFiltro;

        Tutor tutorSesion = tutorDao.findByIdUsuario(idUsuario);
        if (tutorSesion != null) {
            idTutorFiltro = tutorSesion.getIdTutor();
        } else {
            idTutorFiltro = parseIntOrNull(request.getParameter("idTutor"));
        }

        Integer idCuatrimestre = parseIntOrNull(request.getParameter("idCuatrimestre"));
        Integer idLetraGrupo = parseIntOrNull(request.getParameter("idLetraGrupo"));
        Integer idCarrera = parseIntOrNull(request.getParameter("idCarrera"));

        String desdeParam = request.getParameter("desde");
        String hastaParam = request.getParameter("hasta");
        boolean tieneFiltroFechas = (desdeParam != null && !desdeParam.isBlank())
                || (hastaParam != null && !hastaParam.isBlank());

        LocalDate desde = parseFechaOrDefault(desdeParam, FECHA_DEFAULT_DESDE);
        LocalDate hasta = parseFechaOrDefault(hastaParam, LocalDate.now());

        ReportesDao.ReporteResumen reporte = reportesDao.generarReporte(
                idTutorFiltro, idCarrera, idCuatrimestre, idLetraGrupo, null, desde, hasta);

        String formato = request.getParameter("formato");
        String nombreCarrera = request.getParameter("nombreCarrera");
        String nombreCuatrimestre = request.getParameter("nombreCuatrimestre");
        String nombreGrupo = request.getParameter("nombreGrupo");
        String nombreTutor = request.getParameter("nombreTutor");

        if ("csv".equalsIgnoreCase(formato)) {
            exportarCsv(response, reporte, desde, hasta, tieneFiltroFechas,
                    nombreCarrera, nombreCuatrimestre, nombreGrupo, nombreTutor);
            return;
        } else if ("excel".equalsIgnoreCase(formato)) {
            exportarExcel(response, reporte, desde, hasta, tieneFiltroFechas,
                    nombreCarrera, nombreCuatrimestre, nombreGrupo, nombreTutor);
            return;
        } else if ("pdf".equalsIgnoreCase(formato)) {
            exportarPdf(response, reporte, desde, hasta, tieneFiltroFechas,
                    nombreCarrera, nombreCuatrimestre, nombreGrupo, nombreTutor);
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"totalAtendidos\":").append(reporte.totalAtendidos).append(",");
        json.append("\"totalPidieronTutorias\":").append(reporte.totalPidieronTutorias).append(",");
        json.append("\"totalCanalizados\":").append(reporte.totalCanalizados).append(",");
        json.append("\"totalPendientes\":").append(reporte.totalPendientes).append(",");
        json.append("\"totalGruposAtendidos\":").append(reporte.totalGruposAtendidos).append(",");
        json.append("\"totalAsistencias\":").append(reporte.totalAsistencias).append(",");
        json.append("\"distribucionCanalizados\":[");

        boolean primero = true;
        for (Map.Entry<String, Integer> entrada : reporte.distribucionCanalizados.entrySet()) {
            if (!primero) json.append(",");
            json.append("{\"nombreServicio\":\"").append(escaparJson(entrada.getKey())).append("\",");
            json.append("\"totalAbsoluto\":").append(entrada.getValue()).append("}");
            primero = false;
        }

        json.append("]");
        json.append("}");

        out.print(json);
        out.flush();
    }

    // ==================== CSV ====================
    private void exportarCsv(HttpServletResponse response, ReportesDao.ReporteResumen reporte,
                             LocalDate desde, LocalDate hasta, boolean tieneFiltroFechas,
                             String nombreCarrera, String nombreCuatrimestre, String nombreGrupo,
                             String nombreTutor) throws IOException {

        String nombreArchivo;
        String tituloPeriodo;

        if (tieneFiltroFechas) {
            nombreArchivo = "reporte_tutorias_" + desde.format(FORMATO_FECHA_ARCHIVO)
                    + "_a_" + hasta.format(FORMATO_FECHA_ARCHIVO) + ".csv";
            tituloPeriodo = desde.format(FORMATO_FECHA) + " a " + hasta.format(FORMATO_FECHA);
        } else {
            nombreArchivo = "reporte_tutorias_completo.csv";
            tituloPeriodo = "Historico completo";
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"");

        PrintWriter out = response.getWriter();
        out.write('\uFEFF');

        out.println("Reporte de Tutorias");
        out.println("Periodo," + tituloPeriodo);
        out.println("Cuatrimestre," + (esVacio(nombreCuatrimestre) ? "Todos" : nombreCuatrimestre));
        out.println("Grupo," + (esVacio(nombreGrupo) ? "Todos" : nombreGrupo));
        out.println("Carrera," + (esVacio(nombreCarrera) ? "Todas" : nombreCarrera));
        out.println("Tutor," + (esVacio(nombreTutor) ? "Todos" : nombreTutor));
        out.println();

        out.println("Indicador,Cantidad");
        out.println("Alumnos Atendidos," + reporte.totalAtendidos);
        out.println("Pidieron Tutoria," + reporte.totalPidieronTutorias);
        out.println("Canalizaciones," + reporte.totalCanalizados);
        out.println("Pendientes," + reporte.totalPendientes);
        out.println("Grupos Atendidos," + reporte.totalGruposAtendidos);
        out.println("Asistencias," + reporte.totalAsistencias);

        out.println();
        out.println("Area de Canalizacion,Total");
        for (Map.Entry<String, Integer> entrada : reporte.distribucionCanalizados.entrySet()) {
            out.println(entrada.getKey() + "," + entrada.getValue());
        }

        out.flush();
    }

    // ==================== EXCEL ====================
    private void exportarExcel(HttpServletResponse response, ReportesDao.ReporteResumen reporte,
                               LocalDate desde, LocalDate hasta, boolean tieneFiltroFechas,
                               String nombreCarrera, String nombreCuatrimestre, String nombreGrupo,
                               String nombreTutor) throws IOException {

        String nombreArchivo;
        String tituloPeriodo;

        if (tieneFiltroFechas) {
            nombreArchivo = "reporte_tutorias_" + desde.format(FORMATO_FECHA_ARCHIVO)
                    + "_a_" + hasta.format(FORMATO_FECHA_ARCHIVO) + ".xlsx";
            tituloPeriodo = desde.format(FORMATO_FECHA) + " a " + hasta.format(FORMATO_FECHA);
        } else {
            nombreArchivo = "reporte_tutorias_completo.xlsx";
            tituloPeriodo = "Historico completo";
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte Global");

            CellStyle tituloStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font tituloFont = workbook.createFont();
            tituloFont.setBold(true);
            tituloFont.setFontHeightInPoints((short) 14);
            tituloStyle.setFont(tituloFont);

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int fila = 0;

            Row tituloRow = sheet.createRow(fila++);
            Cell tituloCell = tituloRow.createCell(0);
            tituloCell.setCellValue("Reporte de Tutorias");
            tituloCell.setCellStyle(tituloStyle);

            fila++;
            fila = agregarFilaTexto(sheet, fila, "Periodo", tituloPeriodo);
            fila = agregarFilaTexto(sheet, fila, "Cuatrimestre", esVacio(nombreCuatrimestre) ? "Todos" : nombreCuatrimestre);
            fila = agregarFilaTexto(sheet, fila, "Grupo", esVacio(nombreGrupo) ? "Todos" : nombreGrupo);
            fila = agregarFilaTexto(sheet, fila, "Carrera", esVacio(nombreCarrera) ? "Todas" : nombreCarrera);
            fila = agregarFilaTexto(sheet, fila, "Tutor", esVacio(nombreTutor) ? "Todos" : nombreTutor);
            fila++;

            Row headerIndicadores = sheet.createRow(fila++);
            crearCeldaConEstilo(headerIndicadores, 0, "Indicador", headerStyle);
            crearCeldaConEstilo(headerIndicadores, 1, "Cantidad", headerStyle);

            fila = agregarFilaTexto(sheet, fila, "Alumnos Atendidos", String.valueOf(reporte.totalAtendidos));
            fila = agregarFilaTexto(sheet, fila, "Pidieron Tutoria", String.valueOf(reporte.totalPidieronTutorias));
            fila = agregarFilaTexto(sheet, fila, "Canalizaciones", String.valueOf(reporte.totalCanalizados));
            fila = agregarFilaTexto(sheet, fila, "Pendientes", String.valueOf(reporte.totalPendientes));
            fila = agregarFilaTexto(sheet, fila, "Grupos Atendidos", String.valueOf(reporte.totalGruposAtendidos));
            fila = agregarFilaTexto(sheet, fila, "Asistencias", String.valueOf(reporte.totalAsistencias));

            fila++;
            Row headerArea = sheet.createRow(fila++);
            crearCeldaConEstilo(headerArea, 0, "Area de Canalizacion", headerStyle);
            crearCeldaConEstilo(headerArea, 1, "Total", headerStyle);

            for (Map.Entry<String, Integer> entrada : reporte.distribucionCanalizados.entrySet()) {
                fila = agregarFilaTexto(sheet, fila, entrada.getKey(), String.valueOf(entrada.getValue()));
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"");

            workbook.write(response.getOutputStream());
        }
    }

    private int agregarFilaTexto(Sheet sheet, int fila, String col1, String col2) {
        Row row = sheet.createRow(fila);
        row.createCell(0).setCellValue(col1);
        row.createCell(1).setCellValue(col2);
        return fila + 1;
    }

    private void crearCeldaConEstilo(Row row, int idx, String valor, CellStyle estilo) {
        Cell cell = row.createCell(idx);
        cell.setCellValue(valor);
        cell.setCellStyle(estilo);
    }

    // ==================== PDF ====================
    private void exportarPdf(HttpServletResponse response, ReportesDao.ReporteResumen reporte,
                             LocalDate desde, LocalDate hasta, boolean tieneFiltroFechas,
                             String nombreCarrera, String nombreCuatrimestre, String nombreGrupo,
                             String nombreTutor) throws IOException {

        String nombreArchivo;
        String tituloPeriodo;

        if (tieneFiltroFechas) {
            nombreArchivo = "reporte_tutorias_" + desde.format(FORMATO_FECHA_ARCHIVO)
                    + "_a_" + hasta.format(FORMATO_FECHA_ARCHIVO) + ".pdf";
            tituloPeriodo = desde.format(FORMATO_FECHA) + " a " + hasta.format(FORMATO_FECHA);
        } else {
            nombreArchivo = "reporte_tutorias_completo.pdf";
            tituloPeriodo = "Historico completo";
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"");

        Document document = new Document(PageSize.LETTER, 40, 40, 50, 50);
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            com.lowagie.text.Font tituloFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD);
            Paragraph titulo = new Paragraph("Reporte de Tutorias", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new Paragraph(" "));

            com.lowagie.text.Font normal = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10);
            com.lowagie.text.Font negrita = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);

            document.add(crearParrafoEtiqueta("Periodo: ", tituloPeriodo, negrita, normal));
            document.add(crearParrafoEtiqueta("Cuatrimestre: ", esVacio(nombreCuatrimestre) ? "Todos" : nombreCuatrimestre, negrita, normal));
            document.add(crearParrafoEtiqueta("Grupo: ", esVacio(nombreGrupo) ? "Todos" : nombreGrupo, negrita, normal));
            document.add(crearParrafoEtiqueta("Carrera: ", esVacio(nombreCarrera) ? "Todas" : nombreCarrera, negrita, normal));
            document.add(crearParrafoEtiqueta("Tutor: ", esVacio(nombreTutor) ? "Todos" : nombreTutor, negrita, normal));
            document.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            agregarFilaPdf(tabla, "Indicador", "Cantidad", true);
            agregarFilaPdf(tabla, "Alumnos Atendidos", String.valueOf(reporte.totalAtendidos), false);
            agregarFilaPdf(tabla, "Pidieron Tutoria", String.valueOf(reporte.totalPidieronTutorias), false);
            agregarFilaPdf(tabla, "Canalizaciones", String.valueOf(reporte.totalCanalizados), false);
            agregarFilaPdf(tabla, "Pendientes", String.valueOf(reporte.totalPendientes), false);
            agregarFilaPdf(tabla, "Grupos Atendidos", String.valueOf(reporte.totalGruposAtendidos), false);
            agregarFilaPdf(tabla, "Asistencias", String.valueOf(reporte.totalAsistencias), false);
            document.add(tabla);

            document.add(new Paragraph(" "));
            com.lowagie.text.Font subtitulo = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD);
            document.add(new Paragraph("Area de Canalizacion", subtitulo));

            PdfPTable tablaArea = new PdfPTable(2);
            tablaArea.setWidthPercentage(100);
            agregarFilaPdf(tablaArea, "Area", "Total", true);
            for (Map.Entry<String, Integer> entrada : reporte.distribucionCanalizados.entrySet()) {
                agregarFilaPdf(tablaArea, entrada.getKey(), String.valueOf(entrada.getValue()), false);
            }
            document.add(tablaArea);

        } catch (DocumentException e) {
            throw new IOException("Error generando PDF", e);
        } finally {
            document.close();
        }
    }

    private Paragraph crearParrafoEtiqueta(String etiqueta, String valor, com.lowagie.text.Font negrita, com.lowagie.text.Font normal) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(etiqueta, negrita));
        p.add(new Chunk(valor, normal));
        return p;
    }

    private void agregarFilaPdf(PdfPTable tabla, String col1, String col2, boolean esHeader) {
        com.lowagie.text.Font f = esHeader
                ? new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.BOLD)
                : new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10);
        PdfPCell c1 = new PdfPCell(new Phrase(col1, f));
        PdfPCell c2 = new PdfPCell(new Phrase(col2, f));
        if (esHeader) {
            c1.setBackgroundColor(new Color(230, 230, 230));
            c2.setBackgroundColor(new Color(230, 230, 230));
        }
        tabla.addCell(c1);
        tabla.addCell(c2);
    }

    // ==================== UTILIDADES ====================
    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String escaparJson(String valor) {
        if (valor == null) return "";
        return valor.replace("\"", "\\\"");
    }

    private Integer parseIntOrNull(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseFechaOrDefault(String valor, LocalDate porDefecto) {
        if (valor == null || valor.isBlank()) return porDefecto;
        try {
            return LocalDate.parse(valor);
        } catch (Exception e) {
            return porDefecto;
        }
    }
}