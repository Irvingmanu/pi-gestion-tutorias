package mx.edu.utez.pigestiontutorias.utils;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import mx.edu.utez.pigestiontutorias.models.AtencionAlumnoDTO;
import mx.edu.utez.pigestiontutorias.models.AvanceTutorGrupal;
import mx.edu.utez.pigestiontutorias.models.CanalizacionAlumnoDTO;
import mx.edu.utez.pigestiontutorias.models.ReporteExportDatos;
import mx.edu.utez.pigestiontutorias.models.dao.ReportesDao;

import java.awt.*;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Utilidad que construye el archivo PDF del reporte ejecutivo global de tutorías,
 * generando el encabezado, resumen ejecutivo, tutorías grupales, tutorías individuales
 * y canalizaciones a partir de los datos agregados en {@link ReporteExportDatos}.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-16
 */
public class ReportePdfBuilder {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color VERDE_INSTITUCIONAL = new Color(0, 139, 116);
    private static final Color AZUL_MARINO = new Color(11, 37, 68);
    private static final Color GRIS_NEUTRO = new Color(107, 114, 128);

    private final Font fuenteTitulo = new Font(Font.HELVETICA, 18, Font.BOLD, AZUL_MARINO);
    private final Font fuenteSubtitulo = new Font(Font.HELVETICA, 11, Font.NORMAL, GRIS_NEUTRO);
    private final Font fuenteSeccion = new Font(Font.HELVETICA, 13, Font.BOLD, AZUL_MARINO);
    private final Font fuenteEncabezadoTabla = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private final Font fuenteCelda = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private final Font fuenteEtiquetaAlumno = new Font(Font.HELVETICA, 9, Font.BOLD, AZUL_MARINO);

    /**
     * Genera el documento PDF completo del reporte ejecutivo y lo escribe en el flujo de salida.
     * @param salida el flujo de salida donde se escribirá el PDF generado
     * @param datos los datos agregados del reporte a exportar
     * @throws DocumentException si ocurre un error al construir o escribir el documento PDF
     */
    public void generar(OutputStream salida, ReporteExportDatos datos) throws DocumentException {
        Document documento = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter.getInstance(documento, salida);
        documento.open();

        agregarEncabezado(documento, datos);
        agregarResumenEjecutivo(documento, datos);
        agregarTutoriasGrupales(documento, datos);
        agregarAtenciones(documento, datos);
        agregarCanalizaciones(documento, datos);

        documento.close();
    }

    /**
     * Agrega el encabezado del documento: título del sistema, subtítulo del reporte,
     * los filtros aplicados (periodo, carrera, cuatrimestre, grupo, tutor, alumno) y,
     * si aplica, los datos del alumno filtrado.
     * @param documento el documento PDF al que se agrega el encabezado
     * @param datos los datos agregados del reporte
     * @throws DocumentException si ocurre un error al agregar los elementos al documento
     */
    private void agregarEncabezado(Document documento, ReporteExportDatos datos) throws DocumentException {
        Paragraph titulo = new Paragraph("Sistema de Gestión de Tutorías", fuenteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);

        Paragraph subtitulo = new Paragraph("Reporte Ejecutivo de Tutorías Globales", fuenteSeccion);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(8f);
        documento.add(subtitulo);

        Paragraph filtros = new Paragraph();
        filtros.setFont(fuenteSubtitulo);
        filtros.setAlignment(Element.ALIGN_CENTER);
        filtros.add("Periodo: " + datos.getTituloPeriodo());
        if (datos.getDatosAlumno() == null) {
            filtros.add("\nCarrera: " + orTodas(datos.getNombreCarrera())
                    + "  |  Cuatrimestre: " + orTodos(datos.getNombreCuatrimestre())
                    + "  |  Grupo: " + orTodos(datos.getNombreGrupo())
                    + "  |  Tutor: " + orTodos(datos.getNombreTutor())
                    + (datos.getNombreAlumno() != null && !datos.getNombreAlumno().isBlank()
                    ? "  |  Alumno: " + datos.getNombreAlumno() : ""));
        }
        filtros.setSpacingAfter(16f);
        documento.add(filtros);

        agregarDatosAlumno(documento, datos);
    }

    /**
     * Agrega, si el reporte está filtrado por un alumno específico, la sección con sus
     * datos académicos (nombre, matrícula, carrera, nivel, cuatrimestre/grupo y generación).
     * @param documento el documento PDF al que se agrega la sección
     * @param datos los datos agregados del reporte
     * @throws DocumentException si ocurre un error al agregar los elementos al documento
     */
    private void agregarDatosAlumno(Document documento, ReporteExportDatos datos) throws DocumentException {
        ReporteExportDatos.DatosAcademicosAlumno da = datos.getDatosAlumno();
        if (da == null) return;

        documento.add(nuevoTituloSeccion("Datos del Alumno"));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(85);
        tabla.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.setWidths(new float[]{1.3f, 2.4f});
        tabla.setSpacingAfter(14f);

        String nombreMatricula = (da.getNombreCompleto() != null ? da.getNombreCompleto() : "")
                + (da.getMatricula() != null && !da.getMatricula().isBlank() ? " (" + da.getMatricula() + ")" : "");
        agregarFilaAlumno(tabla, "Alumno", nombreMatricula);
        agregarFilaAlumno(tabla, "Carrera", orTodas(da.getCarrera()));
        agregarFilaAlumno(tabla, "Nivel", orTodos(da.getNivel()));
        agregarFilaAlumno(tabla, "Cuatrimestre y Grupo", orTodos(da.getCuatrimestreGrupo()));
        agregarFilaAlumno(tabla, "Generación", orTodos(da.getGeneracion()));

        documento.add(tabla);
    }

    /**
     * Agrega a la tabla de datos del alumno una fila con una etiqueta resaltada y su valor.
     * @param tabla la tabla PDF donde agregar la fila
     * @param etiqueta el texto de la etiqueta del campo
     * @param valor el texto del valor asociado a la etiqueta
     */
    private void agregarFilaAlumno(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fuenteEtiquetaAlumno));
        celdaEtiqueta.setBackgroundColor(new Color(232, 243, 240));
        celdaEtiqueta.setPadding(5f);
        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaTexto(valor));
    }

    /**
     * Agrega la sección de resumen ejecutivo con las métricas principales (alumnos
     * atendidos, canalizados, tutorías grupales, pendientes), la distribución de
     * canalizados por área y las gráficas del reporte.
     * @param documento el documento PDF al que se agrega la sección
     * @param datos los datos agregados del reporte
     * @throws DocumentException si ocurre un error al agregar los elementos al documento
     */
    private void agregarResumenEjecutivo(Document documento, ReporteExportDatos datos) throws DocumentException {
        ReportesDao.ReporteResumen r = datos.getResumen();

        documento.add(nuevoTituloSeccion("Resumen Ejecutivo"));

        PdfPTable tablaMetricas = new PdfPTable(4);
        tablaMetricas.setWidthPercentage(100);
        tablaMetricas.setSpacingAfter(14f);
        agregarCabecera(tablaMetricas, "Alumnos Atendidos", "Canalizados", "Tutorías Grupales", "Pendientes");
        tablaMetricas.addCell(celdaValor(String.valueOf(r.totalAtendidos)));
        tablaMetricas.addCell(celdaValor(String.valueOf(r.totalCanalizados)));
        tablaMetricas.addCell(celdaValor(String.valueOf(r.totalGruposAtendidos)));
        tablaMetricas.addCell(celdaValor(String.valueOf(r.totalPendientes)));
        documento.add(tablaMetricas);

        if (!r.distribucionCanalizados.isEmpty()) {
            documento.add(nuevoTituloSeccion("Distribución de Alumnos Canalizados"));
            PdfPTable tablaDistribucion = new PdfPTable(2);
            tablaDistribucion.setWidthPercentage(70);
            tablaDistribucion.setHorizontalAlignment(Element.ALIGN_LEFT);
            tablaDistribucion.setSpacingAfter(14f);
            agregarCabecera(tablaDistribucion, "Área", "Total");
            for (Map.Entry<String, Integer> entrada : r.distribucionCanalizados.entrySet()) {
                tablaDistribucion.addCell(celdaTexto(entrada.getKey()));
                tablaDistribucion.addCell(celdaValor(String.valueOf(entrada.getValue())));
            }
            documento.add(tablaDistribucion);
        }

        agregarGraficas(documento, datos);
    }

    /**
     * Agrega al documento las gráficas de pastel y/o barras del reporte, en una tabla
     * de una o dos columnas según cuáles estén disponibles.
     * @param documento el documento PDF al que se agregan las gráficas
     * @param datos los datos agregados del reporte
     * @throws DocumentException si ocurre un error al agregar las imágenes al documento
     */
    private void agregarGraficas(Document documento, ReporteExportDatos datos) throws DocumentException {
        boolean hayPastel = datos.getImagenPastel() != null && datos.getImagenPastel().length > 0;
        boolean hayBarras = datos.getImagenBarras() != null && datos.getImagenBarras().length > 0;
        if (!hayPastel && !hayBarras) {
            return;
        }

        PdfPTable tablaGraficas = new PdfPTable(hayPastel && hayBarras ? 2 : 1);
        tablaGraficas.setWidthPercentage(100);
        tablaGraficas.setSpacingAfter(14f);

        if (hayPastel) {
            tablaGraficas.addCell(celdaImagen(datos.getImagenPastel()));
        }
        if (hayBarras) {
            tablaGraficas.addCell(celdaImagen(datos.getImagenBarras()));
        }
        documento.add(tablaGraficas);
    }

    /**
     * Construye una celda de tabla sin bordes que contiene una imagen escalada a 240x240 puntos.
     * @param png los bytes de la imagen PNG a insertar
     * @return la celda con la imagen incrustada
     * @throws DocumentException si ocurre un error al cargar o escalar la imagen
     */
    private PdfPCell celdaImagen(byte[] png) throws DocumentException {
        try {
            Image imagen = Image.getInstance(png);
            imagen.scaleToFit(240f, 240f);
            PdfPCell celda = new PdfPCell(imagen, true);
            celda.setBorder(Rectangle.NO_BORDER);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setPadding(6f);
            return celda;
        } catch (java.io.IOException e) {
            throw new DocumentException(e);
        }
    }

    /**
     * Agrega la sección y tabla de tutorías grupales por tutor, mostrando el avance de
     * cada tutor respecto al objetivo del periodo escolar vigente.
     * @param documento el documento PDF al que se agrega la sección
     * @param datos los datos agregados del reporte
     * @throws DocumentException si ocurre un error al agregar los elementos al documento
     */
    private void agregarTutoriasGrupales(Document documento, ReporteExportDatos datos) throws DocumentException {
        documento.add(nuevoTituloSeccion("Tutorías Grupales por Tutor"));

        String periodo = datos.getPeriodoVigenteNombre() != null
                ? "Periodo vigente: " + datos.getPeriodoVigenteNombre() + " — Objetivo: " + datos.getObjetivoGrupal() + " tutorías grupales por tutor"
                : "No hay un periodo escolar vigente";
        Paragraph parPeriodo = new Paragraph(periodo, fuenteSubtitulo);
        parPeriodo.setSpacingAfter(6f);
        documento.add(parPeriodo);

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{3f, 2.5f, 2.5f, 1.6f});
        tabla.setSpacingAfter(14f);
        tabla.setHeaderRows(1);
        agregarCabecera(tabla, "Tutor", "Grupo", "Total de Tutorías Impartidas", "Estatus");

        if (datos.getAvanceGrupal().isEmpty()) {
            PdfPCell vacio = celdaTexto("No hay avance grupal registrado para el periodo vigente.");
            vacio.setColspan(4);
            tabla.addCell(vacio);
        } else {
            for (AvanceTutorGrupal a : datos.getAvanceGrupal()) {
                tabla.addCell(celdaTexto(a.getNombreTutor()));
                tabla.addCell(celdaTexto(a.getGrupoAsignado()));
                tabla.addCell(celdaValor(a.getRealizadas() + " de " + a.getObjetivo()));
                tabla.addCell(celdaTexto(traducirEstatusGrupal(a.getEstatus())));
            }
        }
        documento.add(tabla);
    }

    /**
     * Agrega la sección y tabla de detalle de tutorías individuales, mostrando cada
     * atención registrada con sus temas y acuerdos.
     * @param documento el documento PDF al que se agrega la sección
     * @param datos los datos agregados del reporte
     * @throws DocumentException si ocurre un error al agregar los elementos al documento
     */
    private void agregarAtenciones(Document documento, ReporteExportDatos datos) throws DocumentException {
        documento.add(nuevoTituloSeccion("Detalle de Tutorías Individuales"));

        PdfPTable tabla = new PdfPTable(7);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{1.8f, 1f, 1.2f, 0.9f, 2f, 2f, 2f});
        tabla.setHeaderRows(1);
        agregarCabecera(tabla, "Alumno", "Tipo", "Fecha", "Hora", "Temas Tratados", "Acuerdos", "Vínculo Directo");

        if (datos.getAtenciones().isEmpty()) {
            PdfPCell vacio = celdaTexto("No hay tutorías individuales registradas con los filtros seleccionados.");
            vacio.setColspan(7);
            tabla.addCell(vacio);
        } else {
            for (AtencionAlumnoDTO a : datos.getAtenciones()) {
                tabla.addCell(celdaTexto(a.getNombreAlumno()));
                tabla.addCell(celdaTexto(a.getTipo()));
                tabla.addCell(celdaTexto(a.getFecha() != null ? a.getFecha().toLocalDate().format(FORMATO_FECHA) : ""));
                tabla.addCell(celdaTexto(a.getHora()));
                tabla.addCell(celdaTexto(a.getTemasTratados()));
                tabla.addCell(celdaTexto(a.getAcuerdos()));
                tabla.addCell(celdaTexto(a.getVinculoDirecto() != null && !a.getVinculoDirecto().isBlank()
                        ? a.getVinculoDirecto() : "N/A"));
            }
        }
        documento.add(tabla);
    }

    /**
     * Agrega la sección y tabla de desglose de canalizaciones, mostrando cada canalización
     * de alumno registrada con su estatus y área destino.
     * @param documento el documento PDF al que se agrega la sección
     * @param datos los datos agregados del reporte
     * @throws DocumentException si ocurre un error al agregar los elementos al documento
     */
    private void agregarCanalizaciones(Document documento, ReporteExportDatos datos) throws DocumentException {
        documento.add(nuevoTituloSeccion("Desglose de Canalizaciones"));

        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{2.4f, 1.8f, 1.4f, 2f, 1.8f, 1.4f});
        tabla.setHeaderRows(1);
        agregarCabecera(tabla, "Alumno", "Grupo", "Fecha", "Tutor", "Área", "Estatus");

        if (datos.getCanalizaciones().isEmpty()) {
            PdfPCell vacio = celdaTexto("No hay canalizaciones registradas con los filtros seleccionados.");
            vacio.setColspan(6);
            tabla.addCell(vacio);
        } else {
            for (CanalizacionAlumnoDTO c : datos.getCanalizaciones()) {
                tabla.addCell(celdaTexto(c.getNombreAlumno()));
                tabla.addCell(celdaTexto(c.getGrupoAsignado()));
                tabla.addCell(celdaTexto(c.getFechaCanalizacion() != null ? c.getFechaCanalizacion().toLocalDate().format(FORMATO_FECHA) : ""));
                tabla.addCell(celdaTexto(c.getNombreTutor() != null ? c.getNombreTutor() : "N/D"));
                tabla.addCell(celdaTexto(c.getNombreArea()));
                tabla.addCell(celdaTexto(c.getEstatus()));
            }
        }
        documento.add(tabla);
    }

    /**
     * Crea un párrafo con el estilo de título de sección y espaciado estándar.
     * @param texto el texto del título de sección
     * @return el párrafo con el estilo aplicado
     */
    private Paragraph nuevoTituloSeccion(String texto) {
        Paragraph p = new Paragraph(texto, fuenteSeccion);
        p.setSpacingBefore(6f);
        p.setSpacingAfter(8f);
        return p;
    }

    /**
     * Agrega a la tabla una fila de cabecera con fondo verde institucional para cada título dado.
     * @param tabla la tabla PDF donde agregar la cabecera
     * @param titulos los textos de las columnas de la cabecera, en orden
     */
    private void agregarCabecera(PdfPTable tabla, String... titulos) {
        for (String titulo : titulos) {
            PdfPCell celda = new PdfPCell(new Phrase(titulo, fuenteEncabezadoTabla));
            celda.setBackgroundColor(VERDE_INSTITUCIONAL);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setPadding(6f);
            tabla.addCell(celda);
        }
    }

    /**
     * Construye una celda de texto estándar con relleno, usando cadena vacía si el texto es nulo.
     * @param texto el texto a mostrar en la celda
     * @return la celda de texto construida
     */
    private PdfPCell celdaTexto(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto != null ? texto : "", fuenteCelda));
        celda.setPadding(5f);
        return celda;
    }

    /**
     * Construye una celda de valor numérico/centrado a partir de una celda de texto estándar.
     * @param texto el texto del valor a mostrar en la celda
     * @return la celda con el texto centrado horizontalmente
     */
    private PdfPCell celdaValor(String texto) {
        PdfPCell celda = celdaTexto(texto);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        return celda;
    }

    /**
     * Traduce el código de estatus de avance grupal a su texto legible en español.
     * @param estatus el código de estatus ("AL_DIA", "RIESGO" u otro)
     * @return "Al día", "En Riesgo" o "Sin objetivo" según corresponda
     */
    private String traducirEstatusGrupal(String estatus) {
        if ("AL_DIA".equals(estatus)) return "Al día";
        if ("RIESGO".equals(estatus)) return "En Riesgo";
        return "Sin objetivo";
    }

    /**
     * Devuelve el valor recibido, o "Todas" si es nulo o está en blanco (usado para filtros femeninos, p. ej. carrera).
     * @param valor el valor del filtro a evaluar
     * @return el valor original, o "Todas" si estaba vacío
     */
    private String orTodas(String valor) {
        return (valor == null || valor.isBlank()) ? "Todas" : valor;
    }

    /**
     * Devuelve el valor recibido, o "Todos" si es nulo o está en blanco (usado para filtros masculinos, p. ej. cuatrimestre, grupo, tutor).
     * @param valor el valor del filtro a evaluar
     * @return el valor original, o "Todos" si estaba vacío
     */
    private String orTodos(String valor) {
        return (valor == null || valor.isBlank()) ? "Todos" : valor;
    }
}
