package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.Canalizacion;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.CanalizacionDao;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Servlet que muestra al alumno autenticado el historial de sus canalizaciones
 * hacia áreas de apoyo institucional.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-08-07
 */
@WebServlet("/alumno/canalizaciones")
public class CanalizacionesServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final CanalizacionDao canalizacionDao = new CanalizacionDao();

    /**
     * Atiende la petición GET, valida la sesión activa del alumno y obtiene su
     * listado de canalizaciones para reenviarlo a la vista correspondiente.
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

        List<Canalizacion> listaCanalizaciones = (alumno != null)
                ? canalizacionDao.getByMatricula(alumno.getMatricula())
                : Collections.emptyList();

        request.setAttribute("listaCanalizaciones", listaCanalizaciones);

        request.getRequestDispatcher("/alumno/canalizaciones.jsp").forward(request, response);
    }
}
