package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.SesionGrupal;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SesionGrupalDao implements Dao<SesionGrupal, Integer> {

    // Inserta la SESION_GRUPAL y, con el ID generado, la ASISTENCIA de cada matricula
    // recibida en entidad.getAsistentes() (checkboxes marcados en el registro). Todo en
    // una sola transaccion: si falla la asistencia, tampoco debe quedar la sesion huerfana.
    @Override
    public boolean create(SesionGrupal entidad) {
        String sqlSesion = "INSERT INTO SESION_GRUPAL " +
                "(ID_GRUPO, ID_TUTOR, FECHA, HORA, TEMAS_TRATADOS, ACUERDOS, ASESORIAS_GRUPALES, ESTADO) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlAsistencia = "INSERT INTO ASISTENCIA (ID_SESION_GRUPAL, MATRICULA, ESTATUS_ASISTENCIA) VALUES (?, ?, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idSesionGrupal;
            try (PreparedStatement psSesion = con.prepareStatement(sqlSesion, new String[]{"ID_SESION_GRUPAL"})) {
                psSesion.setInt(1, entidad.getIdGrupo());
                psSesion.setInt(2, entidad.getIdTutor());
                psSesion.setDate(3, entidad.getFecha());
                psSesion.setString(4, entidad.getHora());
                psSesion.setString(5, entidad.getTemasTratados());
                psSesion.setString(6, entidad.getAcuerdos());

                if (entidad.getAsesoriasGrupales() != null) {
                    psSesion.setString(7, entidad.getAsesoriasGrupales());
                } else {
                    psSesion.setNull(7, Types.CLOB);
                }

                psSesion.setString(8, entidad.getEstado() != null ? entidad.getEstado() : "Completado");

                if (psSesion.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }

                try (ResultSet keys = psSesion.getGeneratedKeys()) {
                    if (!keys.next()) {
                        con.rollback();
                        return false;
                    }
                    idSesionGrupal = keys.getInt(1);
                }
            }

            String[] asistentes = entidad.getAsistentes();
            if (asistentes != null && asistentes.length > 0) {
                try (PreparedStatement psAsistencia = con.prepareStatement(sqlAsistencia)) {
                    for (String matricula : asistentes) {
                        if (matricula == null || matricula.isBlank()) {
                            continue;
                        }
                        psAsistencia.setInt(1, idSesionGrupal);
                        psAsistencia.setString(2, matricula.trim());
                        psAsistencia.setString(3, "Presente");
                        psAsistencia.addBatch();
                    }
                    psAsistencia.executeBatch();
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    @Override
    public List<SesionGrupal> getAll() {
        return null;
    }

    @Override
    public SesionGrupal getById(Integer id) {
        return null;
    }

    @Override
    public boolean update(SesionGrupal entidad) {
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        return false;
    }

    public List<SesionGrupal> getAcuerdosPorAlumno(String matricula) {
        List<SesionGrupal> lista = new ArrayList<>();
        String sql = "SELECT sg.* FROM SESION_GRUPAL sg " +
                "INNER JOIN ALUMNO a ON sg.ID_GRUPO = a.ID_GRUPO " +
                "WHERE a.MATRICULA = ? AND sg.ESTADO = 'Completado' ORDER BY sg.FECHA DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, matricula);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSesion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Historial: sesiones grupales YA REALIZADAS (Completado) del tutor, filtrables por
    // rango de fechas (cualquiera de los dos limites puede venir null/blank).
    public List<SesionGrupal> getHistorialByTutor(int idTutor, String fechaInicio, String fechaFin) {
        List<SesionGrupal> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM SESION_GRUPAL WHERE ID_TUTOR = ? AND ESTADO = 'Completado'");

        if (fechaInicio != null && !fechaInicio.isBlank()) {
            sql.append(" AND FECHA >= ?");
        }
        if (fechaFin != null && !fechaFin.isBlank()) {
            sql.append(" AND FECHA <= ?");
        }
        sql.append(" ORDER BY FECHA DESC, HORA DESC");

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setInt(idx++, idTutor);
            if (fechaInicio != null && !fechaInicio.isBlank()) {
                ps.setDate(idx++, Date.valueOf(fechaInicio));
            }
            if (fechaFin != null && !fechaFin.isBlank()) {
                ps.setDate(idx++, Date.valueOf(fechaFin));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSesion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private SesionGrupal mapearSesion(ResultSet rs) throws SQLException {
        SesionGrupal s = new SesionGrupal();
        s.setIdSesionGrupal(rs.getInt("ID_SESION_GRUPAL"));
        s.setIdGrupo(rs.getInt("ID_GRUPO"));
        s.setIdTutor(rs.getInt("ID_TUTOR"));
        s.setFecha(rs.getDate("FECHA"));
        s.setHora(rs.getString("HORA"));
        s.setTemasTratados(rs.getString("TEMAS_TRATADOS"));
        s.setAcuerdos(rs.getString("ACUERDOS"));
        s.setEstado(rs.getString("ESTADO"));
        return s;
    }
}
