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
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Tutor tutor = tutorDao.findByIdUsuario((Integer) session.getAttribute("idUsuario"));
        if (tutor == null) {
            request.setAttribute("error", "No se encontró el perfil de tutor asociado a tu cuenta.");
            request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
            return;
        }

        cargarListas(request, tutor);
        request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
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
            request.setAttribute("error", "No se encontró el perfil de tutor asociado a tu cuenta.");
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
            request.setAttribute("error", "Completa todos los campos obligatorios.");
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
            request.setAttribute("error", "Indica si el alumno asistió o faltó a la sesión.");
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
                request.setAttribute("error", "Completa todos los campos obligatorios.");
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
                request.setAttribute("error", "La fecha capturada no es válida.");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }
            if (fechaSesion.isAfter(LocalDate.now())) {
                request.setAttribute("error", "No se pueden registrar tutorías con fecha futura.");
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
                request.setAttribute("error", "La hora capturada no es válida.");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }
            if (horaSesion.isBefore(HORA_MIN) || horaSesion.isAfter(HORA_MAX)) {
                request.setAttribute("error", "Las tutorías solo pueden agendarse entre las 7:00 AM y las 9:00 PM.");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }

            matricula = matricula.trim().toUpperCase();

            // Blindaje contra ORA-02291: SESION_INDIVIDUAL.MATRICULA es FK a ALUMNO.MATRICULA,
            // asi que una matricula mal formada o inexistente revienta el INSERT en el DAO.
            // Antes esto era un sendRedirect que perdia todo lo escrito en el formulario;
            // ahora se reenvia (forward) a la misma pantalla con los datos ya capturados.
            if (matricula.length() != 10) {
                request.setAttribute("error", "La matrícula debe tener exactamente 10 caracteres.");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }

            if (alumnoDAO.getById(matricula) == null) {
                request.setAttribute("error", "El alumno no está registrado en el sistema. Verifica la matrícula.");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }

            if (!asignacionTutorDao.alumnoPerteneceATutor(tutor.getIdTutor(), matricula)) {
                request.setAttribute("error", "El alumno con esta matrícula existe, pero no está asignado a tus grupos.");
                marcarTabEspontanea(request, matricula, fechaStr, hora, temasTratados, acuerdos);
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }

            Integer idCanalizacionPrincipal = registrarCanalizaciones(idMotivos, matricula, baseUrl);

            SesionIndividual sesion = new SesionIndividual();
            sesion.setIdTutor(tutor.getIdTutor());
            sesion.setMatricula(matricula);
            sesion.setFecha(Date.valueOf(fechaStr));
            sesion.setHora(hora);
            sesion.setTemasTratados(temasTratados);
            sesion.setAcuerdos(acuerdos);
            sesion.setIdCanalizacion(idCanalizacionPrincipal);
            sesion.setEstado("Tomada");
            sesion.setEstatusAsistencia("Presente");

            guardado = sesionIndividualDao.create(sesion);
        }

        if (guardado) {
            String exito = esCompletado ? "completada" : "tutoria_guardada";
            response.sendRedirect(request.getContextPath() + "/tutoria-individual?exito=" + exito);
        } else {
            request.setAttribute("error", "Ocurrió un error al guardar el registro. Intenta de nuevo.");
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
        List<SesionIndividual> sesionesProgramadas = sesionIndividualDao.getSesionesProgramadasByTutor(tutor.getIdTutor());
        List<Area> areasConMotivos = areaDAO.getAllConMotivos();

        Map<String, String> nombresAlumnos = new HashMap<>();
        for (SesionIndividual s : sesionesProgramadas) {
            if (!nombresAlumnos.containsKey(s.getMatricula())) {
                Alumno alumno = alumnoDAO.getById(s.getMatricula());
                nombresAlumnos.put(s.getMatricula(), alumno != null ? alumno.getNombres() + " " + alumno.getApellidos() : s.getMatricula());
            }
        }

        request.setAttribute("sesionesProgramadas", sesionesProgramadas);
        request.setAttribute("areasConMotivos", areasConMotivos);
        request.setAttribute("nombresAlumnos", nombresAlumnos);
    }
}