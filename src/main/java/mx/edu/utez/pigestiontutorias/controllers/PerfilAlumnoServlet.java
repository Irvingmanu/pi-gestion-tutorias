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

@WebServlet(name = "PerfilAlumnoServlet", value = "/perfilAlumno")
public class PerfilAlumnoServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();


    /*
     * Obtiene la matrícula del alumno que inició sesión.
     */
    private String obtenerMatricula(HttpSession session) {

        if (session == null) {
            return null;
        }

        // Opción 1: idUsuario
        Object idUsuario = session.getAttribute("idUsuario");

        if (idUsuario != null) {
            if (idUsuario instanceof String) {
                return ((String) idUsuario).trim();
            }

            // Por si por alguna razón se guardó otro tipo
            return String.valueOf(idUsuario).trim();
        }


        // Opción 2: matricula
        Object matricula = session.getAttribute("matricula");

        if (matricula != null) {
            return String.valueOf(matricula).trim();
        }


        // Opción 3: alumno como objeto completo
        Object alumnoSesion = session.getAttribute("alumno");

        if (alumnoSesion instanceof Alumno) {
            Alumno alumno = (Alumno) alumnoSesion;
            return alumno.getMatricula();
        }


        return null;
    }


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


    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        // ==============================
        // OBTENER SESIÓN Y MATRÍCULA
        // ==============================

        HttpSession session = request.getSession(false);

        String matricula = obtenerMatricula(session);


        // Si no encontramos la matrícula,
        // entonces la sesión realmente no tiene
        // información suficiente del alumno.
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


        // ==============================
        // BUSCAR AL ALUMNO
        // ==============================

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


        // ==============================
        // OBTENER ACCIÓN
        // ==============================

        String accion = request.getParameter("accion");


        if (accion == null || accion.isBlank()) {

            response.getWriter().write(
                    "{\"exito\":false,"
                            + "\"mensaje\":\"Acción no especificada.\"}"
            );

            return;
        }


        // ==========================================
        // PASO 1: VERIFICAR CONTRASEÑA ACTUAL
        // ==========================================

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


        // ==========================================
        // PASO 2: CAMBIAR CONTRASEÑA
        // ==========================================

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


            // ==========================================
            // ENVIAR CORREO SI EL CAMBIO FUE EXITOSO
            // ==========================================

            if (CambioPasswordUtil.fueExitoso(resultado)) {

                try {

                    EmailSender emailSender =
                            new EmailSender();

                    emailSender.enviarConfirmacionCambio(
                            alumno.getCorreoInstitucional()
                    );

                } catch (Exception e) {

                    // No cancelamos el cambio de contraseña
                    // si falla solamente el envío del correo.
                    e.printStackTrace();
                }
            }


            response.getWriter().write(resultado);

            return;
        }


        // ==========================================
        // ACCIÓN DESCONOCIDA
        // ==========================================

        response.getWriter().write(
                "{\"exito\":false,"
                        + "\"mensaje\":\"Acción no reconocida.\"}"
        );
    }
}