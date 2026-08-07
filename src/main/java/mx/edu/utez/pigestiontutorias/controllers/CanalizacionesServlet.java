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

@WebServlet("/alumno/canalizaciones")
public class CanalizacionesServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final CanalizacionDao canalizacionDao = new CanalizacionDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int idUsuario = (Integer) session.getAttribute("idUsuario");
        Alumno alumno = alumnoDAO.getByIdUsuario(idUsuario);

        List<Canalizacion> listaCanalizaciones = (alumno != null)
                ? canalizacionDao.getByMatricula(alumno.getMatricula())
                : Collections.emptyList();

        request.setAttribute("listaCanalizaciones", listaCanalizaciones);

        request.getRequestDispatcher("/alumno/canalizaciones.jsp").forward(request, response);
    }
}
