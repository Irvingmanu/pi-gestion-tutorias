package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.Grupo;
import mx.edu.utez.pigestiontutorias.models.SesionGrupal;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "SesionGrupalServlet", value = "/tutoria-grupal")
public class SesionGrupalServlet extends HttpServlet {

    private final TutorDao tutorDao = new TutorDao();
    private final AsignacionTutorDao asignacionTutorDao = new AsignacionTutorDao();
    private final AsistenciaGrupalDao asistenciaGrupalDao = new AsistenciaGrupalDao();
    private final SesionGrupalDao sesionGrupalDao = new SesionGrupalDao();
    private final PeriodoEscolarDao periodoDao = new PeriodoEscolarDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("obtenerAlumnos".equals(request.getParameter("accion"))) {
            obtenerAlumnosPorGrupo(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Tutor tutor = tutorDao.getById((Integer) session.getAttribute("idUsuario"));
        mx.edu.utez.pigestiontutorias.models.PeriodoEscolar periodoVigente = periodoDao.getPeriodoVigente();

        List<Grupo> gruposAsignados = (tutor != null && periodoVigente != null)
                ? asignacionTutorDao.obtenerGruposPorTutor(tutor.getNumeroEmpleado(), periodoVigente.getIdPeriodo())
                : Collections.emptyList();

        request.setAttribute("gruposAsignados", gruposAsignados);
        request.setAttribute("paginaActiva", "grupal");
        request.setAttribute("periodoVigente", periodoVigente);
        request.getRequestDispatcher("/tutor/registro-grupal.jsp").forward(request, response);
    }

    // AJAX consumido desde registro-grupal.jsp al elegir un grupo: arma el JSON a mano con
    // los alumnos activos de ese grupo (ya no hay que cruzar Carrera+Cuatrimestre+Letra,
    // ID_GRUPO alcanza) para pintar la tabla de asistencia sin recargar la pagina.
    private void obtenerAlumnosPorGrupo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=tutor_no_encontrado");
            return;
        }

        String idGrupoStr = request.getParameter("idGrupo");
        String fechaStr = request.getParameter("fecha");
        String hora = request.getParameter("hora");
        String acuerdos = request.getParameter("acuerdos");
        String temas = request.getParameter("temas");
        String asesorias = request.getParameter("asesorias");

        if (idGrupoStr == null || idGrupoStr.isBlank()
                || fechaStr == null || fechaStr.isBlank()
                || hora == null || hora.isBlank()
                || acuerdos == null || acuerdos.isBlank()
                || temas == null || temas.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=campos_incompletos");
            return;
        }

        int idGrupo;
        Date fecha;
        try {
            idGrupo = Integer.parseInt(idGrupoStr.trim());
            fecha = Date.valueOf(fechaStr.trim());
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=datos_invalidos");
            return;
        }

        java.time.LocalDate fechaSesion = fecha.toLocalDate();
        java.time.LocalDate hoy = java.time.LocalDate.now();
        if (fechaSesion.isAfter(hoy)) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=fecha_futura");
            return;
        }

        mx.edu.utez.pigestiontutorias.models.PeriodoEscolar periodoVigente = periodoDao.getPeriodoVigente();
        if (periodoVigente != null && fechaSesion.isBefore(periodoVigente.getFechaInicio().toLocalDate())) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=fecha_fuera_periodo");
            return;
        }
        // Blindaje de servidor: la hora debe estar dentro del horario académico permitido (7:00 - 21:00)
        java.time.LocalTime horaSesion;
        try {
            horaSesion = java.time.LocalTime.parse(hora.trim());
        } catch (java.time.format.DateTimeParseException e) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=datos_invalidos");
            return;
        }
        java.time.LocalTime horaMin = java.time.LocalTime.of(7, 0);
        java.time.LocalTime horaMax = java.time.LocalTime.of(21, 0);
        if (horaSesion.isBefore(horaMin) || horaSesion.isAfter(horaMax)) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=horario_no_permitido");
            return;
        }

        // Blindaje de servidor: el grupo enviado debe ser uno de los que el tutor
        // realmente tiene asignados, sin confiar en que el <select> del formulario
        // no fue manipulado.
        if (!asignacionTutorDao.existeAsignacionParaTutor(tutor.getNumeroEmpleado(), idGrupo)) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=grupo_no_asignado");
            return;
        }

        // "asesorias" es opcional: se guarda NULL en ASESORIAS_GRUPALES si no se captura
        asesorias = (asesorias != null && !asesorias.isBlank()) ? asesorias.trim() : null;

        SesionGrupal sesion = new SesionGrupal();
        sesion.setIdGrupo(idGrupo);
        sesion.setIdTutor(tutor.getNumeroEmpleado());
        sesion.setFecha(fecha);
        sesion.setHora(hora.trim());
        sesion.setTemasTratados(temas.trim());
        sesion.setAcuerdos(acuerdos.trim());
        sesion.setAsesoriasGrupales(asesorias);
        sesion.setEstado("Completado");
        sesion.setAsistentes(request.getParameterValues("asistentes"));

        boolean guardado = sesionGrupalDao.create(sesion);

        if (guardado) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?exito=grupal_guardada");
        } else {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=guardado_fallido");
        }
    }
}
