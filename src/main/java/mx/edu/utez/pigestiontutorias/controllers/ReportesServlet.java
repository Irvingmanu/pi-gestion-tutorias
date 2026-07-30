package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.ReportesDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Map;

@WebServlet(name = "ReportesServlet", urlPatterns = {"/ReportesServlet"})
public class ReportesServlet extends HttpServlet {

    private final ReportesDao reportesDao = new ReportesDao();
    private final TutorDao tutorDao = new TutorDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int idUsuario = (Integer) session.getAttribute("idUsuario");

        // El alcance se decide solo: si el usuario logueado es un tutor,
        // el reporte se limita a sus alumnos asignados. Si no es tutor
        // (coordinador), el reporte es global. Así un mismo endpoint sirve
        // a las dos pantallas sin duplicar lógica de negocio ni exponer
        // un parámetro que el cliente pudiera manipular para ver datos
        // de otro tutor.
        Tutor tutorSesion = tutorDao.findByIdUsuario(idUsuario);
        Integer idTutorAlcance = (tutorSesion != null) ? tutorSesion.getIdTutor() : null;

        Integer idCarrera = parametroEntero(request, "idCarrera");
        Integer idCuatrimestre = parametroEntero(request, "idCuatrimestre");
        Integer idLetraGrupo = parametroEntero(request, "idLetraGrupo");

        LocalDate hasta = parametroFecha(request, "hasta");
        if (hasta == null) hasta = LocalDate.now();

        LocalDate desde = parametroFecha(request, "desde");
        if (desde == null) desde = hasta.minusMonths(4); // ~ un cuatrimestre si no se especifica

        ReportesDao.ReporteResumen reporte = reportesDao.generarReporte(
                idTutorAlcance, idCarrera, idCuatrimestre, idLetraGrupo, desde, hasta);

        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(construirJson(reporte));
        }
    }

    private Integer parametroEntero(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        if (valor == null || valor.isBlank()) return null;
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parametroFecha(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        if (valor == null || valor.isBlank()) return null;
        try {
            return LocalDate.parse(valor);
        } catch (Exception e) {
            return null;
        }
    }

    private String construirJson(ReportesDao.ReporteResumen reporte) {
        StringBuilder json = new StringBuilder("{");
        json.append("\"totalAtendidos\":").append(reporte.totalAtendidos).append(',');
        json.append("\"totalPidieronTutorias\":").append(reporte.totalPidieronTutorias).append(',');
        json.append("\"totalCanalizados\":").append(reporte.totalCanalizados).append(',');
        json.append("\"totalPendientes\":").append(reporte.totalPendientes).append(',');
        json.append("\"totalGruposAtendidos\":").append(reporte.totalGruposAtendidos).append(',');
        json.append("\"totalAsistencias\":").append(reporte.totalAsistencias).append(',');

        json.append("\"distribucionCanalizados\":[");
        boolean primero = true;
        for (Map.Entry<String, Integer> entrada : reporte.distribucionCanalizados.entrySet()) {
            if (!primero) json.append(',');
            primero = false;
            json.append("{\"nombreServicio\":\"").append(escaparJson(entrada.getKey()))
                    .append("\",\"totalAbsoluto\":").append(entrada.getValue()).append('}');
        }
        json.append(']');

        json.append('}');
        return json.toString();
    }

    private String escaparJson(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
