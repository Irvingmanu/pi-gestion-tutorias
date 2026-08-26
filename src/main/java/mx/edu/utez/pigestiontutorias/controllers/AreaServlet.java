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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servlet que gestiona las áreas de apoyo institucional y sus motivos de canalización:
 * alta, edición y baja de áreas (vía formulario web) y operaciones CRUD de motivos
 * asociados a un área (vía peticiones JSON POST/PUT/DELETE).
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-17
 */
@WebServlet(name = "AreaServlet", value = "/areas-apoyo")
public class AreaServlet extends HttpServlet {

    private static final String REGEX_NOMBRE = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s.,()/-]+$";
    private static final String REGEX_CORREO = "^[a-zA-Z0-9._-]+@utez\\.edu\\.mx$";

    private final AreaDAO areaDAO = new AreaDAO();
    private final MotivoDAO motivoDAO = new MotivoDAO();

    /**
     * Atiende las peticiones POST del formulario web: si el contenido es JSON delega
     * en la creación de un motivo; en caso contrario procesa eliminación de área o
     * el alta/edición de un área validando formato y nombre duplicado, y creando
     * sus motivos iniciales cuando es un alta nueva.
     * @param request petición HTTP con los datos del área/motivo o el parámetro "accion"
     * @param response respuesta HTTP usada para redirigir o reenviar a la vista JSP
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith("application/json")) {
            crearMotivoJson(request, response);
            return;
        }

        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            boolean eliminado = areaDAO.delete(Integer.parseInt(request.getParameter("idArea")));
            String parametro = eliminado ? "exito=eliminado" : "error=area_en_uso";
            response.sendRedirect(request.getContextPath() + "/areas-apoyo?" + parametro);
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

    /**
     * Atiende las peticiones GET del módulo de áreas de apoyo: elimina un área,
     * prepara el formulario de edición precargando sus datos, o carga el listado
     * general de áreas para la vista de gestión.
     * @param request petición HTTP con el parámetro "accion" y, según el caso, "idArea"
     * @param response respuesta HTTP usada para redirigir o reenviar a la vista JSP
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
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

    /**
     * Crea un nuevo motivo de canalización para un área a partir de un cuerpo JSON,
     * validando el formato del nombre y que el área no tenga alumnos ya canalizados.
     * @param request petición HTTP cuyo cuerpo JSON contiene "idArea" y "nombreMotivo"
     * @param response respuesta HTTP en formato JSON con el resultado de la operación
     * @throws IOException si ocurre un error de entrada/salida al leer el cuerpo o escribir la respuesta
     */
    private void crearMotivoJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String cuerpo = leerCuerpo(request);

        String idAreaStr = extraerJson(cuerpo, "idArea");
        String nombreMotivo = extraerJson(cuerpo, "nombreMotivo");

        if (idAreaStr == null || nombreMotivo == null || !nombreMotivo.trim().matches(REGEX_NOMBRE)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"formato_invalido\"}");
            return;
        }

        int idArea = Integer.parseInt(idAreaStr);
        if (areaDAO.contarCanalizados(idArea) > 0) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"area_bloqueada\"}");
            return;
        }

        Motivo motivo = new Motivo();
        motivo.setIdArea(idArea);
        motivo.setNombreMotivo(nombreMotivo.trim());

        int idMotivo = motivoDAO.createAndGetId(motivo);
        if (idMotivo <= 0) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"exito\":false}");
            return;
        }

        response.getWriter().write("{\"exito\":true,\"idMotivo\":" + idMotivo + ",\"nombreMotivo\":\"" + motivo.getNombreMotivo() + "\"}");
    }

    /**
     * Actualiza un motivo de canalización existente a partir de un cuerpo JSON,
     * validando formato del nombre y que el área asociada no esté bloqueada por
     * tener alumnos ya canalizados.
     * @param request petición HTTP cuyo cuerpo JSON contiene "idArea", "idMotivo" y "nombreMotivo"
     * @param response respuesta HTTP en formato JSON con el resultado de la actualización
     * @throws IOException si ocurre un error de entrada/salida al leer el cuerpo o escribir la respuesta
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String cuerpo = leerCuerpo(request);

        String idAreaStr = extraerJson(cuerpo, "idArea");
        String idMotivoStr = extraerJson(cuerpo, "idMotivo");
        String nombreMotivo = extraerJson(cuerpo, "nombreMotivo");

        if (idAreaStr == null || idMotivoStr == null || nombreMotivo == null
                || !nombreMotivo.trim().matches(REGEX_NOMBRE)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"formato_invalido\"}");
            return;
        }

        int idArea = Integer.parseInt(idAreaStr);
        if (areaDAO.contarCanalizados(idArea) > 0) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"area_bloqueada\"}");
            return;
        }

        Motivo motivo = new Motivo();
        motivo.setIdMotivo(Integer.parseInt(idMotivoStr));
        motivo.setIdArea(idArea);
        motivo.setNombreMotivo(nombreMotivo.trim());

        response.getWriter().write("{\"exito\":" + motivoDAO.update(motivo) + "}");
    }

    /**
     * Elimina un motivo de canalización a partir de un cuerpo JSON, validando que
     * el área asociada no esté bloqueada por tener alumnos ya canalizados.
     * @param request petición HTTP cuyo cuerpo JSON contiene "idArea" y "idMotivo"
     * @param response respuesta HTTP en formato JSON con el resultado de la eliminación
     * @throws IOException si ocurre un error de entrada/salida al leer el cuerpo o escribir la respuesta
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String cuerpo = leerCuerpo(request);

        String idAreaStr = extraerJson(cuerpo, "idArea");
        String idMotivoStr = extraerJson(cuerpo, "idMotivo");

        if (idAreaStr == null || idMotivoStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"formato_invalido\"}");
            return;
        }

        int idArea = Integer.parseInt(idAreaStr);
        if (areaDAO.contarCanalizados(idArea) > 0) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"area_bloqueada\"}");
            return;
        }

        boolean eliminado = motivoDAO.delete(Integer.parseInt(idMotivoStr));
        response.getWriter().write(eliminado
                ? "{\"exito\":true}"
                : "{\"exito\":false,\"mensaje\":\"motivo_en_uso\"}");
    }

    /**
     * Lee por completo el cuerpo de la petición HTTP como texto plano.
     * @param request petición HTTP de la cual leer el cuerpo
     * @return el contenido íntegro del cuerpo de la petición
     * @throws IOException si ocurre un error de entrada/salida al leer el cuerpo
     */
    private String leerCuerpo(HttpServletRequest request) throws IOException {
        StringBuilder cuerpo = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                cuerpo.append(linea);
            }
        }
        return cuerpo.toString();
    }

    /**
     * Extrae el valor asociado a una clave dentro de una cadena JSON simple,
     * ya sea de tipo texto o numérico, mediante expresión regular.
     * @param json la cadena JSON de la cual extraer el valor
     * @param clave el nombre de la clave a buscar
     * @return el valor asociado a la clave, o {@code null} si no se encuentra
     */
    private String extraerJson(String json, String clave) {
        Matcher matcher = Pattern.compile("\"" + clave + "\"\\s*:\\s*(?:\"([^\"]*)\"|(-?\\d+))").matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
    }

    /**
     * Redirige de vuelta al formulario de edición de un área indicando un mensaje de éxito.
     * @param request petición HTTP en curso
     * @param response respuesta HTTP usada para realizar la redirección
     * @param idArea el identificador del área a la que se redirige
     * @param exito el código del mensaje de éxito a mostrar
     * @throws IOException si ocurre un error de entrada/salida al redirigir la respuesta
     */
    private void redirigirAEdicion(HttpServletRequest request, HttpServletResponse response, int idArea, String exito) throws IOException {
        redirigirAEdicion(request, response, idArea, exito, null);
    }

    /**
     * Redirige de vuelta al formulario de edición de un área indicando un mensaje
     * de éxito o de error, según cuál de los dos se proporcione.
     * @param request petición HTTP en curso
     * @param response respuesta HTTP usada para realizar la redirección
     * @param idArea el identificador del área a la que se redirige
     * @param exito el código del mensaje de éxito a mostrar, o {@code null} si se reporta un error
     * @param error el código del mensaje de error a mostrar cuando {@code exito} es {@code null}
     * @throws IOException si ocurre un error de entrada/salida al redirigir la respuesta
     */
    private void redirigirAEdicion(HttpServletRequest request, HttpServletResponse response, int idArea, String exito, String error) throws IOException {
        String parametro = exito != null ? "exito=" + exito : "error=" + error;
        response.sendRedirect(request.getContextPath() + "/areas-apoyo?accion=prepararEdicion&idArea=" + idArea + "&" + parametro);
    }

    /**
     * Reenvía la petición al formulario de área, precargando los datos capturados
     * sobre el área existente (en edición) o como área nueva, para que el usuario
     * corrija los datos tras un error de validación.
     * @param request petición HTTP que será reenviada al formulario
     * @param response respuesta HTTP usada para reenviar la petición a la vista JSP
     * @param areaSubmitted los datos del área capturados en el formulario que generaron el error
     * @param esEdicion indica si la operación en curso es una edición de área existente
     * @param idAreaParam el identificador del área en edición, como texto
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
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