package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.AsistenciaFilaDTO;
import mx.edu.utez.pigestiontutorias.models.CeldaAsistenciaDTO;
import mx.edu.utez.pigestiontutorias.models.Grupo;
import mx.edu.utez.pigestiontutorias.models.PeriodoEscolar;
import mx.edu.utez.pigestiontutorias.models.SesionGrupal;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SesionGrupalServlet", value = "/tutoria-grupal")
public class SesionGrupalServlet extends HttpServlet {

    private static final DateTimeFormatter FORMATO_FECHA_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TutorDao tutorDao = new TutorDao();
    private final AsignacionTutorDao asignacionTutorDao = new AsignacionTutorDao();
    private final AsistenciaGrupalDao asistenciaGrupalDao = new AsistenciaGrupalDao();
    private final SesionGrupalDao sesionGrupalDao = new SesionGrupalDao();
    private final PeriodoEscolarDao periodoDao = new PeriodoEscolarDao();
    private final GrupoDao grupoDao = new GrupoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("obtenerCuadricula".equals(request.getParameter("accion"))) {
            obtenerCuadriculaPorGrupo(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Tutor tutor = tutorDao.getById((Integer) session.getAttribute("idUsuario"));
        PeriodoEscolar periodoVigente = periodoDao.getPeriodoVigente();

        List<Grupo> gruposAsignados = (tutor != null && periodoVigente != null)
                ? asignacionTutorDao.obtenerGruposPorTutor(tutor.getNumeroEmpleado(), periodoVigente.getIdPeriodo())
                : Collections.emptyList();

        request.setAttribute("gruposAsignados", gruposAsignados);
        request.setAttribute("paginaActiva", "grupal");
        request.setAttribute("periodoVigente", periodoVigente);

        String idGrupoParam = request.getParameter("idGrupo");
        if (idGrupoParam != null && !idGrupoParam.isBlank()) {
            request.setAttribute("idGrupoPreseleccionado", idGrupoParam);
        }

        request.getRequestDispatcher("/tutor/registro-grupal.jsp").forward(request, response);
    }

    private void obtenerCuadriculaPorGrupo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        Integer idTutor = session != null ? (Integer) session.getAttribute("idUsuario") : null;
        if (idTutor == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{}");
            return;
        }

        int idGrupo;
        try {
            idGrupo = Integer.parseInt(request.getParameter("idGrupo").trim());
        } catch (Exception e) {
            response.getWriter().write("{}");
            return;
        }

        if (!asignacionTutorDao.existeAsignacionParaTutor(idTutor, idGrupo)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{}");
            return;
        }

        Grupo grupo = grupoDao.getById(idGrupo);
        PeriodoEscolar periodo = grupo != null ? periodoDao.getById(grupo.getIdPeriodo()) : null;

        List<SesionGrupal> sesiones = periodo != null
                ? asistenciaGrupalDao.getSesionesDelGrupoEnPeriodo(idGrupo, periodo.getFechaInicio(), periodo.getFechaFin())
                : Collections.emptyList();
        List<AsistenciaFilaDTO> filas = asistenciaGrupalDao.construirFilasAsistencia(idGrupo, sesiones);

        StringBuilder json = new StringBuilder("{");

        json.append("\"sesiones\":[");
        for (int i = 0; i < sesiones.size(); i++) {
            if (i > 0) json.append(",");
            SesionGrupal s = sesiones.get(i);
            json.append("{\"idSesionGrupal\":").append(s.getIdSesionGrupal())
                    .append(",\"fechaIso\":\"").append(s.getFecha().toLocalDate().format(FORMATO_FECHA_ISO)).append("\"}");
        }
        json.append("],");

        json.append("\"filas\":[");
        for (int i = 0; i < filas.size(); i++) {
            if (i > 0) json.append(",");
            AsistenciaFilaDTO f = filas.get(i);
            json.append("{\"matricula\":\"").append(escaparJson(f.getMatricula())).append("\",")
                    .append("\"nombreCompleto\":\"").append(escaparJson(f.getNombreCompleto())).append("\",")
                    .append("\"estatusPorSesion\":{");
            int j = 0;
            for (Map.Entry<Integer, String> entry : f.getEstatusPorSesion().entrySet()) {
                if (j++ > 0) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":\"").append(escaparJson(entry.getValue())).append("\"");
            }
            json.append("}}");
        }
        json.append("]}");

        try (PrintWriter writer = response.getWriter()) {
            writer.write(json.toString());
        }
    }

    private String escaparJson(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Tutor tutor = tutorDao.getById((Integer) session.getAttribute("idUsuario"));
        if (tutor == null) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=tutor_no_encontrado");
            return;
        }

        String idGrupoStr = request.getParameter("idGrupo");
        String fechaStr = request.getParameter("fecha");
        String acuerdos = request.getParameter("acuerdos");
        String temas = request.getParameter("temas");
        String asesorias = request.getParameter("asesorias");

        if ("true".equals(request.getParameter("soloAsistencia"))) {
            guardarSoloAsistencia(request, response, tutor);
            return;
        }

        if (idGrupoStr == null || idGrupoStr.isBlank()
                || fechaStr == null || fechaStr.isBlank()
                || acuerdos == null || acuerdos.isBlank()
                || temas == null || temas.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=campos_incompletos");
            return;
        }

        int idGrupo;
        Date fecha;
        try {
            idGrupo = Integer.parseInt(idGrupoStr.trim());
            fecha = Date.valueOf(fechaStr.trim());
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=datos_invalidos");
            return;
        }

        java.time.LocalDate fechaSesion = fecha.toLocalDate();
        java.time.LocalDate hoy = java.time.LocalDate.now();
        if (fechaSesion.isAfter(hoy)) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=fecha_futura");
            return;
        }

        if (fechaSesion.isBefore(hoy.minusDays(15))) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=fecha_fuera_rango_15_dias");
            return;
        }

        PeriodoEscolar periodoVigente = periodoDao.getPeriodoVigente();
        if (periodoVigente != null && fechaSesion.isBefore(periodoVigente.getFechaInicio().toLocalDate())) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=fecha_fuera_periodo");
            return;
        }
        if (!asignacionTutorDao.existeAsignacionParaTutor(tutor.getNumeroEmpleado(), idGrupo)) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=grupo_no_asignado");
            return;
        }

        asesorias = (asesorias != null && !asesorias.isBlank()) ? asesorias.trim() : null;

        SesionGrupal sesion = new SesionGrupal();
        sesion.setIdGrupo(idGrupo);
        sesion.setIdTutor(tutor.getNumeroEmpleado());
        sesion.setFecha(fecha);
        sesion.setHora(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        sesion.setTemasTratados(temas.trim());
        sesion.setAcuerdos(acuerdos.trim());
        sesion.setAsesoriasGrupales(asesorias);
        sesion.setEstado("Completado");

        boolean guardado = sesionGrupalDao.create(sesion);

        if (!guardado) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=guardado_fallido");
            return;
        }

        List<CeldaAsistenciaDTO> celdas = parsearCeldas(request.getParameterValues("celda"), sesion.getIdSesionGrupal());
        if (!celdas.isEmpty()) {
            asistenciaGrupalDao.guardarCeldas(celdas);
        }

        response.sendRedirect(request.getContextPath() + "/tutoria-grupal?idGrupo=" + idGrupo + "&exito=grupal_guardada");
    }

    private List<CeldaAsistenciaDTO> parsearCeldas(String[] valores, int idSesionNueva) {
        List<CeldaAsistenciaDTO> celdas = new ArrayList<>();
        if (valores == null) {
            return celdas;
        }

        for (String valor : valores) {
            String[] partes = valor.split("\\|", 3);
            if (partes.length != 3) {
                continue;
            }

            int idSesion;
            if ("nueva".equals(partes[0])) {
                idSesion = idSesionNueva;
            } else {
                try {
                    idSesion = Integer.parseInt(partes[0]);
                } catch (NumberFormatException e) {
                    continue;
                }
            }

            celdas.add(new CeldaAsistenciaDTO(idSesion, partes[1], partes[2]));
        }

        celdas.removeIf(c -> c.getIdSesionGrupal() <= 0);

        return celdas;
    }

    private void guardarSoloAsistencia(HttpServletRequest request, HttpServletResponse response, Tutor tutor) throws IOException {
        String idGrupoStr = request.getParameter("idGrupo");

        if (idGrupoStr == null || idGrupoStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=datos_invalidos");
            return;
        }

        int idGrupo;
        try {
            idGrupo = Integer.parseInt(idGrupoStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/tutoria-grupal?error=datos_invalidos");
            return;
        }

        String redirectUrl = request.getContextPath() + "/tutoria-grupal?idGrupo=" + idGrupo;

        if (!asignacionTutorDao.existeAsignacionParaTutor(tutor.getNumeroEmpleado(), idGrupo)) {
            response.sendRedirect(redirectUrl + "&error=grupo_no_asignado");
            return;
        }

        List<CeldaAsistenciaDTO> celdas = parsearCeldas(request.getParameterValues("celda"), -1);

        boolean guardado = celdas.isEmpty() || asistenciaGrupalDao.guardarCeldas(celdas);

        response.sendRedirect(redirectUrl + (guardado ? "&exito=asistencia_actualizada" : "&error=guardado_fallido"));
    }
}
