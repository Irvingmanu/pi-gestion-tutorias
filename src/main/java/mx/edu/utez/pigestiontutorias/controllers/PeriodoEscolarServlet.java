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
import java.util.List;

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

        String nombre = request.getParameter("nombre");
        String fechaInicioStr = request.getParameter("fechaInicio");
        String fechaFinStr = request.getParameter("fechaFin");

        if (nombre == null || nombre.isBlank()
                || fechaInicioStr == null || fechaInicioStr.isBlank()
                || fechaFinStr == null || fechaFinStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?error=campos_incompletos");
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
        periodo.setActivo("S");

        boolean guardado = periodoDao.create(periodo);

        if (guardado) {
            response.sendRedirect(request.getContextPath() + "/gestion-periodos?exito=guardado");
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