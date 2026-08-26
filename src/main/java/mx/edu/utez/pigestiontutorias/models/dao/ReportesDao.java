package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Canalizacion;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO de acceso a datos para la generación de reportes estadísticos de tutorías:
 * alumnos atendidos, solicitudes, canalizaciones, grupos atendidos y asistencias,
 * con filtros opcionales por tutor, carrera, cuatrimestre, letra de grupo y matrícula.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-07-30
 */
public class ReportesDao {

    /**
     * Estructura de resultado que agrupa los totales e información detallada
     * de un reporte generado por {@link #generarReporte}.
     */
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

    /**
     * Genera un reporte estadístico de tutorías para el rango de fechas y filtros indicados, sin filtrar por matrícula.
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar por tutor
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar por carrera
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar por cuatrimestre
     * @param letra la letra de grupo a filtrar, o {@code null}/vacío para no filtrar por letra
     * @param desde la fecha inicial del rango del reporte
     * @param hasta la fecha final del rango del reporte
     * @return el resumen del reporte con los totales, la distribución por área y el detalle de canalizaciones
     */
    public ReporteResumen generarReporte(Integer idTutor, Integer idCarrera, Integer cuatrimestre,
                                         String letra, LocalDate desde, LocalDate hasta) {
        return generarReporte(idTutor, idCarrera, cuatrimestre, letra, desde, hasta, null);
    }

    /**
     * Genera un reporte estadístico de tutorías para el rango de fechas, filtros y matrícula indicados.
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar por tutor
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar por carrera
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar por cuatrimestre
     * @param letra la letra de grupo a filtrar, o {@code null}/vacío para no filtrar por letra
     * @param desde la fecha inicial del rango del reporte
     * @param hasta la fecha final del rango del reporte
     * @param matricula la matrícula del alumno a filtrar, o {@code null}/vacía para no filtrar por alumno
     * @return el resumen del reporte con los totales, la distribución por área y el detalle de canalizaciones
     */
    public ReporteResumen generarReporte(Integer idTutor, Integer idCarrera, Integer cuatrimestre,
                                         String letra, LocalDate desde, LocalDate hasta, String matricula) {
        ReporteResumen reporte = new ReporteResumen();
        Date sqlDesde = Date.valueOf(desde);
        Date sqlHasta = Date.valueOf(hasta);

        try (Connection con = SQLConnector.getConnection()) {
            reporte.totalAtendidos = contarAlumnosAtendidos(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra, matricula);
            reporte.totalPidieronTutorias = contarPidieronTutorias(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra, matricula);
            reporte.totalCanalizados = contarCanalizados(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra, matricula);
            reporte.totalPendientes = contarPendientes(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra, matricula);
            reporte.totalGruposAtendidos = contarGruposAtendidos(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra, matricula);
            reporte.totalAsistencias = contarAsistencias(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra, matricula);
            reporte.distribucionCanalizados = distribucionPorArea(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra, matricula);
            reporte.canalizaciones = listarCanalizaciones(con, sqlDesde, sqlHasta, idTutor, idCarrera, cuatrimestre, letra, matricula);
        } catch (SQLException e) {
            System.err.println("Error al generar el reporte: " + e.getMessage());
            e.printStackTrace();
        }

        return reporte;
    }

    /**
     * Agrega a una consulta SQL las cláusulas de filtro opcionales por tutor asignado al grupo actual
     * del alumno, carrera/cuatrimestre/letra del grupo y matrícula, añadiendo sus parámetros en orden.
     * @param sql el StringBuilder de la consulta SQL a la que se agregan las cláusulas de filtro
     * @param params la lista de parámetros donde se agregan, en orden, los valores de los filtros aplicados
     * @param aliasAlumno el alias de la tabla ALUMNO usado en la consulta
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param matricula la matrícula a filtrar, o {@code null}/vacía para no filtrar
     */
    private void agregarFiltrosAlumno(StringBuilder sql, List<Object> params, String aliasAlumno,
                                      Integer idTutor, Integer idCarrera, Integer cuatrimestre, String letra, String matricula) {
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
        if (matricula != null && !matricula.isBlank()) {
            sql.append(" AND ").append(aliasAlumno).append(".MATRICULA = ? ");
            params.add(matricula);
        }
    }

    // Filtros opcionales de tutor/carrera/cuatrimestre/letra/matricula para las consultas sobre
    // CANALIZACION. A diferencia de agregarFiltrosAlumno (que exige una asignacion activa sobre
    // el grupo ACTUAL del alumno via ASIGNACION_TUTOR), el filtro de tutor aqui se resuelve
    // contra quien realmente registro la SESION_INDIVIDUAL (Programada o Espontanea) que origino
    // la canalizacion -- mismo criterio que ya usa CanalizacionDao.getCanalizacionesDetalladas
    // para el modal "Alumnos Canalizados". Con ASIGNACION_TUTOR, una canalizacion de una tutoria
    // espontanea atendida por un tutor distinto al asignado al grupo del alumno quedaba excluida
    // del conteo/grafica/exportacion, aunque si aparecia en ese modal. Se usa EXISTS (no JOIN)
    // para no duplicar filas si una canalizacion llegara a quedar referenciada por mas de una
    // sesion.
    /**
     * Agrega a una consulta SQL las cláusulas de filtro opcionales para reportes sobre CANALIZACION.
     * A diferencia de {@link #agregarFiltrosAlumno}, el filtro de tutor se resuelve contra quien
     * realmente registró la SESION_INDIVIDUAL que originó la canalización, no contra la asignación
     * actual del grupo del alumno.
     * @param sql el StringBuilder de la consulta SQL a la que se agregan las cláusulas de filtro
     * @param params la lista de parámetros donde se agregan, en orden, los valores de los filtros aplicados
     * @param aliasAlumno el alias de la tabla ALUMNO usado en la consulta
     * @param aliasCanalizacion el alias de la tabla CANALIZACION usado en la consulta
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param matricula la matrícula a filtrar, o {@code null}/vacía para no filtrar
     */
    private void agregarFiltrosCanalizacion(StringBuilder sql, List<Object> params, String aliasAlumno,
                                            String aliasCanalizacion, Integer idTutor, Integer idCarrera,
                                            Integer cuatrimestre, String letra, String matricula) {
        if (idTutor != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM SESION_INDIVIDUAL si " +
                    "WHERE si.ID_CANALIZACION = ").append(aliasCanalizacion).append(".ID_CANALIZACION AND si.ID_TUTOR = ?) ");
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
        if (matricula != null && !matricula.isBlank()) {
            sql.append(" AND ").append(aliasAlumno).append(".MATRICULA = ? ");
            params.add(matricula);
        }
    }

    /**
     * Ejecuta una consulta SQL de conteo, aplicando los parámetros indicados, y devuelve el valor de la columna TOTAL.
     * @param con la conexión abierta a la base de datos
     * @param sql la sentencia SQL de conteo a ejecutar, con una columna alias "TOTAL"
     * @param params la lista de parámetros a aplicar a la sentencia, en orden
     * @return el valor de TOTAL devuelto por la consulta, o 0 si no hay resultados
     * @throws SQLException si ocurre un error al ejecutar la consulta
     */
    private int ejecutarConteo(Connection con, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            aplicarParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("TOTAL") : 0;
            }
        }
    }

    /**
     * Aplica en orden una lista de parámetros heterogéneos (fecha, texto o entero) a un PreparedStatement.
     * @param ps el PreparedStatement al que se aplican los parámetros
     * @param params la lista de valores a aplicar, en el mismo orden que los marcadores "?" de la sentencia
     * @throws SQLException si ocurre un error al asignar alguno de los parámetros
     */
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

    /**
     * Cuenta los alumnos distintos con al menos una sesión individual completada dentro del rango de fechas,
     * aplicando los filtros opcionales indicados.
     * @param con la conexión abierta a la base de datos
     * @param desde la fecha inicial del rango
     * @param hasta la fecha final del rango
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param matricula la matrícula a filtrar, o {@code null}/vacía para no filtrar
     * @return el total de alumnos distintos atendidos
     * @throws SQLException si ocurre un error al ejecutar la consulta
     */
    private int contarAlumnosAtendidos(Connection con, Date desde, Date hasta, Integer idTutor,
                                       Integer idCarrera, Integer cuatrimestre, String letra, String matricula) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT a.MATRICULA) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN SESION_INDIVIDUAL si ON si.MATRICULA = a.MATRICULA " +
                        "WHERE si.ESTADO = 'Completado' AND si.FECHA BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, cuatrimestre, letra, matricula);

        return ejecutarConteo(con, sql.toString(), params);
    }

    /**
     * Cuenta los alumnos distintos que registraron al menos una solicitud de tutoría dentro del rango de fechas,
     * aplicando los filtros opcionales indicados.
     * @param con la conexión abierta a la base de datos
     * @param desde la fecha inicial del rango
     * @param hasta la fecha final del rango
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param matricula la matrícula a filtrar, o {@code null}/vacía para no filtrar
     * @return el total de alumnos distintos que solicitaron tutoría
     * @throws SQLException si ocurre un error al ejecutar la consulta
     */
    private int contarPidieronTutorias(Connection con, Date desde, Date hasta, Integer idTutor,
                                       Integer idCarrera, Integer cuatrimestre, String letra, String matricula) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT a.MATRICULA) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN SOLICITUD_TUTORIA s ON s.MATRICULA = a.MATRICULA " +
                        "WHERE TRUNC(s.FECHA_REGISTRO) BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, cuatrimestre, letra, matricula);

        return ejecutarConteo(con, sql.toString(), params);
    }

    /**
     * Cuenta los alumnos distintos canalizados a un área de apoyo dentro del rango de fechas,
     * aplicando los filtros opcionales indicados.
     * @param con la conexión abierta a la base de datos
     * @param desde la fecha inicial del rango
     * @param hasta la fecha final del rango
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param matricula la matrícula a filtrar, o {@code null}/vacía para no filtrar
     * @return el total de alumnos distintos canalizados
     * @throws SQLException si ocurre un error al ejecutar la consulta
     */
    private int contarCanalizados(Connection con, Date desde, Date hasta, Integer idTutor,
                                  Integer idCarrera, Integer cuatrimestre, String letra, String matricula) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT a.MATRICULA) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN CANALIZACION c ON c.MATRICULA = a.MATRICULA " +
                        "WHERE TRUNC(c.FECHA_CANALIZACION) BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosCanalizacion(sql, params, "a", "c", idTutor, idCarrera, cuatrimestre, letra, matricula);

        return ejecutarConteo(con, sql.toString(), params);
    }

    /**
     * Cuenta las solicitudes de tutoría con estatus "Pendiente" registradas dentro del rango de fechas,
     * aplicando los filtros opcionales indicados.
     * @param con la conexión abierta a la base de datos
     * @param desde la fecha inicial del rango
     * @param hasta la fecha final del rango
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param matricula la matrícula a filtrar, o {@code null}/vacía para no filtrar
     * @return el total de solicitudes pendientes
     * @throws SQLException si ocurre un error al ejecutar la consulta
     */
    private int contarPendientes(Connection con, Date desde, Date hasta, Integer idTutor,
                                 Integer idCarrera, Integer cuatrimestre, String letra, String matricula) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN SOLICITUD_TUTORIA s ON s.MATRICULA = a.MATRICULA " +
                        "WHERE s.ESTATUS = 'Pendiente' AND TRUNC(s.FECHA_REGISTRO) BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosAlumno(sql, params, "a", idTutor, idCarrera, cuatrimestre, letra, matricula);

        return ejecutarConteo(con, sql.toString(), params);
    }

    /**
     * Cuenta las sesiones grupales completadas dentro del rango de fechas, aplicando los filtros
     * opcionales indicados (aplicados directamente sobre SESION_GRUPAL y el grupo del alumno).
     * @param con la conexión abierta a la base de datos
     * @param desde la fecha inicial del rango
     * @param hasta la fecha final del rango
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param matricula la matrícula de un alumno del grupo a filtrar, o {@code null}/vacía para no filtrar
     * @return el total de sesiones grupales completadas
     * @throws SQLException si ocurre un error al ejecutar la consulta
     */
    private int contarGruposAtendidos(Connection con, Date desde, Date hasta, Integer idTutor,
                                      Integer idCarrera, Integer cuatrimestre, String letra, String matricula) throws SQLException {
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
        if (matricula != null && !matricula.isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM ALUMNO au WHERE au.ID_GRUPO = sg.ID_GRUPO AND au.MATRICULA = ?) ");
            params.add(matricula);
        }

        return ejecutarConteo(con, sql.toString(), params);
    }

    /**
     * Cuenta los registros de asistencia con estatus "Presente" en sesiones grupales dentro del rango
     * de fechas, aplicando los filtros opcionales indicados.
     * @param con la conexión abierta a la base de datos
     * @param desde la fecha inicial del rango
     * @param hasta la fecha final del rango
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param matricula la matrícula a filtrar, o {@code null}/vacía para no filtrar
     * @return el total de asistencias registradas como presente
     * @throws SQLException si ocurre un error al ejecutar la consulta
     */
    private int contarAsistencias(Connection con, Date desde, Date hasta, Integer idTutor,
                                  Integer idCarrera, Integer cuatrimestre, String letra, String matricula) throws SQLException {
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
        if (matricula != null && !matricula.isBlank()) {
            sql.append(" AND asi.MATRICULA = ? ");
            params.add(matricula);
        }

        return ejecutarConteo(con, sql.toString(), params);
    }

    /**
     * Calcula la distribución de alumnos canalizados por área de apoyo dentro del rango de fechas,
     * aplicando los filtros opcionales indicados, ordenada de mayor a menor total.
     * @param con la conexión abierta a la base de datos
     * @param desde la fecha inicial del rango
     * @param hasta la fecha final del rango
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param matricula la matrícula a filtrar, o {@code null}/vacía para no filtrar
     * @return un mapa ordenado del nombre del área hacia el total de alumnos canalizados a ella
     * @throws SQLException si ocurre un error al ejecutar la consulta
     */
    private Map<String, Integer> distribucionPorArea(Connection con, Date desde, Date hasta, Integer idTutor,
                                                     Integer idCarrera, Integer cuatrimestre, String letra, String matricula) throws SQLException {
        Map<String, Integer> distribucion = new LinkedHashMap<>();

        StringBuilder sql = new StringBuilder(
                "SELECT ar.NOMBRE AS NOMBRE_AREA, COUNT(DISTINCT a.MATRICULA) AS TOTAL " +
                        "FROM ALUMNO a " +
                        "JOIN CANALIZACION c ON c.MATRICULA = a.MATRICULA " +
                        "JOIN AREA_APOYO ar ON ar.ID_AREA = c.ID_AREA " +
                        "WHERE TRUNC(c.FECHA_CANALIZACION) BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosCanalizacion(sql, params, "a", "c", idTutor, idCarrera, cuatrimestre, letra, matricula);
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

    /**
     * Obtiene el detalle de las canalizaciones registradas dentro del rango de fechas, con área y
     * motivo, aplicando los filtros opcionales indicados, ordenadas por fecha descendente.
     * @param con la conexión abierta a la base de datos
     * @param desde la fecha inicial del rango
     * @param hasta la fecha final del rango
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param matricula la matrícula a filtrar, o {@code null}/vacía para no filtrar
     * @return la lista de canalizaciones encontradas, con nombre de área y motivo incluidos
     * @throws SQLException si ocurre un error al ejecutar la consulta
     */
    private List<Canalizacion> listarCanalizaciones(Connection con, Date desde, Date hasta, Integer idTutor,
                                                    Integer idCarrera, Integer cuatrimestre, String letra, String matricula) throws SQLException {
        List<Canalizacion> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT c.ID_CANALIZACION, c.MATRICULA, c.FECHA_CANALIZACION, c.ESTATUS, c.OBSERVACIONES, " +
                        "ar.NOMBRE AS NOMBRE_AREA, m.NOMBRE AS NOMBRE_MOTIVO " +
                        "FROM ALUMNO a " +
                        "JOIN CANALIZACION c ON c.MATRICULA = a.MATRICULA " +
                        "JOIN AREA_APOYO ar ON ar.ID_AREA = c.ID_AREA " +
                        "LEFT JOIN MOTIVO_AREA m ON m.ID_MOTIVO = c.ID_MOTIVO " +
                        "WHERE TRUNC(c.FECHA_CANALIZACION) BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);
        agregarFiltrosCanalizacion(sql, params, "a", "c", idTutor, idCarrera, cuatrimestre, letra, matricula);
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
