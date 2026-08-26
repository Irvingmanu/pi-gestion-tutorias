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
import mx.edu.utez.pigestiontutorias.utils.PasswordUtil;
import mx.edu.utez.pigestiontutorias.utils.SesionActivaManager;

import java.io.IOException;

/**
 * Servlet que autentica a alumnos, tutores y coordinadores mediante correo y
 * contraseña, verificando las credenciales contra cada catálogo de usuarios,
 * abriendo una sesión segura de un único inicio activo por cuenta y redirigiendo
 * a la vista de inicio correspondiente al rol.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-17
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final TutorDao tutorDao = new TutorDao();
    private final CoordinadorDAO coordinadorDAO = new CoordinadorDAO();

    /**
     * Atiende la petición POST del formulario de inicio de sesión, buscando el correo
     * ingresado entre alumnos, tutores y coordinadores (en ese orden), validando la
     * contraseña y el estado de la cuenta, e iniciando sesión con los atributos
     * correspondientes al rol detectado.
     * @param request petición HTTP con los parámetros "correo" y "password"
     * @param response respuesta HTTP usada para redirigir a la vista principal del rol o reenviar al login con error
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
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

        Alumno alumno = alumnoDAO.findByCorreo(correo);
        if (alumno != null) {
            if (!credencialesValidas(alumno.getEstado(), alumno.getPass(), password)) {
                credencialesInvalidas(request, response);
                return;
            }
            HttpSession session = iniciarSesionSegura(request, alumno.getCorreoInstitucional());
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
            HttpSession session = iniciarSesionSegura(request, tutor.getCorreoInstitucional());
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
            HttpSession session = iniciarSesionSegura(request, coordinador.getCorreoInstitucional());
            session.setAttribute("usuario", coordinador.getCorreoInstitucional());
            session.setAttribute("rol", "Coordinador");
            session.setAttribute("idUsuario", coordinador.getNumeroEmpleado());
            session.setAttribute("coordinador", coordinador);
            response.sendRedirect(request.getContextPath() + "/gestion-tutores");
            return;
        }

        credencialesInvalidas(request, response);
    }

    /**
     * Invalida cualquier sesión previa del navegador, crea una nueva sesión HTTP y
     * la registra como la sesión activa del usuario, cerrando cualquier sesión
     * anterior que ese mismo correo tuviera abierta en otro dispositivo.
     * @param request petición HTTP sobre la que se crea la nueva sesión
     * @param correoInstitucional el correo institucional del usuario que inicia sesión
     * @return la nueva sesión HTTP creada y registrada
     */
    private HttpSession iniciarSesionSegura(HttpServletRequest request, String correoInstitucional) {
        HttpSession sesionPrevia = request.getSession(false);
        if (sesionPrevia != null) {
            sesionPrevia.invalidate();
        }
        HttpSession session = request.getSession(true);
        SesionActivaManager.registrarSesion(correoInstitucional, session.getId());
        return session;
    }

    /**
     * Verifica que la cuenta esté activa y que la contraseña ingresada, una vez
     * aplicado el hash, coincida con la contraseña almacenada.
     * @param estado el estado de la cuenta ("S" para activa)
     * @param passAlmacenada el hash de la contraseña almacenado para el usuario
     * @param passIngresada la contraseña en texto plano ingresada en el formulario
     * @return {@code true} si la cuenta está activa y la contraseña coincide; {@code false} en caso contrario
     */
    private boolean credencialesValidas(String estado, String passAlmacenada, String passIngresada) {
        return "S".equals(estado) && passAlmacenada != null
                && passAlmacenada.equalsIgnoreCase(PasswordUtil.hash(passIngresada));
    }

    /**
     * Reenvía la petición al formulario de login con un mensaje de error de
     * credenciales incorrectas.
     * @param request petición HTTP que será reenviada al formulario de login
     * @param response respuesta HTTP usada para reenviar la petición a la vista JSP
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    private void credencialesInvalidas(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("error", "Correo o contraseña incorrectos.");
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }
}