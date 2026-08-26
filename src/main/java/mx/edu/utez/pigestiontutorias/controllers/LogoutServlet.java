package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.utils.SesionActivaManager;

import java.io.IOException;

/**
 * Servlet que cierra la sesión del usuario autenticado, eliminando su registro de
 * sesión activa e invalidando la sesión HTTP antes de redirigir al login.
 * @author J4IROXD
 * @version 1.0
 * @since 2026-07-21
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    /**
     * Atiende la petición GET de cierre de sesión: elimina el registro de sesión
     * activa asociado al correo del usuario, invalida la sesión HTTP y redirige
     * a la pantalla de login.
     * @param request petición HTTP con la sesión a cerrar
     * @param response respuesta HTTP usada para redirigir al login
     * @throws ServletException si ocurre un error al procesar la petición
     * @throws IOException si ocurre un error de entrada/salida al redirigir la respuesta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            String correo = (String) session.getAttribute("usuario");
            SesionActivaManager.eliminarSesion(correo, session.getId());
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}