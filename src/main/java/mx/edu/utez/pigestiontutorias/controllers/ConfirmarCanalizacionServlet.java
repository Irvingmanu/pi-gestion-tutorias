package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.pigestiontutorias.models.dao.CanalizacionDao;

import java.io.IOException;

/**
 * Servlet que confirma, mediante un token recibido por enlace, la asistencia de un
 * alumno canalizado a un área de apoyo.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-08-07
 */
@WebServlet("/confirmar-canalizacion")
public class ConfirmarCanalizacionServlet extends HttpServlet {

    private final CanalizacionDao canalizacionDao = new CanalizacionDao();

    /**
     * Atiende la petición GET, valida el token recibido y delega su confirmación
     * en el DAO de canalizaciones, reenviando el resultado a la vista correspondiente.
     * @param request petición HTTP con el parámetro "token"
     * @param response respuesta HTTP usada para reenviar a la vista JSP
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");

        String resultado = (token != null && !token.isBlank())
                ? canalizacionDao.confirmarPorToken(token.trim())
                : "invalido";

        request.setAttribute("resultado", resultado);
        request.getRequestDispatcher("/confirmar-canalizacion.jsp").forward(request, response);
    }
}
