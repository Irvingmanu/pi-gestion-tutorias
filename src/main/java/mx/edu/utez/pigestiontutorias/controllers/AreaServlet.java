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

    // Se amplio para aceptar numeros, comas, guion y parentesis: nombres de encargado con
    // guion (ej. "Perez-Garcia") y motivos de canalizacion en texto libre (ej. "TDAH (dx),
    // seguimiento 2") ya existian en BD y fallaban contra el patron original, dejando el
    // campo pre-cargado como "invalido" sin que el usuario tocara nada (Guardar quedaba
    // deshabilitado desde que cargaba la pantalla).
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

            // El area ya tiene alumnos canalizados: sus motivos quedan bloqueados aunque el
            // cliente manipule el HTML para saltarse el disabled/hidden del formulario.
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

            // Si falla (p.ej. FK: ya hay alumnos canalizados con este motivo), no se puede
            // reportar "eliminado" como si nada, hay que avisar el motivo real del fallo.
            redirigirAEdicion(request, response, idArea, eliminado ? "eliminado" : null, eliminado ? null : "motivo_en_uso");
            return;
        }

        // accion=guardarArea: alta de un area nueva (con sus motivos iniciales)
        // o actualizacion de los datos del area (sin tocar sus motivos)
        String idAreaParam = request.getParameter("idArea");
        boolean esEdicion = idAreaParam != null && !idAreaParam.isEmpty();

        Area area = new Area();
        area.setNombresEncargado(request.getParameter("nombresEncargado"));
        area.setApellidoPaternoEncargado(request.getParameter("apellidoPaternoEncargado"));
        area.setApellidoMaternoEncargado(request.getParameter("apellidoMaternoEncargado"));
        area.setCorreoContacto(request.getParameter("correo"));

        // Si el area ya tiene alumnos canalizados, su nombre queda bloqueado: se ignora lo
        // que venga en "nombreArea" (el input es readonly en el cliente, pero un cliente
        // manipulado podria enviar otro valor) y se conserva el nombre real de BD.
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

        // Al menos un motivo es obligatorio al crear un area (el <input> required estatico
        // se quito del HTML porque ahora los motivos se agregan dinamicamente por JS).
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

        // Listado principal: carga las areas y reenvia (forward) al JSP,
        // conservando exito/error que vengan en la query string.
        List<Area> listaAreas = areaDAO.getAll();
        request.setAttribute("listaAreas", listaAreas);
        request.getRequestDispatcher("/coordinador/areas-apoyo.jsp").forward(request, response);
    }

    // Vuelve al maestro-detalle de un area ya existente tras agregar/editar un motivo
    private void redirigirAEdicion(HttpServletRequest request, HttpServletResponse response, int idArea, String exito) throws IOException {
        redirigirAEdicion(request, response, idArea, exito, null);
    }

    // Variante con error: usada por eliminarMotivo, que puede fallar (p.ej. motivo con
    // canalizaciones vinculadas) y necesita avisarlo en vez de reportar exito a ciegas.
    private void redirigirAEdicion(HttpServletRequest request, HttpServletResponse response, int idArea, String exito, String error) throws IOException {
        String parametro = exito != null ? "exito=" + exito : "error=" + error;
        response.sendRedirect(request.getContextPath() + "/areas-apoyo?accion=prepararEdicion&idArea=" + idArea + "&" + parametro);
    }

    // Reenvia al formulario tras un error de validacion, conservando lo que el usuario capturo
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
            request.setAttribute("areaEdit", areaEdit);
        } else {
            request.setAttribute("area", areaSubmitted);
        }
        request.getRequestDispatcher("/coordinador/formulario-area.jsp").forward(request, response);
    }
}