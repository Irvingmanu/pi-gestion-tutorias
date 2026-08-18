package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Canalizacion;
import mx.edu.utez.pigestiontutorias.models.PeriodoEscolar;
import mx.edu.utez.pigestiontutorias.models.SeguimientoTutorGrupalDTO;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportesDao {

    public static class ReporteResumen {
        public int totalAtendidos;
        public int totalPidieronTutorias;
        public int totalCanalizados;
        public int totalPendientes;
        public int totalGruposAtendidos;
        public int totalAsistencias;
        public Map<String, Integer> distribucionCanalizados = new LinkedHashMap<>();
        public List<Canalizacion> canalizaciones = new ArrayList<>();
    }

    public ReporteResumen generarReporte(Integer idTutor, Integer idCarrera, Integer idCuatrimestre,
                                         Integer idLetraGrupo, String matricula, LocalDate desde, LocalDate hasta) {
        ReporteResumen reporte = new ReporteResumen();
        Date sqlDesde = Date.valueOf(desde);
        Date sqlHasta = Date.valueOf(hasta);

        try (Connection con = SQLConnector.getConnection()) {
            reporte.totalAtendidos = contarAlumnosAtendidos(con, sqlDesde, sqlHasta, idTutor, idCarrera, idCuatrimestre, idLetraGrupo, matricula);
            reporte.totalPidieronTutorias = contarPidieronTutorias(con, sqlDesde, sqlHasta, idTutor, idCarrera, idCuatrimestre, idLetraGrupo, matricula);
            reporte.totalCanalizados = contarCanalizados(con, sqlDesde, sqlHasta, idTutor, idCarrera, idCuatrimestre, idLetraGrupo, matricula);
            reporte.totalPendientes = contarPendientes(con, sqlDesde, sqlHasta, idTutor, idCarrera, idCuatrimestre, idLetraGrupo, matricula);
            reporte.totalGruposAtendidos = contarGruposAtendidos(con, sqlDesde, sqlHasta, idTutor, idCuatrimestre, idLetraGrupo);
            reporte.totalAsistencias = contarAsistencias(con, sqlDesde, sqlHasta, idTutor, idCarrera, idCuatrimestre, idLetraGrupo, matricula);
            reporte.distribucionCanalizados = distribucionPorArea(con, sqlDesde, sqlHasta, idTutor, idCarrera, idCuatrimestre, idLetraGrupo, matricula);
            reporte.canalizaciones = listarCanalizaciones(con, sqlDesde, sqlHasta, idTutor, idCarrera, idCuatrimestre, idLetraGrupo, matricula);
        } catch (SQLException e) {
            System.err.println("Error al generar el reporte: " + e.getMessage());
            e.printStackTrace();
        }

        return reporte;
    }

    // Nota: el parametro "matricula" se conserva en las firmas para no romper
    // las llamadas existentes, pero ya no se usa para filtrar (se quito el
    // buscador de alumno de la pantalla de Reportes Globales).
    private void agregarFiltrosAlumno(StringBuilder sql, List<Object> params, String aliasAlumno,
                                      Integer idTutor, Integer idCarrera, Integer idCuatrimestre, Integer idLetraGrupo,
                                      String matricula) {
        if (idTutor != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM ASIGNACION_TUTOR asg " +
                    "WHERE asg.ID_TUTOR = ? AND asg.ACTIVO = 'S' " +
                    "AND asg.ID_LETRA_GRUPO = ").append(aliasAlumno).append(".ID_LETRA_GRUPO " +
                    "AND asg.ID_CUATRIMESTRE = ").append(aliasAlumno).append(".ID_CUATRIMESTRE) ");
            params.add(idTutor);
        }
        if (idCarrera != null) {
            sql.append(" AND ").append(aliasAlumno).append(".ID_CARRERA = ? ");
            params.add(idCarrera);
        }
        if (idCuatrimestre != null) {
            sql.append(" AND ").append(aliasAlumno).append(".ID_CUATRIMESTRE = ? ");
            params.add(idCuatrimestre);
        }
        if (idLetraGrupo != null) {
            sql.append(" AND ").append(aliasAlumno).append(".ID_LETRA_GRUPO = ? ");
            params.add(idLetraGrupo);
        }
        // Filtro por alumno removido de Reportes Globales:
        // if (matricula != null && !matricula.isBlank()) {
        //     sql.append(" AND TRIM(").append(aliasAlumno).append(".MATRICULA) = ? ");
        //     params.add(matricula.trim());
        // }
    }

    private int ejecutarConteo(Connection con, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            aplicarParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("TOTAL") : 0;
            }
        }
    }

    private void aplicarParametros(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object valor = params.get(i);
            if (valor instanceof Date) {
                ps.setDate(i + 1, (Date) valor);
            } else if (valor instanceof String) {
                ps.setString(i + 1, (String) valor);
            } else {
                ps.setInt(i + 1, (Integer) valor);
            }
        }
    }

    // 1. Alumnos atendidos: al menos una sesión individual "Tomada"
    private int contarAlumnosAtendidos(Connection con, Date desde, Date hasta, Integer idTutor,
                                       Integer idCarrera, Integer idCuatrimestre, Integer idLetraGrupo, String matricula) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT a.MATRICULA) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN SESION_INDIVIDUAL si ON si.MATRICULA = a.MATRICULA " +
                        "WHERE si.ESTADO = 'Tomada' AND si.FECHA BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, idCuatrimestre, idLetraGrupo, matricula);

        return ejecutarConteo(con, sql.toString(), params);
    }

    // 2. Alumnos que pidieron tutoría: al menos una solicitud registrada
    private int contarPidieronTutorias(Connection con, Date desde, Date hasta, Integer idTutor,
                                       Integer idCarrera, Integer idCuatrimestre, Integer idLetraGrupo, String matricula) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT a.MATRICULA) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN SOLICITUD_TUTORIA s ON s.MATRICULA = a.MATRICULA " +
                        "WHERE TRUNC(s.FECHA_REGISTRO) BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, idCuatrimestre, idLetraGrupo, matricula);

        return ejecutarConteo(con, sql.toString(), params);
    }

    // 3. Alumnos canalizados a algún área de apoyo
    private int contarCanalizados(Connection con, Date desde, Date hasta, Integer idTutor,
                                  Integer idCarrera, Integer idCuatrimestre, Integer idLetraGrupo, String matricula) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT a.MATRICULA) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN CANALIZACION c ON c.MATRICULA = a.MATRICULA " +
                        "WHERE c.FECHA_CANALIZACION BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, idCuatrimestre, idLetraGrupo, matricula);

        return ejecutarConteo(con, sql.toString(), params);
    }

    // 4. Solicitudes pendientes de responder
    private int contarPendientes(Connection con, Date desde, Date hasta, Integer idTutor,
                                 Integer idCarrera, Integer idCuatrimestre, Integer idLetraGrupo, String matricula) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN SOLICITUD_TUTORIA s ON s.MATRICULA = a.MATRICULA " +
                        "WHERE s.ESTATUS = 'Pendiente' AND TRUNC(s.FECHA_REGISTRO) BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, idCuatrimestre, idLetraGrupo, matricula);

        return ejecutarConteo(con, sql.toString(), params);
    }

    // 5. Sesiones grupales completadas (no aplica filtro por alumno individual:
    //    la sesión es del grupo completo, no de un alumno en particular)
    private int contarGruposAtendidos(Connection con, Date desde, Date hasta, Integer idTutor,
                                      Integer idCuatrimestre, Integer idLetraGrupo) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) AS TOTAL FROM SESION_GRUPAL sg " +
                        "WHERE sg.ESTADO = 'Completado' AND sg.FECHA BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        if (idTutor != null) {
            sql.append(" AND sg.ID_TUTOR = ? ");
            params.add(idTutor);
        }
        if (idCuatrimestre != null) {
            sql.append(" AND sg.ID_CUATRIMESTRE = ? ");
            params.add(idCuatrimestre);
        }
        if (idLetraGrupo != null) {
            sql.append(" AND sg.ID_LETRA_GRUPO = ? ");
            params.add(idLetraGrupo);
        }

        return ejecutarConteo(con, sql.toString(), params);
    }

    // 6. Asistencias 'Presente' registradas en sesiones grupales
    private int contarAsistencias(Connection con, Date desde, Date hasta, Integer idTutor,
                                  Integer idCarrera, Integer idCuatrimestre, Integer idLetraGrupo, String matricula) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) AS TOTAL " +
                        "FROM ASISTENCIA asi " +
                        "JOIN SESION_GRUPAL sg ON sg.ID_SESION_GRUPAL = asi.ID_SESION_GRUPAL " +
                        "JOIN ALUMNO a ON a.MATRICULA = asi.MATRICULA " +
                        "WHERE asi.ESTATUS_ASISTENCIA = 'Presente' AND sg.FECHA BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        if (idTutor != null) {
            sql.append(" AND sg.ID_TUTOR = ? ");
            params.add(idTutor);
        }
        if (idCarrera != null) {
            sql.append(" AND a.ID_CARRERA = ? ");
            params.add(idCarrera);
        }
        if (idCuatrimestre != null) {
            sql.append(" AND sg.ID_CUATRIMESTRE = ? ");
            params.add(idCuatrimestre);
        }
        if (idLetraGrupo != null) {
            sql.append(" AND sg.ID_LETRA_GRUPO = ? ");
            params.add(idLetraGrupo);
        }
        // Filtro por alumno removido de Reportes Globales:
        // if (matricula != null && !matricula.isBlank()) {
        //     sql.append(" AND TRIM(a.MATRICULA) = ? ");
        //     params.add(matricula.trim());
        // }

        return ejecutarConteo(con, sql.toString(), params);
    }

    // 7. Distribución de canalizados por área de apoyo (gráfica de pastel)
    private Map<String, Integer> distribucionPorArea(Connection con, Date desde, Date hasta, Integer idTutor,
                                                     Integer idCarrera, Integer idCuatrimestre, Integer idLetraGrupo, String matricula) throws SQLException {
        Map<String, Integer> distribucion = new LinkedHashMap<>();

        StringBuilder sql = new StringBuilder(
                "SELECT ar.NOMBRE AS NOMBRE_AREA, COUNT(DISTINCT a.MATRICULA) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN CANALIZACION c ON c.MATRICULA = a.MATRICULA " +
                        "JOIN AREA_APOYO ar ON ar.ID_AREA = c.ID_AREA " +
                        "WHERE c.FECHA_CANALIZACION BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, idCuatrimestre, idLetraGrupo, matricula);
        sql.append(" GROUP BY ar.NOMBRE ORDER BY TOTAL DESC");

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            aplicarParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    distribucion.put(rs.getString("NOMBRE_AREA"), rs.getInt("TOTAL"));
                }
            }
        }

        return distribucion;
    }

    // 8. Listado detallado de canalizaciones (para la tabla de Reportes, no solo el conteo
    // agregado que ya da distribucionPorArea). Reutiliza agregarFiltrosAlumno igual que el
    // resto de los conteos, asi que respeta los mismos filtros de tutor/carrera/cuatrimestre/grupo.
    private List<Canalizacion> listarCanalizaciones(Connection con, Date desde, Date hasta, Integer idTutor,
                                                    Integer idCarrera, Integer idCuatrimestre, Integer idLetraGrupo, String matricula) throws SQLException {
        List<Canalizacion> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT c.ID_CANALIZACION, c.MATRICULA, c.FECHA_CANALIZACION, c.ESTATUS, c.OBSERVACIONES, " +
                        "ar.NOMBRE AS NOMBRE_AREA, m.NOMBRE_MOTIVO " +
                        "FROM ALUMNO a " +
                        "JOIN CANALIZACION c ON c.MATRICULA = a.MATRICULA " +
                        "JOIN AREA_APOYO ar ON ar.ID_AREA = c.ID_AREA " +
                        "LEFT JOIN MOTIVO_AREA m ON m.ID_MOTIVO = c.ID_MOTIVO " +
                        "WHERE c.FECHA_CANALIZACION BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, idCuatrimestre, idLetraGrupo, matricula);
        sql.append(" ORDER BY c.FECHA_CANALIZACION DESC");

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            aplicarParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Canalizacion c = new Canalizacion();
                    c.setIdCanalizacion(rs.getInt("ID_CANALIZACION"));
                    c.setMatricula(rs.getString("MATRICULA"));
                    c.setFechaCanalizacion(rs.getDate("FECHA_CANALIZACION"));
                    c.setEstatus(rs.getString("ESTATUS"));
                    c.setObservaciones(rs.getString("OBSERVACIONES"));
                    c.setNombreArea(rs.getString("NOMBRE_AREA"));
                    c.setNombreMotivo(rs.getString("NOMBRE_MOTIVO"));
                    lista.add(c);
                }
            }
        }

        return lista;
    }

    // 9. Seguimiento de Tutorías Grupales (modal de la tarjeta "Tutorías Grupales" en
    // Reportes Globales): avance de cada tutor-grupo dentro del periodo escolar vigente,
    // ordenado de MENOR a MAYOR numero de tutorias realizadas (los rezagados primero).
    public List<SeguimientoTutorGrupalDTO> listarSeguimientoTutoriasGrupales(PeriodoEscolar periodoVigente,
                                                                             Integer idTutor, Integer idCarrera,
                                                                             Integer idCuatrimestre, Integer idLetraGrupo) {
        List<SeguimientoTutorGrupalDTO> lista = new ArrayList<>();
        if (periodoVigente == null) {
            return lista;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT t.ID_TUTOR, t.NOMBRES, t.APELLIDOS, t.CORREO_INSTITUCIONAL, " +
                        "a.ID_CARRERA, car.NOMBRE AS NOMBRE_CARRERA, " +
                        "a.ID_CUATRIMESTRE, cu.NUMERO AS NUMERO_CUATRI, " +
                        "a.ID_LETRA_GRUPO, lg.LETRA, " +
                        "(SELECT COUNT(*) FROM SESION_GRUPAL sg " +
                        " WHERE sg.ID_TUTOR = a.ID_TUTOR AND sg.ID_CARRERA = a.ID_CARRERA " +
                        " AND sg.ID_CUATRIMESTRE = a.ID_CUATRIMESTRE AND sg.ID_LETRA_GRUPO = a.ID_LETRA_GRUPO " +
                        " AND sg.ESTADO = 'Tomada' AND sg.FECHA BETWEEN ? AND ?) AS REALIZADAS " +
                        "FROM ASIGNACION_TUTOR a " +
                        "JOIN TUTOR t ON t.ID_TUTOR = a.ID_TUTOR " +
                        "JOIN CARRERA car ON car.ID_CARRERA = a.ID_CARRERA " +
                        "JOIN CUATRIMESTRE cu ON cu.ID_CUATRIMESTRE = a.ID_CUATRIMESTRE " +
                        "JOIN LETRA_GRUPO lg ON lg.ID_LETRA = a.ID_LETRA_GRUPO " +
                        "WHERE a.ACTIVO = 'S' AND a.ID_PERIODO = ? ");

        List<Object> params = new ArrayList<>();
        params.add(periodoVigente.getFechaInicio());
        params.add(periodoVigente.getFechaFin());
        params.add(periodoVigente.getIdPeriodo());

        if (idTutor != null) {
            sql.append(" AND a.ID_TUTOR = ? ");
            params.add(idTutor);
        }
        if (idCarrera != null) {
            sql.append(" AND a.ID_CARRERA = ? ");
            params.add(idCarrera);
        }
        if (idCuatrimestre != null) {
            sql.append(" AND a.ID_CUATRIMESTRE = ? ");
            params.add(idCuatrimestre);
        }
        if (idLetraGrupo != null) {
            sql.append(" AND a.ID_LETRA_GRUPO = ? ");
            params.add(idLetraGrupo);
        }
        sql.append(" ORDER BY REALIZADAS ASC, t.APELLIDOS ASC");

        int objetivo = periodoVigente.getObjetivoTutoriasGrupales();
        int esperadasAlDia = calcularAvanceEsperado(periodoVigente, objetivo);

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            aplicarParametros(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SeguimientoTutorGrupalDTO dto = new SeguimientoTutorGrupalDTO();
                    dto.setIdTutor(rs.getInt("ID_TUTOR"));
                    dto.setNombreTutor(rs.getString("NOMBRES") + " " + rs.getString("APELLIDOS"));
                    dto.setCorreoTutor(rs.getString("CORREO_INSTITUCIONAL"));
                    dto.setIdCarrera(rs.getInt("ID_CARRERA"));
                    dto.setIdCuatrimestre(rs.getInt("ID_CUATRIMESTRE"));
                    dto.setIdLetraGrupo(rs.getInt("ID_LETRA_GRUPO"));
                    dto.setGrupoAsignado(rs.getString("NOMBRE_CARRERA") + " - " + rs.getInt("NUMERO_CUATRI") + "° " + rs.getString("LETRA"));
                    int realizadas = rs.getInt("REALIZADAS");
                    dto.setRealizadas(realizadas);
                    dto.setObjetivo(objetivo);
                    dto.setEstatus(realizadas >= esperadasAlDia ? "Al día" : "En Riesgo");
                    lista.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar el seguimiento de tutorias grupales: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    // Cuantas tutorias grupales deberia llevar un tutor "al dia" segun cuanto ha
    // avanzado el periodo escolar (regla de tres sobre el objetivo del periodo).
    // Antes de iniciar el periodo nadie esta en riesgo; al terminarlo, se exige el objetivo completo.
    private int calcularAvanceEsperado(PeriodoEscolar periodo, int objetivo) {
        LocalDate inicio = periodo.getFechaInicio().toLocalDate();
        LocalDate fin = periodo.getFechaFin().toLocalDate();
        LocalDate hoy = LocalDate.now();

        long totalDias = ChronoUnit.DAYS.between(inicio, fin);
        if (totalDias <= 0) {
            return objetivo;
        }

        long diasTranscurridos = ChronoUnit.DAYS.between(inicio, hoy);
        diasTranscurridos = Math.max(0, Math.min(diasTranscurridos, totalDias));

        return (int) Math.ceil(objetivo * ((double) diasTranscurridos / totalDias));
    }
}