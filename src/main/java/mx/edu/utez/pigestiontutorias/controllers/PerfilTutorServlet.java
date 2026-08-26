package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;
import mx.edu.utez.pigestiontutorias.utils.CambioPasswordUtil;
import mx.edu.utez.pigestiontutorias.utils.EmailSender;

import java.io.IOException;

/**
 * Servlet que gestiona el perfil del tutor: muestra su información y
 * permite verificar y cambiar su contraseña, notificando por correo el cambio.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-08-20
 */
@WebServlet(name = "PerfilTutorServlet", value = "/perfilTutor")
public class PerfilTutorServlet extends HttpServlet {

    private final TutorDao tutorDao = new TutorDao();

    /**
     * Carga los datos del tutor autenticado en sesión y reenvía a la vista de su perfil.
     * @param request petición HTTP con la sesión del tutor autenticado
     * @param response respuesta HTTP usada para reenviar a la vista JSP del perfil
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer numeroEmpleado = session != null ? (Integer) session.getAttribute("idUsuario") : null;
        Tutor tutor = numeroEmpleado != null ? tutorDao.getById(numeroEmpleado) : null;
        request.setAttribute("tutor", tutor);
        request.getRequestDispatcher("/tutor/perfil.jsp").forward(request, response);
    }

    /**
     * Atiende peticiones AJAX del perfil del tutor para verificar la contraseña actual
     * o cambiarla, devolviendo la respuesta en formato JSON. Envía un correo de confirmación
     * cuando el cambio de contraseña se realiza con éxito.
     * @param request petición HTTP con el parámetro "accion" y los datos de contraseña
     * @param response respuesta HTTP en formato JSON con el resultado de la operación
     * @throws ServletException si ocurre un error al procesar la petición
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        Integer numeroEmpleado = session != null ? (Integer) session.getAttribute("idUsuario") : null;

        if (numeroEmpleado == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"Tu sesión expiró, inicia sesión de nuevo.\"}");
            return;
        }

        Tutor tutor = tutorDao.getById(numeroEmpleado);
        if (tutor == null) {
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"No se encontró la información del tutor.\"}");
            return;
        }

        String accion = request.getParameter("accion");

        if ("verificarPassword".equals(accion)) {
            String passwordActual = request.getParameter("passwordActual");
            response.getWriter().write(CambioPasswordUtil.verificarPassword(passwordActual, tutor.getPass()));
            return;
        }

        if ("cambiarPassword".equals(accion)) {
            String passwordActual = request.getParameter("passwordActual");
            String passwordNueva = request.getParameter("passwordNueva");
            String passwordConfirmar = request.getParameter("passwordConfirmar");

            String resultado = CambioPasswordUtil.cambiarPassword(
                    passwordActual, passwordNueva, passwordConfirmar, tutor.getPass(),
                    nuevaPassSinHash -> tutorDao.actualizarPassword(tutor.getNumeroEmpleado(), nuevaPassSinHash)
            );

            if (CambioPasswordUtil.fueExitoso(resultado)) {
                try {
                    new EmailSender().enviarConfirmacionCambio(tutor.getCorreoInstitucional());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            response.getWriter().write(resultado);
            return;
        }

        response.getWriter().write("{\"exito\":false,\"mensaje\":\"Acción no reconocida.\"}");
    }
}