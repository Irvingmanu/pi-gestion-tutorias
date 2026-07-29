package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.ReporteTutor;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReporteDao {

    public ReporteTutor obtenerReporte(int idTutor, Integer idCuatrimestre, Integer idLetraGrupo, Integer idCarrera) {
        ReporteTutor r = new ReporteTutor();
        r.setAlumnosAtendidos(contarAlumnosAtendidos(idTutor, idCuatrimestre, idLetraGrupo, idCarrera));
        r.setCanalizaciones(contarCanalizaciones(idTutor, idCuatrimestre, idLetraGrupo, idCarrera));
        r.setGruposAtendidos(contarGruposAtendidos(idTutor, idCuatrimestre, idLetraGrupo));
        r.setAsistencias(contarAsistencias(idTutor, idCuatrimestre, idLetraGrupo));
        return r;
    }

    private int contarAlumnosAtendidos(int idTutor, Integer idCuatrimestre, Integer idLetraGrupo, Integer idCarrera) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT si.MATRICULA) " +
                        "FROM SESION_INDIVIDUAL si " +
                        "JOIN ALUMNO a ON a.MATRICULA = si.MATRICULA " +
                        "WHERE si.ID_TUTOR = ?"
        );
        if (idCuatrimestre != null) sql.append(" AND a.ID_CUATRIMESTRE = ?");
        if (idLetraGrupo != null) sql.append(" AND a.ID_LETRA_GRUPO = ?");
        if (idCarrera != null) sql.append(" AND a.ID_CARRERA = ?");

        return ejecutarConteo(sql.toString(), idTutor, idCuatrimestre, idLetraGrupo, idCarrera);
    }

    private int contarCanalizaciones(int idTutor, Integer idCuatrimestre, Integer idLetraGrupo, Integer idCarrera) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) " +
                        "FROM SESION_INDIVIDUAL si " +
                        "JOIN CANALIZACION c ON c.ID_CANALIZACION = si.ID_CANALIZACION " +
                        "JOIN ALUMNO a ON a.MATRICULA = si.MATRICULA " +
                        "WHERE si.ID_TUTOR = ? AND si.ID_CANALIZACION IS NOT NULL"
        );
        if (idCuatrimestre != null) sql.append(" AND a.ID_CUATRIMESTRE = ?");
        if (idLetraGrupo != null) sql.append(" AND a.ID_LETRA_GRUPO = ?");
        if (idCarrera != null) sql.append(" AND a.ID_CARRERA = ?");

        return ejecutarConteo(sql.toString(), idTutor, idCuatrimestre, idLetraGrupo, idCarrera);
    }

    private int contarGruposAtendidos(int idTutor, Integer idCuatrimestre, Integer idLetraGrupo) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT ID_LETRA_GRUPO || '-' || ID_CUATRIMESTRE) " +
                        "FROM SESION_GRUPAL " +
                        "WHERE ID_TUTOR = ?"
        );
        if (idCuatrimestre != null) sql.append(" AND ID_CUATRIMESTRE = ?");
        if (idLetraGrupo != null) sql.append(" AND ID_LETRA_GRUPO = ?");

        return ejecutarConteo(sql.toString(), idTutor, idCuatrimestre, idLetraGrupo, null);
    }

    private int contarAsistencias(int idTutor, Integer idCuatrimestre, Integer idLetraGrupo) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) " +
                        "FROM ASISTENCIA ast " +
                        "JOIN SESION_GRUPAL sg ON sg.ID_SESION_GRUPAL = ast.ID_SESION_GRUPAL " +
                        "WHERE sg.ID_TUTOR = ?"
        );
        if (idCuatrimestre != null) sql.append(" AND sg.ID_CUATRIMESTRE = ?");
        if (idLetraGrupo != null) sql.append(" AND sg.ID_LETRA_GRUPO = ?");

        return ejecutarConteo(sql.toString(), idTutor, idCuatrimestre, idLetraGrupo, null);
    }

    private int ejecutarConteo(String sql, int idTutor, Integer idCuatrimestre, Integer idLetraGrupo, Integer idCarrera) {
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int idx = 1;
            ps.setInt(idx++, idTutor);
            if (idCuatrimestre != null) ps.setInt(idx++, idCuatrimestre);
            if (idLetraGrupo != null) ps.setInt(idx++, idLetraGrupo);
            if (idCarrera != null) ps.setInt(idx++, idCarrera);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}