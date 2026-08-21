package mx.edu.utez.pigestiontutorias.controllers.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.utils.SesionActivaManager;

import java.io.IOException;
import java.util.List;

@WebFilter("/*")
public class FiltroAutenticacion extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String requestURI = request.getRequestURI();
        HttpSession session = request.getSession(false);

        // El endpoint de polling (session-guard.js) SIEMPRE debe llegar a
        // SesionCheckServlet y responder JSON real, sin que el filtro lo intercepte
        // ni redirija: si aqui invalidamos/redirigimos, un fetch() normal sigue el
        // redirect solo, recibe el HTML de login.jsp en vez del JSON, y el
        // JavaScript nunca se entera de que la sesion ya no es valida.
        boolean sesionCheckRequest = requestURI.endsWith("/verificar-sesion");
        if (sesionCheckRequest) {
            chain.doFilter(request, response);
            return;
        }

        boolean loggedIn = (session != null && session.getAttribute("usuario") != null);

        // Sesion unica: si otra sesion (otro navegador/incognito) inicio sesion despues
        // con el mismo usuario, esta sesion quedo obsoleta.
        if (loggedIn) {
            String correoSesion = (String) session.getAttribute("usuario");
            if (!SesionActivaManager.esSesionValida(correoSesion, session.getId())) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login.jsp?motivo=sesion_duplicada");
                return;
            }
        }

        boolean loginRequest =
                requestURI.endsWith("/login.jsp") ||
                        requestURI.endsWith("/login") ||
                        requestURI.endsWith("/recuperar-contra.jsp") ||
                        requestURI.endsWith("/recuperar") ||
                        requestURI.endsWith("/logout");

        boolean isResource = requestURI.contains("/assets/") || requestURI.contains("/includes/");

        boolean rutaConfirmacionCanalizacion = requestURI.endsWith("/confirmar-canalizacion")
                || requestURI.endsWith("/confirmar-canalizacion.jsp");

        if (loggedIn) {
            String rol = (String) session.getAttribute("rol");
            boolean tryingToAccessLogin = loginRequest || requestURI.endsWith("/index.jsp") || requestURI.equals(request.getContextPath() + "/");

            if (tryingToAccessLogin && !requestURI.endsWith("/logout")) {
                response.sendRedirect(request.getContextPath() + destinoSegunRol(rol));
                return;
            }

            // Autorizacion por rol: la ruta pedida debe pertenecer al rol de esta sesion.
            String rutaRelativa = requestURI.substring(request.getContextPath().length());
            List<String> rolesPermitidos = rolesPermitidosPara(rutaRelativa);

            if (rolesPermitidos != null && (rol == null || !rolesPermitidos.contains(rol))) {
                response.sendRedirect(request.getContextPath() + destinoSegunRol(rol));
                return;
            }

            chain.doFilter(request, response);
        } else {
            if (loginRequest || isResource || rutaConfirmacionCanalizacion) {
                chain.doFilter(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }
        }
    }

    private String destinoSegunRol(String rol) {
        if ("Coordinador".equalsIgnoreCase(rol)) {
            return "/gestion-tutores";
        } else if ("Tutor".equalsIgnoreCase(rol)) {
            return "/tutoria-individual";
        } else if ("Alumno".equalsIgnoreCase(rol)) {
            return "/agenda";
        }
        return "/login.jsp";
    }

    private List<String> rolesPermitidosPara(String ruta) {

        if (ruta.startsWith("/alumno/")) return List.of("Alumno");
        if (ruta.startsWith("/tutor/")) return List.of("Tutor");
        if (ruta.startsWith("/coordinador/")) return List.of("Coordinador");

        switch (ruta) {
            case "/agenda":
            case "/perfilAlumno":
                return List.of("Alumno");

            case "/tutoria-individual":
            case "/tutoria-grupal":
            case "/historial-tutorias":
            case "/perfilTutor":
            case "/ReportesServlet":
            case "/TutoriaServlet":
                return List.of("Tutor");

            case "/gestion-tutores":
            case "/gestion-grupos":
            case "/areas-apoyo":
            case "/asignacion":
            case "/generarCredenciales":
            case "/perfil":
            case "/gestion-periodos":
            case "/reportes-globales":
                return List.of("Coordinador");

            case "/solicitudes":
                return List.of("Alumno", "Tutor");

            default:
                return null;
        }
    }
}