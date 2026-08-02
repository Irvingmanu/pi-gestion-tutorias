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
            // El usuario es tutor: siempre se filtra por si mismo, sin importar
            // que venga un idTutor distinto en la URL (evita que un tutor vea datos de otro).
            idTutorFiltro = tutorSesion.getIdTutor();
        } else {
            // El usuario es coordinador (u otro rol sin tutor asociado):
            // puede elegir un tutor especifico desde el select, o dejarlo vacio para ver todo.
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
        if ("csv".equalsIgnoreCase(formato)) {
            String nombreCarrera = request.getParameter("nombreCarrera");
            String nombreCuatrimestre = request.getParameter("nombreCuatrimestre");
            String nombreGrupo = request.getParameter("nombreGrupo");
            String nombreTutor = request.getParameter("nombreTutor");
            exportarCsv(response, reporte, desde, hasta, tieneFiltroFechas,
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