package mx.edu.utez.pigestiontutorias.utils;

import mx.edu.utez.pigestiontutorias.models.AtencionAlumnoDTO;
import mx.edu.utez.pigestiontutorias.models.AvanceTutorGrupal;
import mx.edu.utez.pigestiontutorias.models.CanalizacionAlumnoDTO;
import mx.edu.utez.pigestiontutorias.models.ReporteExportDatos;
import mx.edu.utez.pigestiontutorias.models.dao.ReportesDao;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ReporteExcelBuilder {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color VERDE_INSTITUCIONAL = new Color(0, 139, 116);

    public void generar(OutputStream salida, ReporteExportDatos datos) throws java.io.IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            CellStyle estiloEncabezado = crearEstiloEncabezado(libro);
            CellStyle estiloTitulo = crearEstiloTitulo(libro);
            CellStyle estiloEtiqueta = crearEstiloEtiqueta(libro);

            construirResumenGlobal(libro, datos, estiloEncabezado, estiloTitulo, estiloEtiqueta);
            construirTutoriasGrupales(libro, datos, estiloEncabezado, estiloTitulo);
            construirAlumnosAtendidos(libro, datos, estiloEncabezado, estiloTitulo);
            construirCanalizaciones(libro, datos, estiloEncabezado, estiloTitulo);

            libro.write(salida);
        }
    }

    private CellStyle crearEstiloEncabezado(XSSFWorkbook libro) {
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());

        XSSFCellStyle estilo = libro.createCellStyle();
        estilo.setFillForegroundColor(new XSSFColor(VERDE_INSTITUCIONAL, new DefaultIndexedColorMap()));
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setFont(fuente);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        return estilo;
    }

    private CellStyle crearEstiloTitulo(XSSFWorkbook libro) {
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 13);
        CellStyle estilo = libro.createCellStyle();
        estilo.setFont(fuente);
        return estilo;
    }

    private CellStyle crearEstiloEtiqueta(XSSFWorkbook libro) {
        Font fuente = libro.createFont();
        fuente.setBold(true);
        CellStyle estilo = libro.createCellStyle();
        estilo.setFont(fuente);
        return estilo;
    }

    private Row nuevaFila(Sheet hoja, int indice) {
        return hoja.createRow(indice);
    }

    private void celda(Row fila, int columna, String valor) {
        fila.createCell(columna).setCellValue(valor != null ? valor : "");
    }

    private void celda(Row fila, int columna, double valor) {
        fila.createCell(columna).setCellValue(valor);
    }

    private void encabezados(Sheet hoja, int indiceFila, CellStyle estilo, String... titulos) {
        Row fila = nuevaFila(hoja, indiceFila);
        for (int i = 0; i < titulos.length; i++) {
            Cell c = fila.createCell(i);
            c.setCellValue(titulos[i]);
            c.setCellStyle(estilo);
        }
    }

    private void autoajustarColumnas(Sheet hoja, int cantidadColumnas) {
        for (int i = 0; i < cantidadColumnas; i++) {
            hoja.autoSizeColumn(i);
        }
    }

    private void construirResumenGlobal(XSSFWorkbook libro, ReporteExportDatos datos, CellStyle estiloEncabezado,
                                        CellStyle estiloTitulo, CellStyle estiloEtiqueta) {
        Sheet hoja = libro.createSheet("Resumen Global");
        ReportesDao.ReporteResumen r = datos.getResumen();
        int i = 0;

        Row filaTitulo = nuevaFila(hoja, i++);
        Cell tituloCelda = filaTitulo.createCell(0);
        tituloCelda.setCellValue("Reporte Global de Tutorías - Resumen");
        tituloCelda.setCellStyle(estiloTitulo);

        Row filaPeriodo = nuevaFila(hoja, i++);
        celda(filaPeriodo, 0, "Periodo:");
        filaPeriodo.getCell(0).setCellStyle(estiloEtiqueta);
        celda(filaPeriodo, 1, datos.getTituloPeriodo());

        if (datos.getDatosAlumno() == null) {
            Row filaFiltros = nuevaFila(hoja, i++);
            celda(filaFiltros, 0, "Filtros:");
            filaFiltros.getCell(0).setCellStyle(estiloEtiqueta);
            celda(filaFiltros, 1, "Carrera: " + orTodas(datos.getNombreCarrera())
                    + " | Cuatrimestre: " + orTodos(datos.getNombreCuatrimestre())
                    + " | Grupo: " + orTodos(datos.getNombreGrupo())
                    + " | Tutor: " + orTodos(datos.getNombreTutor())
                    + (datos.getNombreAlumno() != null && !datos.getNombreAlumno().isBlank()
                    ? " | Alumno: " + datos.getNombreAlumno() : ""));
        }
        i++;

        i = agregarDatosAlumno(hoja, datos, i, estiloEncabezado, estiloEtiqueta);

        encabezados(hoja, i++, estiloEncabezado, "Indicador", "Cantidad");
        celda(nuevaFila(hoja, i), 0, "Alumnos Atendidos");
        celda(hoja.getRow(i++), 1, r.totalAtendidos);
        celda(nuevaFila(hoja, i), 0, "Canalizados");
        celda(hoja.getRow(i++), 1, r.totalCanalizados);
        celda(nuevaFila(hoja, i), 0, "Tutorías Grupales (Completadas)");
        celda(hoja.getRow(i++), 1, r.totalGruposAtendidos);
        celda(nuevaFila(hoja, i), 0, "Pendientes");
        celda(hoja.getRow(i++), 1, r.totalPendientes);
        i++;

        encabezados(hoja, i++, estiloEncabezado, "Distribución de Alumnos Canalizados", "Total");
        for (Map.Entry<String, Integer> entrada : r.distribucionCanalizados.entrySet()) {
            Row fila = nuevaFila(hoja, i++);
            celda(fila, 0, entrada.getKey());
            celda(fila, 1, entrada.getValue());
        }
        i++;

        encabezados(hoja, i++, estiloEncabezado, "Estado de Solicitudes de Asesoría (General)", "Total");
        Row filaPend = nuevaFila(hoja, i++);
        celda(filaPend, 0, "Pendientes");
        celda(filaPend, 1, r.totalPendientes);
        Row filaAten = nuevaFila(hoja, i++);
        celda(filaAten, 0, "Atendidas");
        celda(filaAten, 1, r.totalAtendidos);
        Row filaCana = nuevaFila(hoja, i++);
        celda(filaCana, 0, "Canalizadas");
        celda(filaCana, 1, r.totalCanalizados);
        i += 2;

        i = insertarGrafica(libro, hoja, datos.getImagenPastel(), i, 0, 4, estiloEtiqueta,
                "Gráfica: Distribución de Alumnos Canalizados");
        i = insertarGrafica(libro, hoja, datos.getImagenBarras(), i, 0, 4, estiloEtiqueta,
                "Gráfica: Estado de Solicitudes de Asesoría");

        autoajustarColumnas(hoja, 2);
    }

    private int agregarDatosAlumno(Sheet hoja, ReporteExportDatos datos, int indiceFila,
                                   CellStyle estiloEncabezado, CellStyle estiloEtiqueta) {
        ReporteExportDatos.DatosAcademicosAlumno da = datos.getDatosAlumno();
        if (da == null) return indiceFila;

        int i = indiceFila;

        encabezados(hoja, i++, estiloEncabezado, "Datos del Alumno", "");

        String nombreMatricula = (da.getNombreCompleto() != null ? da.getNombreCompleto() : "")
                + (da.getMatricula() != null && !da.getMatricula().isBlank() ? " (" + da.getMatricula() + ")" : "");
        i = filaEtiquetaValor(hoja, i, estiloEtiqueta, "Alumno:", nombreMatricula);
        i = filaEtiquetaValor(hoja, i, estiloEtiqueta, "Carrera:", orTodas(da.getCarrera()));
        i = filaEtiquetaValor(hoja, i, estiloEtiqueta, "Nivel:", orTodos(da.getNivel()));
        i = filaEtiquetaValor(hoja, i, estiloEtiqueta, "Cuatrimestre y Grupo:", orTodos(da.getCuatrimestreGrupo()));
        i = filaEtiquetaValor(hoja, i, estiloEtiqueta, "Generación:", orTodos(da.getGeneracion()));

        return i + 1;
    }

    private int filaEtiquetaValor(Sheet hoja, int indiceFila, CellStyle estiloEtiqueta, String etiqueta, String valor) {
        Row fila = nuevaFila(hoja, indiceFila);
        celda(fila, 0, etiqueta);
        fila.getCell(0).setCellStyle(estiloEtiqueta);
        celda(fila, 1, valor);
        return indiceFila + 1;
    }

    private int insertarGrafica(XSSFWorkbook libro, Sheet hoja, byte[] imagenPng, int filaInicio,
                                int colInicio, int colFin, CellStyle estiloEtiqueta, String etiqueta) {
        if (imagenPng == null || imagenPng.length == 0) {
            return filaInicio;
        }

        Row filaEtiqueta = nuevaFila(hoja, filaInicio);
        celda(filaEtiqueta, colInicio, etiqueta);
        filaEtiqueta.getCell(colInicio).setCellStyle(estiloEtiqueta);

        int filaImagenInicio = filaInicio + 1;
        int filaImagenFin = filaImagenInicio + 18;

        int idImagen = libro.addPicture(imagenPng, Workbook.PICTURE_TYPE_PNG);
        XSSFDrawing dibujo = (XSSFDrawing) hoja.createDrawingPatriarch();
        XSSFClientAnchor ancla = dibujo.createAnchor(0, 0, 0, 0, colInicio, filaImagenInicio, colFin, filaImagenFin);
        dibujo.createPicture(ancla, idImagen);

        return filaImagenFin + 1;
    }

    private void construirTutoriasGrupales(XSSFWorkbook libro, ReporteExportDatos datos, CellStyle estiloEncabezado,
                                           CellStyle estiloTitulo) {
        Sheet hoja = libro.createSheet("Tutorías Grupales");
        int i = 0;

        Row filaTitulo = nuevaFila(hoja, i++);
        Cell tituloCelda = filaTitulo.createCell(0);
        String periodo = datos.getPeriodoVigenteNombre() != null
                ? "Periodo vigente: " + datos.getPeriodoVigenteNombre() + " — Objetivo: " + datos.getObjetivoGrupal() + " tutorías grupales por tutor"
                : "No hay un periodo escolar vigente";
        tituloCelda.setCellValue(periodo);
        tituloCelda.setCellStyle(estiloTitulo);
        i++;

        encabezados(hoja, i++, estiloEncabezado, "Nombre del Tutor", "Grupo Asignado", "Total de Tutorías Impartidas", "Estatus");
        for (AvanceTutorGrupal a : datos.getAvanceGrupal()) {
            Row fila = nuevaFila(hoja, i++);
            celda(fila, 0, a.getNombreTutor());
            celda(fila, 1, a.getGrupoAsignado());
            celda(fila, 2, a.getRealizadas() + " de " + a.getObjetivo());
            celda(fila, 3, traducirEstatusGrupal(a.getEstatus()));
        }

        autoajustarColumnas(hoja, 4);
    }

    private void construirAlumnosAtendidos(XSSFWorkbook libro, ReporteExportDatos datos, CellStyle estiloEncabezado,
                                           CellStyle estiloTitulo) {
        Sheet hoja = libro.createSheet("Alumnos Atendidos");
        int i = 0;

        encabezados(hoja, i++, estiloEncabezado, "Tipo", "Fecha", "Hora", "Grupo", "Matrícula", "Alumno",
                "Estado", "Temas Tratados", "Acuerdos", "Vínculo Directo");
        for (AtencionAlumnoDTO a : datos.getAtenciones()) {
            Row fila = nuevaFila(hoja, i++);
            celda(fila, 0, a.getTipo());
            celda(fila, 1, a.getFecha() != null ? a.getFecha().toLocalDate().format(FORMATO_FECHA) : "");
            celda(fila, 2, a.getHora());
            celda(fila, 3, a.getGrupoAsignado());
            celda(fila, 4, a.getMatricula());
            celda(fila, 5, a.getNombreAlumno());
            celda(fila, 6, a.getEstado());
            celda(fila, 7, a.getTemasTratados());
            celda(fila, 8, a.getAcuerdos());
            celda(fila, 9, a.getVinculoDirecto() != null && !a.getVinculoDirecto().isBlank() ? a.getVinculoDirecto() : "N/A");
        }

        autoajustarColumnas(hoja, 10);
    }

    private void construirCanalizaciones(XSSFWorkbook libro, ReporteExportDatos datos, CellStyle estiloEncabezado,
                                         CellStyle estiloTitulo) {
        Sheet hoja = libro.createSheet("Canalizaciones");
        int i = 0;

        encabezados(hoja, i++, estiloEncabezado, "Alumno", "Matrícula", "Grupo", "Fecha de Canalización",
                "Tutor", "Área de Canalización", "Motivo", "Estatus", "Observaciones");
        for (CanalizacionAlumnoDTO c : datos.getCanalizaciones()) {
            Row fila = nuevaFila(hoja, i++);
            celda(fila, 0, c.getNombreAlumno());
            celda(fila, 1, c.getMatricula());
            celda(fila, 2, c.getGrupoAsignado());
            celda(fila, 3, c.getFechaCanalizacion() != null ? c.getFechaCanalizacion().toLocalDate().format(FORMATO_FECHA) : "");
            celda(fila, 4, c.getNombreTutor() != null ? c.getNombreTutor() : "N/D");
            celda(fila, 5, c.getNombreArea());
            celda(fila, 6, c.getNombreMotivo() != null ? c.getNombreMotivo() : "");
            celda(fila, 7, c.getEstatus());
            celda(fila, 8, c.getObservaciones() != null ? c.getObservaciones() : "");
        }

        autoajustarColumnas(hoja, 9);
    }

    private String traducirEstatusGrupal(String estatus) {
        if ("AL_DIA".equals(estatus)) return "Al día";
        if ("RIESGO".equals(estatus)) return "En Riesgo";
        return "Sin objetivo";
    }

    private String orTodas(String valor) {
        return (valor == null || valor.isBlank()) ? "Todas" : valor;
    }

    private String orTodos(String valor) {
        return (valor == null || valor.isBlank()) ? "Todos" : valor;
    }
}
