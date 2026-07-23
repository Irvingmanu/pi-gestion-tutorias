package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.pigestiontutorias.models.Coordinador;
import mx.edu.utez.pigestiontutorias.models.dao.CoordinadorDAO;

import java.io.IOException;

@WebServlet("/PerfilServlet")
public class PerfilServlet extends HttpServlet {

    private final CoordinadorDAO coordinadorDAO = new CoordinadorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Coordinador coordinador = coordinadorDAO.getCoordinadorTemporal();
        request.setAttribute("coordinador", coordinador);
        request.getRequestDispatcher("/coordinador/perfil.jsp").forward(request, response);
    }
}
