package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// ALUMNO ya no tiene ID_ALUMNO surrogate ni ID_CARRERA/ID_CUATRIMESTRE/ID_LETRA_GRUPO
// propios: todo se resuelve via ID_GRUPO, y MATRICULA es la unica PK/identificador.
public class AsistenciaGrupalDao {

    public List<Alumno> getAlumnosPorGrupo(int idGrupo) {
        List<Alumno> lista = new ArrayList<>();
        String sql = "SELECT * FROM ALUMNO WHERE ID_GRUPO = ? AND ESTADO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idGrupo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Alumno alumno = new Alumno();
                    alumno.setMatricula(rs.getString("MATRICULA"));
                    alumno.setNombres(rs.getString("NOMBRES"));
                    alumno.setApellidoPaterno(rs.getString("APELLIDO_PATERNO"));
                    alumno.setApellidoMaterno(rs.getString("APELLIDO_MATERNO"));
                    alumno.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
                    alumno.setTelefono(rs.getString("TELEFONO"));
                    alumno.setIdGenero(rs.getInt("ID_GENERO"));
                    alumno.setIdGrupo(rs.getInt("ID_GRUPO"));
                    alumno.setEstado(rs.getString("ESTADO"));
                    lista.add(alumno);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean registrarAsistenciaGrupal(int idGrupo, String fecha, String[] matriculasAsistentes, int idTutor) {
        String sqlBuscarSesion = "SELECT ID_SESION_GRUPAL FROM SESION_GRUPAL WHERE TRUNC(FECHA) = TO_DATE(?, 'YYYY-MM-DD') AND ID_GRUPO = ?";
        String sqlCrearSesion = "INSERT INTO SESION_GRUPAL (FECHA, ID_GRUPO, ID_TUTOR, ESTADO, TEMAS_TRATADOS, ACUERDOS) VALUES (TO_DATE(?, 'YYYY-MM-DD'), ?, ?, 'Completado', 'N/A', 'N/A')";
        String deleteSql = "DELETE FROM ASISTENCIA WHERE ID_SESION_GRUPAL = ?";
        String insertSql = "INSERT INTO ASISTENCIA (ID_SESION_GRUPAL, MATRICULA, ESTATUS_ASISTENCIA) VALUES (?, ?, ?)";

        try (Connection con = SQLConnector.getConnection()) {
            con.setAutoCommit(false);

            int idSesionGrupal = -1;

            try (PreparedStatement psSesion = con.prepareStatement(sqlBuscarSesion)) {
                psSesion.setString(1, fecha);
                psSesion.setInt(2, idGrupo);
                try (ResultSet rs = psSesion.executeQuery()) {
                    if (rs.next()) {
                        idSesionGrupal = rs.getInt("ID_SESION_GRUPAL");
                    }
                }
            }

            if (idSesionGrupal == -1) {
                try (PreparedStatement psInsSesion = con.prepareStatement(sqlCrearSesion, new String[]{"ID_SESION_GRUPAL"})) {
                    psInsSesion.setString(1, fecha);
                    psInsSesion.setInt(2, idGrupo);
                    psInsSesion.setInt(3, idTutor);
                    psInsSesion.executeUpdate();

                    try (ResultSet rsGen = psInsSesion.getGeneratedKeys()) {
                        if (rsGen.next()) {
                            idSesionGrupal = rsGen.getInt(1);
                        }
                    }
                }
            }

            if (idSesionGrupal != -1) {
                try (PreparedStatement psDel = con.prepareStatement(deleteSql)) {
                    psDel.setInt(1, idSesionGrupal);
                    psDel.executeUpdate();
                }

                if (matriculasAsistentes != null && matriculasAsistentes.length > 0) {
                    try (PreparedStatement psIns = con.prepareStatement(insertSql)) {
                        for (String matricula : matriculasAsistentes) {
                            if (matricula == null || matricula.isBlank()) {
                                continue;
                            }
                            psIns.setInt(1, idSesionGrupal);
                            psIns.setString(2, matricula.trim());
                            psIns.setString(3, "Presente");
                            psIns.addBatch();
                        }
                        psIns.executeBatch();
                    }
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAlumnosConAsistencia(int idGrupo, String fecha) {
        List<String> matriculasAsistentes = new ArrayList<>();

        String sql = "SELECT a.MATRICULA " +
                "FROM ASISTENCIA a " +
                "JOIN SESION_GRUPAL sg ON a.ID_SESION_GRUPAL = sg.ID_SESION_GRUPAL " +
                "WHERE sg.ID_GRUPO = ? " +
                "AND TRUNC(sg.FECHA) = TO_DATE(?, 'YYYY-MM-DD') " +
                "AND a.ESTATUS_ASISTENCIA = 'Presente'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idGrupo);
            ps.setString(2, fecha);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    matriculasAsistentes.add(rs.getString("MATRICULA"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matriculasAsistentes;
    }
}
