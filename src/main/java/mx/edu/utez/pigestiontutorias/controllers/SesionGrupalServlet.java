package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.AsignacionDTO;
import mx.edu.utez.pigestiontutorias.models.SesionGrupal;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.AsignacionTutorDao;
import mx.edu.utez.pigestiontutorias.models.dao.AsistenciaGrupalDao;
import mx.edu.utez.pigestiontutorias.models.dao.SesionGrupalDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;

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

        Tutor tutor = tutorDao.findByIdUsuario((Integer) session.getAttribute("idUsuario"));
        List<AsignacionDTO> asignaciones = (tutor != null)
                ? asignacionTutorDao.obtenerAsignacionesPorTutor(tutor.getIdTutor())
                : Collections.emptyList();

        request.setAttribute("asignaciones", asignaciones);
        request.setAttribute("paginaActiva", "grupal");
        request.getRequestDispatcher("/tutor/registro-grupal.jsp").forward(request, response);
    }

    // AJAX consumido desde registro-grupal.jsp al elegir un grupo (Carrera+Cuatrimestre+Letra):
    // arma el JSON a mano con los alumnos activos de ese grupo real para pintar la tabla
    // de asistencia sin recargar la pagina.
    private void obtenerAlumnosPorGrupo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("[]");
            return;
        }

        int idCarrera;
        int idCuatrimestre;
        int idLetra;
        try {
            idCarrera = Integer.parseInt(request.getParameter("idCarrera").trim());
            idCuatrimestre = Integer.parseInt(request.getParameter("idCuatrimestre").trim());
            idLetra = Integer.parseInt(request.getParameter("idLetra").trim());
        } catch (Exception e) {
            response.getWriter().write("[]");
            return;
        }

        List<Alumno> alumnos = asistenciaGrupalDao.getAlumnosPorFiltros(idLetra, idCarrera, idCuatrimestre);

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

        Tutor tutor = tutorDao.findByIdUsuario((Integer) session.getAttribute("idUsuario"));
        if (tutor == null) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=tutor_no_encontrado");
            return;
        }

        String grupoAsignadoStr = request.getParameter("grupoAsignado");
        String fechaStr = request.getParameter("fecha");
        String hora = request.getParameter("hora");
        String acuerdos = request.getParameter("acuerdos");
        String temas = request.getParameter("temas");
        String asesorias = request.getParameter("asesorias");

        if (grupoAsignadoStr == null || grupoAsignadoStr.isBlank()
                || fechaStr == null || fechaStr.isBlank()
                || hora == null || hora.isBlank()
                || acuerdos == null || acuerdos.isBlank()
                || temas == null || temas.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=campos_incompletos");
            return;
        }

        // "grupoAsignado" viene del <select> como "idCarrera|idCuatrimestre|idLetra"
        String[] partesGrupo = grupoAsignadoStr.split("\\|");

        int idCarrera;
        int idCuatrimestre;
        int idLetra;
        Date fecha;
        try {
            if (partesGrupo.length != 3) {
                throw new IllegalArgumentException("Formato de grupo inválido");
            }
            idCarrera = Integer.parseInt(partesGrupo[0].trim());
            idCuatrimestre = Integer.parseInt(partesGrupo[1].trim());
            idLetra = Integer.parseInt(partesGrupo[2].trim());
            fecha = Date.valueOf(fechaStr.trim());
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=datos_invalidos");
            return;
        }

        // Blindaje de servidor: la fecha no puede ser futura, sin importar lo que
        // mande el formulario (el <input type="date"> se puede manipular).
        java.time.LocalDate fechaSesion = fecha.toLocalDate();
        java.time.LocalDate hoy = java.time.LocalDate.now();
        if (fechaSesion.isAfter(hoy)) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=fecha_futura");
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
        if (!asignacionTutorDao.existeAsignacionParaTutor(tutor.getIdTutor(), idCarrera, idCuatrimestre, idLetra)) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=grupo_no_asignado");
            return;
        }

        // "asesorias" es opcional: se guarda NULL en ASESORIAS_GRUPALES si no se captura
        asesorias = (asesorias != null && !asesorias.isBlank()) ? asesorias.trim() : null;

        SesionGrupal sesion = new SesionGrupal();
        sesion.setIdCarrera(idCarrera);
        sesion.setIdLetraGrupo(idLetra);
        sesion.setIdCuatrimestre(idCuatrimestre);
        sesion.setIdTutor(tutor.getIdTutor());
        sesion.setFecha(fecha);
        sesion.setHora(hora.trim());
        sesion.setTemasTratados(temas.trim());
        sesion.setAcuerdos(acuerdos.trim());
        sesion.setAsesoriasGrupales(asesorias);
        sesion.setEstado("Tomada");
        sesion.setAsistentes(request.getParameterValues("asistentes"));

        boolean guardado = sesionGrupalDao.create(sesion);

        if (guardado) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?exito=grupal_guardada");
        } else {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=guardado_fallido");
        }
    }
}
