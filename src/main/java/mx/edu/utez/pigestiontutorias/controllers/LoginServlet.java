package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.Coordinador;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.CoordinadorDAO;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;

import java.io.IOException;

// todos los roles inician sesion con su correo institucional + password, y el correo se busca primero en
// ALUMNO, luego en TUTOR y finalmente en COORDINADOR (regla de negocio del nuevo esquema).
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final TutorDao tutorDao = new TutorDao();
    private final CoordinadorDAO coordinadorDAO = new CoordinadorDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String correo = request.getParameter("correo");
        String password = request.getParameter("password");

        if (correo == null || correo.isBlank() || password == null || password.isBlank()) {
            request.setAttribute("error", "Por favor completa todos los campos.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        correo = correo.trim();
        password = password.trim();
        HttpSession session = request.getSession(true);

        Alumno alumno = alumnoDAO.findByCorreo(correo);
        if (alumno != null) {
            if (!credencialesValidas(alumno.getEstado(), alumno.getPass(), password)) {
                credencialesInvalidas(request, response);
                return;
            }
            session.setAttribute("usuario", alumno.getCorreoInstitucional());
            session.setAttribute("rol", "Alumno");
            session.setAttribute("matricula", alumno.getMatricula());
            session.setAttribute("alumno", alumnoDAO.getPerfilCompleto(alumno.getMatricula()));
            response.sendRedirect(request.getContextPath() + "/agenda");
            return;
        }

        Tutor tutor = tutorDao.findByCorreo(correo);
        if (tutor != null) {
            if (!credencialesValidas(tutor.getEstado(), tutor.getPass(), password)) {
                credencialesInvalidas(request, response);
                return;
            }
            session.setAttribute("usuario", tutor.getCorreoInstitucional());
            session.setAttribute("rol", "Tutor");
            session.setAttribute("idUsuario", tutor.getNumeroEmpleado());
            session.setAttribute("tutor", tutorDao.getById(tutor.getNumeroEmpleado()));
            response.sendRedirect(request.getContextPath() + "/tutoria-individual");
            return;
        }

        Coordinador coordinador = coordinadorDAO.findByCorreo(correo);
        if (coordinador != null) {
            if (!credencialesValidas(coordinador.getEstado(), coordinador.getPass(), password)) {
                credencialesInvalidas(request, response);
                return;
            }
            session.setAttribute("usuario", coordinador.getCorreoInstitucional());
            session.setAttribute("rol", "Coordinador");
            session.setAttribute("idUsuario", coordinador.getNumeroEmpleado());
            session.setAttribute("coordinador", coordinador);
            response.sendRedirect(request.getContextPath() + "/gestion-tutores");
            return;
        }

        credencialesInvalidas(request, response);
    }

    private boolean credencialesValidas(String estado, String passAlmacenada, String passIngresada) {
        return "S".equals(estado) && passAlmacenada != null
                && passAlmacenada.equalsIgnoreCase(PasswordUtil.hash(passIngresada));
    }

    private void credencialesInvalidas(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("error", "Correo o contraseña incorrectos.");
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }
}