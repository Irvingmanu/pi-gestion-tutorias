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

// AJAX consumido desde coordinador/formulario-alumno.jsp al elegir un grupo en "Asignar a
// Grupo" (ver obtenerCredenciales() en formulario-alumno.js): genera la Matricula sugerida
// para un alumno nuevo, y el Correo se arma en cliente a partir de ella (matricula en
// minusculas + "@utez.edu.mx").
//
// Formato de MATRICULA (10 caracteres, igual al REGEX_MATRICULA de AlumnoServlet):
//   [Anio actual: 4] + [Digito de Periodo: 1] + [Sigla de Carrera: 2] + [Contador: 3]
//   Ej. 2026 + 3 + ds + 071 = "20263ds071"
//
// El "Periodo" se toma del PERIODO_ESCOLAR propio del GRUPO elegido (no de "el periodo
// vigente hoy"): un alumno que se suma a un grupo ya existente debe quedar etiquetado con
// el periodo de ESE grupo, sin importar si para la fecha de alta ya cambio el periodo
// activo. OJO: no se usa el ID_PERIODO (la PK autoincremental de PERIODO_ESCOLAR) tal
// cual, porque esa secuencia no tiene tope de digitos (con suficientes periodos creados
// llega a 2+ digitos) y el formato de MATRICULA exige exactamente 10 caracteres siempre.
// En su lugar se deriva un digito 1/2/3 del MES de FECHA_INICIO (Ene-Abr / May-Ago /
// Sep-Dic), que es la convencion real de cuatrimestre de ingreso y siempre es 1 digito.
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

        // -- 1) Carrera + Periodo asociados al grupo -----------------------------------
        // SELECT g.ID_CARRERA, g.ID_PERIODO, car.NOMBRE AS NOMBRE_CARRERA
        // FROM GRUPO g JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA
        // WHERE g.ID_GRUPO = ?
        // (ya es GrupoDao.getById(): una sola consulta resuelve, en la misma fila, la
        // Carrera y el ID_PERIODO que pide la Matricula).
        Grupo grupo = grupoDao.getById(idGrupo);
        if (grupo == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            escribir(response, "{\"error\":\"El grupo indicado no existe.\"}");
            return;
        }

        // -- 1b) Fechas del periodo, para derivar su digito 1/2/3 -----------------------
        // SELECT FECHA_INICIO FROM PERIODO_ESCOLAR WHERE ID_PERIODO = ?
        // (PeriodoEscolarDao.getById(): ya trae FECHA_INICIO para calcular el cuatrimestre
        // de ingreso, ver resolverDigitoPeriodo()).
        PeriodoEscolar periodoDelGrupo = periodoEscolarDao.getById(grupo.getIdPeriodo());
        if (periodoDelGrupo == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            escribir(response, "{\"error\":\"No se encontró el periodo escolar del grupo.\"}");
            return;
        }

        String prefijo = GeneradorMatricula.construirPrefijo(grupo.getNombreCarrera(), periodoDelGrupo.getFechaInicio());

        // -- 2) Ultimo contador ya usado con ese prefijo --------------------------------
        // SELECT MATRICULA FROM ALUMNO WHERE MATRICULA LIKE '<ANIOPERIODOSIGLA>%'
        // (AlumnoDAO.obtenerSiguienteContador(): toma el sufijo numerico de cada
        // matricula que ya empieza con ese prefijo y regresa el mayor + 1, o 1 si es la
        // primera). La busqueda va en MAYUSCULAS porque asi es como AlumnoServlet guarda
        // toda MATRICULA; el prefijo que se regresa al cliente se arma con la sigla en
        // minusculas (formato pedido: "20263ds071"), sin afectar la busqueda.
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
