package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.SesionGrupal;
import mx.edu.utez.pigestiontutorias.models.SesionIndividual;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.SesionGrupalDao;
import mx.edu.utez.pigestiontutorias.models.dao.SesionIndividualDao;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet("/alumno/acuerdos")
public class AcuerdosServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final SesionIndividualDao sesionIndividualDao = new SesionIndividualDao();
    private final SesionGrupalDao sesionGrupalDao = new SesionGrupalDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int idUsuario = (Integer) session.getAttribute("idUsuario");
        Alumno alumno = alumnoDAO.getByIdUsuario(idUsuario);

        List<SesionIndividual> listaIndividuales = (alumno != null)
                ? sesionIndividualDao.getAcuerdosPorAlumno(alumno.getMatricula())
                : Collections.emptyList();
        List<SesionGrupal> listaGrupales = (alumno != null)
                ? sesionGrupalDao.getAcuerdosPorAlumno(alumno.getMatricula())
                : Collections.emptyList();

        request.setAttribute("listaIndividuales", listaIndividuales);
        request.setAttribute("listaGrupales", listaGrupales);

        request.getRequestDispatcher("/alumno/acuerdos.jsp").forward(request, response);
    }
}
