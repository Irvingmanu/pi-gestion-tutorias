package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Canalizacion;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Los filtros de Carrera/Cuatrimestre/Letra ya no son 3 FKs sueltas en ALUMNO/SESION_GRUPAL:
// ahora se resuelven contra GRUPO (via ALUMNO.ID_GRUPO / SESION_GRUPAL.ID_GRUPO).
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

    public ReporteResumen generarReporte(Integer idTutor, Integer idCarrera, Integer cuatrimestre,
                                         String letra, LocalDate desde, LocalDate hasta) {
        ReporteResumen reporte = new ReporteResumen();
        Date sqlDesde = Date.valueOf(desde);
        Date sqlHasta = Date.valueOf(hasta);

        try (Connection con = SQLConnector.getConnection()) {
            reporte.totalAtendidos = contarAlumnosAtendidos(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra);
            reporte.totalPidieronTutorias = contarPidieronTutorias(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra);
            reporte.totalCanalizados = contarCanalizados(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra);
            reporte.totalPendientes = contarPendientes(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra);
            reporte.totalGruposAtendidos = contarGruposAtendidos(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra);
            reporte.totalAsistencias = contarAsistencias(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra);
            reporte.distribucionCanalizados = distribucionPorArea(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra);
            reporte.canalizaciones = listarCanalizaciones(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra);
        } catch (SQLException e) {
            System.err.println("Error al generar el reporte: " + e.getMessage());
            e.printStackTrace();
        }

        return reporte;
    }

    // Agrega los filtros opcionales de tutor/carrera/cuatrimestre/letra sobre un alias de
    // ALUMNO que ya trae ID_GRUPO. El filtro de tutor exige una asignacion activa sobre
    // ese mismo grupo; carrera/cuatrimestre/letra se validan contra GRUPO.
    private void agregarFiltrosAlumno(StringBuilder sql, List<Object> params, String aliasAlumno,
                                      Integer idTutor, Integer idCarrera, Integer cuatrimestre, String letra) {
        if (idTutor != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM ASIGNACION_TUTOR asg " +
                    "WHERE asg.ID_TUTOR = ? AND asg.ESTADO = 'S' AND asg.ID_GRUPO = ").append(aliasAlumno).append(".ID_GRUPO) ");
            params.add(idTutor);
        }
        if (idCarrera != null || cuatrimestre != null || (letra != null && !letra.isBlank())) {
            sql.append(" AND EXISTS (SELECT 1 FROM GRUPO g2 WHERE g2.ID_GRUPO = ").append(aliasAlumno).append(".ID_GRUPO ");
            if (idCarrera != null) {
                sql.append(" AND g2.ID_CARRERA = ? ");
                params.add(idCarrera);
            }
            if (cuatrimestre != null) {
                sql.append(" AND g2.CUATRIMESTRE = ? ");
                params.add(cuatrimestre);
            }
            if (letra != null && !letra.isBlank()) {
                sql.append(" AND g2.LETRA = ? ");
                params.add(letra);
            }
            sql.append(") ");
        }
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

    // 1. Alumnos atendidos: al menos una sesión individual "Completado"
    private int contarAlumnosAtendidos(Connection con, Date desde, Date hasta, Integer idTutor,
                                       Integer idCarrera, Integer cuatrimestre, String letra) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT a.MATRICULA) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN SESION_INDIVIDUAL si ON si.MATRICULA = a.MATRICULA " +
                        "WHERE si.ESTADO = 'Completado' AND si.FECHA BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, cuatrimestre, letra);

        return ejecutarConteo(con, sql.toString(), params);
    }

    // 2. Alumnos que pidieron tutoría: al menos una solicitud registrada
    private int contarPidieronTutorias(Connection con, Date desde, Date hasta, Integer idTutor,
                                       Integer idCarrera, Integer cuatrimestre, String letra) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT a.MATRICULA) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN SOLICITUD_TUTORIA s ON s.MATRICULA = a.MATRICULA " +
                        "WHERE TRUNC(s.FECHA_REGISTRO) BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, cuatrimestre, letra);

        return ejecutarConteo(con, sql.toString(), params);
    }

    // 3. Alumnos canalizados a algún área de apoyo
    private int contarCanalizados(Connection con, Date desde, Date hasta, Integer idTutor,
                                  Integer idCarrera, Integer cuatrimestre, String letra) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT a.MATRICULA) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN CANALIZACION c ON c.MATRICULA = a.MATRICULA " +
                        "WHERE c.FECHA_CANALIZACION BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, cuatrimestre, letra);

        return ejecutarConteo(con, sql.toString(), params);
    }

    // 4. Solicitudes pendientes de responder
    private int contarPendientes(Connection con, Date desde, Date hasta, Integer idTutor,
                                 Integer idCarrera, Integer cuatrimestre, String letra) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN SOLICITUD_TUTORIA s ON s.MATRICULA = a.MATRICULA " +
                        "WHERE s.ESTATUS = 'Pendiente' AND TRUNC(s.FECHA_REGISTRO) BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, cuatrimestre, letra);

        return ejecutarConteo(con, sql.toString(), params);
    }

    // 5. Sesiones grupales completadas
    private int contarGruposAtendidos(Connection con, Date desde, Date hasta, Integer idTutor,
                                      Integer idCarrera, Integer cuatrimestre, String letra) throws SQLException {
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
        if (idCarrera != null || cuatrimestre != null || (letra != null && !letra.isBlank())) {
            sql.append(" AND EXISTS (SELECT 1 FROM GRUPO g2 WHERE g2.ID_GRUPO = sg.ID_GRUPO ");
            if (idCarrera != null) {
                sql.append(" AND g2.ID_CARRERA = ? ");
                params.add(idCarrera);
            }
            if (cuatrimestre != null) {
                sql.append(" AND g2.CUATRIMESTRE = ? ");
                params.add(cuatrimestre);
            }
            if (letra != null && !letra.isBlank()) {
                sql.append(" AND g2.LETRA = ? ");
                params.add(letra);
            }
            sql.append(") ");
        }

        return ejecutarConteo(con, sql.toString(), params);
    }

    // 6. Asistencias 'Presente' registradas en sesiones grupales
    private int contarAsistencias(Connection con, Date desde, Date hasta, Integer idTutor,
                                  Integer idCarrera, Integer cuatrimestre, String letra) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) AS TOTAL " +
                        "FROM ASISTENCIA asi " +
                        "JOIN SESION_GRUPAL sg ON sg.ID_SESION_GRUPAL = asi.ID_SESION_GRUPAL " +
                        "WHERE asi.ESTATUS_ASISTENCIA = 'Presente' AND sg.FECHA BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        if (idTutor != null) {
            sql.append(" AND sg.ID_TUTOR = ? ");
            params.add(idTutor);
        }
        if (idCarrera != null || cuatrimestre != null || (letra != null && !letra.isBlank())) {
            sql.append(" AND EXISTS (SELECT 1 FROM GRUPO g2 WHERE g2.ID_GRUPO = sg.ID_GRUPO ");
            if (idCarrera != null) {
                sql.append(" AND g2.ID_CARRERA = ? ");
                params.add(idCarrera);
            }
            if (cuatrimestre != null) {
                sql.append(" AND g2.CUATRIMESTRE = ? ");
                params.add(cuatrimestre);
            }
            if (letra != null && !letra.isBlank()) {
                sql.append(" AND g2.LETRA = ? ");
                params.add(letra);
            }
            sql.append(") ");
        }

        return ejecutarConteo(con, sql.toString(), params);
    }

    // 7. Distribución de canalizados por área de apoyo (gráfica de pastel)
    private Map<String, Integer> distribucionPorArea(Connection con, Date desde, Date hasta, Integer idTutor,
                                                     Integer idCarrera, Integer cuatrimestre, String letra) throws SQLException {
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
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, cuatrimestre, letra);
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
    // agregado que ya da distribucionPorArea).
    private List<Canalizacion> listarCanalizaciones(Connection con, Date desde, Date hasta, Integer idTutor,
                                                     Integer idCarrera, Integer cuatrimestre, String letra) throws SQLException {
        List<Canalizacion> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT c.ID_CANALIZACION, c.MATRICULA, c.FECHA_CANALIZACION, c.ESTATUS, c.OBSERVACIONES, " +
                        "ar.NOMBRE AS NOMBRE_AREA, m.NOMBRE AS NOMBRE_MOTIVO " +
                        "FROM ALUMNO a " +
                        "JOIN CANALIZACION c ON c.MATRICULA = a.MATRICULA " +
                        "JOIN AREA_APOYO ar ON ar.ID_AREA = c.ID_AREA " +
                        "LEFT JOIN MOTIVO_AREA m ON m.ID_MOTIVO = c.ID_MOTIVO " +
                        "WHERE c.FECHA_CANALIZACION BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, cuatrimestre, letra);
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
}
