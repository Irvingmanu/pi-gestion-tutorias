package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Carrera;
import mx.edu.utez.pigestiontutorias.models.Cuatrimestre;
import mx.edu.utez.pigestiontutorias.models.LetraGrupo;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;

import java.io.IOException;
import java.util.List;

@WebServlet("/reportes-globales")
public class ReportesGlobalesServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final TutorDao tutorDao = new TutorDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        List<Carrera> listaCarreras = alumnoDAO.getAllCarreras();
        List<Cuatrimestre> listaCuatrimestres = alumnoDAO.getAllCuatrimestres();
        List<LetraGrupo> listaLetrasGrupo = alumnoDAO.getAllLetrasGrupo();
        List<Tutor> listaTutores = tutorDao.findAll();

        request.setAttribute("paginaActiva", "reportes");
        request.setAttribute("listaCarreras", listaCarreras);
        request.setAttribute("listaCuatrimestres", listaCuatrimestres);
        request.setAttribute("listaLetrasGrupo", listaLetrasGrupo);
        request.setAttribute("listaTutores", listaTutores);

        RequestDispatcher rd = request.getRequestDispatcher("/coordinador/reportes-globales.jsp");
        rd.forward(request, response);
    }
}