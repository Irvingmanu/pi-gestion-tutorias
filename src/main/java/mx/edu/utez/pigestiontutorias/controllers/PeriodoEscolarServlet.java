package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.pigestiontutorias.models.PeriodoEscolar;
import mx.edu.utez.pigestiontutorias.models.dao.PeriodoEscolarDao;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

/**
 * Servlet que gestiona los periodos escolares: alta, edición, baja lógica y
 * reactivación, validando mes de inicio, duración, rango de fechas y nombre único
 * calculado a partir del mes de inicio.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-08-10
 */
@WebServlet(name = "PeriodoEscolarServlet", value = "/gestion-periodos")
public class PeriodoEscolarServlet extends HttpServlet {

    private static final Set<Integer> MESES_PERMITIDOS = Set.of(1, 5, 9);

    private static final Map<Integer, String> NOMBRES_MES_INICIO = Map.of(
            1, "Enero - Abril",
            5, "Mayo - Agosto",
            9, "Septiembre - Diciembre"
    );

    private final PeriodoEscolarDao periodoDao = new PeriodoEscolarDao();

    /**
     * Atiende las peticiones GET del módulo de gestión de periodos escolares:
     * elimina o reactiva un periodo según el parámetro "accion", o carga el
     * listado completo de periodos para la vista de gestión.
     * @param request petición HTTP con el parámetro "accion" opcional y el idPeriodo asociado
     * @param response respuesta HTTP usada para redirigir o reenviar a la vista JSP
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            procesarEliminacion(request, response);
            return;
        }
        if ("reactivar".equals(accion)) {
            procesarReactivacion(request, response);
            return;
        }

        request.setAttribute("listaPeriodos", periodoDao.getAll());
        request.getRequestDispatcher("/coordinador/gestion-periodos.jsp").forward(request, response);
    }

    /**
     * Atiende las peticiones POST del módulo de gestión de periodos escolares:
     * elimina, reactiva o edita un periodo existente, o bien valida y registra
     * un nuevo periodo escolar (mes de inicio permitido, duración de 91 a 123 días,
     * objetivo de asistencias grupales y nombre calculado sin duplicados).
     * @param request petición HTTP con el parámetro "accion" y los datos del periodo
     * @param response respuesta HTTP usada para redirigir con el resultado de la operación
     * @throws ServletException si ocurre un error al procesar la petición
     * @throws IOException si ocurre un error de entrada/salida al redirigir la respuesta
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            procesarEliminacion(request, response);
            return;
        }
        if ("reactivar".equals(accion)) {
            procesarReactivacion(request, response);
            return;
        }
        if ("editar".equals(accion)) {
            procesarEdicion(request, response);
            return;
        }

        String nombre = request.getParameter("nombre");
        String fechaInicioStr = request.getParameter("fechaInicio");
        String fechaFinStr = request.getParameter("fechaFin");
        String asistenciasGrupalesStr = request.getParameter("asistenciasGrupales");

        if (nombre == null || nombre.isBlank()
                || fechaInicioStr == null || fechaInicioStr.isBlank()
                || fechaFinStr == null || fechaFinStr.isBlank()
                || asistenciasGrupalesStr == null || asistenciasGrupalesStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=campos_incompletos");
            return;
        }

        int asistenciasGrupales;
        try {
            asistenciasGrupales = Integer.parseInt(asistenciasGrupalesStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=objetivo_invalido");
            return;
        }
        if (asistenciasGrupales < 0) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=objetivo_invalido");
            return;
        }

        LocalDate fechaInicio;
        LocalDate fechaFin;
        try {
            fechaInicio = LocalDate.parse(fechaInicioStr.trim());
            fechaFin = LocalDate.parse(fechaFinStr.trim());
        } catch (DateTimeParseException e) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=fechas_invalidas");
            return;
        }

        if (!fechaFin.isAfter(fechaInicio)) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=rango_invalido");
            return;
        }

        if (!mesValido(fechaInicio)) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=mes_invalido");
            return;
        }

        if (!duracionValida(fechaInicio, fechaFin)) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=duracion_invalida");
            return;
        }

        String nombreCalculado = calcularNombrePeriodo(fechaInicio);
        if (periodoDao.existeNombre(nombreCalculado)) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=nombre_duplicado");
            return;
        }

        PeriodoEscolar periodo = new PeriodoEscolar();
        periodo.setNombre(nombreCalculado);
        periodo.setFechaInicio(Date.valueOf(fechaInicio));
        periodo.setFechaFin(Date.valueOf(fechaFin));
        periodo.setEstado("S");
        periodo.setAsistenciasGrupales(asistenciasGrupales);

        boolean guardado = periodoDao.create(periodo);

        if (guardado) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?exito=guardado");
        } else {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=registro_fallido");
        }
    }

    /**
     * Valida y aplica la edición de un periodo escolar existente: verifica que el
     * periodo exista, que los campos estén completos, que las fechas y duración sean
     * válidas y que el nombre calculado no choque con el de otro periodo.
     * @param request petición HTTP con el idPeriodo y los nuevos datos del periodo
     * @param response respuesta HTTP usada para redirigir con el resultado de la edición
     * @throws IOException si ocurre un error de entrada/salida al redirigir la respuesta
     */
    private void procesarEdicion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer idPeriodo = null;
        try {
            String idStr = request.getParameter("idPeriodo");
            if (idStr != null && !idStr.isBlank()) {
                idPeriodo = Integer.parseInt(idStr.trim());
            }
        } catch (NumberFormatException ignored) {
        }

        if (idPeriodo == null || periodoDao.getById(idPeriodo) == null) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=periodo_no_encontrado");
            return;
        }

        String nombre = request.getParameter("nombre");
        String fechaInicioStr = request.getParameter("fechaInicio");
        String fechaFinStr = request.getParameter("fechaFin");
        String asistenciasGrupalesStr = request.getParameter("asistenciasGrupales");

        if (nombre == null || nombre.isBlank()
                || fechaInicioStr == null || fechaInicioStr.isBlank()
                || fechaFinStr == null || fechaFinStr.isBlank()
                || asistenciasGrupalesStr == null || asistenciasGrupalesStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=campos_incompletos");
            return;
        }

        int asistenciasGrupales;
        try {
            asistenciasGrupales = Integer.parseInt(asistenciasGrupalesStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=objetivo_invalido");
            return;
        }
        if (asistenciasGrupales < 0) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=objetivo_invalido");
            return;
        }

        LocalDate fechaInicio;
        LocalDate fechaFin;
        try {
            fechaInicio = LocalDate.parse(fechaInicioStr.trim());
            fechaFin = LocalDate.parse(fechaFinStr.trim());
        } catch (DateTimeParseException e) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=fechas_invalidas");
            return;
        }

        if (!fechaFin.isAfter(fechaInicio)) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=rango_invalido");
            return;
        }

        if (!mesValido(fechaInicio)) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=mes_invalido");
            return;
        }

        if (!duracionValida(fechaInicio, fechaFin)) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=duracion_invalida");
            return;
        }

        String nombreCalculado = calcularNombrePeriodo(fechaInicio);
        if (periodoDao.existeNombreParaOtro(nombreCalculado, idPeriodo)) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=nombre_duplicado");
            return;
        }

        PeriodoEscolar periodo = new PeriodoEscolar();
        periodo.setIdPeriodo(idPeriodo);
        periodo.setNombre(nombreCalculado);
        periodo.setFechaInicio(Date.valueOf(fechaInicio));
        periodo.setFechaFin(Date.valueOf(fechaFin));
        periodo.setAsistenciasGrupales(asistenciasGrupales);

        boolean actualizado = periodoDao.update(periodo);

        if (actualizado) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?exito=editado");
        } else {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=registro_fallido");
        }
    }

    /**
     * Verifica que el mes de la fecha de inicio sea uno de los meses permitidos
     * para iniciar un periodo escolar (enero, mayo o septiembre).
     * @param fechaInicio la fecha de inicio del periodo a validar
     * @return {@code true} si el mes de inicio es válido; {@code false} en caso contrario
     */
    private boolean mesValido(LocalDate fechaInicio) {
        return MESES_PERMITIDOS.contains(fechaInicio.getMonthValue());
    }

    /**
     * Verifica que la duración entre la fecha de inicio y la de fin esté dentro
     * del rango permitido para un periodo escolar (más de 90 días y hasta 123 días).
     * @param fechaInicio la fecha de inicio del periodo
     * @param fechaFin la fecha de fin del periodo
     * @return {@code true} si la duración es válida; {@code false} en caso contrario
     */
    private boolean duracionValida(LocalDate fechaInicio, LocalDate fechaFin) {
        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        return dias > 90 && dias <= 123;
    }

    /**
     * Calcula el nombre descriptivo del periodo escolar a partir del mes y año de inicio.
     * @param fechaInicio la fecha de inicio del periodo
     * @return el nombre del periodo, por ejemplo "Enero - Abril 2026"
     */
    private String calcularNombrePeriodo(LocalDate fechaInicio) {
        return NOMBRES_MES_INICIO.get(fechaInicio.getMonthValue()) + " " + fechaInicio.getYear();
    }

    /**
     * Elimina (baja lógica) un periodo escolar identificado por su id y redirige
     * con el resultado de la operación.
     * @param request petición HTTP con el parámetro idPeriodo a eliminar
     * @param response respuesta HTTP usada para redirigir con el resultado de la eliminación
     * @throws IOException si ocurre un error de entrada/salida al redirigir la respuesta
     */
    private void procesarEliminacion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String parametro = "error=periodo_no_encontrado";
        String idStr = request.getParameter("idPeriodo");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int idPeriodo = Integer.parseInt(idStr.trim());
                boolean eliminado = periodoDao.delete(idPeriodo);
                parametro = eliminado ? "exito=eliminado" : "error=periodo_en_uso";
            } catch (Exception e) {
                e.printStackTrace();
                parametro = "error=periodo_en_uso";
            }
        }
        response.sendRedirect(request.getContextPath() + "/gestion-periodos?" + parametro);
    }

    /**
     * Reactiva un periodo escolar identificado por su id y redirige con el
     * resultado de la operación.
     * @param request petición HTTP con el parámetro idPeriodo a reactivar
     * @param response respuesta HTTP usada para redirigir con el resultado de la reactivación
     * @throws IOException si ocurre un error de entrada/salida al redirigir la respuesta
     */
    private void procesarReactivacion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String parametro = "error=periodo_no_encontrado";
        String idStr = request.getParameter("idPeriodo");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int idPeriodo = Integer.parseInt(idStr.trim());
                boolean reactivado = periodoDao.reactivar(idPeriodo);
                parametro = reactivado ? "exito=reactivado" : "error=reactivacion_fallida";
            } catch (Exception e) {
                e.printStackTrace();
                parametro = "error=reactivacion_fallida";
            }
        }
        response.sendRedirect(request.getContextPath() + "/gestion-periodos?" + parametro);
    }
}