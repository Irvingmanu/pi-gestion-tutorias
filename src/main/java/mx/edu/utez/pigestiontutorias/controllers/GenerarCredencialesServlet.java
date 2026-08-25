package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Grupo;
import mx.edu.utez.pigestiontutorias.models.PeriodoEscolar;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.GrupoDao;
import mx.edu.utez.pigestiontutorias.models.dao.PeriodoEscolarDao;
import mx.edu.utez.pigestiontutorias.utils.GeneradorMatricula;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

@WebServlet(name = "GenerarCredencialesServlet", value = "/generarCredenciales")
public class GenerarCredencialesServlet extends HttpServlet {

    private final GrupoDao grupoDao = new GrupoDao();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final PeriodoEscolarDao periodoEscolarDao = new PeriodoEscolarDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || !"Coordinador".equals(session.getAttribute("rol"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            escribir(response, "{\"error\":\"Tu sesión expiró, recarga la página.\"}");
            return;
        }

        Integer idGrupo = parseIntOrNull(request.getParameter("idGrupo"));
        if (idGrupo == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribir(response, "{\"error\":\"idGrupo es obligatorio.\"}");
            return;
        }

        Grupo grupo = grupoDao.getById(idGrupo);
        if (grupo == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            escribir(response, "{\"error\":\"El grupo indicado no existe.\"}");
            return;
        }

        PeriodoEscolar periodoDelGrupo = periodoEscolarDao.getById(grupo.getIdPeriodo());
        if (periodoDelGrupo == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            escribir(response, "{\"error\":\"No se encontró el periodo escolar del grupo.\"}");
            return;
        }

        String prefijo = GeneradorMatricula.construirPrefijo(grupo.getNombreCarrera(), periodoDelGrupo.getFechaInicio());

        int contador = alumnoDAO.obtenerSiguienteContador(prefijo.toUpperCase(Locale.ROOT));
        String contadorFormateado = String.format(Locale.ROOT, "%03d", contador);

        String matricula = prefijo + contadorFormateado;

        escribir(response, "{\"matricula\":\"" + escaparJson(matricula) + "\"}");
    }

    private Integer parseIntOrNull(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void escribir(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter writer = response.getWriter()) {
            writer.write(json);
        }
    }

    private String escaparJson(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
