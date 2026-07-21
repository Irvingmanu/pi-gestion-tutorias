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
            try {
                if (idAreaParam != null && !idAreaParam.isEmpty()) {
                    areaDAO.delete(Integer.parseInt(idAreaParam));
                }
            } catch (Throwable t) {
                request.getSession().setAttribute("errorSession", "Error al intentar eliminar el registro.");
            }
            response.sendRedirect(request.getContextPath() + "/coordinador/areas-apoyo.jsp");
            return;
        }

        String nombre = request.getParameter("nombreArea");
        String encargado = request.getParameter("encargado");
        String correo = request.getParameter("correo");

        // VALIDACIÓN: Campos vacíos
        if (nombre == null || nombre.trim().isEmpty() ||
                encargado == null || encargado.trim().isEmpty() ||
                correo == null || correo.trim().isEmpty()) {

            request.getSession().setAttribute("errorSession", "Todos los campos tienen que ser obligatorios.");
            String redir = "/coordinador/formulario-area.jsp?accion=" + ("editar".equals(accion) ? "editar" : "nueva");
            if (idAreaParam != null && !idAreaParam.isEmpty()) {
                redir += "&idArea=" + idAreaParam;
            }
            response.sendRedirect(request.getContextPath() + redir);
            return;
        }

        nombre = nombre.trim();
        encargado = encargado.trim();
        correo = correo.trim();

        // VALIDACIÓN: Formato de correo
        String regexEmail = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        if (!correo.matches(regexEmail)) {
            request.getSession().setAttribute("errorSession", "El formato del correo electrónico no es válido.");
            String redir = "/coordinador/formulario-area.jsp?accion=" + ("editar".equals(accion) ? "editar" : "nueva");
            if (idAreaParam != null && !idAreaParam.isEmpty()) {
                redir += "&idArea=" + idAreaParam;
            }
            response.sendRedirect(request.getContextPath() + redir);
            return;
        }

        boolean esEdicion = "editar".equals(accion);

        try {
            if (esEdicion) {
                int idArea = Integer.parseInt(idAreaParam);

                if (areaDAO.Duplicado(nombre, correo, idArea)) {
                    request.getSession().setAttribute("errorSession", "El nombre del área o correo ya pertenece a otro registro.");
                    response.sendRedirect(request.getContextPath() + "/coordinador/formulario-area.jsp?accion=editar&idArea=" + idArea);
                    return;
                }
                Area area = new Area();
                area.setIdArea(idArea);
                area.setNombre(nombre);
                area.setEncargado(encargado);
                area.setCorreoContacto(correo);
                areaDAO.update(area);
            } else {
                if (areaDAO.existeNombreCorreo(nombre, correo)) {
                    request.getSession().setAttribute("errorSession", "El nombre del área o correo electrónico ya se encuentran registrados.");
                    response.sendRedirect(request.getContextPath() + "/coordinador/formulario-area.jsp?accion=nueva");
                    return;
                }

                Area area = new Area();
                area.setNombre(nombre);
                area.setEncargado(encargado);
                area.setCorreoContacto(correo);
                areaDAO.create(area);
            }
            response.sendRedirect(request.getContextPath() + "/coordinador/areas-apoyo.jsp");
        } catch (Throwable t) {
            request.getSession().setAttribute("errorSession", "Error crítico en la base de datos.");
            response.sendRedirect(request.getContextPath() + "/coordinador/formulario-area.jsp?accion=" + (esEdicion ? "editar" : "nueva") + (idAreaParam != null ? "&idArea=" + idAreaParam : ""));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("prepararEdicion".equals(accion)) {
            try {
                int idArea = Integer.parseInt(request.getParameter("idArea"));
                Area areaEdit = areaDAO.getById(idArea);
                request.setAttribute("areaEdit", areaEdit);
                request.getRequestDispatcher("/coordinador/formulario-area.jsp?accion=editar").forward(request, response);
                return;
            } catch (Throwable t) {
                request.getSession().setAttribute("errorSession", "Error al cargar los datos para edición.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/coordinador/areas-apoyo.jsp");
    }
}