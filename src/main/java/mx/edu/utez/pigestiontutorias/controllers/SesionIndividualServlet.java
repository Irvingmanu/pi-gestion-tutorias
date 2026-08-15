package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.*;
import mx.edu.utez.pigestiontutorias.models.dao.*;
import mx.edu.utez.pigestiontutorias.utils.UrlUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SesionIndividualServlet", value = "/tutoria-individual")
public class SesionIndividualServlet extends HttpServlet {

    private static final LocalTime HORA_MIN = LocalTime.of(7, 0);
    private static final LocalTime HORA_MAX = LocalTime.of(21, 0);

    private final TutorDao tutorDao = new TutorDao();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final AsignacionTutorDao asignacionTutorDao = new AsignacionTutorDao();
    private final AreaDAO areaDAO = new AreaDAO();
    private final MotivoDAO motivoDAO = new MotivoDAO();
    private final CanalizacionDao canalizacionDao = new CanalizacionDao();
    private final SesionIndividualDao sesionIndividualDao = new SesionIndividualDao();
    private final PeriodoEscolarDao periodoEscolarDao= new PeriodoEscolarDao();
    // Reutilizado solo por su consulta "alumnos activos de un grupo" (misma que usa
    // SesionGrupalServlet); no tiene relacion con sesiones grupales en si.
    private final AsistenciaGrupalDao asistenciaGrupalDao = new AsistenciaGrupalDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("validarMatricula".equals(request.getParameter("accion"))) {
            validarMatricula(request, response);
            return;
        }
        if ("obtenerAlumnosPorGrupo".equals(request.getParameter("accion"))) {
            obtenerAlumnosPorGrupo(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Tutor tutor = tutorDao.getById((Integer) session.getAttribute("idUsuario"));
        if (tutor == null) {
            request.setAttribute("error", "tutor_no_encontrado");
            request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
            return;
        }

        cargarListas(request, tutor);
        request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
    }

    // AJAX consumido desde tutoria-individual.jsp mientras el tutor escribe la matricula:
    // en vez de que se entere hasta el submit final (despues de llenar fecha/hora/temas/
    // acuerdos) de que el alumno no existe o no es suyo, aqui se valida al vuelo.
    private void validarMatricula(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"valido\":false,\"mensaje\":\"Tu sesión expiró, recarga la página.\"}");
            return;
        }

        Tutor tutor = tutorDao.getById((Integer) session.getAttribute("idUsuario"));
        if (tutor == null) {
            response.getWriter().write("{\"valido\":false,\"mensaje\":\"No se encontró tu perfil de tutor.\"}");
            return;
        }

        String matricula = request.getParameter("matricula");
        matricula = matricula != null ? matricula.trim().toUpperCase() : "";

        String json;
        if (matricula.length() != 10) {
            json = "{\"valido\":false,\"mensaje\":\"La matrícula debe tener exactamente 10 caracteres.\"}";
        } else {
            Alumno alumno = alumnoDAO.getById(matricula);
            if (alumno == null) {
                json = "{\"valido\":false,\"mensaje\":\"El alumno no está registrado en el sistema.\"}";
            } else if (!"S".equals(alumno.getEstado())) {
                json = "{\"valido\":false,\"mensaje\":\"El alumno está dado de baja.\"}";
            } else if (!asignacionTutorDao.alumnoPerteneceATutor(tutor.getNumeroEmpleado(), matricula)) {
                json = "{\"valido\":false,\"mensaje\":\"El alumno existe, pero no está asignado a tus grupos.\"}";
            } else {
                json = "{\"valido\":true,\"mensaje\":\"" + escaparJson(alumno.getNombres() + " " + alumno.getApellidos()) + "\"}";
            }
        }

        try (PrintWriter writer = response.getWriter()) {
            writer.write(json);
        }
    }

    // AJAX consumido desde tutoria-individual.jsp al elegir un grupo en el <select>: llena
    // el <datalist> del buscador de alumnos con los alumnos activos de ese grupo, para que
    // el tutor busque por nombre en vez de teclear la matricula a mano.
    private void obtenerAlumnosPorGrupo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("[]");
            return;
        }

        Tutor tutor = tutorDao.getById((Integer) session.getAttribute("idUsuario"));
        if (tutor == null) {
            response.getWriter().write("[]");
            return;
        }

        int idGrupo;
        try {
            idGrupo = Integer.parseInt(request.getParameter("idGrupo").trim());
        } catch (Exception e) {
            response.getWriter().write("[]");
            return;
        }

        // Blindaje de servidor: el grupo debe ser realmente uno de los asignados a este
        // tutor, sin confiar en que el <select> del formulario no fue manipulado.
        if (!asignacionTutorDao.existeAsignacionParaTutor(tutor.getNumeroEmpleado(), idGrupo)) {
            response.getWriter().write("[]");
            return;
        }

        List<Alumno> alumnos = asistenciaGrupalDao.getAlumnosPorGrupo(idGrupo);

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < alumnos.size(); i++) {
            Alumno a = alumnos.get(i);
            if (i > 0) {
                json.append(",");
            }
            json.append("{")
                    .append("\"matricula\":\"").append(escaparJson(a.getMatricula())).append("\",")
                    .append("\"nombres\":\"").append(escaparJson(a.getNombres())).append("\",")
                    .append("\"apellidos\":\"").append(escaparJson(a.getApellidos())).append("\"")
                    .append("}");
        }
        json.append("]");

        try (PrintWriter writer = response.getWriter()) {
            writer.write(json.toString());
        }
    }

    private String escaparJson(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Tutor tutor = tutorDao.getById((Integer) session.getAttribute("idUsuario"));
        if (tutor == null) {
            request.setAttribute("error", "tutor_no_encontrado");
            request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
            return;
        }

        String temasTratados = request.getParameter("temasTratados");
        String acuerdos = request.getParameter("acuerdos");
        String[] idMotivos = request.getParameterValues("idMotivo");
        String idSesionStr = request.getParameter("idSesion");
        String estatusAsistencia = request.getParameter("estatusAsistencia");
        String matricula = request.getParameter("matricula");
        String fechaStr = request.getParameter("fecha");
        String hora = request.getParameter("hora");

        boolean esCompletado = idSesionStr != null && !idSesionStr.isBlank();
        String baseUrl = UrlUtils.baseUrl(request);

        if (temasTratados == null || temasTratados.isBlank() || acuerdos == null || acuerdos.isBlank()) {
            request.setAttribute("error", "campos_incompletos");
            if (!esCompletado) {
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
            }
            cargarListas(request, tutor);
            request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
            return;
        }

        // El radio "Asistió/Faltó" solo existe en el modal de completar sesión; se valida
        // aparte porque las sesiones nuevas (alta directa) no lo tienen en el formulario.
        if (esCompletado && (estatusAsistencia == null
                || !(estatusAsistencia.equals("Presente") || estatusAsistencia.equals("Falta")))) {
            request.setAttribute("error", "asistencia_no_indicada");
            cargarListas(request, tutor);
            request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
            return;
        }

        boolean guardado;

        if (esCompletado) {
            int idSesion = Integer.parseInt(idSesionStr.trim());
            guardado = sesionIndividualDao.completarSesion(idSesion, temasTratados, acuerdos, idMotivos, estatusAsistencia, baseUrl);
        } else {
            if (matricula == null || matricula.isBlank() || fechaStr == null || fechaStr.isBlank()
                    || hora == null || hora.isBlank()) {
                request.setAttribute("error", "campos_incompletos");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }

            // Blindaje de servidor: la identidad del alumno se valida primero (antes de
            // fecha/hora), igual que ya se valida en vivo via AJAX en el frontend. Asi el
            // tutor nunca llena el resto del formulario para enterarse hasta el final de
            // que la matricula no existe o no es de sus grupos.
            matricula = matricula.trim().toUpperCase();

            // Blindaje contra ORA-02291: SESION_INDIVIDUAL.MATRICULA es FK a ALUMNO.MATRICULA,
            // asi que una matricula mal formada o inexistente revienta el INSERT en el DAO.
            // Antes esto era un sendRedirect que perdia todo lo escrito en el formulario;
            // ahora se reenvia (forward) a la misma pantalla con los datos ya capturados.
            if (matricula.length() != 10) {
                request.setAttribute("error", "matricula_invalida");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }

            if (alumnoDAO.getById(matricula) == null) {
                request.setAttribute("error", "matricula_no_existe");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }

            if (!asignacionTutorDao.alumnoPerteneceATutor(tutor.getNumeroEmpleado(), matricula)) {
                request.setAttribute("error", "alumno_no_asignado");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }

            // Blindaje de servidor: la fecha no puede ser futura, sin importar lo que
            // mande el formulario (el <input type="date"> se puede manipular).
            LocalDate fechaSesion;
            try {
                fechaSesion = LocalDate.parse(fechaStr.trim());
            } catch (DateTimeParseException e) {
                request.setAttribute("error", "fecha_invalida");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }
            if (fechaSesion.isAfter(LocalDate.now())) {
                request.setAttribute("error", "fecha_futura");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }

            PeriodoEscolar periodoVigente = periodoEscolarDao.getPeriodoVigente();
            if (periodoVigente != null && fechaSesion.isBefore(periodoVigente.getFechaInicio().toLocalDate())) {
                request.setAttribute("error", "fecha_fuera_periodo");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }

            // Blindaje de servidor: la hora debe estar dentro del horario académico permitido (7:00 - 21:00)
            LocalTime horaSesion;
            try {
                horaSesion = LocalTime.parse(hora.trim());
            } catch (DateTimeParseException e) {
                request.setAttribute("error", "hora_invalida");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }
            if (horaSesion.isBefore(HORA_MIN) || horaSesion.isAfter(HORA_MAX)) {
                request.setAttribute("error", "horario_no_permitido");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }

            Integer idCanalizacionPrincipal = registrarCanalizaciones(idMotivos, matricula, baseUrl);

            SesionIndividual sesion = new SesionIndividual();
            sesion.setIdTutor(tutor.getNumeroEmpleado());
            sesion.setMatricula(matricula);
            sesion.setFecha(Date.valueOf(fechaStr));
            sesion.setHora(hora);
            sesion.setTemasTratados(temasTratados);
            sesion.setAcuerdos(acuerdos);
            sesion.setIdCanalizacion(idCanalizacionPrincipal);
            sesion.setEstado("Completado");
            sesion.setEstatusAsistencia("Presente");

            guardado = sesionIndividualDao.create(sesion);
        }

        if (guardado) {
            String exito = esCompletado ? "completada" : "tutoria_guardada";
            response.sendRedirect(request.getContextPath() + "/tutoria-individual?exito=" + exito);
        } else {
            request.setAttribute("error", "guardado_fallido");
            if (!esCompletado) {
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
            }
            cargarListas(request, tutor);
            request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
        }
    }

    // Reabre la pestaña "Tutoria Espontanea" (en vez de la de Sesiones Programadas,
    // que es la que se ve por defecto) y reenvia al JSP lo que el tutor ya habia
    // escrito, para que un error de validacion no lo obligue a recapturar todo.
    private void marcarTabEspontanea(HttpServletRequest request, String matricula, String fecha, String hora,
                                     String temas, String acuerdos) {
        request.setAttribute("tabActiva", "espontanea");
        request.setAttribute("matriculaEnviada", matricula);
        request.setAttribute("fechaEnviada", fecha);
        request.setAttribute("horaEnviada", hora);
        request.setAttribute("temasEnviados", temas);
        request.setAttribute("acuerdosEnviados", acuerdos);
    }

    // Crea una CANALIZACION por cada motivo seleccionado en "Vinculo Directo" y devuelve
    // la primera generada, para enlazarla como ID_CANALIZACION de la sesion nueva.
    private Integer registrarCanalizaciones(String[] idMotivos, String matricula, String baseUrl) {
        if (idMotivos == null) {
            return null;
        }

        Integer idPrincipal = null;

        for (String idMotivoStr : idMotivos) {
            if (idMotivoStr == null || idMotivoStr.isBlank()) {
                continue;
            }

            Motivo motivo = motivoDAO.getById(Integer.parseInt(idMotivoStr.trim()));
            if (motivo == null) {
                continue;
            }

            Canalizacion c = new Canalizacion();
            c.setIdArea(motivo.getIdArea());
            c.setIdMotivo(motivo.getIdMotivo());
            c.setMatricula(matricula);
            c.setObservaciones(motivo.getNombreMotivo());

            int idGenerado = canalizacionDao.crearYObtenerId(c, baseUrl);
            if (idGenerado > 0 && idPrincipal == null) {
                idPrincipal = idGenerado;
            }
        }

        return idPrincipal;
    }

    private void cargarListas(HttpServletRequest request, Tutor tutor) {
        List<SesionIndividual> sesionesProgramadas = sesionIndividualDao.getSesionesProgramadasByTutor(tutor.getNumeroEmpleado());
        List<Area> areasConMotivos = areaDAO.getAllConMotivos();
        PeriodoEscolar periodoVigente = periodoEscolarDao.getPeriodoVigente();

        Map<String, String> nombresAlumnos = new HashMap<>();
        for (SesionIndividual s : sesionesProgramadas) {
            if (!nombresAlumnos.containsKey(s.getMatricula())) {
                Alumno alumno = alumnoDAO.getById(s.getMatricula());
                nombresAlumnos.put(s.getMatricula(), alumno != null ? alumno.getNombres() + " " + alumno.getApellidos() : s.getMatricula());
            }
        }

        List<Grupo> gruposAsignados = periodoVigente != null
                ? asignacionTutorDao.obtenerGruposPorTutor(tutor.getNumeroEmpleado(), periodoVigente.getIdPeriodo())
                : Collections.emptyList();

        request.setAttribute("sesionesProgramadas", sesionesProgramadas);
        request.setAttribute("areasConMotivos", areasConMotivos);
        request.setAttribute("nombresAlumnos", nombresAlumnos);
        request.setAttribute("periodoVigente", periodoVigente);
        request.setAttribute("gruposAsignados", gruposAsignados);
    }
}