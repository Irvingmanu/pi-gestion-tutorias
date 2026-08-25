package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.pigestiontutorias.models.dao.CanalizacionDao;

import java.io.IOException;

@WebServlet("/confirmar-canalizacion")
public class ConfirmarCanalizacionServlet extends HttpServlet {

    private final CanalizacionDao canalizacionDao = new CanalizacionDao();

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
