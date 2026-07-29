package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.ReporteTutor;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.CatalogoDao;
import mx.edu.utez.pigestiontutorias.models.dao.ReporteDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/ReportesServlet")
public class ReportesServlet extends HttpServlet {

    private final TutorDao tutorDao = new TutorDao();
    private final ReporteDao reporteDao = new ReporteDao();
    private final CatalogoDao catalogoDao = new CatalogoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        Tutor tutor = tutorDao.findByIdUsuario(idUsuario);

        if (tutor == null) {
            request.setAttribute("error", "No se encontró el perfil de tutor asociado a tu cuenta.");
            request.getRequestDispatcher("/tutor/reportes.jsp").forward(request, response);
            return;
        }

        request.setAttribute("carreras", catalogoDao.getCarreras());
        request.setAttribute("cuatrimestres", catalogoDao.getCuatrimestres());
        request.setAttribute("letrasGrupo", catalogoDao.getLetrasGrupo());

        Integer idCuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        Integer idLetraGrupo = parseIntOrNull(request.getParameter("grupo"));
        Integer idCarrera = parseIntOrNull(request.getParameter("carrera"));

        ReporteTutor reporte = reporteDao.obtenerReporte(tutor.getIdTutor(), idCuatrimestre, idLetraGrupo, idCarrera);
        request.setAttribute("reporte", reporte);

        request.setAttribute("filtroCuatrimestre", idCuatrimestre);
        request.setAttribute("filtroGrupo", idLetraGrupo);
        request.setAttribute("filtroCarrera", idCarrera);

        request.getRequestDispatcher("/tutor/reportes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        Tutor tutor = tutorDao.findByIdUsuario(idUsuario);
        if (tutor == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Integer idCuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        Integer idLetraGrupo = parseIntOrNull(request.getParameter("grupo"));
        Integer idCarrera = parseIntOrNull(request.getParameter("carrera"));

        ReporteTutor reporte = reporteDao.obtenerReporte(tutor.getIdTutor(), idCuatrimestre, idLetraGrupo, idCarrera);

        String nombreCuatrimestre = buscarNumeroCuatrimestre(idCuatrimestre);
        String nombreGrupo = buscarLetraGrupo(idLetraGrupo);
        String nombreCarrera = buscarNombreCarrera(idCarrera);
        String nombreArchivo = construirNombreArchivo(nombreCuatrimestre, nombreGrupo, nombreCarrera);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"");

        PrintWriter out = response.getWriter();
        out.write('\uFEFF');
        out.println("Reporte de Tutorias");
        out.println("Tutor," + tutor.getNombres() + " " + tutor.getApellidos());
        out.println("Cuatrimestre," + nombreCuatrimestre);
        out.println("Grupo," + nombreGrupo);
        out.println("Carrera," + nombreCarrera);
        out.println();
        out.println("Indicador,Cantidad");
        out.println("Alumnos Atendidos," + reporte.getAlumnosAtendidos());
        out.println("Canalizaciones," + reporte.getCanalizaciones());
        out.println("Grupos Atendidos," + reporte.getGruposAtendidos());
        out.println("Asistencias," + reporte.getAsistencias());
        out.flush();
    }

    private String construirNombreArchivo(String cuatrimestre, String grupo, String carrera) {
        StringBuilder nombre = new StringBuilder("reporte_tutorias");

        if (!"Todos".equals(cuatrimestre)) {
            nombre.append("_").append(cuatrimestre.replace("°", ""));
        }
        if (!"Todos".equals(grupo)) {
            nombre.append(grupo);
        }
        if (!"Todos".equals(carrera)) {
            nombre.append("_").append(carrera);
        }

        nombre.append(".csv");
        return nombre.toString();
    }

    private String buscarNumeroCuatrimestre(Integer id) {
        if (id == null) return "Todos";
        return catalogoDao.getCuatrimestres().stream()
                .filter(c -> c.getIdCuatrimestre() == id)
                .findFirst()
                .map(c -> c.getNumero() + "°")
                .orElse("Todos");
    }

    private String buscarLetraGrupo(Integer id) {
        if (id == null) return "Todos";
        return catalogoDao.getLetrasGrupo().stream()
                .filter(l -> l.getIdLetra() == id)
                .findFirst()
                .map(l -> l.getLetra())
                .orElse("Todos");
    }

    private String buscarNombreCarrera(Integer id) {
        if (id == null) return "Todos";
        return catalogoDao.getCarreras().stream()
                .filter(c -> c.getIdCarrera() == id)
                .findFirst()
                .map(c -> c.getNombre())
                .orElse("Todos");
    }

    private Integer parseIntOrNull(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}