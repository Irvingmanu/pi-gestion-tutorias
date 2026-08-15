package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.AcuerdoAgenda;
import mx.edu.utez.pigestiontutorias.models.SesionGrupal;
import mx.edu.utez.pigestiontutorias.models.SesionIndividual;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.SesionGrupalDao;
import mx.edu.utez.pigestiontutorias.models.dao.SesionIndividualDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@WebServlet("/alumno/acuerdos")
public class AcuerdosServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final SesionIndividualDao sesionIndividualDao = new SesionIndividualDao();
    private final SesionGrupalDao sesionGrupalDao = new SesionGrupalDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("matricula") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String matricula = (String) session.getAttribute("matricula");
        Alumno alumno = alumnoDAO.getById(matricula);

        List<SesionIndividual> listaIndividuales = (alumno != null)
                ? sesionIndividualDao.getAcuerdosPorAlumno(alumno.getMatricula())
                : Collections.emptyList();
        List<SesionGrupal> listaGrupales = (alumno != null)
                ? sesionGrupalDao.getAcuerdosPorAlumno(alumno.getMatricula())
                : Collections.emptyList();

        // Se mantienen ambas listas originales por si se usan en otro lado.
        request.setAttribute("listaIndividuales", listaIndividuales);
        request.setAttribute("listaGrupales", listaGrupales);

        // Lista combinada, solo para pintar la vista con lo mas reciente arriba,
        // intercalando individuales y grupales segun su fecha.
        List<AcuerdoAgenda> listaAcuerdos = new ArrayList<>();
        for (SesionIndividual individual : listaIndividuales) {
            listaAcuerdos.add(new AcuerdoAgenda("Individual", individual.getFecha(), individual.getAcuerdos()));
        }
        for (SesionGrupal grupal : listaGrupales) {
            listaAcuerdos.add(new AcuerdoAgenda("Grupal", grupal.getFecha(), grupal.getAcuerdos()));
        }
        listaAcuerdos.sort(Comparator.comparing(AcuerdoAgenda::getFecha, Comparator.nullsLast(Comparator.reverseOrder())));

        request.setAttribute("listaAcuerdos", listaAcuerdos);

        request.getRequestDispatcher("/alumno/acuerdos.jsp").forward(request, response);
    }
}