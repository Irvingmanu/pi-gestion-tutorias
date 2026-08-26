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
import java.util.Arrays;
import java.util.List;

/**
 * Filtro global de autenticación y autorización por rol: desactiva el caché de
 * las respuestas, invalida la sesión si fue reemplazada por un inicio de sesión
 * concurrente, evita que un usuario autenticado vuelva a la pantalla de login,
 * y restringe el acceso a cada ruta según el rol (Alumno, Tutor o Coordinador)
 * del usuario en sesión.
 * @author Irvingmanu, JAIROXD, 20253DS-ART
 * @version 1.0
 * @since 2026-07-17
 */
@WebFilter("/*")
public class FiltroAutenticacion extends HttpFilter {

    /**
     * Intercepta todas las peticiones de la aplicación para aplicar cabeceras
     * anti-caché, verificar la validez de la sesión activa, redirigir a los
     * usuarios autenticados que intenten acceder al login, y validar que el rol
     * del usuario en sesión tenga permiso sobre la ruta solicitada antes de
     * continuar la cadena de filtros.
     * @param request petición HTTP entrante
     * @param response respuesta HTTP sobre la que se establecen cabeceras o se redirige
     * @param chain la cadena de filtros a continuar si la petición está autorizada
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     * @throws ServletException si ocurre un error al continuar la cadena de filtros
     */
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String requestURI = request.getRequestURI();
        HttpSession session = request.getSession(false);

        boolean loggedIn = (session != null && session.getAttribute("usuario") != null);

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

    /**
     * Determina la ruta de destino por defecto según el rol del usuario, usada
     * para redirigir cuando intenta acceder a una ruta no autorizada o al login estando ya autenticado.
     * @param rol el rol del usuario en sesión ("Coordinador", "Tutor" o "Alumno")
     * @return la ruta de destino correspondiente al rol, o "/login.jsp" si el rol no coincide con ninguno conocido
     */
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

    /**
     * Determina qué roles tienen permitido el acceso a una ruta relativa dada,
     * ya sea por prefijo de carpeta (/alumno/, /tutor/, /coordinador/) o por
     * coincidencia exacta de servlet.
     * @param ruta la ruta relativa (sin el context path) a evaluar
     * @return la lista de roles permitidos para esa ruta, o {@code null} si la ruta no tiene restricción conocida (acceso libre a cualquier usuario autenticado)
     */
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

            case "/ReportesServlet":
                return List.of("Tutor", "Coordinador");

            default:
                return null;
        }
    }
}