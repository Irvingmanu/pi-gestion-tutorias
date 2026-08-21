package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.lowagie.text.DocumentException;
import mx.edu.utez.pigestiontutorias.models.AlumnoBusquedaDTO;
import mx.edu.utez.pigestiontutorias.models.AtencionAlumnoDTO;
import mx.edu.utez.pigestiontutorias.models.AvanceTutorGrupal;
import mx.edu.utez.pigestiontutorias.models.CanalizacionAlumnoDTO;
import mx.edu.utez.pigestiontutorias.models.Carrera;
import mx.edu.utez.pigestiontutorias.models.PeriodoEscolar;
import mx.edu.utez.pigestiontutorias.models.ReporteExportDatos;
import mx.edu.utez.pigestiontutorias.models.SesionGrupal;
import mx.edu.utez.pigestiontutorias.models.SolicitudPendienteDTO;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.TrayectoriaGrupoDTO;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.CanalizacionDao;
import mx.edu.utez.pigestiontutorias.models.dao.PeriodoEscolarDao;
import mx.edu.utez.pigestiontutorias.models.dao.ReportesDao;
import mx.edu.utez.pigestiontutorias.models.dao.SesionGrupalDao;
import mx.edu.utez.pigestiontutorias.models.dao.SesionIndividualDao;
import mx.edu.utez.pigestiontutorias.models.dao.SolicitudDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;
import mx.edu.utez.pigestiontutorias.utils.EmailSender;
import mx.edu.utez.pigestiontutorias.utils.ReporteExcelBuilder;
import mx.edu.utez.pigestiontutorias.utils.ReportePdfBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@WebServlet("/reportes-globales")
public class ReportesGlobalesServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final TutorDao tutorDao = new TutorDao();
    private final PeriodoEscolarDao periodoDao = new PeriodoEscolarDao();
    private final SesionGrupalDao sesionGrupalDao = new SesionGrupalDao();
    private final SesionIndividualDao sesionIndividualDao = new SesionIndividualDao();
    private final CanalizacionDao canalizacionDao = new CanalizacionDao();
    private final SolicitudDao solicitudDao = new SolicitudDao();
    private final ReportesDao reportesDao = new ReportesDao();
    private final EmailSender emailSender = new EmailSender();

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final java.sql.Date FECHA_DEFAULT_DESDE = java.sql.Date.valueOf(LocalDate.of(2000, 1, 1));

    // Mismo motivo que ReportesServlet.doGet: sin este try-catch, una excepcion no controlada
    // en cualquiera de las ramas JSON de abajo (incluida trayectoriaAlumno) llegaba a Tomcat
    // como pagina HTML de error 500, que rompe el fetch().then(r => r.json()) del dashboard
    // con "Unexpected token '<'" y no deja ver la causa real en ningun lado del cliente.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            doGetInterno(request, response);
        } catch (Exception e) {
            System.err.println("Error inesperado en ReportesGlobalesServlet.doGet: " + e.getMessage());
            e.printStackTrace();
            responderErrorJson(response, e);
        }
    }

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

    private void doGetInterno(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            if (request.getParameter("accion") != null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            }
            return;
        }

        String accion = request.getParameter("accion");
        if ("avanceGrupal".equals(accion)) {
            responderAvanceGrupal(response);
            return;
        }
        if ("detalleSesiones".equals(accion)) {
            responderDetalleSesiones(request, response);
            return;
        }
        if ("atencionesIndividuales".equals(accion)) {
            responderAtencionesIndividuales(request, response);
            return;
        }
        if ("canalizacionesDetalle".equals(accion)) {
            responderCanalizacionesDetalle(request, response);
            return;
        }
        if ("solicitudesPendientes".equals(accion)) {
            responderSolicitudesPendientes(request, response);
            return;
        }
        if ("buscarAlumnos".equals(accion)) {
            responderBuscarAlumnos(request, response);
            return;
        }
        if ("trayectoriaAlumno".equals(accion)) {
            responderTrayectoriaAlumno(request, response);
            return;
        }
        if ("exportarExcel".equals(accion)) {
            exportarExcel(request, response);
            return;
        }
        if ("exportarPdf".equals(accion)) {
            exportarPdf(request, response);
            return;
        }

        List<Carrera> listaCarreras = alumnoDAO.getAllCarreras();
        List<Tutor> listaTutores = tutorDao.findAll();

        request.setAttribute("paginaActiva", "reportes");
        request.setAttribute("listaCarreras", listaCarreras);
        request.setAttribute("listaTutores", listaTutores);

        // Accesos directos desde gestion-grupos.jsp ("Ver historial de tutorias"/"Ver
        // historial del alumno"): si vienen estos parametros en la URL, se exponen como
        // atributos para que reportes-globales.jsp preseleccione los filtros y dispare la
        // busqueda solo, sin que el coordinador tenga que volver a elegirlos a mano.
        request.setAttribute("prefiltroIdCarrera", request.getParameter("idCarrera"));
        request.setAttribute("prefiltroCuatrimestre", request.getParameter("cuatrimestre"));
        request.setAttribute("prefiltroLetra", request.getParameter("letra"));
        String prefiltroMatricula = request.getParameter("matricula");
        request.setAttribute("prefiltroMatricula", prefiltroMatricula);
        if (prefiltroMatricula != null && !prefiltroMatricula.isBlank()) {
            var alumno = alumnoDAO.getById(prefiltroMatricula);
            if (alumno != null) {
                request.setAttribute("prefiltroNombreAlumno", alumno.getNombres() + " " + alumno.getApellidos());
            }
        }

        RequestDispatcher rd = request.getRequestDispatcher("/coordinador/reportes-globales.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String accion = request.getParameter("accion");
        if ("alertarTutor".equals(accion)) {
            responderAlertarTutor(request, response);
            return;
        }
        if ("recordarTutorSolicitud".equals(accion)) {
            responderRecordarTutorSolicitud(request, response);
            return;
        }
        if ("exportarExcel".equals(accion)) {
            exportarExcel(request, response);
            return;
        }
        if ("exportarPdf".equals(accion)) {
            exportarPdf(request, response);
        }
    }

    // Modal "Seguimiento de Tutorias Grupales": avance de cada tutor-grupo dentro del
    // periodo vigente, comparado contra el objetivo (ASISTENCIASGRUPALES) definido por
    // el coordinador al crear ese periodo.
    private void responderAvanceGrupal(HttpServletResponse response) throws IOException {
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
            if (!primero) json.append(",");
            json.append("{");
            json.append("\"idTutor\":").append(a.getIdTutor()).append(",");
            json.append("\"nombreTutor\":\"").append(escaparJson(a.getNombreTutor())).append("\",");
            json.append("\"correoTutor\":\"").append(escaparJson(a.getCorreoTutor())).append("\",");
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

    // "Ver detalles" dentro del modal de seguimiento: datos exactos capturados en
    // "Registro de Tutoria Grupal" por ese tutor, en ese grupo, durante el periodo vigente.
    private void responderDetalleSesiones(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Integer idTutor = parseIntOrNull(request.getParameter("idTutor"));
        Integer idGrupo = parseIntOrNull(request.getParameter("idGrupo"));
        PeriodoEscolar periodoVigente = periodoDao.getPeriodoVigente();

        if (idTutor == null || idGrupo == null || periodoVigente == null) {
            out.print("[]");
            out.flush();
            return;
        }

        List<SesionGrupal> sesiones = sesionGrupalDao.getSesionesPorTutorYGrupo(
                idTutor, idGrupo, periodoVigente.getFechaInicio(), periodoVigente.getFechaFin());

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

    // Boton de correo del modal: alerta directa al tutor con su avance actual.
    private void responderAlertarTutor(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Integer idTutor = parseIntOrNull(request.getParameter("idTutor"));
        Integer idGrupo = parseIntOrNull(request.getParameter("idGrupo"));
        if (idTutor == null || idGrupo == null) {
            out.print("{\"exito\":false,\"mensaje\":\"Datos inválidos.\"}");
            out.flush();
            return;
        }

        Tutor tutor = tutorDao.getById(idTutor);
        PeriodoEscolar periodoVigente = periodoDao.getPeriodoVigente();
        if (tutor == null || tutor.getCorreoInstitucional() == null || periodoVigente == null) {
            out.print("{\"exito\":false,\"mensaje\":\"No se encontró el correo del tutor o el periodo vigente.\"}");
            out.flush();
            return;
        }

        int objetivo = periodoVigente.getAsistenciasGrupales();
        List<AvanceTutorGrupal> avance = sesionGrupalDao.getAvancePorPeriodo(
                periodoVigente.getIdPeriodo(), periodoVigente.getFechaInicio(), periodoVigente.getFechaFin(), objetivo);

        AvanceTutorGrupal fila = avance.stream()
                .filter(a -> a.getIdTutor() == idTutor && a.getIdGrupo() == idGrupo)
                .findFirst().orElse(null);

        if (fila == null) {
            out.print("{\"exito\":false,\"mensaje\":\"No se encontró el avance de ese tutor en ese grupo.\"}");
            out.flush();
            return;
        }

        boolean enviado = emailSender.enviarAlertaTutoriasGrupales(
                tutor.getCorreoInstitucional(), tutor.getNombres() + " " + tutor.getApellidos(),
                fila.getGrupoAsignado(), fila.getRealizadas(), fila.getObjetivo());

        if (enviado) {
            out.print("{\"exito\":true,\"mensaje\":\"Alerta enviada correctamente.\"}");
        } else {
            out.print("{\"exito\":false,\"mensaje\":\"No se pudo enviar el correo. Intenta de nuevo.\"}");
        }
        out.flush();
    }

    // Modal "Alumnos Atendidos": listado de sesiones Individual/Espontanea completadas,
    // EXCLUYENDO estrictamente las grupales. Reutiliza los mismos filtros de la barra de
    // Reportes Globales (carrera/cuatrimestre/grupo/tutor/fechas) para que el desglose
    // sea coherente con el KPI de la tarjeta.
    private void responderAtencionesIndividuales(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Integer idTutor = parseIntOrNull(request.getParameter("idTutor"));
        Integer idCarrera = parseIntOrNull(request.getParameter("idCarrera"));
        Integer cuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        String letra = request.getParameter("letra");
        String matricula = request.getParameter("matricula");

        java.sql.Date desde = parseFechaOrDefault(request.getParameter("desde"), FECHA_DEFAULT_DESDE);
        java.sql.Date hasta = parseFechaOrDefault(request.getParameter("hasta"), java.sql.Date.valueOf(LocalDate.now()));

        List<AtencionAlumnoDTO> atenciones = sesionIndividualDao.getAtencionesIndividuales(
                idTutor, idCarrera, cuatrimestre, letra, desde, hasta, matricula);

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

    // Modal "Alumnos Canalizados": listado detallado de canalizaciones a areas de apoyo.
    // Reutiliza los mismos filtros de la barra de Reportes Globales para que el desglose
    // sea coherente con el KPI de la tarjeta "Canalizados".
    private void responderCanalizacionesDetalle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Integer idTutor = parseIntOrNull(request.getParameter("idTutor"));
        Integer idCarrera = parseIntOrNull(request.getParameter("idCarrera"));
        Integer cuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        String letra = request.getParameter("letra");
        String matricula = request.getParameter("matricula");

        java.sql.Date desde = parseFechaOrDefault(request.getParameter("desde"), FECHA_DEFAULT_DESDE);
        java.sql.Date hasta = parseFechaOrDefault(request.getParameter("hasta"), java.sql.Date.valueOf(LocalDate.now()));

        List<CanalizacionAlumnoDTO> canalizaciones = canalizacionDao.getCanalizacionesDetalladas(
                idTutor, idCarrera, cuatrimestre, letra, desde, hasta, matricula);

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
            json.append("\"nombreTutor\":").append(c.getNombreTutor() != null
                    ? "\"" + escaparJson(c.getNombreTutor()) + "\"" : "null").append(",");
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

    // Modal "Solicitudes Pendientes": solicitudes de tutoria creadas por los alumnos que aun no
    // han sido procesadas (ESTATUS = 'Pendiente'). Reutiliza los mismos filtros de la barra de
    // Reportes Globales para que el desglose sea coherente con el KPI de la tarjeta "Pendientes".
    private void responderSolicitudesPendientes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Integer idTutor = parseIntOrNull(request.getParameter("idTutor"));
        Integer idCarrera = parseIntOrNull(request.getParameter("idCarrera"));
        Integer cuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        String letra = request.getParameter("letra");
        String matricula = request.getParameter("matricula");

        java.sql.Date desde = parseFechaOrDefault(request.getParameter("desde"), FECHA_DEFAULT_DESDE);
        java.sql.Date hasta = parseFechaOrDefault(request.getParameter("hasta"), java.sql.Date.valueOf(LocalDate.now()));

        List<SolicitudPendienteDTO> solicitudes = solicitudDao.getSolicitudesPendientesGlobal(
                idTutor, idCarrera, cuatrimestre, letra, desde, hasta, matricula);

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
            json.append("\"estatus\":\"").append(escaparJson(s.getEstatus())).append("\",");
            json.append("\"idTutor\":").append(s.getIdTutor()).append(",");
            json.append("\"nombreTutor\":\"").append(escaparJson(s.getNombreTutor())).append("\"");
            json.append("}");
            primero = false;
        }
        json.append("]");

        out.print(json);
        out.flush();
    }

    // Buscador de alumnos del dashboard del coordinador: a diferencia del tutor (ver
    // ReportesServlet.responderBuscarAlumnosTutor), aqui idTutor va en null porque el
    // coordinador puede ver a TODOS los alumnos, sin importar el tutor asignado.
    private void responderBuscarAlumnos(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String texto = request.getParameter("texto");
        List<AlumnoBusquedaDTO> resultados = alumnoDAO.buscarAlumnos(texto, null);

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

    // Seccion "Trayectoria academica" del historial del alumno (Parte A): recorrido completo
    // por ALUMNO_GRUPO_HISTORICO, sin importar el tutor/carrera/periodo actual del coordinador
    // en sesion -- un alumno solo tiene una MATRICULA, asi que no hace falta mas autorizacion
    // que estar logueado como coordinador.
    private void responderTrayectoriaAlumno(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String matricula = request.getParameter("matricula");
        if (matricula == null || matricula.isBlank()) {
            out.print("[]");
            out.flush();
            return;
        }

        List<TrayectoriaGrupoDTO> trayectoria = alumnoDAO.getTrayectoriaPorAlumno(matricula);

        StringBuilder json = new StringBuilder("[");
        boolean primero = true;
        for (TrayectoriaGrupoDTO t : trayectoria) {
            if (!primero) json.append(",");
            String desde = t.getFechaInicio() != null ? t.getFechaInicio().toLocalDate().format(FORMATO_FECHA) : "";
            String hasta = t.getFechaFin() != null ? t.getFechaFin().toLocalDate().format(FORMATO_FECHA) : null;
            json.append("{");
            json.append("\"nombreCarrera\":\"").append(escaparJson(t.getNombreCarrera())).append("\",");
            json.append("\"nivel\":\"").append(escaparJson(t.getNivel())).append("\",");
            json.append("\"cuatrimestre\":").append(t.getCuatrimestre()).append(",");
            json.append("\"letra\":\"").append(escaparJson(t.getLetra())).append("\",");
            json.append("\"generacion\":\"").append(escaparJson(t.getGeneracion())).append("\",");
            json.append("\"fechaInicio\":\"").append(escaparJson(desde)).append("\",");
            json.append("\"fechaFin\":").append(hasta != null ? "\"" + escaparJson(hasta) + "\"" : "null").append(",");
            json.append("\"motivoCambio\":\"").append(escaparJson(t.getMotivoCambio())).append("\"");
            json.append("}");
            primero = false;
        }
        json.append("]");

        out.print(json);
        out.flush();
    }

    // Boton "Enviar recordatorio" del detalle de una solicitud pendiente: le manda un correo
    // directo al tutor asignado para que la atienda. Vuelve a consultar el detalle por
    // ID_SOLICITUD (no confia en el nombre/correo que mando el cliente) para tomar el correo
    // institucional real del tutor.
    private void responderRecordarTutorSolicitud(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Integer idSolicitud = parseIntOrNull(request.getParameter("idSolicitud"));
        if (idSolicitud == null) {
            out.print("{\"exito\":false,\"mensaje\":\"Datos inválidos.\"}");
            out.flush();
            return;
        }

        SolicitudPendienteDTO solicitud = solicitudDao.getDetalleParaRecordatorio(idSolicitud);
        if (solicitud == null || solicitud.getCorreoTutor() == null) {
            out.print("{\"exito\":false,\"mensaje\":\"No se encontró la solicitud o el correo del tutor.\"}");
            out.flush();
            return;
        }

        boolean enviado = emailSender.enviarRecordatorioSolicitud(
                solicitud.getCorreoTutor(), solicitud.getNombreTutor(), solicitud.getNombreAlumno(), solicitud.getAsunto());

        if (enviado) {
            out.print("{\"exito\":true,\"mensaje\":\"Recordatorio enviado correctamente.\"}");
        } else {
            out.print("{\"exito\":false,\"mensaje\":\"No se pudo enviar el correo. Intenta de nuevo.\"}");
        }
        out.flush();
    }

    // Exportacion a Excel del reporte global: reutiliza exactamente los mismos filtros y las
    // mismas consultas (ReportesDao/SesionGrupalDao/SesionIndividualDao/CanalizacionDao) que
    // alimentan las tarjetas y los modales, para que el archivo descargado sea consistente con
    // lo que el coordinador ve en pantalla.
    private void exportarExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ReporteExportDatos datos = recolectarDatosExportacion(request);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivoExportacion("xlsx") + "\"");

        try {
            new ReporteExcelBuilder().generar(response.getOutputStream(), datos);
        } catch (IOException e) {
            System.err.println("Error al generar el Excel del reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Exportacion a PDF (formato ejecutivo) del reporte global: mismos datos que exportarExcel,
    // con encabezado institucional, resumen de metricas y tablas paginadas de tutorías
    // grupales/canalizaciones con la paleta verde institucional.
    private void exportarPdf(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ReporteExportDatos datos = recolectarDatosExportacion(request);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivoExportacion("pdf") + "\"");

        try {
            new ReportePdfBuilder().generar(response.getOutputStream(), datos);
        } catch (DocumentException e) {
            System.err.println("Error al generar el PDF del reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ReporteExportDatos recolectarDatosExportacion(HttpServletRequest request) {
        Integer idTutor = parseIntOrNull(request.getParameter("idTutor"));
        Integer idCarrera = parseIntOrNull(request.getParameter("idCarrera"));
        Integer cuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        String letra = request.getParameter("letra");
        String matricula = request.getParameter("matricula");

        String desdeParam = request.getParameter("desde");
        String hastaParam = request.getParameter("hasta");
        boolean tieneFiltroFechas = (desdeParam != null && !desdeParam.isBlank())
                || (hastaParam != null && !hastaParam.isBlank());

        java.sql.Date desde = parseFechaOrDefault(desdeParam, FECHA_DEFAULT_DESDE);
        java.sql.Date hasta = parseFechaOrDefault(hastaParam, java.sql.Date.valueOf(LocalDate.now()));

        ReportesDao.ReporteResumen resumen = reportesDao.generarReporte(
                idTutor, idCarrera, cuatrimestre, letra, desde.toLocalDate(), hasta.toLocalDate(), matricula);

        // El modal/hoja de Tutorías Grupales no aplica los filtros de carrera/cuatrimestre/tutor
        // (ver responderAvanceGrupal): siempre es el avance de todos los tutores en el periodo vigente.
        PeriodoEscolar periodoVigente = periodoDao.getPeriodoVigente();
        List<AvanceTutorGrupal> avanceGrupal = periodoVigente != null
                ? sesionGrupalDao.getAvancePorPeriodo(periodoVigente.getIdPeriodo(), periodoVigente.getFechaInicio(),
                periodoVigente.getFechaFin(), periodoVigente.getAsistenciasGrupales())
                : Collections.emptyList();

        List<AtencionAlumnoDTO> atenciones = sesionIndividualDao.getAtencionesIndividuales(
                idTutor, idCarrera, cuatrimestre, letra, desde, hasta, matricula);

        List<CanalizacionAlumnoDTO> canalizaciones = canalizacionDao.getCanalizacionesDetalladas(
                idTutor, idCarrera, cuatrimestre, letra, desde, hasta, matricula);

        String tituloPeriodo = tieneFiltroFechas
                ? desde.toLocalDate().format(FORMATO_FECHA) + " a " + hasta.toLocalDate().format(FORMATO_FECHA)
                : "Histórico completo";

        return new ReporteExportDatos(resumen, periodoVigente != null ? periodoVigente.getNombre() : null,
                periodoVigente != null ? periodoVigente.getAsistenciasGrupales() : 0,
                avanceGrupal, atenciones, canalizaciones, tituloPeriodo,
                request.getParameter("nombreCarrera"), request.getParameter("nombreCuatrimestre"),
                request.getParameter("nombreGrupo"), request.getParameter("nombreTutor"),
                request.getParameter("nombreAlumno"),
                decodificarImagenBase64(request.getParameter("imagenPastel")),
                decodificarImagenBase64(request.getParameter("imagenBarras")));
    }

    // Las graficas (Chart.js) solo existen en el navegador: el JS las captura con
    // canvas.toBase64Image() antes de exportar y las manda como "data:image/png;base64,..."
    // para que Excel/PDF incluyan la misma imagen que ve el coordinador en pantalla.
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

    private String nombreArchivoExportacion(String extension) {
        return "reporte_tutorias_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "." + extension;
    }

    private java.sql.Date parseFechaOrDefault(String valor, java.sql.Date porDefecto) {
        if (valor == null || valor.isBlank()) return porDefecto;
        try {
            return java.sql.Date.valueOf(valor.trim());
        } catch (Exception e) {
            return porDefecto;
        }
    }

    private Integer parseIntOrNull(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String escaparJson(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
    }
}