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

/**
 * Servlet que expone, en formato JSON, la siguiente matrícula disponible para un
 * grupo, calculada a partir de la carrera y el periodo escolar del grupo. Solo
 * accesible para sesiones con rol de Coordinador.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-08-20
 */
@WebServlet(name = "GenerarCredencialesServlet", value = "/generarCredenciales")
public class GenerarCredencialesServlet extends HttpServlet {

    private final GrupoDao grupoDao = new GrupoDao();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final PeriodoEscolarDao periodoEscolarDao = new PeriodoEscolarDao();

    /**
     * Atiende la petición GET, valida la sesión de coordinador y el grupo indicado,
     * y calcula la siguiente matrícula disponible a partir del prefijo de la carrera
     * y el periodo escolar del grupo, devolviéndola como JSON.
     * @param request petición HTTP con el parámetro "idGrupo"
     * @param response respuesta HTTP en formato JSON con la matrícula generada o el error correspondiente
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
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

    /**
     * Convierte una cadena de texto a entero de forma segura.
     * @param valor el texto a convertir
     * @return el valor entero resultante, o {@code null} si el texto es nulo, está en blanco o no es numérico
     */
    private Integer parseIntOrNull(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Escribe una cadena JSON como cuerpo completo de la respuesta HTTP.
     * @param response la respuesta HTTP sobre la que se escribe
     * @param json el contenido JSON a escribir
     * @throws IOException si ocurre un error de entrada/salida al escribir la respuesta
     */
    private void escribir(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter writer = response.getWriter()) {
            writer.write(json);
        }
    }

    /**
     * Escapa las barras invertidas y comillas dobles de un texto para insertarlo
     * de forma segura como valor dentro de una cadena JSON.
     * @param valor el texto a escapar
     * @return el texto escapado, o una cadena vacía si el valor original era {@code null}
     */
    private String escaparJson(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
