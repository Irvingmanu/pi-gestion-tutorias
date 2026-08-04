package mx.edu.utez.pigestiontutorias.controllers.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter("/*")
public class FiltroAutenticacion extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        HttpSession session = request.getSession(false);

        boolean loggedIn = (session != null && session.getAttribute("usuario") != null);

        boolean loginRequest =
                requestURI.endsWith("/login.jsp") ||
                        requestURI.endsWith("/login") ||
                        requestURI.endsWith("/recuperar-contra.jsp") ||
                        requestURI.endsWith("/recuperar") ||
                        requestURI.endsWith("/logout");

        boolean isResource = requestURI.contains("/assets/") || requestURI.contains("/includes/");

        if (loggedIn) {
            // También agregamos index.jsp y la raíz "/" a las rutas que no deberían ver si ya tienen sesión
            boolean tryingToAccessLogin = loginRequest || requestURI.endsWith("/index.jsp") || requestURI.equals(request.getContextPath() + "/");

            if (tryingToAccessLogin && !requestURI.endsWith("/logout")) {

                // Leemos qué rol tiene el usuario actualmente
                String rol = (String) session.getAttribute("rol");
                String destino = "/"; // Destino por defecto

                if ("Coordinador".equalsIgnoreCase(rol)) {
                    destino = "/gestion-tutores";
                } else if ("Tutor".equalsIgnoreCase(rol)) {
                    destino = "/tutoria-individual";
                } else if ("Alumno".equalsIgnoreCase(rol)) {
                    destino = "/agenda";
                }

                // Lo mandamos a su panel correspondiente
                response.sendRedirect(request.getContextPath() + destino);
                return;
            } else {
                chain.doFilter(request, response);
            }
        } else {
            if (loginRequest || isResource) {
                chain.doFilter(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }
        }
    }
}