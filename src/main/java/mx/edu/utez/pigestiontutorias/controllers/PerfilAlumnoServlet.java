package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.utils.CambioPasswordUtil;
import mx.edu.utez.pigestiontutorias.utils.EmailSender;

import java.io.IOException;

/**
 * Servlet que gestiona el perfil del alumno autenticado: muestra su información
 * completa y atiende, vía JSON, la verificación y el cambio de su contraseña.
 * @author J4IROXD
 * @version 1.0
 * @since 2026-08-20
 */
@WebServlet(name = "PerfilAlumnoServlet", value = "/perfilAlumno")
public class PerfilAlumnoServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();

    /**
     * Resuelve la matrícula del alumno en sesión, buscando en orden el atributo
     * "idUsuario", luego "matricula" y finalmente el objeto "alumno" almacenado en sesión.
     * @param session la sesión HTTP de la cual obtener la matrícula
     * @return la matrícula del alumno en sesión, o {@code null} si no hay sesión o no se pudo determinar
     */
    private String obtenerMatricula(HttpSession session) {

        if (session == null) {
            return null;
        }

        Object idUsuario = session.getAttribute("idUsuario");

        if (idUsuario != null) {
            if (idUsuario instanceof String) {
                return ((String) idUsuario).trim();
            }

            return String.valueOf(idUsuario).trim();
        }

        Object matricula = session.getAttribute("matricula");

        if (matricula != null) {
            return String.valueOf(matricula).trim();
        }

        Object alumnoSesion = session.getAttribute("alumno");

        if (alumnoSesion instanceof Alumno) {
            Alumno alumno = (Alumno) alumnoSesion;
            return alumno.getMatricula();
        }

        return null;
    }

    /**
     * Atiende la petición GET, resuelve la matrícula del alumno en sesión y carga
     * su perfil completo para reenviarlo a la vista de perfil.
     * @param request petición HTTP con la sesión activa del alumno
     * @param response respuesta HTTP usada para redirigir al login, responder 404, o reenviar a la vista
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        String matricula = obtenerMatricula(session);

        if (matricula == null || matricula.isBlank()) {
            response.sendRedirect(
                    request.getContextPath() + "/login.jsp"
            );
            return;
        }

        Alumno alumno = alumnoDAO.getPerfilCompleto(matricula);

        if (alumno == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "No se encontró la información del alumno."
            );
            return;
        }

        request.setAttribute("alumno", alumno);

        request.getRequestDispatcher(
                "/alumno/perfil.jsp"
        ).forward(request, response);
    }

    /**
     * Atiende la petición POST del perfil del alumno en formato JSON: según el
     * parámetro "accion" verifica la contraseña actual o realiza el cambio de
     * contraseña (enviando un correo de confirmación si el cambio fue exitoso).
     * @param request petición HTTP con la sesión activa del alumno y el parámetro "accion"
     * @param response respuesta HTTP en formato JSON con el resultado de la operación
     * @throws ServletException si ocurre un error al procesar la petición
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        String matricula = obtenerMatricula(session);

        if (matricula == null || matricula.isBlank()) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().write(
                    "{\"exito\":false,"
                            + "\"mensaje\":\"Tu sesión expiró o no se encontró tu información de usuario. Inicia sesión nuevamente.\"}"
            );

            return;
        }

        Alumno alumno = alumnoDAO.getById(matricula);

        if (alumno == null) {

            response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND
            );

            response.getWriter().write(
                    "{\"exito\":false,"
                            + "\"mensaje\":\"No se encontró la información del alumno.\"}"
            );

            return;
        }

        String accion = request.getParameter("accion");

        if (accion == null || accion.isBlank()) {

            response.getWriter().write(
                    "{\"exito\":false,"
                            + "\"mensaje\":\"Acción no especificada.\"}"
            );

            return;
        }

        if ("verificarPassword".equals(accion)) {

            String passwordActual =
                    request.getParameter("passwordActual");

            String resultado =
                    CambioPasswordUtil.verificarPassword(
                            passwordActual,
                            alumno.getPass()
                    );

            response.getWriter().write(resultado);

            return;
        }

        if ("cambiarPassword".equals(accion)) {

            String passwordActual =
                    request.getParameter("passwordActual");

            String passwordNueva =
                    request.getParameter("passwordNueva");

            String passwordConfirmar =
                    request.getParameter("passwordConfirmar");

            String resultado =
                    CambioPasswordUtil.cambiarPassword(

                            passwordActual,

                            passwordNueva,

                            passwordConfirmar,

                            alumno.getPass(),

                            nuevaPassSinHash ->
                                    alumnoDAO.actualizarPassword(
                                            alumno.getMatricula(),
                                            nuevaPassSinHash
                                    )
                    );

            if (CambioPasswordUtil.fueExitoso(resultado)) {

                try {

                    EmailSender emailSender =
                            new EmailSender();

                    emailSender.enviarConfirmacionCambio(
                            alumno.getCorreoInstitucional()
                    );

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

            response.getWriter().write(resultado);

            return;
        }

        response.getWriter().write(
                "{\"exito\":false,"
                        + "\"mensaje\":\"Acción no reconocida.\"}"
        );
    }
}