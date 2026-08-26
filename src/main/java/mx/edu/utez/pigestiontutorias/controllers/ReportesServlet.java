package mx.edu.utez.pigestiontutorias.controllers;

import com.lowagie.text.DocumentException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.AlumnoBusquedaDTO;
import mx.edu.utez.pigestiontutorias.models.AtencionAlumnoDTO;
import mx.edu.utez.pigestiontutorias.models.AvanceTutorGrupal;
import mx.edu.utez.pigestiontutorias.models.Canalizacion;
import mx.edu.utez.pigestiontutorias.models.CanalizacionAlumnoDTO;
import mx.edu.utez.pigestiontutorias.models.CanalizacionRecordatorioDTO;
import mx.edu.utez.pigestiontutorias.models.PeriodoEscolar;
import mx.edu.utez.pigestiontutorias.models.ReporteExportDatos;
import mx.edu.utez.pigestiontutorias.models.SesionGrupal;
import mx.edu.utez.pigestiontutorias.models.SolicitudPendienteDTO;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.TrayectoriaGrupoDTO;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.CanalizacionDao;
import mx.edu.utez.pigestiontutorias.models.dao.GrupoDao;
import mx.edu.utez.pigestiontutorias.models.dao.PeriodoEscolarDao;
import mx.edu.utez.pigestiontutorias.models.dao.ReportesDao;
import mx.edu.utez.pigestiontutorias.models.dao.SesionGrupalDao;
import mx.edu.utez.pigestiontutorias.models.dao.SesionIndividualDao;
import mx.edu.utez.pigestiontutorias.models.dao.SolicitudDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;
import mx.edu.utez.pigestiontutorias.utils.EmailSender;
import mx.edu.utez.pigestiontutorias.utils.ReporteExcelBuilder;
import mx.edu.utez.pigestiontutorias.utils.ReportePdfBuilder;
import mx.edu.utez.pigestiontutorias.utils.UrlUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Servlet que expone el módulo de reportes para el tutor: avance de tutorías
 * grupales, detalle de sesiones, atenciones individuales, canalizaciones,
 * solicitudes pendientes, búsqueda de alumnos, recordatorios al área de apoyo,
 * y exportación de reportes a Excel, PDF y CSV.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-07-28
 */
@WebServlet("/ReportesServlet")
public class ReportesServlet extends HttpServlet {

    private final ReportesDao reportesDao = new ReportesDao();
    private final PeriodoEscolarDao periodoDao = new PeriodoEscolarDao();
    private final SesionGrupalDao sesionGrupalDao = new SesionGrupalDao();
    private final SesionIndividualDao sesionIndividualDao = new SesionIndividualDao();
    private final CanalizacionDao canalizacionDao = new CanalizacionDao();
    private final SolicitudDao solicitudDao = new SolicitudDao();
    private final TutorDao tutorDao = new TutorDao();
    private final AlumnoDAO alumnoDao = new AlumnoDAO();
    private final GrupoDao grupoDao = new GrupoDao();
    private final EmailSender emailSender = new EmailSender();

    private static final LocalDate FECHA_DEFAULT_DESDE = LocalDate.of(2000, 1, 1);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_ARCHIVO = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Punto de entrada de las peticiones GET del módulo de reportes del tutor.
     * Delega en {@link #doGetInterno} y captura cualquier excepción inesperada
     * para responderla como JSON de error en lugar de propagarla.
     * @param request petición HTTP con el parámetro "accion" o "formato" y sus filtros asociados
     * @param response respuesta HTTP usada para redirigir, reenviar o responder JSON/CSV
     * @throws ServletException si ocurre un error al procesar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            doGetInterno(request, response);
        } catch (Exception e) {
            System.err.println("Error inesperado en ReportesServlet.doGet: " + e.getMessage());
            e.printStackTrace();
            responderErrorJson(response, e);
        }
    }

    /**
     * Escribe una respuesta JSON de error genérico con el mensaje de la excepción,
     * evitando escribir si la respuesta ya fue confirmada.
     * @param response respuesta HTTP sobre la cual escribir el error
     * @param e la excepción capturada cuyo mensaje se incluye en la respuesta
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    private void responderErrorJson(HttpServletResponse response, Exception e) throws IOException {
        if (response.isCommitted()) return;
        response.reset();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String mensaje = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        response.getWriter().print("{\"status\":\"error\",\"message\":\"" + escaparJson(mensaje) + "\"}");
        response.getWriter().flush();
    }

    /**
     * Enruta las peticiones GET autenticadas del módulo de reportes: si no hay
     * acción ni formato, carga la vista principal de reportes del tutor; si el rol
     * es Tutor y la acción corresponde a un endpoint restringido (avance grupal,
     * detalle de sesiones, canalizaciones, solicitudes pendientes, atenciones
     * individuales, búsqueda de alumnos), lo despacha limitado a su propio id;
     * en cualquier otro caso genera el resumen general del reporte (coordinador o
     * tutor) en JSON, o lo exporta como CSV si se solicita.
     * @param request petición HTTP con los parámetros "accion", "formato" y los filtros del reporte
     * @param response respuesta HTTP usada para redirigir, reenviar o responder JSON/CSV
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    private void doGetInterno(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            if ("datos".equals(request.getParameter("accion")) || "csv".equalsIgnoreCase(request.getParameter("formato"))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            }
            return;
        }

        String formato = request.getParameter("formato");
        String accion = request.getParameter("accion");

        if (accion == null && formato == null) {
            request.setAttribute("listaCarreras", alumnoDao.getAllCarreras());

            String rolSesionVista = (String) session.getAttribute("rol");
            if ("Tutor".equals(rolSesionVista)) {
                Integer idUsuarioVista = (Integer) session.getAttribute("idUsuario");
                if (idUsuarioVista != null) {
                    request.setAttribute("listaGruposTutor", grupoDao.getGruposByTutor(idUsuarioVista));
                }
            }

            request.setAttribute("paginaActiva", "reportes");
            request.getRequestDispatcher("/tutor/reportes.jsp").forward(request, response);
            return;
        }

        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        String rolSesion = (String) session.getAttribute("rol");

        if ("avanceGrupal".equals(accion) || "detalleSesiones".equals(accion)
                || "canalizacionesDetalle".equals(accion) || "solicitudesPendientes".equals(accion)
                || "atencionesIndividuales".equals(accion) || "buscarAlumnos".equals(accion)) {
            if (!"Tutor".equals(rolSesion) || idUsuario == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            switch (accion) {
                case "avanceGrupal":
                    responderAvanceGrupalTutor(response, idUsuario);
                    return;
                case "detalleSesiones":
                    responderDetalleSesionesTutor(request, response, idUsuario);
                    return;
                case "canalizacionesDetalle":
                    responderCanalizacionesTutor(request, response, idUsuario);
                    return;
                case "solicitudesPendientes":
                    responderSolicitudesPendientesTutor(request, response, idUsuario);
                    return;
                case "atencionesIndividuales":
                    responderAtencionesIndividualesTutor(request, response, idUsuario);
                    return;
                case "buscarAlumnos":
                    responderBuscarAlumnosTutor(request, response, idUsuario);
                    return;
            }
        }

        Integer idTutorFiltro;

        if ("Tutor".equals(rolSesion)) {

            idTutorFiltro = idUsuario;
        } else {

            idTutorFiltro = parseIntOrNull(request.getParameter("idTutor"));
        }

        Integer cuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        String letra = request.getParameter("letra");
        Integer idCarrera = parseIntOrNull(request.getParameter("idCarrera"));
        String matricula = request.getParameter("matricula");

        String desdeParam = request.getParameter("desde");
        String hastaParam = request.getParameter("hasta");
        boolean tieneFiltroFechas = (desdeParam != null && !desdeParam.isBlank())
                || (hastaParam != null && !hastaParam.isBlank());

        LocalDate desde = parseFechaOrDefault(desdeParam, FECHA_DEFAULT_DESDE);
        LocalDate hasta = parseFechaOrDefault(hastaParam, LocalDate.now());

        ReportesDao.ReporteResumen reporte = reportesDao.generarReporte(
                idTutorFiltro, idCarrera, cuatrimestre, letra, desde, hasta, matricula);

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

        json.append("],");
        json.append("\"canalizaciones\":[");

        boolean primeraCanalizacion = true;
        for (Canalizacion c : reporte.canalizaciones) {
            if (!primeraCanalizacion) json.append(",");
            String fecha = c.getFechaCanalizacion() != null ? c.getFechaCanalizacion().toLocalDate().format(FORMATO_FECHA) : "";
            String motivo = c.getNombreMotivo() != null ? c.getNombreMotivo() : c.getObservaciones();
            json.append("{\"nombreArea\":\"").append(escaparJson(c.getNombreArea())).append("\",");
            json.append("\"nombreMotivo\":\"").append(escaparJson(motivo)).append("\",");
            json.append("\"estatus\":\"").append(escaparJson(c.getEstatus())).append("\",");
            json.append("\"fechaCanalizacion\":\"").append(escaparJson(fecha)).append("\"}");
            primeraCanalizacion = false;
        }

        json.append("]");
        json.append("}");

        out.print(json);
        out.flush();
    }

    /**
     * Punto de entrada de las peticiones POST del módulo de reportes del tutor.
     * Delega en {@link #doPostInterno} y captura cualquier excepción inesperada
     * para responderla como JSON de error en lugar de propagarla.
     * @param request petición HTTP con el parámetro "accion" y sus datos asociados
     * @param response respuesta HTTP usada para responder JSON o el archivo exportado
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            doPostInterno(request, response);
        } catch (Exception e) {
            System.err.println("Error inesperado en ReportesServlet.doPost: " + e.getMessage());
            e.printStackTrace();
            responderErrorJson(response, e);
        }
    }

    /**
     * Enruta las peticiones POST autenticadas de tutor según el parámetro "accion"
     * hacia el recordatorio al área de apoyo, o la exportación de reportes a Excel/PDF.
     * @param request petición HTTP con el parámetro "accion" y sus datos asociados
     * @param response respuesta HTTP usada para responder JSON o el archivo exportado
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    private void doPostInterno(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Integer idUsuario = session != null ? (Integer) session.getAttribute("idUsuario") : null;
        String rolSesion = session != null ? (String) session.getAttribute("rol") : null;

        if (session == null || session.getAttribute("usuario") == null || !"Tutor".equals(rolSesion) || idUsuario == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String accion = request.getParameter("accion");
        if ("recordarAreaApoyo".equals(accion)) {
            responderRecordarAreaApoyo(request, response, idUsuario);
            return;
        }
        if ("exportarExcel".equals(accion)) {
            exportarExcelTutor(request, response, idUsuario);
            return;
        }
        if ("exportarPdf".equals(accion)) {
            exportarPdfTutor(request, response, idUsuario);
        }
    }

    /**
     * Responde en JSON el avance de tutorías grupales del tutor autenticado respecto
     * al objetivo del periodo escolar vigente.
     * @param response respuesta HTTP donde se escribe el JSON con el periodo, objetivo y avance
     * @param idTutorSesion el id del tutor autenticado en sesión
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    private void responderAvanceGrupalTutor(HttpServletResponse response, int idTutorSesion) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        PeriodoEscolar periodoVigente = periodoDao.getPeriodoVigente();
        if (periodoVigente == null) {
            out.print("{\"periodo\":null,\"objetivo\":0,\"avance\":[]}");
            out.flush();
            return;
        }

        int objetivo = periodoVigente.getAsistenciasGrupales();
        List<AvanceTutorGrupal> avance = sesionGrupalDao.getAvancePorPeriodo(
                periodoVigente.getIdPeriodo(), periodoVigente.getFechaInicio(), periodoVigente.getFechaFin(), objetivo);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"periodo\":\"").append(escaparJson(periodoVigente.getNombre())).append("\",");
        json.append("\"objetivo\":").append(objetivo).append(",");
        json.append("\"avance\":[");

        boolean primero = true;
        for (AvanceTutorGrupal a : avance) {
            if (a.getIdTutor() != idTutorSesion) continue;
            if (!primero) json.append(",");
            json.append("{");
            json.append("\"idGrupo\":").append(a.getIdGrupo()).append(",");
            json.append("\"grupoAsignado\":\"").append(escaparJson(a.getGrupoAsignado())).append("\",");
            json.append("\"realizadas\":").append(a.getRealizadas()).append(",");
            json.append("\"objetivo\":").append(a.getObjetivo()).append(",");
            json.append("\"estatus\":\"").append(a.getEstatus()).append("\"");
            json.append("}");
            primero = false;
        }

        json.append("]}");
        out.print(json);
        out.flush();
    }

    /**
     * Responde en JSON el detalle de las sesiones grupales del tutor autenticado
     * para un grupo específico dentro del periodo escolar vigente.
     * @param request petición HTTP con el parámetro idGrupo
     * @param response respuesta HTTP donde se escribe el JSON con la lista de sesiones
     * @param idTutorSesion el id del tutor autenticado en sesión
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    private void responderDetalleSesionesTutor(HttpServletRequest request, HttpServletResponse response, int idTutorSesion) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Integer idGrupo = parseIntOrNull(request.getParameter("idGrupo"));
        PeriodoEscolar periodoVigente = periodoDao.getPeriodoVigente();

        if (idGrupo == null || periodoVigente == null) {
            out.print("[]");
            out.flush();
            return;
        }

        List<SesionGrupal> sesiones = sesionGrupalDao.getSesionesPorTutorYGrupo(
                idTutorSesion, idGrupo, periodoVigente.getFechaInicio(), periodoVigente.getFechaFin());

        StringBuilder json = new StringBuilder("[");
        boolean primero = true;
        for (SesionGrupal s : sesiones) {
            if (!primero) json.append(",");
            String fecha = s.getFecha() != null ? s.getFecha().toLocalDate().format(FORMATO_FECHA) : "";
            json.append("{");
            json.append("\"fecha\":\"").append(escaparJson(fecha)).append("\",");
            json.append("\"hora\":\"").append(escaparJson(s.getHora())).append("\",");
            json.append("\"temasTratados\":\"").append(escaparJson(s.getTemasTratados())).append("\",");
            json.append("\"acuerdos\":\"").append(escaparJson(s.getAcuerdos())).append("\",");
            json.append("\"asesoriasGrupales\":\"").append(escaparJson(s.getAsesoriasGrupales())).append("\"");
            json.append("}");
            primero = false;
        }
        json.append("]");

        out.print(json);
        out.flush();
    }

    /**
     * Responde en JSON el listado detallado de canalizaciones del tutor autenticado,
     * filtradas por carrera, cuatrimestre, letra, rango de fechas y matrícula.
     * @param request petición HTTP con los parámetros de filtro (idCarrera, cuatrimestre, letra, matricula, desde, hasta)
     * @param response respuesta HTTP donde se escribe el JSON con la lista de canalizaciones
     * @param idTutorSesion el id del tutor autenticado en sesión
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    private void responderCanalizacionesTutor(HttpServletRequest request, HttpServletResponse response, int idTutorSesion) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Integer idCarrera = parseIntOrNull(request.getParameter("idCarrera"));
        Integer cuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        String letra = request.getParameter("letra");
        String matricula = request.getParameter("matricula");

        java.sql.Date desde = parseFechaSqlOrDefault(request.getParameter("desde"), java.sql.Date.valueOf(FECHA_DEFAULT_DESDE));
        java.sql.Date hasta = parseFechaSqlOrDefault(request.getParameter("hasta"), java.sql.Date.valueOf(LocalDate.now()));

        List<CanalizacionAlumnoDTO> canalizaciones = canalizacionDao.getCanalizacionesDetalladas(
                idTutorSesion, idCarrera, cuatrimestre, letra, desde, hasta, matricula);

        StringBuilder json = new StringBuilder("[");
        boolean primero = true;
        for (CanalizacionAlumnoDTO c : canalizaciones) {
            if (!primero) json.append(",");
            String fecha = c.getFechaCanalizacion() != null ? c.getFechaCanalizacion().toLocalDate().format(FORMATO_FECHA) : "";
            json.append("{");
            json.append("\"idCanalizacion\":").append(c.getIdCanalizacion()).append(",");
            json.append("\"matricula\":\"").append(escaparJson(c.getMatricula())).append("\",");
            json.append("\"nombreAlumno\":\"").append(escaparJson(c.getNombreAlumno())).append("\",");
            json.append("\"grupoAsignado\":\"").append(escaparJson(c.getGrupoAsignado())).append("\",");
            json.append("\"fechaCanalizacion\":\"").append(escaparJson(fecha)).append("\",");
            json.append("\"nombreArea\":\"").append(escaparJson(c.getNombreArea())).append("\",");
            json.append("\"nombreMotivo\":").append(c.getNombreMotivo() != null
                    ? "\"" + escaparJson(c.getNombreMotivo()) + "\"" : "null").append(",");
            json.append("\"estatus\":\"").append(escaparJson(c.getEstatus())).append("\",");
            json.append("\"observaciones\":").append(c.getObservaciones() != null
                    ? "\"" + escaparJson(c.getObservaciones()) + "\"" : "null");
            json.append("}");
            primero = false;
        }
        json.append("]");

        out.print(json);
        out.flush();
    }

    /**
     * Responde en JSON el listado de solicitudes pendientes del tutor autenticado,
     * filtradas por carrera, cuatrimestre, letra, rango de fechas y matrícula.
     * @param request petición HTTP con los parámetros de filtro (idCarrera, cuatrimestre, letra, matricula, desde, hasta)
     * @param response respuesta HTTP donde se escribe el JSON con la lista de solicitudes pendientes
     * @param idTutorSesion el id del tutor autenticado en sesión
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    private void responderSolicitudesPendientesTutor(HttpServletRequest request, HttpServletResponse response, int idTutorSesion) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Integer idCarrera = parseIntOrNull(request.getParameter("idCarrera"));
        Integer cuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        String letra = request.getParameter("letra");
        String matricula = request.getParameter("matricula");

        java.sql.Date desde = parseFechaSqlOrDefault(request.getParameter("desde"), java.sql.Date.valueOf(FECHA_DEFAULT_DESDE));
        java.sql.Date hasta = parseFechaSqlOrDefault(request.getParameter("hasta"), java.sql.Date.valueOf(LocalDate.now()));

        List<SolicitudPendienteDTO> solicitudes = solicitudDao.getSolicitudesPendientesGlobal(
                idTutorSesion, idCarrera, cuatrimestre, letra, desde, hasta, matricula);

        StringBuilder json = new StringBuilder("[");
        boolean primero = true;
        for (SolicitudPendienteDTO s : solicitudes) {
            if (!primero) json.append(",");
            String fecha = s.getFechaPropuesta() != null ? s.getFechaPropuesta().toLocalDate().format(FORMATO_FECHA) : "";
            json.append("{");
            json.append("\"idSolicitud\":").append(s.getIdSolicitud()).append(",");
            json.append("\"matricula\":\"").append(escaparJson(s.getMatricula())).append("\",");
            json.append("\"nombreAlumno\":\"").append(escaparJson(s.getNombreAlumno())).append("\",");
            json.append("\"grupoAsignado\":\"").append(escaparJson(s.getGrupoAsignado())).append("\",");
            json.append("\"asunto\":\"").append(escaparJson(s.getAsunto())).append("\",");
            json.append("\"descripcion\":\"").append(escaparJson(s.getDescripcion())).append("\",");
            json.append("\"fechaPropuesta\":\"").append(escaparJson(fecha)).append("\",");
            json.append("\"horaPropuesta\":\"").append(escaparJson(s.getHoraPropuesta())).append("\",");
            json.append("\"duracion\":").append(s.getDuracion() != null ? s.getDuracion() : "null").append(",");
            json.append("\"estatus\":\"").append(escaparJson(s.getEstatus())).append("\"");
            json.append("}");
            primero = false;
        }
        json.append("]");

        out.print(json);
        out.flush();
    }

    /**
     * Responde en JSON el listado de atenciones individuales del tutor autenticado,
     * filtradas por carrera, cuatrimestre, letra, rango de fechas y matrícula.
     * @param request petición HTTP con los parámetros de filtro (idCarrera, cuatrimestre, letra, matricula, desde, hasta)
     * @param response respuesta HTTP donde se escribe el JSON con la lista de atenciones
     * @param idTutorSesion el id del tutor autenticado en sesión
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    private void responderAtencionesIndividualesTutor(HttpServletRequest request, HttpServletResponse response, int idTutorSesion) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Integer idCarrera = parseIntOrNull(request.getParameter("idCarrera"));
        Integer cuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        String letra = request.getParameter("letra");
        String matricula = request.getParameter("matricula");

        java.sql.Date desde = parseFechaSqlOrDefault(request.getParameter("desde"), java.sql.Date.valueOf(FECHA_DEFAULT_DESDE));
        java.sql.Date hasta = parseFechaSqlOrDefault(request.getParameter("hasta"), java.sql.Date.valueOf(LocalDate.now()));

        List<AtencionAlumnoDTO> atenciones = sesionIndividualDao.getAtencionesIndividuales(
                idTutorSesion, idCarrera, cuatrimestre, letra, desde, hasta, matricula);

        StringBuilder json = new StringBuilder("[");
        boolean primero = true;
        for (AtencionAlumnoDTO a : atenciones) {
            if (!primero) json.append(",");
            String fecha = a.getFecha() != null ? a.getFecha().toLocalDate().format(FORMATO_FECHA) : "";
            json.append("{");
            json.append("\"idSesion\":").append(a.getIdSesion()).append(",");
            json.append("\"tipo\":\"").append(escaparJson(a.getTipo())).append("\",");
            json.append("\"fecha\":\"").append(escaparJson(fecha)).append("\",");
            json.append("\"hora\":\"").append(escaparJson(a.getHora())).append("\",");
            json.append("\"grupoAsignado\":\"").append(escaparJson(a.getGrupoAsignado())).append("\",");
            json.append("\"matricula\":\"").append(escaparJson(a.getMatricula())).append("\",");
            json.append("\"nombreAlumno\":\"").append(escaparJson(a.getNombreAlumno())).append("\",");
            json.append("\"estado\":\"").append(escaparJson(a.getEstado())).append("\",");
            json.append("\"temasTratados\":\"").append(escaparJson(a.getTemasTratados())).append("\",");
            json.append("\"acuerdos\":\"").append(escaparJson(a.getAcuerdos())).append("\",");
            json.append("\"vinculoDirecto\":").append(a.getVinculoDirecto() != null
                    ? "\"" + escaparJson(a.getVinculoDirecto()) + "\"" : "null");
            json.append("}");
            primero = false;
        }
        json.append("]");

        out.print(json);
        out.flush();
    }

    /**
     * Responde en JSON los alumnos del tutor autenticado cuya matrícula o nombre
     * coincidan con el texto de búsqueda recibido, para el autocompletado del buscador.
     * @param request petición HTTP con el parámetro "texto" a buscar
     * @param response respuesta HTTP donde se escribe el JSON con los resultados de la búsqueda
     * @param idTutorSesion el id del tutor autenticado en sesión
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    private void responderBuscarAlumnosTutor(HttpServletRequest request, HttpServletResponse response, int idTutorSesion) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String texto = request.getParameter("texto");
        List<AlumnoBusquedaDTO> resultados = alumnoDao.buscarAlumnos(texto, idTutorSesion);

        StringBuilder json = new StringBuilder("[");
        boolean primero = true;
        for (AlumnoBusquedaDTO a : resultados) {
            if (!primero) json.append(",");
            json.append("{");
            json.append("\"matricula\":\"").append(escaparJson(a.getMatricula())).append("\",");
            json.append("\"nombreCompleto\":\"").append(escaparJson(a.getNombreCompleto())).append("\",");
            json.append("\"grupoAsignado\":\"").append(escaparJson(a.getGrupoAsignado())).append("\"");
            json.append("}");
            primero = false;
        }
        json.append("]");

        out.print(json);
        out.flush();
    }

    /**
     * Envía por correo un recordatorio al área de apoyo sobre una canalización
     * que sigue "En proceso", incluyendo un enlace de confirmación, y responde en JSON el resultado.
     * @param request petición HTTP con el parámetro idCanalizacion
     * @param response respuesta HTTP donde se escribe el JSON con el resultado del envío
     * @param idTutorSesion el id del tutor autenticado en sesión, dueño de la canalización
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    private void responderRecordarAreaApoyo(HttpServletRequest request, HttpServletResponse response, int idTutorSesion) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Integer idCanalizacion = parseIntOrNull(request.getParameter("idCanalizacion"));
        if (idCanalizacion == null) {
            out.print("{\"exito\":false,\"mensaje\":\"Datos inválidos.\"}");
            out.flush();
            return;
        }

        CanalizacionRecordatorioDTO detalle = canalizacionDao.getDetalleParaRecordatorio(idCanalizacion, idTutorSesion);
        if (detalle == null || detalle.getCorreoContactoArea() == null) {
            out.print("{\"exito\":false,\"mensaje\":\"No se encontró la canalización o el correo del área.\"}");
            out.flush();
            return;
        }
        if (!"En proceso".equals(detalle.getEstatus())) {
            out.print("{\"exito\":false,\"mensaje\":\"Esa canalización ya fue atendida por el área.\"}");
            out.flush();
            return;
        }

        String link = UrlUtils.baseUrl(request) + "/confirmar-canalizacion?token=" + detalle.getIdToken();
        boolean enviado = emailSender.enviarRecordatorioCanalizacion(
                detalle.getCorreoContactoArea(), detalle.getEncargadoArea(), detalle.getNombreArea(),
                detalle.getNombreAlumno(), detalle.getMatricula(), detalle.getMotivoODetalle(), link);

        if (enviado) {
            out.print("{\"exito\":true,\"mensaje\":\"Recordatorio enviado correctamente.\"}");
        } else {
            out.print("{\"exito\":false,\"mensaje\":\"No se pudo enviar el correo. Intenta de nuevo.\"}");
        }
        out.flush();
    }

    /**
     * Genera y envía como descarga un archivo Excel con el reporte del tutor
     * autenticado, filtrado según los parámetros de la petición.
     * @param request petición HTTP con los filtros del reporte y las imágenes de las gráficas en base64
     * @param response respuesta HTTP sobre la que se escribe el archivo Excel generado
     * @param idTutorSesion el id del tutor autenticado en sesión
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    private void exportarExcelTutor(HttpServletRequest request, HttpServletResponse response, int idTutorSesion) throws IOException {
        ReporteExportDatos datos = recolectarDatosExportacionTutor(request, idTutorSesion);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivoExportacion("xlsx") + "\"");

        try {
            new ReporteExcelBuilder().generar(response.getOutputStream(), datos);
        } catch (IOException e) {
            System.err.println("Error al generar el Excel del reporte del tutor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera y envía como descarga un archivo PDF con el reporte del tutor
     * autenticado, filtrado según los parámetros de la petición.
     * @param request petición HTTP con los filtros del reporte y las imágenes de las gráficas en base64
     * @param response respuesta HTTP sobre la que se escribe el archivo PDF generado
     * @param idTutorSesion el id del tutor autenticado en sesión
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    private void exportarPdfTutor(HttpServletRequest request, HttpServletResponse response, int idTutorSesion) throws IOException {
        ReporteExportDatos datos = recolectarDatosExportacionTutor(request, idTutorSesion);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivoExportacion("pdf") + "\"");

        try {
            new ReportePdfBuilder().generar(response.getOutputStream(), datos);
        } catch (DocumentException e) {
            System.err.println("Error al generar el PDF del reporte del tutor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recolecta y arma toda la información necesaria para exportar el reporte del
     * tutor autenticado (resumen general, avance grupal propio, atenciones individuales,
     * canalizaciones, datos académicos del alumno filtrado y gráficas), aplicando
     * los filtros de la petición.
     * @param request petición HTTP con los filtros del reporte y las imágenes de las gráficas en base64
     * @param idTutorSesion el id del tutor autenticado en sesión
     * @return el objeto {@link ReporteExportDatos} con toda la información lista para exportar
     */
    private ReporteExportDatos recolectarDatosExportacionTutor(HttpServletRequest request, int idTutorSesion) {
        Integer idCarrera = parseIntOrNull(request.getParameter("idCarrera"));
        Integer cuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        String letra = request.getParameter("letra");
        String matricula = request.getParameter("matricula");

        String desdeParam = request.getParameter("desde");
        String hastaParam = request.getParameter("hasta");
        boolean tieneFiltroFechas = (desdeParam != null && !desdeParam.isBlank())
                || (hastaParam != null && !hastaParam.isBlank());

        java.sql.Date desde = parseFechaSqlOrDefault(desdeParam, java.sql.Date.valueOf(FECHA_DEFAULT_DESDE));
        java.sql.Date hasta = parseFechaSqlOrDefault(hastaParam, java.sql.Date.valueOf(LocalDate.now()));

        ReportesDao.ReporteResumen resumen = reportesDao.generarReporte(
                idTutorSesion, idCarrera, cuatrimestre, letra, desde.toLocalDate(), hasta.toLocalDate(), matricula);

        PeriodoEscolar periodoVigente = periodoDao.getPeriodoVigente();
        List<AvanceTutorGrupal> avanceGrupal = Collections.emptyList();
        if (periodoVigente != null) {
            avanceGrupal = sesionGrupalDao.getAvancePorPeriodo(periodoVigente.getIdPeriodo(),
                            periodoVigente.getFechaInicio(), periodoVigente.getFechaFin(), periodoVigente.getAsistenciasGrupales())
                    .stream().filter(a -> a.getIdTutor() == idTutorSesion).toList();
        }

        List<AtencionAlumnoDTO> atenciones = sesionIndividualDao.getAtencionesIndividuales(
                idTutorSesion, idCarrera, cuatrimestre, letra, desde, hasta, matricula);

        List<CanalizacionAlumnoDTO> canalizaciones = canalizacionDao.getCanalizacionesDetalladas(
                idTutorSesion, idCarrera, cuatrimestre, letra, desde, hasta, matricula);

        String tituloPeriodo = tieneFiltroFechas
                ? desde.toLocalDate().format(FORMATO_FECHA) + " a " + hasta.toLocalDate().format(FORMATO_FECHA)
                : "Histórico completo";

        Tutor tutorSesion = tutorDao.getById(idTutorSesion);
        String nombreTutor = tutorSesion != null ? tutorSesion.getNombres() + " " + tutorSesion.getApellidos() : null;

        return new ReporteExportDatos(resumen, periodoVigente != null ? periodoVigente.getNombre() : null,
                periodoVigente != null ? periodoVigente.getAsistenciasGrupales() : 0,
                avanceGrupal, atenciones, canalizaciones, tituloPeriodo,
                request.getParameter("nombreCarrera"), request.getParameter("nombreCuatrimestre"),
                request.getParameter("nombreGrupo"), nombreTutor, request.getParameter("nombreAlumno"),
                resolverDatosAlumno(matricula),
                decodificarImagenBase64(request.getParameter("imagenPastel")),
                decodificarImagenBase64(request.getParameter("imagenBarras")));
    }

    /**
     * Resuelve los datos académicos actuales del alumno filtrado (carrera, nivel,
     * grupo y generación) a partir de su trayectoria, tomando el grupo vigente
     * (fecha fin nula) o el más reciente. Nombre y matrícula vienen de ALUMNO;
     * carrera/nivel/cuatrimestre-grupo/generación vienen de ALUMNO_GRUPO_HISTORICO,
     * la misma fuente que usa la sección "Trayectoria académica" del coordinador.
     * @param matricula la matrícula del alumno a resolver
     * @return los datos académicos resueltos del alumno, o {@code null} si la matrícula es vacía o el alumno no existe
     */
    private ReporteExportDatos.DatosAcademicosAlumno resolverDatosAlumno(String matricula) {
        if (matricula == null || matricula.isBlank()) return null;

        Alumno alumno = alumnoDao.getById(matricula);
        if (alumno == null) return null;

        List<TrayectoriaGrupoDTO> trayectoria = alumnoDao.getTrayectoriaPorAlumno(matricula);
        TrayectoriaGrupoDTO actual = trayectoria.stream()
                .filter(t -> t.getFechaFin() == null)
                .findFirst()
                .orElse(trayectoria.isEmpty() ? null : trayectoria.get(trayectoria.size() - 1));

        String nombreCompleto = alumno.getNombres() + " " + alumno.getApellidos();
        String nivel = actual != null ? actual.getNivel() : null;
        // "Carrera" combina nivel + nombre (ej. "TSU en Contabilidad"), como se ve en el resto
        // del sistema; el nivel tambien se expone por separado (ej. "TSU").
        String carrera = actual != null
                ? (nivel != null && !nivel.isBlank() ? nivel + " en " + actual.getNombreCarrera() : actual.getNombreCarrera())
                : null;
        String cuatrimestreGrupo = actual != null ? actual.getCuatrimestre() + "° " + actual.getLetra() : null;
        String generacion = actual != null ? actual.getGeneracion() : null;

        return new ReporteExportDatos.DatosAcademicosAlumno(
                matricula, nombreCompleto, carrera, nivel, cuatrimestreGrupo, generacion);
    }

    /**
     * Construye el nombre de archivo para el reporte exportado, incluyendo la fecha
     * actual y la extensión indicada.
     * @param extension la extensión del archivo a generar (por ejemplo "xlsx" o "pdf")
     * @return el nombre de archivo generado, por ejemplo "reporte_tutorias_25-08-2026.xlsx"
     */
    private String nombreArchivoExportacion(String extension) {
        return "reporte_tutorias_" + LocalDate.now().format(FORMATO_FECHA_ARCHIVO) + "." + extension;
    }

    /**
     * Decodifica una imagen codificada en base64 (formato data URL) enviada desde
     * el cliente, para insertarla en los reportes exportados.
     * @param dataUrl la cadena en formato data URL con la imagen codificada en base64
     * @return el arreglo de bytes de la imagen decodificada, o {@code null} si es inválida o vacía
     */
    private byte[] decodificarImagenBase64(String dataUrl) {
        if (dataUrl == null || dataUrl.isBlank()) return null;
        int coma = dataUrl.indexOf(',');
        String base64 = coma >= 0 ? dataUrl.substring(coma + 1) : dataUrl;
        try {
            return java.util.Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            System.err.println("Error al decodificar la imagen de la gráfica: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convierte una cadena de texto en una fecha SQL, devolviendo un valor por
     * defecto si el texto es nulo, está en blanco o no es una fecha válida.
     * @param valor el texto de la fecha en formato ISO (yyyy-MM-dd)
     * @param porDefecto la fecha a devolver si el valor es inválido o está vacío
     * @return la fecha convertida, o {@code porDefecto} si no se pudo convertir
     */
    private java.sql.Date parseFechaSqlOrDefault(String valor, java.sql.Date porDefecto) {
        if (valor == null || valor.isBlank()) return porDefecto;
        try {
            return java.sql.Date.valueOf(valor.trim());
        } catch (Exception e) {
            return porDefecto;
        }
    }

    /**
     * Genera y envía como descarga un archivo CSV con el resumen del reporte,
     * incluyendo indicadores generales, distribución de canalizaciones por área
     * y el detalle de cada canalización.
     * @param response respuesta HTTP sobre la que se escribe el archivo CSV generado
     * @param reporte el resumen del reporte a exportar
     * @param desde la fecha de inicio del rango filtrado
     * @param hasta la fecha de fin del rango filtrado
     * @param tieneFiltroFechas indica si se aplicó un filtro de fechas explícito
     * @param nombreCarrera el nombre de la carrera filtrada, para mostrar en el encabezado
     * @param nombreCuatrimestre el nombre del cuatrimestre filtrado, para mostrar en el encabezado
     * @param nombreGrupo el nombre del grupo filtrado, para mostrar en el encabezado
     * @param nombreTutor el nombre del tutor filtrado, para mostrar en el encabezado
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
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

        out.println();
        out.println("Canalizaciones Detalladas");
        out.println("Area,Motivo,Estatus,Fecha");
        for (Canalizacion c : reporte.canalizaciones) {
            String fecha = c.getFechaCanalizacion() != null ? c.getFechaCanalizacion().toLocalDate().format(FORMATO_FECHA) : "";
            String motivo = c.getNombreMotivo() != null ? c.getNombreMotivo() : c.getObservaciones();
            out.println(c.getNombreArea() + "," + motivo + "," + c.getEstatus() + "," + fecha);
        }

        out.flush();
    }

    /**
     * Determina si una cadena de texto es nula o está en blanco.
     * @param valor el texto a evaluar
     * @return {@code true} si el texto es {@code null} o está en blanco; {@code false} en caso contrario
     */
    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    /**
     * Escapa los caracteres especiales de una cadena para que pueda incrustarse
     * de forma segura como valor de texto dentro de un documento JSON construido manualmente.
     * @param valor el texto a escapar
     * @return el texto escapado, o cadena vacía si el valor es {@code null}
     */
    private String escaparJson(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
    }

    /**
     * Convierte una cadena de texto a entero de forma segura.
     * @param valor el texto a convertir
     * @return el valor entero resultante, o {@code null} si el texto es nulo, está en blanco o no es numérico
     */
    private Integer parseIntOrNull(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convierte una cadena de texto en una fecha, devolviendo un valor por
     * defecto si el texto es nulo, está en blanco o no es una fecha válida.
     * @param valor el texto de la fecha en formato ISO (yyyy-MM-dd)
     * @param porDefecto la fecha a devolver si el valor es inválido o está vacío
     * @return la fecha convertida, o {@code porDefecto} si no se pudo convertir
     */
    private LocalDate parseFechaOrDefault(String valor, LocalDate porDefecto) {
        if (valor == null || valor.isBlank()) return porDefecto;
        try {
            return LocalDate.parse(valor);
        } catch (Exception e) {
            return porDefecto;
        }
    }
}