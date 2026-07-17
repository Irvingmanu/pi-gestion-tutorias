package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.pigestiontutorias.models.Area;
import mx.edu.utez.pigestiontutorias.models.dao.AreaDAO;

import java.io.IOException;

@WebServlet("/AreaServlet")
public class AreaServlet extends HttpServlet {

    private final AreaDAO areaDAO = new AreaDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idAreaParam = request.getParameter("idArea");
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            areaDAO.delete(Integer.parseInt(idAreaParam));
            response.sendRedirect(request.getContextPath() + "/coordinador/areas-apoyo.jsp");
            return;
        }

        boolean esEdicion = "editar".equals(accion) || (idAreaParam != null && !idAreaParam.isEmpty());

        Area area = new Area();
        area.setNombre(request.getParameter("nombreArea"));
        area.setEncargado(request.getParameter("encargado"));
        area.setCorreoContacto(request.getParameter("correo"));

        if (esEdicion) {
            area.setIdArea(Integer.parseInt(idAreaParam));
            areaDAO.update(area);
        } else {
            areaDAO.create(area);
        }

        response.sendRedirect(request.getContextPath() + "/coordinador/areas-apoyo.jsp");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            int idArea = Integer.parseInt(request.getParameter("idArea"));
            areaDAO.delete(idArea);
            response.sendRedirect(request.getContextPath() + "/coordinador/areas-apoyo.jsp");
            return;
        }

        if ("prepararEdicion".equals(accion)) {
            int idArea = Integer.parseInt(request.getParameter("idArea"));
            Area areaEdit = areaDAO.getById(idArea);
            request.setAttribute("areaEdit", areaEdit);
            request.getRequestDispatcher("/coordinador/formulario-area.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/coordinador/areas-apoyo.jsp");
    }
}
