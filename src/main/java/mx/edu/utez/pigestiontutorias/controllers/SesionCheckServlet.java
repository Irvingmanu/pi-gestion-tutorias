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

// Endpoint AJAX consumido por session-guard.js (incluido en los 3 navbars): el navegador
// pregunta cada cierto tiempo si su sesion sigue siendo la activa para ese correo, para
// que si el usuario inicio sesion en otro dispositivo/navegador se le saque del sistema
// de inmediato, sin esperar a que haga clic en algo y dispare el FiltroAutenticacion.
@WebServlet(name = "SesionCheckServlet", urlPatterns = {"/verificar-sesion"})
public class SesionCheckServlet extends HttpServlet {

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