package mx.edu.utez.pigestiontutorias.controllers;

import mx.edu.utez.pigestiontutorias.models.Area;
import mx.edu.utez.pigestiontutorias.models.Motivo;
import mx.edu.utez.pigestiontutorias.models.dao.AreaDAO;
import mx.edu.utez.pigestiontutorias.models.dao.MotivoDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/AreaServlet")
public class AreaServlet extends HttpServlet {

    private static final String REGEX_NOMBRE = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s./]+$";
    private static final String REGEX_CORREO = "^[a-zA-Z0-9._-]+@utez\\.edu\\.mx$";

    private final AreaDAO areaDAO = new AreaDAO();
    private final MotivoDAO motivoDAO = new MotivoDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            boolean eliminado = areaDAO.delete(Integer.parseInt(request.getParameter("idArea")));
            String parametro = eliminado ? "exito=eliminado" : "error=area_en_uso";
            response.sendRedirect(request.getContextPath() + "/coordinador/areas-apoyo.jsp?" + parametro);
            return;
        }

        if ("agregarMotivo".equals(accion)) {
            int idArea = Integer.parseInt(request.getParameter("idArea"));
            String nuevoMotivo = request.getParameter("nuevoMotivo");

            boolean motivoValido = nuevoMotivo != null && nuevoMotivo.trim().matches(REGEX_NOMBRE);

            if (!motivoValido) {
                request.setAttribute("error", "formato_invalido");
                request.setAttribute("areaEdit", areaDAO.getById(idArea));
                request.getRequestDispatcher("/coordinador/formulario-area.jsp").forward(request, response);
                return;
            }

            Motivo motivo = new Motivo();
            motivo.setIdArea(idArea);
            motivo.setNombreMotivo(nuevoMotivo.trim());
            motivoDAO.create(motivo);

            redirigirAEdicion(request, response, idArea, "guardado");
            return;
        }

        if ("editarMotivo".equals(accion)) {
            int idArea = Integer.parseInt(request.getParameter("idArea"));
            int idMotivo = Integer.parseInt(request.getParameter("idMotivo"));
            String nombreMotivo = request.getParameter("nombreMotivo");

            boolean motivoValido = nombreMotivo != null && nombreMotivo.trim().matches(REGEX_NOMBRE);

            if (!motivoValido) {
                request.setAttribute("error", "formato_invalido");
                request.setAttribute("areaEdit", areaDAO.getById(idArea));
                request.getRequestDispatcher("/coordinador/formulario-area.jsp").forward(request, response);
                return;
            }

            Motivo motivo = new Motivo();
            motivo.setIdMotivo(idMotivo);
            motivo.setIdArea(idArea);
            motivo.setNombreMotivo(nombreMotivo.trim());
            motivoDAO.update(motivo);

            redirigirAEdicion(request, response, idArea, "editado");
            return;
        }

        if ("eliminarMotivo".equals(accion)) {
            int idArea = Integer.parseInt(request.getParameter("idArea"));
            motivoDAO.delete(Integer.parseInt(request.getParameter("idMotivo")));

            redirigirAEdicion(request, response, idArea, "eliminado");
            return;
        }

        // accion=guardarArea: alta de un area nueva (con sus motivos iniciales)
        // o actualizacion de los datos del area (sin tocar sus motivos)
        String idAreaParam = request.getParameter("idArea");
        boolean esEdicion = idAreaParam != null && !idAreaParam.isEmpty();

        Area area = new Area();
        area.setNombre(request.getParameter("nombreArea"));
        area.setEncargado(request.getParameter("encargado"));
        area.setCorreoContacto(request.getParameter("correo"));

        String[] motivos = request.getParameterValues("motivos[]");

        boolean formatoValido = area.getNombre() != null && area.getNombre().trim().matches(REGEX_NOMBRE)
                && area.getEncargado() != null && area.getEncargado().trim().matches(REGEX_NOMBRE)
                && area.getCorreoContacto() != null && area.getCorreoContacto().trim().matches(REGEX_CORREO);

        if (formatoValido && !esEdicion && motivos != null) {
            for (String nombreMotivo : motivos) {
                if (nombreMotivo == null || !nombreMotivo.trim().matches(REGEX_NOMBRE)) {
                    formatoValido = false;
                    break;
                }
            }
        }

        if (!formatoValido) {
            request.setAttribute("error", "formato_invalido");
            reenviarAFormulario(request, response, area, esEdicion, idAreaParam);
            return;
        }

        boolean nombreDuplicado = esEdicion
                ? areaDAO.existeNombreArea(area.getNombre().trim(), Integer.parseInt(idAreaParam))
                : areaDAO.existeNombreArea(area.getNombre().trim());

        if (nombreDuplicado) {
            request.setAttribute("error", "nombre_duplicado");
            reenviarAFormulario(request, response, area, esEdicion, idAreaParam);
            return;
        }

        if (esEdicion) {
            area.setIdArea(Integer.parseInt(idAreaParam));
            areaDAO.update(area);
            response.sendRedirect(request.getContextPath() + "/coordinador/areas-apoyo.jsp?exito=editado");
        } else {
            int idArea = areaDAO.createAndGetId(area);

            if (idArea > 0 && motivos != null) {
                for (String nombreMotivo : motivos) {
                    if (nombreMotivo != null && !nombreMotivo.trim().isEmpty()) {
                        Motivo motivo = new Motivo();
                        motivo.setIdArea(idArea);
                        motivo.setNombreMotivo(nombreMotivo.trim());
                        motivoDAO.create(motivo);
                    }
                }
            }

            response.sendRedirect(request.getContextPath() + "/coordinador/areas-apoyo.jsp?exito=guardado");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            int idArea = Integer.parseInt(request.getParameter("idArea"));
            boolean eliminado = areaDAO.delete(idArea);
            String parametro = eliminado ? "exito=eliminado" : "error=area_en_uso";
            response.sendRedirect(request.getContextPath() + "/coordinador/areas-apoyo.jsp?" + parametro);
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

    // Vuelve al maestro-detalle de un area ya existente tras agregar/eliminar un motivo
    private void redirigirAEdicion(HttpServletRequest request, HttpServletResponse response, int idArea, String exito) throws IOException {
        response.sendRedirect(request.getContextPath() + "/AreaServlet?accion=prepararEdicion&idArea=" + idArea + "&exito=" + exito);
    }

    // Reenvia al formulario tras un error de validacion, conservando lo que el usuario capturo
    private void reenviarAFormulario(HttpServletRequest request, HttpServletResponse response,
                                     Area areaSubmitted, boolean esEdicion, String idAreaParam)
            throws ServletException, IOException {
        if (esEdicion) {
            Area areaEdit = areaDAO.getById(Integer.parseInt(idAreaParam));
            areaEdit.setNombre(areaSubmitted.getNombre());
            areaEdit.setEncargado(areaSubmitted.getEncargado());
            areaEdit.setCorreoContacto(areaSubmitted.getCorreoContacto());
            request.setAttribute("areaEdit", areaEdit);
        } else {
            request.setAttribute("area", areaSubmitted);
        }
        request.getRequestDispatcher("/coordinador/formulario-area.jsp").forward(request, response);
    }
}
