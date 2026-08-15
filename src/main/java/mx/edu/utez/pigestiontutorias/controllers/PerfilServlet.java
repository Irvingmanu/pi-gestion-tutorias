package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Coordinador;
import mx.edu.utez.pigestiontutorias.models.dao.CoordinadorDAO;

import java.io.IOException;

@WebServlet(name = "PerfilServlet", value = "/perfil")
public class PerfilServlet extends HttpServlet {

    private final CoordinadorDAO coordinadorDAO = new CoordinadorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer numeroEmpleado = session != null ? (Integer) session.getAttribute("idUsuario") : null;
        Coordinador coordinador = numeroEmpleado != null ? coordinadorDAO.getById(numeroEmpleado) : null;
        request.setAttribute("coordinador", coordinador);
        request.getRequestDispatcher("/coordinador/perfil.jsp").forward(request, response);
    }
}
