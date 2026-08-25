package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.*;
import mx.edu.utez.pigestiontutorias.models.dao.*;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@WebServlet(name = "SolicitudServlet", urlPatterns = {"/solicitudes"})
public class SolicitudServlet extends HttpServlet {

    private static final String[] DIAS_SEMANA = {
            "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"
    };

    private final SolicitudDao solicitudDao = new SolicitudDao();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final TutorDao tutorDao = new TutorDao();
    private final AsignacionTutorDao asignacionTutorDao = new AsignacionTutorDao();
    private final SesionIndividualDao sesionIndividualDao = new SesionIndividualDao();
    private final HorarioDao horarioDao = new HorarioDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        solicitudDao.cancelarSolicitudesVencidas();

        String accion = request.getParameter("accion");

        if ("nueva".equals(accion)) {
            mostrarFormularioNuevaSolicitud(request, response, (String) session.getAttribute("matricula"));
            return;
        }

        if ("historial".equals(accion)) {
            mostrarHistorialAlumno(request, response, (String) session.getAttribute("matricula"));
            return;
        }

        if ("detalle".equals(accion)) {
            int idSolicitud = Integer.parseInt(request.getParameter("idSolicitud"));
            Solicitud solicitud = solicitudDao.getById(idSolicitud);

            if (solicitud == null) {
                response.sendRedirect(request.getContextPath() + "/solicitudes");
                return;
            }

            request.setAttribute("paginaActiva", "solicitudes");

            String badgeColor;
            switch (solicitud.getEstatus() != null ? solicitud.getEstatus() : "") {
                case "Confirmada": badgeColor = "success"; break;
                case "Rechazada": badgeColor = "danger"; break;
                case "Reprogramada": badgeColor = "info"; break;
                case "Cancelada": badgeColor = "secondary"; break;
                default: badgeColor = "warning";
            }
            request.setAttribute("badgeColor", badgeColor);

            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd MMMM yyyy", new Locale("es", "MX"));
            if (solicitud.getFechaPropuesta() != null) {
                request.setAttribute("fechaPropuestaFormateada", formatoFecha.format(solicitud.getFechaPropuesta()));
            }
            if (solicitud.getNuevaFecha() != null) {
                request.setAttribute("nuevaFechaFormateada", formatoFecha.format(solicitud.getNuevaFecha()));
            }

            if ("Pendiente".equals(solicitud.getEstatus())) {
                List<Horario> listaHorarios = horarioDao.findDisponiblesByTutor(solicitud.getIdTutor());
                String disponibilidadJson = construirDisponibilidadJson(solicitud.getIdTutor(), listaHorarios);
                request.setAttribute("disponibilidadJson", disponibilidadJson);
                request.setAttribute("duracionSolicitud", solicitud.getDuracion() != null ? solicitud.getDuracion() : 1);
            }

            request.setAttribute("solicitud", solicitud);
            request.getRequestDispatcher("/tutor/solicitud-detalle.jsp").forward(request, response);
            return;
        }

        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        Tutor tutor = idUsuario != null ? tutorDao.getById(idUsuario) : null;

        request.setAttribute("paginaActiva", "solicitudes");

        if (tutor == null) {
            request.setAttribute("error", "No se encontró el tutor asociado a este usuario.");
            request.getRequestDispatcher("/tutor/solicitudes.jsp").forward(request, response);
            return;
        }

        List<Solicitud> listaSolicitudes = solicitudDao.findByTutor(tutor.getNumeroEmpleado());
        request.setAttribute("listaSolicitudes", listaSolicitudes);
        request.getRequestDispatcher("/tutor/solicitudes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String accion = request.getParameter("accion");

        if ("crear".equals(accion)) {
            String matricula = (String) session.getAttribute("matricula");
            Alumno alumno = matricula != null ? alumnoDAO.getById(matricula) : null;

            if (alumno == null || alumno.getIdGrupo() == null) {
                response.sendRedirect(request.getContextPath() + "/alumno/solicitud.jsp?exito=error");
                return;
            }

            if (solicitudDao.tieneSolicitudReciente(alumno.getMatricula())) {
                response.sendRedirect(request.getContextPath() + "/solicitudes?accion=nueva&error=limite_semanal");
                return;
            }

            Integer idTutor = asignacionTutorDao.findIdTutorByGrupo(alumno.getIdGrupo());

            if (idTutor == null) {
                response.sendRedirect(request.getContextPath() + "/alumno/solicitud.jsp?exito=error");
                return;
            }

            String fechaPropuestaStr = request.getParameter("fechaPropuesta");
            LocalDate fechaPropuesta = null;
            try {
                if (fechaPropuestaStr != null && !fechaPropuestaStr.isBlank()) {
                    fechaPropuesta = LocalDate.parse(fechaPropuestaStr);
                }
            } catch (DateTimeParseException e) {
                fechaPropuesta = null;
            }

            LocalDate fechaMinima = LocalDate.now().plusDays(2);
            if (fechaPropuesta == null || fechaPropuesta.isBefore(fechaMinima)) {
                response.sendRedirect(request.getContextPath() + "/solicitudes?accion=nueva&error=fecha_invalida");
                return;
            }

            Solicitud solicitud = new Solicitud();
            solicitud.setMatricula(alumno.getMatricula());
            solicitud.setIdTutor(idTutor);
            solicitud.setAsunto(sanitizarTexto(request.getParameter("asunto")));
            solicitud.setDescripcion(sanitizarTexto(request.getParameter("descripcion")));
            solicitud.setFechaPropuesta(Date.valueOf(fechaPropuesta));

            String duracionStr = request.getParameter("duracion");
            if (duracionStr != null && !duracionStr.isBlank()) {
                solicitud.setDuracion(Integer.parseInt(duracionStr));
            }

            solicitud.setHoraPropuesta(request.getParameter("horaPropuesta"));

            boolean exito = solicitudDao.create(solicitud);

            String parametro = exito ? "enviada" : "error";
            response.sendRedirect(request.getContextPath() + "/alumno/solicitud.jsp?exito=" + parametro);
            return;
        }

        if ("aceptar".equals(accion)) {
            int idSolicitud = Integer.parseInt(request.getParameter("idSolicitud"));

            solicitudDao.cancelarSolicitudesVencidas();
            Solicitud solicitud = solicitudDao.getById(idSolicitud);

            if (solicitud == null || !"Pendiente".equals(solicitud.getEstatus())) {
                response.sendRedirect(request.getContextPath() + "/solicitudes?accion=detalle&idSolicitud="
                        + idSolicitud + "&error=solicitud_vencida");
                return;
            }

            solicitudDao.actualizarEstatus(idSolicitud, "Confirmada");

            if (solicitud.getFechaPropuesta() != null) {
                SesionIndividual sesion = new SesionIndividual();
                sesion.setIdTutor(solicitud.getIdTutor());
                sesion.setMatricula(solicitud.getMatricula());
                sesion.setFecha(solicitud.getFechaPropuesta());
                sesion.setHora(solicitud.getHoraPropuesta());
                sesion.setTemasTratados("Por definir");
                sesion.setAcuerdos("Por definir");
                sesion.setEstado("Pendiente");
                sesionIndividualDao.create(sesion);
            }

            response.sendRedirect(request.getContextPath() + "/solicitudes");
            return;
        }

        if ("rechazar".equals(accion)) {
            int idSolicitud = Integer.parseInt(request.getParameter("idSolicitud"));
            solicitudDao.actualizarEstatus(idSolicitud, "Rechazada");

            response.sendRedirect(request.getContextPath() + "/solicitudes");
            return;
        }

        if ("reprogramar".equals(accion)) {
            int idSolicitud = Integer.parseInt(request.getParameter("idSolicitud"));
            Solicitud solicitud = solicitudDao.getById(idSolicitud);

            if (solicitud == null) {
                response.sendRedirect(request.getContextPath() + "/solicitudes");
                return;
            }

            String nuevaFechaStr = request.getParameter("nuevaFecha");
            String nuevaHora = request.getParameter("nuevaHora");

            LocalDate nuevaFecha = null;
            try {
                if (nuevaFechaStr != null && !nuevaFechaStr.isBlank()) {
                    nuevaFecha = LocalDate.parse(nuevaFechaStr);
                }
            } catch (DateTimeParseException e) {
                nuevaFecha = null;
            }

            LocalDate fechaMinima = LocalDate.now().plusDays(2);
            boolean fechaValida = nuevaFecha != null && !nuevaFecha.isBefore(fechaMinima);

            boolean horaValida = false;
            if (fechaValida && nuevaHora != null && !nuevaHora.isBlank()) {
                List<Horario> listaHorarios = horarioDao.findDisponiblesByTutor(solicitud.getIdTutor());
                int duracion = solicitud.getDuracion() != null ? solicitud.getDuracion() : 1;
                Map<LocalDate, Set<String>> disponibilidad = construirDisponibilidad(
                        solicitud.getIdTutor(), listaHorarios, nuevaFecha, nuevaFecha);
                horaValida = horarioDisponible(disponibilidad.get(nuevaFecha), nuevaHora, duracion);
            }

            if (!fechaValida || !horaValida) {
                response.sendRedirect(request.getContextPath() + "/solicitudes?accion=detalle&idSolicitud="
                        + idSolicitud + "&error=fecha_invalida");
                return;
            }

            solicitudDao.reprogramar(idSolicitud, Date.valueOf(nuevaFecha), nuevaHora);

            response.sendRedirect(request.getContextPath() + "/solicitudes");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/solicitudes");
    }

    private String sanitizarTexto(String valor) {
        if (valor == null) return null;
        return StringEscapeUtils.escapeHtml4(valor.trim());
    }

    private void mostrarFormularioNuevaSolicitud(HttpServletRequest request, HttpServletResponse response, String matricula)
            throws ServletException, IOException {

        Alumno alumno = matricula != null ? alumnoDAO.getById(matricula) : null;
        List<Horario> listaHorarios = new ArrayList<>();
        Integer idTutorAsignado = null;

        if (alumno != null && alumno.getIdGrupo() != null) {
            idTutorAsignado = asignacionTutorDao.findIdTutorByGrupo(alumno.getIdGrupo());

            if (idTutorAsignado != null) {
                listaHorarios = horarioDao.findDisponiblesByTutor(idTutorAsignado);
            }
        }

        String disponibilidadJson = (idTutorAsignado != null)
                ? construirDisponibilidadJson(idTutorAsignado, listaHorarios)
                : "{}";

        request.setAttribute("idTutorAsignado", idTutorAsignado);
        request.setAttribute("listaHorarios", listaHorarios);
        request.setAttribute("disponibilidadJson", disponibilidadJson);
        request.getRequestDispatcher("/alumno/solicitud.jsp").forward(request, response);
    }

    private Map<LocalDate, Set<String>> construirDisponibilidad(int idTutor, List<Horario> listaHorarios,
                                                                LocalDate fechaInicio, LocalDate limite) {
        Map<LocalDate, Set<String>> horasOcupadas = solicitudDao.getHorasOcupadas(idTutor, fechaInicio, limite);
        Map<LocalDate, Set<String>> disponibilidad = new LinkedHashMap<>();

        for (LocalDate fecha = fechaInicio; !fecha.isAfter(limite); fecha = fecha.plusDays(1)) {
            String nombreDia = DIAS_SEMANA[fecha.getDayOfWeek().getValue() - 1];

            Set<String> horasDelDia = new LinkedHashSet<>();
            for (Horario horario : listaHorarios) {
                if (nombreDia.equalsIgnoreCase(horario.getDiaSemana())) {
                    horasDelDia.addAll(fraccionarEnHoras(horario.getHoraDesde(), horario.getHoraHasta()));
                }
            }

            horasDelDia.removeAll(horasOcupadas.getOrDefault(fecha, Collections.emptySet()));

            if (!horasDelDia.isEmpty()) {
                disponibilidad.put(fecha, horasDelDia);
            }
        }

        return disponibilidad;
    }

    private String construirDisponibilidadJson(int idTutor, List<Horario> listaHorarios) {
        LocalDate fechaInicio = LocalDate.now().plusDays(2);
        LocalDate limite = LocalDate.now().plusDays(14);

        Map<LocalDate, Set<String>> disponibilidad = construirDisponibilidad(idTutor, listaHorarios, fechaInicio, limite);

        StringBuilder json = new StringBuilder("{");
        boolean primerDia = true;

        for (Map.Entry<LocalDate, Set<String>> entrada : disponibilidad.entrySet()) {
            if (!primerDia) {
                json.append(',');
            }
            primerDia = false;

            json.append('"').append(entrada.getKey()).append("\":[");
            boolean primeraHora = true;
            for (String hora : entrada.getValue()) {
                if (!primeraHora) {
                    json.append(',');
                }
                json.append('"').append(hora).append('"');
                primeraHora = false;
            }
            json.append(']');
        }

        json.append('}');
        return json.toString();
    }

    private boolean horarioDisponible(Set<String> horasDelDia, String hora, int duracion) {
        if (horasDelDia == null || !horasDelDia.contains(hora)) {
            return false;
        }
        return duracion != 2 || horasDelDia.contains(sumarUnaHora(hora));
    }

    private String sumarUnaHora(String hora) {
        int horaBase = Integer.parseInt(hora.split(":")[0]);
        return String.format("%02d:00", horaBase + 1);
    }

    private void mostrarHistorialAlumno(HttpServletRequest request, HttpServletResponse response, String matricula)
            throws ServletException, IOException {

        List<Solicitud> listaSolicitudes = (matricula != null)
                ? solicitudDao.getSolicitudesByAlumno(matricula)
                : Collections.emptyList();

        request.setAttribute("listaSolicitudes", listaSolicitudes);
        request.getRequestDispatcher("/alumno/mis-solicitudes.jsp").forward(request, response);
    }

    private List<String> fraccionarEnHoras(String horaDesde, String horaHasta) {
        List<String> horas = new ArrayList<>();

        int desde = Integer.parseInt(horaDesde.split(":")[0]);
        int hasta = Integer.parseInt(horaHasta.split(":")[0]);

        for (int hora = desde; hora < hasta; hora++) {
            horas.add(String.format("%02d:00", hora));
        }
        return horas;
    }
}
