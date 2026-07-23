package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Usuario;
import mx.edu.utez.pigestiontutorias.models.dao.UsuarioDao;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final UsuarioDao usuarioDao = new UsuarioDao();

    private static final Map<String, String> ROLES = Map.of(
            "coordinador", "Coordinador",
            "tutor", "Tutor",
            "alumno", "Alumno"
    );

    private static final Map<String, String> DESTINOS = Map.of(
            "coordinador", "TutoresServlet",
            "tutor", "TutoriaServlet",
            "alumno", "AgendaServlet"
    );

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String opcion = request.getParameter("opcionAsignacion");
        String usuarioInput = request.getParameter("usuario");
        String password = request.getParameter("password");

        if (opcion == null || opcion.isBlank()
                || usuarioInput == null || usuarioInput.isBlank()
                || password == null || password.isBlank()) {
            request.setAttribute("error", "Por favor completa todos los campos.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        Usuario usuario = usuarioDao.autenticar(usuarioInput.trim(), password);

        if (usuario == null) {
            request.setAttribute("error", "Usuario o contraseña incorrectos.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        String rolEsperado = ROLES.get(opcion);
        if (rolEsperado == null || !rolEsperado.equalsIgnoreCase(usuario.getRol())) {
            request.setAttribute("error", "Usuario o contraseña incorrectos.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("usuario", usuario.getIdentificador());
        session.setAttribute("idUsuario", usuario.getIdUsuario());
        session.setAttribute("rol", usuario.getRol());

        response.sendRedirect(request.getContextPath() + "/" + DESTINOS.get(opcion));
    }
}