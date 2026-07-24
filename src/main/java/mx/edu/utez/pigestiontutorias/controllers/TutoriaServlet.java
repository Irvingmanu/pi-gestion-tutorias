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

import java.io.IOException;
import java.sql.Date;

@WebServlet("/TutoriaServlet")
public class TutoriaServlet extends HttpServlet {

    private final TutorDao tutorDao = new TutorDao();
    private final SesionIndividualDao sesionIndividualDao = new SesionIndividualDao();
    private final CanalizacionDao canalizacionDao = new CanalizacionDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        request.getRequestDispatcher("/tutor/registro-individual.jsp").forward(request, response);
    }

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

    private void registrarIndividual(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException {

        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        Tutor tutor = tutorDao.findByIdUsuario(idUsuario);

        if (tutor == null) {
            request.setAttribute("error", "No se encontró el perfil de tutor asociado a tu cuenta.");
            request.getRequestDispatcher("/tutor/registro-individual.jsp").forward(request, response);
            return;
        }

        int idTutor = tutor.getIdTutor();

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

        Integer idCanalizacion = null;

        // Si el tutor eligió canalizar a alguna área, primero se registra la canalización
        if (idAreaStr != null && !idAreaStr.isBlank()) {
            Canalizacion c = new Canalizacion();
            c.setIdArea(Integer.parseInt(idAreaStr));
            c.setMatricula(matricula);
            c.setObservaciones(observaciones);

            int idGenerado = canalizacionDao.crearYObtenerId(c);
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
        sesion.setEstado("Registrada");

        boolean guardado = sesionIndividualDao.crear(sesion);

        if (guardado) {
            request.setAttribute("exito", true);
        } else {
            request.setAttribute("error", "Ocurrió un error al guardar el registro. Intenta de nuevo.");
        }

        request.getRequestDispatcher("/tutor/registro-individual.jsp").forward(request, response);
    }
}