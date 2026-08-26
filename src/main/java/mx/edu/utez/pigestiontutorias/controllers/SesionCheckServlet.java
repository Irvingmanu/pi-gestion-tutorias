package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.utils.SesionActivaManager;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet que verifica en JSON si la sesión HTTP del usuario sigue siendo válida
 * y activa (no ha sido invalidada por un inicio de sesión concurrente en otro dispositivo).
 * @author J4IROXD
 * @version 1.0
 * @since 2026-08-20
 */
@WebServlet(name = "SesionCheckServlet", urlPatterns = {"/verificar-sesion"})
public class SesionCheckServlet extends HttpServlet {

    /**
     * Responde en JSON si la sesión actual del usuario es válida, comprobando que
     * exista un usuario autenticado en sesión y que dicha sesión siga siendo la activa
     * registrada para ese correo.
     * @param request petición HTTP con la sesión a verificar
     * @param response respuesta HTTP en formato JSON con el resultado {"valida": true|false}
     * @throws ServletException si ocurre un error al procesar la petición
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        HttpSession session = request.getSession(false);

        boolean valida;
        if (session == null || session.getAttribute("usuario") == null) {
            valida = false;
        } else {
            String correo = (String) session.getAttribute("usuario");
            valida = SesionActivaManager.esSesionValida(correo, session.getId());
        }

        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"valida\":" + valida + "}");
        }
    }
}