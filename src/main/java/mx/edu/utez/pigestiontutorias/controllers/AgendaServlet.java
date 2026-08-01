package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.EventoAgenda;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "AgendaServlet", value = "/agenda")
public class AgendaServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int idUsuario = (Integer) session.getAttribute("idUsuario");
        Alumno alumno = alumnoDAO.getByIdUsuario(idUsuario);

        List<EventoAgenda> listaEventos = (alumno != null)
                ? alumnoDAO.getAgendaAlumno(alumno.getMatricula(), alumno.getIdCarrera(), alumno.getIdCuatrimestre(), alumno.getIdLetraGrupo())
                : Collections.emptyList();

        request.setAttribute("listaEventosAgenda", listaEventos);
        request.getRequestDispatcher("/alumno/agenda.jsp").forward(request, response);
    }
}