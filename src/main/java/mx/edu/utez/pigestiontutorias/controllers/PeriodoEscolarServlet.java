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

@WebServlet(name = "PeriodoEscolarServlet", value = "/gestion-periodos")
public class PeriodoEscolarServlet extends HttpServlet {

    private final PeriodoEscolarDao periodoDao = new PeriodoEscolarDao();

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

        // Blindaje de servidor: fin debe ser posterior a inicio, sin importar
        // lo que manden los <input type="date"> del formulario.
        if (!fechaFin.isAfter(fechaInicio)) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=rango_invalido");
            return;
        }

        if (periodoDao.existeNombre(nombre.trim())) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=nombre_duplicado");
            return;
        }

        PeriodoEscolar periodo = new PeriodoEscolar();
        periodo.setNombre(nombre.trim());
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

    // Edicion de un periodo ya existente (nombre/fechas/objetivo de tutorías grupales), incluida
    // la unica forma soportada de corregir ASISTENCIASGRUPALES desde la interfaz: el DAO ya tenia
    // el metodo update() listo, solo faltaba esta ruta para invocarlo desde el formulario.
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

        if (periodoDao.existeNombreParaOtro(nombre.trim(), idPeriodo)) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=nombre_duplicado");
            return;
        }

        PeriodoEscolar periodo = new PeriodoEscolar();
        periodo.setIdPeriodo(idPeriodo);
        periodo.setNombre(nombre.trim());
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