package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.Solicitud;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.AsignacionTutorDao;
import mx.edu.utez.pigestiontutorias.models.dao.SolicitudDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "SolicitudServlet", urlPatterns = {"/SolicitudServlet"})
public class SolicitudServlet extends HttpServlet {

    private final SolicitudDao solicitudDao = new SolicitudDao();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final TutorDao tutorDao = new TutorDao();
    private final AsignacionTutorDao asignacionTutorDao = new AsignacionTutorDao();

    // -----------------------------------------------------------------
    // GET: listar solicitudes del tutor logueado, o ver el detalle de una
    // -----------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int idUsuario = (Integer) session.getAttribute("idUsuario");
        String accion = request.getParameter("accion");

        // ---- Detalle de una solicitud (imagen 3: Solicitudes_Info) ----
        if ("detalle".equals(accion)) {
            int idSolicitud = Integer.parseInt(request.getParameter("idSolicitud"));
            Solicitud solicitud = solicitudDao.findById(idSolicitud);

            if (solicitud == null) {
                response.sendRedirect(request.getContextPath() + "/SolicitudServlet");
                return;
            }

            request.setAttribute("solicitud", solicitud);
            request.getRequestDispatcher("/tutor/solicitud-detalle.jsp").forward(request, response);
            return;
        }

        // ---- Listado de solicitudes del tutor (imagen 2: Solicitudes) ----
        Tutor tutor = tutorDao.findByIdUsuario(idUsuario);
        if (tutor == null) {
            request.setAttribute("error", "No se encontró el tutor asociado a este usuario.");
            request.getRequestDispatcher("/tutor/solicitudes.jsp").forward(request, response);
            return;
        }

        List<Solicitud> listaSolicitudes = solicitudDao.findByTutor(tutor.getIdTutor());
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
        if (session == null || session.getAttribute("idUsuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int idUsuario = (Integer) session.getAttribute("idUsuario");
        String accion = request.getParameter("accion");

        // ---- Crear solicitud (formulario solicitud.jsp del alumno) ----
        if ("crear".equals(accion)) {
            Alumno alumno = alumnoDAO.getByIdUsuario(idUsuario);

            if (alumno == null) {
                response.sendRedirect(request.getContextPath() + "/alumno/solicitud.jsp?exito=error");
                return;
            }

            // No confiamos en un idTutor mandado por el formulario: lo calculamos
            // aquí igual que en el JSP, a partir del grupo+cuatrimestre del alumno.
            Integer idTutor = asignacionTutorDao.findIdTutorByGrupoYCuatrimestre(
                    alumno.getIdLetraGrupo(), alumno.getIdCuatrimestre());

            if (idTutor == null) {
                response.sendRedirect(request.getContextPath() + "/alumno/solicitud.jsp?exito=error");
                return;
            }

            Solicitud solicitud = new Solicitud();
            solicitud.setMatricula(alumno.getMatricula());
            solicitud.setIdTutor(idTutor);
            solicitud.setIdHorario(Integer.parseInt(request.getParameter("idHorario")));
            solicitud.setAsunto(request.getParameter("asunto"));
            solicitud.setDescripcion(request.getParameter("descripcion"));

            boolean exito = solicitudDao.insertar(solicitud);

            String parametro = exito ? "enviada" : "error";
            response.sendRedirect(request.getContextPath() + "/alumno/solicitud.jsp?exito=" + parametro);
            return;
        }

        // ---- Aceptar o rechazar (pantalla de detalle, botones del tutor) ----
        if ("aceptar".equals(accion) || "rechazar".equals(accion)) {
            int idSolicitud = Integer.parseInt(request.getParameter("idSolicitud"));
            String nuevoEstatus = "aceptar".equals(accion) ? "Confirmada" : "Rechazada";

            solicitudDao.actualizarEstatus(idSolicitud, nuevoEstatus);

            response.sendRedirect(request.getContextPath() + "/SolicitudServlet");
            return;
        }

        // Acción no reconocida: regresamos a la lista por seguridad
        response.sendRedirect(request.getContextPath() + "/SolicitudServlet");
    }
}
