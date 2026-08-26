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

/**
 * Servlet que muestra al alumno autenticado la agenda de eventos de tutoría
 * correspondientes a su grupo.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-22
 */
@WebServlet(name = "AgendaServlet", value = "/agenda")
public class AgendaServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();

    /**
     * Atiende la petición GET, valida la sesión activa del alumno y obtiene los
     * eventos de agenda de su grupo (si tiene uno asignado) para reenviarlos a la vista.
     * @param request petición HTTP con la sesión activa del alumno
     * @param response respuesta HTTP usada para redirigir al login o reenviar a la vista
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("matricula") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String matricula = (String) session.getAttribute("matricula");
        Alumno alumno = alumnoDAO.getById(matricula);

        List<EventoAgenda> listaEventos = (alumno != null && alumno.getIdGrupo() != null)
                ? alumnoDAO.getAgendaAlumno(alumno.getMatricula(), alumno.getIdGrupo())
                : Collections.emptyList();

        request.setAttribute("listaEventosAgenda", listaEventos);
        request.getRequestDispatcher("/alumno/agenda.jsp").forward(request, response);
    }
}