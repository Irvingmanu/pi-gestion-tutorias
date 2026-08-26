package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Canalizacion;
import mx.edu.utez.pigestiontutorias.models.SesionIndividual;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.CanalizacionDao;
import mx.edu.utez.pigestiontutorias.models.dao.SesionIndividualDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;
import mx.edu.utez.pigestiontutorias.utils.UrlUtils;

import java.io.IOException;
import java.sql.Date;

/**
 * Servlet que gestiona el registro directo de tutorías individuales del tutor,
 * incluyendo la canalización opcional del alumno a un área de apoyo.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-22
 */
@WebServlet("/TutoriaServlet")
public class TutoriaServlet extends HttpServlet {

    private final TutorDao tutorDao = new TutorDao();
    private final SesionIndividualDao sesionIndividualDao = new SesionIndividualDao();
    private final CanalizacionDao canalizacionDao = new CanalizacionDao();

    /**
     * Reenvía a la vista de registro individual de tutorías, verificando que exista
     * una sesión de usuario autenticada.
     * @param request petición HTTP con la sesión del tutor autenticado
     * @param response respuesta HTTP usada para redirigir o reenviar a la vista JSP
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        request.getRequestDispatcher("/tutor/registro-individual.jsp").forward(request, response);
    }

    /**
     * Atiende las peticiones POST del registro individual, delegando en
     * {@link #registrarIndividual} cuando la acción es "registrarIndividual".
     * @param request petición HTTP con el parámetro "accion" y los datos de la tutoría
     * @param response respuesta HTTP usada para redirigir o reenviar a la vista JSP
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String accion = request.getParameter("accion");

        if ("registrarIndividual".equals(accion)) {
            registrarIndividual(request, response, session);
        } else {
            response.sendRedirect(request.getContextPath() + "/tutor/registro-individual.jsp");
        }
    }

    /**
     * Valida y registra una tutoría individual con los datos capturados, canalizando
     * opcionalmente al alumno a un área de apoyo si se indicó una.
     * @param request petición HTTP con los datos de la tutoría (matrícula, fecha, temas, acuerdos y canalización)
     * @param response respuesta HTTP usada para reenviar a la vista con el resultado
     * @param session la sesión HTTP con los datos del tutor autenticado
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    private void registrarIndividual(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException {

        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        Tutor tutor = tutorDao.getById(idUsuario);

        if (tutor == null) {
            request.setAttribute("error", "No se encontró el perfil de tutor asociado a tu cuenta.");
            request.getRequestDispatcher("/tutor/registro-individual.jsp").forward(request, response);
            return;
        }

        int idTutor = tutor.getNumeroEmpleado();

        String matricula = request.getParameter("matricula");
        String fechaStr = request.getParameter("fecha");
        String temasTratados = request.getParameter("temasTratados");
        String acuerdos = request.getParameter("acuerdos");
        String idAreaStr = request.getParameter("idAreaCanalizacion");
        String observaciones = request.getParameter("observacionesCanalizacion");

        if (matricula == null || matricula.isBlank()
                || fechaStr == null || fechaStr.isBlank()
                || temasTratados == null || temasTratados.isBlank()
                || acuerdos == null || acuerdos.isBlank()) {
            request.setAttribute("error", "Completa todos los campos obligatorios.");
            request.getRequestDispatcher("/tutor/registro-individual.jsp").forward(request, response);
            return;
        }

        matricula = matricula.trim().toUpperCase();

        Integer idCanalizacion = null;

        if (idAreaStr != null && !idAreaStr.isBlank()) {
            Canalizacion c = new Canalizacion();
            c.setIdArea(Integer.parseInt(idAreaStr));
            c.setMatricula(matricula);
            c.setObservaciones(observaciones);

            int idGenerado = canalizacionDao.crearYObtenerId(c, UrlUtils.baseUrl(request));
            if (idGenerado > 0) {
                idCanalizacion = idGenerado;
            }
        }

        SesionIndividual sesion = new SesionIndividual();
        sesion.setIdTutor(idTutor);
        sesion.setMatricula(matricula);
        sesion.setFecha(Date.valueOf(fechaStr));
        sesion.setTemasTratados(temasTratados);
        sesion.setAcuerdos(acuerdos);
        sesion.setIdCanalizacion(idCanalizacion);
        sesion.setEstado("Completado");

        boolean guardado = sesionIndividualDao.create(sesion);

        if (guardado) {
            request.setAttribute("exito", true);
        } else {
            request.setAttribute("error", "Ocurrió un error al guardar el registro. Intenta de nuevo.");
        }

        request.getRequestDispatcher("/tutor/registro-individual.jsp").forward(request, response);
    }
}