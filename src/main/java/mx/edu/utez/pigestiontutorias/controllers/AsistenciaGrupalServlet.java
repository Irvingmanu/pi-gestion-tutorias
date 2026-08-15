package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.Grupo;
import mx.edu.utez.pigestiontutorias.models.PeriodoEscolar;
import mx.edu.utez.pigestiontutorias.models.dao.AsignacionTutorDao;
import mx.edu.utez.pigestiontutorias.models.dao.AsistenciaGrupalDao;
import mx.edu.utez.pigestiontutorias.models.dao.PeriodoEscolarDao;

import java.io.IOException;
import java.util.List;

// El filtro de "grupo" ahora es un solo <select> de GRUPO (ya no Carrera+Cuatrimestre+Letra
// por separado), poblado con los grupos que el tutor logueado tiene asignados.
@WebServlet(name = "AsistenciaGrupalServlet", value = "/tutor/asistencia-grupal")
public class AsistenciaGrupalServlet extends HttpServlet {

    private final AsistenciaGrupalDao asistenciaDao = new AsistenciaGrupalDao();
    private final AsignacionTutorDao asignacionTutorDao = new AsignacionTutorDao();
    private final PeriodoEscolarDao periodoDao = new PeriodoEscolarDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer idTutor = session != null ? (Integer) session.getAttribute("idUsuario") : null;

        PeriodoEscolar periodoVigente = periodoDao.getPeriodoVigente();
        List<Grupo> listaGrupos = (idTutor != null && periodoVigente != null)
                ? asignacionTutorDao.obtenerGruposPorTutor(idTutor, periodoVigente.getIdPeriodo())
                : java.util.Collections.emptyList();

        request.setAttribute("listaGrupos", listaGrupos);
        request.setAttribute("paginaActiva", "grupal");

        String grupoParam = request.getParameter("idGrupo");
        String fechaParam = request.getParameter("fecha");

        if (grupoParam != null && !grupoParam.trim().isEmpty()) {
            try {
                int idGrupo = Integer.parseInt(grupoParam.trim());

                List<Alumno> listaAlumnos = asistenciaDao.getAlumnosPorGrupo(idGrupo);
                request.setAttribute("listaAlumnosGrupo", listaAlumnos);

                if (fechaParam != null && !fechaParam.trim().isEmpty()) {
                    List<String> matriculasAsistidas = asistenciaDao.getAlumnosConAsistencia(idGrupo, fechaParam);
                    request.setAttribute("matriculasAsistidas", matriculasAsistidas);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        request.getRequestDispatcher("/tutor/asistencia-grupal.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String grupoParam = request.getParameter("idGrupo");
        String fecha = request.getParameter("fecha");
        String[] matriculasAsistentes = request.getParameterValues("asistencia");

        HttpSession session = request.getSession();
        Integer idTutor = (Integer) session.getAttribute("idUsuario");

        String redirectUrl = request.getContextPath() + "/tutor/asistencia-grupal" +
                "?idGrupo=" + (grupoParam != null ? grupoParam : "") +
                "&fecha=" + (fecha != null ? fecha : "");

        if (idTutor == null || grupoParam == null || grupoParam.isBlank()) {
            response.sendRedirect(redirectUrl + "&error=datos_invalidos");
            return;
        }

        try {
            int idGrupo = Integer.parseInt(grupoParam.trim());
            asistenciaDao.registrarAsistenciaGrupal(idGrupo, fecha, matriculasAsistentes, idTutor);
        } catch (NumberFormatException e) {
            response.sendRedirect(redirectUrl + "&error=datos_invalidos");
            return;
        }

        response.sendRedirect(redirectUrl + "&exito=asistencia_guardada");
    }
}
