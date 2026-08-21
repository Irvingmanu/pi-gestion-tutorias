package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.*;
import mx.edu.utez.pigestiontutorias.models.dao.*;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@WebServlet(name = "SolicitudServlet", urlPatterns = {"/solicitudes"})
public class SolicitudServlet extends HttpServlet {

    private static final String[] DIAS_SEMANA = {
            // Sin acento en "Miercoles" para que coincida con CK_HORARIO_DIA y con lo que
            // devuelve HorarioDao (ver TutorDao.normalizarDiaSemana).
            "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"
    };

    private final SolicitudDao solicitudDao = new SolicitudDao();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final TutorDao tutorDao = new TutorDao();
    private final AsignacionTutorDao asignacionTutorDao = new AsignacionTutorDao();
    private final SesionIndividualDao sesionIndividualDao = new SesionIndividualDao();
    private final HorarioDao horarioDao = new HorarioDao();

    // -----------------------------------------------------------------
    // GET: listar solicitudes del tutor logueado, o ver el detalle de una
    // -----------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String accion = request.getParameter("accion");

        // ---- Formulario de nueva solicitud (alumno) ----
        if ("nueva".equals(accion)) {
            mostrarFormularioNuevaSolicitud(request, response, (String) session.getAttribute("matricula"));
            return;
        }

        // ---- Historial de solicitudes del alumno (Mis Solicitudes) ----
        if ("historial".equals(accion)) {
            mostrarHistorialAlumno(request, response, (String) session.getAttribute("matricula"));
            return;
        }

        // ---- Detalle de una solicitud (imagen 3: Solicitudes_Info) ----
        if ("detalle".equals(accion)) {
            int idSolicitud = Integer.parseInt(request.getParameter("idSolicitud"));
            Solicitud solicitud = solicitudDao.getById(idSolicitud);

            if (solicitud == null) {
                response.sendRedirect(request.getContextPath() + "/solicitudes");
                return;
            }

            request.setAttribute("paginaActiva", "solicitudes");

            // 2. Determinar el color de la etiqueta (Badge) según el estatus
            String badgeColor;
            switch (solicitud.getEstatus() != null ? solicitud.getEstatus() : "") {
                case "Confirmada": badgeColor = "success"; break;
                case "Rechazada": badgeColor = "danger"; break;
                case "Reprogramada": badgeColor = "info"; break;
                default: badgeColor = "warning"; // Para "Pendiente"
            }
            request.setAttribute("badgeColor", badgeColor);

            // 3. Formatear las fechas aquí en Java, para enviarlas limpias al JSP
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd MMMM yyyy", new Locale("es", "MX"));
            if (solicitud.getFechaPropuesta() != null) {
                request.setAttribute("fechaPropuestaFormateada", formatoFecha.format(solicitud.getFechaPropuesta()));
            }
            if (solicitud.getNuevaFecha() != null) {
                request.setAttribute("nuevaFechaFormateada", formatoFecha.format(solicitud.getNuevaFecha()));
            }

            // Solo si sigue pendiente tiene sentido ofrecer "Reprogramar": se arma
            // la misma disponibilidad real (día+hora) que ve el alumno al crearla.
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

        // ---- Listado de solicitudes del tutor (imagen 2: Solicitudes) ----
        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        Tutor tutor = idUsuario != null ? tutorDao.getById(idUsuario) : null;

        // >>> FIX: antes esta línea no existía, por eso el navbar nunca marcaba
        // "Solicitudes" como activo al entrar al listado normal.
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

    // -----------------------------------------------------------------
    // POST: crear solicitud (alumno) / aceptar o rechazar (tutor)
    // -----------------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String accion = request.getParameter("accion");

        // ---- Crear solicitud (formulario solicitud.jsp del alumno) ----
        if ("crear".equals(accion)) {
            String matricula = (String) session.getAttribute("matricula");
            Alumno alumno = matricula != null ? alumnoDAO.getById(matricula) : null;

            if (alumno == null || alumno.getIdGrupo() == null) {
                response.sendRedirect(request.getContextPath() + "/alumno/solicitud.jsp?exito=error");
                return;
            }

            // No confiamos en un idTutor mandado por el formulario: lo calculamos
            // aquí igual que en el JSP, a partir del grupo del alumno.
            Integer idTutor = asignacionTutorDao.findIdTutorByGrupo(alumno.getIdGrupo());

            if (idTutor == null) {
                response.sendRedirect(request.getContextPath() + "/alumno/solicitud.jsp?exito=error");
                return;
            }

            // Validamos la fecha propuesta contra el mínimo de 2 días de anticipación
            // ANTES de construir la Solicitud: el <select> del JSP ya excluye estos
            // días, pero eso es solo una restricción de UI — un POST directo
            // (curl/Postman) podría saltársela, así que se vuelve a exigir aquí.
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
            solicitud.setAsunto(request.getParameter("asunto"));
            solicitud.setDescripcion(request.getParameter("descripcion"));
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

        // ---- Aceptar (pantalla de detalle, botón del tutor) ----
        if ("aceptar".equals(accion)) {
            int idSolicitud = Integer.parseInt(request.getParameter("idSolicitud"));
            Solicitud solicitud = solicitudDao.getById(idSolicitud);

            solicitudDao.actualizarEstatus(idSolicitud, "Confirmada");

            // Al aceptar, la solicitud se convierte en una sesión pendiente: así deja de
            // ser invisible para el alumno y aparece en su agenda.
            if (solicitud != null && solicitud.getFechaPropuesta() != null) {
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

        // ---- Rechazar (pantalla de detalle, botón del tutor) ----
        if ("rechazar".equals(accion)) {
            int idSolicitud = Integer.parseInt(request.getParameter("idSolicitud"));
            solicitudDao.actualizarEstatus(idSolicitud, "Rechazada");

            response.sendRedirect(request.getContextPath() + "/solicitudes");
            return;
        }

        // ---- Reprogramar (el tutor propone una nueva fecha) ----
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

            // No basta con la fecha: revalidamos que la hora elegida siga libre en
            // el horario real del tutor (mismo cálculo que arma el <select> en el
            // JSP), por si el bloque se ocupó entre que se cargó la pantalla y se
            // envió el formulario, o si llega un POST directo saltándose la UI.
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

        // Acción no reconocida: regresamos a la lista por seguridad
        response.sendRedirect(request.getContextPath() + "/solicitudes");
    }

    // -----------------------------------------------------------------
    // Formulario de nueva solicitud: arma la disponibilidad real del
    // tutor asignado (próximos 14 días) para reemplazar el dummy del JSP.
    // -----------------------------------------------------------------
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

    // Disponibilidad real del tutor entre fechaInicio y limite (ambos incluidos):
    // cruza el HORARIO_ATENCION (fraccionado en bloques de 1 hora) con las horas
    // ya ocupadas. Los días sin ningún hueco libre no se agregan al mapa.
    // La usan tanto el formulario de nueva solicitud (alumno) como el de
    // reprogramar (tutor), para no calcular la disponibilidad de dos formas distintas.
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

    // Arma a mano el JSON (sin librerías externas) con el formato
    // {"2026-08-03":["13:00","14:00"]} para los próximos 14 días, empezando
    // con el mismo mínimo de 2 días de anticipación que se exige en doPost.
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

    // Verifica que "hora" (y, si la duración es de 2 horas, también el bloque
    // siguiente) estén dentro de las horas libres de ese día.
    private boolean horarioDisponible(Set<String> horasDelDia, String hora, int duracion) {
        if (horasDelDia == null || !horasDelDia.contains(hora)) {
            return false;
        }
        return duracion != 2 || horasDelDia.contains(sumarUnaHora(hora));
    }

    // "13:00" -> "14:00"
    private String sumarUnaHora(String hora) {
        int horaBase = Integer.parseInt(hora.split(":")[0]);
        return String.format("%02d:00", horaBase + 1);
    }

    // -----------------------------------------------------------------
    // Historial de solicitudes del alumno logueado (vista "Mis Solicitudes")
    // -----------------------------------------------------------------
    private void mostrarHistorialAlumno(HttpServletRequest request, HttpServletResponse response, String matricula)
            throws ServletException, IOException {

        List<Solicitud> listaSolicitudes = (matricula != null)
                ? solicitudDao.getSolicitudesByAlumno(matricula)
                : Collections.emptyList();

        request.setAttribute("listaSolicitudes", listaSolicitudes);
        request.getRequestDispatcher("/alumno/mis-solicitudes.jsp").forward(request, response);
    }

    // Convierte un bloque "13:00" - "16:00" en las horas de inicio de 1 hora
    // que contiene: 13:00, 14:00, 15:00.
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