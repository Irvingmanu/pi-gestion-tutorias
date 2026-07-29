package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AsistenciaGrupalDao {

    public List<Alumno> getAlumnosPorFiltros(int idLetraGrupo, int idCarrera, int idCuatrimestre) {
        List<Alumno> lista = new ArrayList<>();
        String sql = "SELECT * FROM ALUMNO WHERE ID_LETRA_GRUPO = ? AND ID_CARRERA = ? AND ID_CUATRIMESTRE = ? AND ACTIVO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idLetraGrupo);
            ps.setInt(2, idCarrera);
            ps.setInt(3, idCuatrimestre);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Alumno alumno = new Alumno();
                    alumno.setIdAlumno(rs.getInt("ID_ALUMNO"));
                    alumno.setMatricula(rs.getString("MATRICULA"));
                    alumno.setNombres(rs.getString("NOMBRES"));
                    alumno.setApellidos(rs.getString("APELLIDOS"));
                    alumno.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
                    alumno.setTelefono(rs.getString("TELEFONO"));
                    alumno.setIdGenero(rs.getInt("ID_GENERO"));
                    alumno.setIdCarrera(rs.getInt("ID_CARRERA"));
                    alumno.setIdCuatrimestre(rs.getInt("ID_CUATRIMESTRE"));
                    alumno.setIdLetraGrupo(rs.getInt("ID_LETRA_GRUPO"));
                    alumno.setIdUsuario(rs.getInt("ID_USUARIO"));
                    lista.add(alumno);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean registrarAsistenciaGrupal(String grupo, String carrera, String cuatrimestre, String fecha, String[] idsAlumnos, int idTutor) {
        String sqlBuscarSesion = "SELECT ID_SESION_GRUPAL FROM SESION_GRUPAL WHERE TRUNC(FECHA) = TO_DATE(?, 'YYYY-MM-DD') AND ID_LETRA_GRUPO = ? AND ID_CUATRIMESTRE = ?";
        String sqlCrearSesion = "INSERT INTO SESION_GRUPAL (FECHA, ID_LETRA_GRUPO, ID_CUATRIMESTRE, ID_TUTOR, ESTADO, TEMAS_TRATADOS, ACUERDOS) VALUES (TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?, 'Tomada', 'N/A', 'N/A')";
        String deleteSql = "DELETE FROM ASISTENCIA WHERE ID_SESION_GRUPAL = ?";
        String insertSql = "INSERT INTO ASISTENCIA (ID_SESION_GRUPAL, MATRICULA, ESTATUS_ASISTENCIA) VALUES (?, ?, ?)";

        try (Connection con = SQLConnector.getConnection()) {
            con.setAutoCommit(false);

            int idSesionGrupal = -1;

            try (PreparedStatement psSesion = con.prepareStatement(sqlBuscarSesion)) {
                psSesion.setString(1, fecha);
                psSesion.setInt(2, Integer.parseInt(grupo));
                psSesion.setInt(3, Integer.parseInt(cuatrimestre));
                try (ResultSet rs = psSesion.executeQuery()) {
                    if (rs.next()) {
                        idSesionGrupal = rs.getInt("ID_SESION_GRUPAL");
                    }
                }
            }

            if (idSesionGrupal == -1) {
                try (PreparedStatement psInsSesion = con.prepareStatement(sqlCrearSesion, new String[]{"ID_SESION_GRUPAL"})) {
                    psInsSesion.setString(1, fecha);
                    psInsSesion.setInt(2, Integer.parseInt(grupo));
                    psInsSesion.setInt(3, Integer.parseInt(cuatrimestre));
                    psInsSesion.setInt(4, idTutor);
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

                if (idsAlumnos != null && idsAlumnos.length > 0) {
                    String selectMatriculaSql = "SELECT MATRICULA FROM ALUMNO WHERE ID_ALUMNO = ?";
                    try (PreparedStatement psIns = con.prepareStatement(insertSql);
                         PreparedStatement psMat = con.prepareStatement(selectMatriculaSql)) {

                        for (String idAlumno : idsAlumnos) {
                            psMat.setInt(1, Integer.parseInt(idAlumno));
                            try (ResultSet rsMat = psMat.executeQuery()) {
                                if (rsMat.next()) {
                                    String matricula = rsMat.getString("MATRICULA");
                                    psIns.setInt(1, idSesionGrupal);
                                    psIns.setString(2, matricula);
                                    psIns.setString(3, "Presente");
                                    psIns.addBatch();
                                }
                            }
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

    public List<Integer> getAlumnosConAsistencia(int idGrupo, int idCarrera, int idCuatrimestre, String fecha) {
        List<Integer> idsAsistentes = new ArrayList<>();

        String sql = "SELECT al.ID_ALUMNO " +
                "FROM ALUMNO al " +
                "JOIN ASISTENCIA a ON al.MATRICULA = a.MATRICULA " +
                "JOIN SESION_GRUPAL sg ON a.ID_SESION_GRUPAL = sg.ID_SESION_GRUPAL " +
                "WHERE sg.ID_LETRA_GRUPO = ? " +
                "AND al.ID_CARRERA = ? " +
                "AND sg.ID_CUATRIMESTRE = ? " +
                "AND TRUNC(sg.FECHA) = TO_DATE(?, 'YYYY-MM-DD') " +
                "AND a.ESTATUS_ASISTENCIA = 'Presente'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idGrupo);
            ps.setInt(2, idCarrera);
            ps.setInt(3, idCuatrimestre);
            ps.setString(4, fecha);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idsAsistentes.add(rs.getInt("ID_ALUMNO"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idsAsistentes;
    }
}
