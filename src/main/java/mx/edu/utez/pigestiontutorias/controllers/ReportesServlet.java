package mx.edu.utez.pigestiontutorias.controllers;

import com.lowagie.text.DocumentException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

    // Cualquier excepcion no controlada dentro de doGetInterno (SQLException de una consulta
    // rota, NullPointerException, etc.) antes se colaba hasta el contenedor y Tomcat devolvia
    // su pagina HTML de error 500 -- que rompia el fetch().then(r => r.json()) del dashboard
    // (SyntaxError: Unexpected token '<') y el usuario solo veia "No se pudo cargar el
    // reporte." sin ninguna pista de la causa real. Ahora se atrapa aqui, se deja el stack
    // trace completo en el log del servidor (unico lugar donde se puede ver la linea exacta
    // que fallo) y se responde JSON valido para que el front no truene al parsearlo.
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

        // 1. MOSTRAR LA VISTA JSP (Por Defecto)
        if (accion == null && formato == null) {
            request.setAttribute("listaCarreras", alumnoDao.getAllCarreras());

            // El filtro de "Grupo Asignado" del dashboard del tutor solo debe ofrecer los
            // grupos que ASIGNACION_TUTOR realmente le asigno (getGruposByTutor ya filtra
            // por ID_TUTOR + ESTADO='S') -- asi el tutor no puede ni siquiera armar, desde
            // el propio <select>, una combinacion de carrera/cuatrimestre/grupo ajena.
            String rolSesionVista = (String) session.getAttribute("rol");
            if ("Tutor".equals(rolSesionVista)) {
                Integer idUsuarioVista = (Integer) session.getAttribute("idUsuario");
                if (idUsuarioVista != null) {
                    request.setAttribute("listaGruposTutor", grupoDao.getGruposByTutor(idUsuarioVista));
                }
            }

            request.setAttribute("paginaActiva", "reportes");
            request.getRequestDispatcher("/tutor/reportes.jsp").forward(request, response);
            return; // Detenemos la ejecución aquí para que no intente generar el JSON
        }

        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        String rolSesion = (String) session.getAttribute("rol");

        // Acciones del dashboard de Reportes del Tutor (tarjetas "Tutorías Grupales", "Pendientes",
        // "Canalizados" y "Alumnos Atendidos", ver tutor/reportes.jsp y assets/js/tutor/*-modal.js):
        // mismos modales que el coordinador, pero el alcance de datos SIEMPRE se ata al tutor de la
        // sesion — nunca a un idTutor que mande el cliente — para que un tutor no pueda ver datos
        // de otro tutor.
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

        // El rol viene de la sesion (seteado por LoginServlet), no de una busqueda en TUTOR
        // por idUsuario: TUTOR y COORDINADOR son tablas independientes, cada una con su propio
        // NUMERO_EMPLEADO, y un coordinador puede coincidir en numero con un tutor cualquiera
        // (ej. ambos con NUMERO_EMPLEADO = 1), lo que hacia que tutorDao.getById(idUsuario)
        // devolviera ese tutor "por casualidad" y el reporte del coordinador quedara filtrado
        // (en cero/vacio) a los datos de ese unico tutor en vez de mostrar el global.
        if ("Tutor".equals(rolSesion)) {
            // El usuario es tutor: siempre se filtra por si mismo, sin importar
            // que venga un idTutor distinto en la URL (evita que un tutor vea datos de otro).
            idTutorFiltro = idUsuario;
        } else {
            // El usuario es coordinador (u otro rol sin tutor asociado):
            // puede elegir un tutor especifico desde el select, o dejarlo vacio para ver todo.
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

        // Si el buscador de alumnos del dashboard tiene un alumno seleccionado, las mismas
        // tarjetas/graficas (KPIs + pastel/barras) se recalculan acotadas a ESE alumno.
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

    // Tarjeta "Tutorías Grupales" del tutor: mismo calculo que el modal del coordinador
    // (SesionGrupalDao.getAvancePorPeriodo), pero filtrado a solo los grupos de este tutor.
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

    // "Ver detalles" del grupo dentro del modal "Tutorías Grupales": el idGrupo lo manda el
    // cliente, pero el idTutor es siempre el de la sesion, asi que solo puede ver el detalle de
    // sus propios grupos.
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

    // Modal "Canalizados" del tutor: unicamente las canalizaciones que EL registro (mismo filtro
    // idTutor que ya soportaba CanalizacionDao.getCanalizacionesDetalladas para el coordinador),
    // reutilizando los filtros de cuatrimestre/grupo/carrera/fechas de la barra de Reportes.
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

    // Tarjeta "Pendientes" del tutor: solo las solicitudes de sus propios alumnos asignados
    // (mismo metodo que usa el coordinador, con idTutor fijo al de la sesion).
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

    // Modal "Alumnos Atendidos" del tutor: mismas sesiones Individual/Espontanea que ve el
    // coordinador en su modal, pero fijas al idTutor de la sesion.
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

    // Buscador de alumnos del dashboard del tutor: solo devuelve alumnos cuyo grupo esta
    // asignado a ESTE tutor (ver AlumnoDAO.buscarAlumnos con idTutor fijo), para que un
    // tutor no pueda ni siquiera enterarse de la existencia de alumnos ajenos.
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

    // Boton "Enviar correo de recordatorio" del modal "Canalizados": re-consulta por
    // ID_CANALIZACION exigiendo que pertenezca al tutor de la sesion (ver
    // CanalizacionDao.getDetalleParaRecordatorio) y solo manda el correo si sigue 'En proceso'.
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

    // Exportacion Excel/PDF del tutor: mismos builders que el coordinador (ReporteExcelBuilder/
    // ReportePdfBuilder), pero el avance grupal se filtra a solo los grupos de este tutor y el
    // idTutor de las demas consultas queda fijo al de la sesion.
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
                decodificarImagenBase64(request.getParameter("imagenPastel")),
                decodificarImagenBase64(request.getParameter("imagenBarras")));
    }

    private String nombreArchivoExportacion(String extension) {
        return "reporte_tutorias_" + LocalDate.now().format(FORMATO_FECHA_ARCHIVO) + "." + extension;
    }

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

    private java.sql.Date parseFechaSqlOrDefault(String valor, java.sql.Date porDefecto) {
        if (valor == null || valor.isBlank()) return porDefecto;
        try {
            return java.sql.Date.valueOf(valor.trim());
        } catch (Exception e) {
            return porDefecto;
        }
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

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String escaparJson(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
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