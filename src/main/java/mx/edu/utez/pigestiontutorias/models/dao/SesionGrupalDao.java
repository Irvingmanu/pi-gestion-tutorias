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
                "(ID_CARRERA, ID_LETRA_GRUPO, ID_CUATRIMESTRE, ID_TUTOR, FECHA, HORA, TEMAS_TRATADOS, ACUERDOS, ASESORIAS_GRUPALES, ESTADO) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlAsistencia = "INSERT INTO ASISTENCIA (ID_SESION_GRUPAL, MATRICULA, ESTATUS_ASISTENCIA) VALUES (?, ?, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idSesionGrupal;
            try (PreparedStatement psSesion = con.prepareStatement(sqlSesion, new String[]{"ID_SESION_GRUPAL"})) {
                psSesion.setInt(1, entidad.getIdCarrera());
                psSesion.setInt(2, entidad.getIdLetraGrupo());
                psSesion.setInt(3, entidad.getIdCuatrimestre());
                psSesion.setInt(4, entidad.getIdTutor());
                psSesion.setDate(5, entidad.getFecha());
                psSesion.setString(6, entidad.getHora());
                psSesion.setString(7, entidad.getTemasTratados());
                psSesion.setString(8, entidad.getAcuerdos());

                if (entidad.getAsesoriasGrupales() != null) {
                    psSesion.setString(9, entidad.getAsesoriasGrupales());
                } else {
                    psSesion.setNull(9, Types.CLOB);
                }

                psSesion.setString(10, entidad.getEstado() != null ? entidad.getEstado() : "Tomada");

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
                "INNER JOIN ALUMNO a ON sg.ID_CARRERA = a.ID_CARRERA " +
                "AND sg.ID_CUATRIMESTRE = a.ID_CUATRIMESTRE AND sg.ID_LETRA_GRUPO = a.ID_LETRA_GRUPO " +
                "WHERE a.MATRICULA = ? AND sg.ESTADO = 'Tomada' ORDER BY sg.FECHA DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, matricula);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SesionGrupal s = new SesionGrupal();
                    s.setIdSesionGrupal(rs.getInt("ID_SESION_GRUPAL"));
                    s.setIdCarrera(rs.getInt("ID_CARRERA"));
                    s.setIdLetraGrupo(rs.getInt("ID_LETRA_GRUPO"));
                    s.setIdCuatrimestre(rs.getInt("ID_CUATRIMESTRE"));
                    s.setIdTutor(rs.getInt("ID_TUTOR"));
                    s.setFecha(rs.getDate("FECHA"));
                    s.setTemasTratados(rs.getString("TEMAS_TRATADOS"));
                    s.setAcuerdos(rs.getString("ACUERDOS"));
                    s.setEstado(rs.getString("ESTADO"));
                    lista.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
