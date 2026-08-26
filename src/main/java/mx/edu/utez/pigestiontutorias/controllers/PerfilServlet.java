package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Coordinador;
import mx.edu.utez.pigestiontutorias.models.dao.CoordinadorDAO;
import mx.edu.utez.pigestiontutorias.utils.CambioPasswordUtil;
import mx.edu.utez.pigestiontutorias.utils.EmailSender;

import java.io.IOException;

/**
 * Servlet que gestiona el perfil del coordinador: muestra su información y
 * permite verificar y cambiar su contraseña, notificando por correo el cambio.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-21
 */
@WebServlet(name = "PerfilServlet", value = "/perfil")
public class PerfilServlet extends HttpServlet {

    private final CoordinadorDAO coordinadorDAO = new CoordinadorDAO();

    /**
     * Carga los datos del coordinador autenticado en sesión y reenvía a la vista de su perfil.
     * @param request petición HTTP con la sesión del coordinador autenticado
     * @param response respuesta HTTP usada para reenviar a la vista JSP del perfil
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer numeroEmpleado = session != null ? (Integer) session.getAttribute("idUsuario") : null;
        Coordinador coordinador = numeroEmpleado != null ? coordinadorDAO.getById(numeroEmpleado) : null;
        request.setAttribute("coordinador", coordinador);
        request.getRequestDispatcher("/coordinador/perfil.jsp").forward(request, response);
    }

    /**
     * Atiende peticiones AJAX del perfil del coordinador para verificar la contraseña actual
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

        Coordinador coordinador = coordinadorDAO.getById(numeroEmpleado);
        if (coordinador == null) {
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"No se encontró la información del coordinador.\"}");
            return;
        }

        String accion = request.getParameter("accion");

        if ("verificarPassword".equals(accion)) {
            String passwordActual = request.getParameter("passwordActual");
            response.getWriter().write(CambioPasswordUtil.verificarPassword(passwordActual, coordinador.getPass()));
            return;
        }

        if ("cambiarPassword".equals(accion)) {
            String passwordActual = request.getParameter("passwordActual");
            String passwordNueva = request.getParameter("passwordNueva");
            String passwordConfirmar = request.getParameter("passwordConfirmar");

            String resultado = CambioPasswordUtil.cambiarPassword(
                    passwordActual, passwordNueva, passwordConfirmar, coordinador.getPass(),
                    nuevaPassSinHash -> coordinadorDAO.actualizarPassword(coordinador.getNumeroEmpleado(), nuevaPassSinHash)
            );

            if (CambioPasswordUtil.fueExitoso(resultado)) {
                try {
                    new EmailSender().enviarConfirmacionCambio(coordinador.getCorreoInstitucional());
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