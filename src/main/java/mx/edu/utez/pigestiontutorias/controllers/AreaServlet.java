package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.pigestiontutorias.models.Area;
import mx.edu.utez.pigestiontutorias.models.Motivo;
import mx.edu.utez.pigestiontutorias.models.dao.AreaDAO;
import mx.edu.utez.pigestiontutorias.models.dao.MotivoDAO;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AreaServlet", value = "/areas-apoyo")
public class AreaServlet extends HttpServlet {

    private static final String REGEX_NOMBRE = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s.,()/-]+$";
    private static final String REGEX_CORREO = "^[a-zA-Z0-9._-]+@utez\\.edu\\.mx$";

    private final AreaDAO areaDAO = new AreaDAO();
    private final MotivoDAO motivoDAO = new MotivoDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            boolean eliminado = areaDAO.delete(Integer.parseInt(request.getParameter("idArea")));
            String parametro = eliminado ? "exito=eliminado" : "error=area_en_uso";
            response.sendRedirect(request.getContextPath() + "/areas-apoyo?" + parametro);
            return;
        }

        if ("agregarMotivo".equals(accion)) {
            int idArea = Integer.parseInt(request.getParameter("idArea"));

            if (areaDAO.contarCanalizados(idArea) > 0) {
                redirigirAEdicion(request, response, idArea, null, "area_bloqueada");
                return;
            }

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

            if (areaDAO.contarCanalizados(idArea) > 0) {
                redirigirAEdicion(request, response, idArea, null, "area_bloqueada");
                return;
            }

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

            if (areaDAO.contarCanalizados(idArea) > 0) {
                redirigirAEdicion(request, response, idArea, null, "area_bloqueada");
                return;
            }

            boolean eliminado = motivoDAO.delete(Integer.parseInt(request.getParameter("idMotivo")));

            redirigirAEdicion(request, response, idArea, eliminado ? "eliminado" : null, eliminado ? null : "motivo_en_uso");
            return;
        }

        String idAreaParam = request.getParameter("idArea");
        boolean esEdicion = idAreaParam != null && !idAreaParam.isEmpty();

        Area area = new Area();
        area.setNombresEncargado(request.getParameter("nombresEncargado"));
        area.setApellidoPaternoEncargado(request.getParameter("apellidoPaternoEncargado"));
        area.setApellidoMaternoEncargado(request.getParameter("apellidoMaternoEncargado"));
        area.setCorreoContacto(request.getParameter("correo"));

        String enlaceCita = request.getParameter("enlaceCita");
        area.setEnlaceCita((enlaceCita != null && !enlaceCita.isBlank()) ? enlaceCita.trim() : null);

        if (esEdicion && areaDAO.contarCanalizados(Integer.parseInt(idAreaParam)) > 0) {
            Area actual = areaDAO.getById(Integer.parseInt(idAreaParam));
            area.setNombre(actual != null ? actual.getNombre() : request.getParameter("nombreArea"));
        } else {
            area.setNombre(request.getParameter("nombreArea"));
        }

        String[] motivos = request.getParameterValues("motivos[]");

        boolean formatoValido = area.getNombre() != null && area.getNombre().trim().matches(REGEX_NOMBRE)
                && area.getNombresEncargado() != null && area.getNombresEncargado().trim().matches(REGEX_NOMBRE)
                && area.getApellidoPaternoEncargado() != null && area.getApellidoPaternoEncargado().trim().matches(REGEX_NOMBRE)
                && (area.getApellidoMaternoEncargado() == null || area.getApellidoMaternoEncargado().isBlank() || area.getApellidoMaternoEncargado().trim().matches(REGEX_NOMBRE))
                && area.getCorreoContacto() != null && area.getCorreoContacto().trim().matches(REGEX_CORREO);

        if (formatoValido && !esEdicion && (motivos == null || motivos.length == 0)) {
            formatoValido = false;
        }

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
            response.sendRedirect(request.getContextPath() + "/areas-apoyo?exito=editado");
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

            response.sendRedirect(request.getContextPath() + "/areas-apoyo?exito=guardado");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            int idArea = Integer.parseInt(request.getParameter("idArea"));
            boolean eliminado = areaDAO.delete(idArea);
            String parametro = eliminado ? "exito=eliminado" : "error=area_en_uso";
            response.sendRedirect(request.getContextPath() + "/areas-apoyo?" + parametro);
            return;
        }

        if ("prepararEdicion".equals(accion)) {
            int idArea = Integer.parseInt(request.getParameter("idArea"));
            Area areaEdit = areaDAO.getById(idArea);
            request.setAttribute("areaEdit", areaEdit);
            request.getRequestDispatcher("/coordinador/formulario-area.jsp").forward(request, response);
            return;
        }

        List<Area> listaAreas = areaDAO.getAll();
        request.setAttribute("listaAreas", listaAreas);
        request.getRequestDispatcher("/coordinador/areas-apoyo.jsp").forward(request, response);
    }

    private void redirigirAEdicion(HttpServletRequest request, HttpServletResponse response, int idArea, String exito) throws IOException {
        redirigirAEdicion(request, response, idArea, exito, null);
    }

    private void redirigirAEdicion(HttpServletRequest request, HttpServletResponse response, int idArea, String exito, String error) throws IOException {
        String parametro = exito != null ? "exito=" + exito : "error=" + error;
        response.sendRedirect(request.getContextPath() + "/areas-apoyo?accion=prepararEdicion&idArea=" + idArea + "&" + parametro);
    }

    private void reenviarAFormulario(HttpServletRequest request, HttpServletResponse response,
                                     Area areaSubmitted, boolean esEdicion, String idAreaParam)
            throws ServletException, IOException {
        if (esEdicion) {
            Area areaEdit = areaDAO.getById(Integer.parseInt(idAreaParam));
            areaEdit.setNombre(areaSubmitted.getNombre());
            areaEdit.setNombresEncargado(areaSubmitted.getNombresEncargado());
            areaEdit.setApellidoPaternoEncargado(areaSubmitted.getApellidoPaternoEncargado());
            areaEdit.setApellidoMaternoEncargado(areaSubmitted.getApellidoMaternoEncargado());
            areaEdit.setCorreoContacto(areaSubmitted.getCorreoContacto());
            areaEdit.setEnlaceCita(areaSubmitted.getEnlaceCita());
            request.setAttribute("areaEdit", areaEdit);
        } else {
            request.setAttribute("area", areaSubmitted);
        }
        request.getRequestDispatcher("/coordinador/formulario-area.jsp").forward(request, response);
    }
}