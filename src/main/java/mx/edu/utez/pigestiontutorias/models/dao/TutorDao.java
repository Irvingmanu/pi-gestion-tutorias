package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Academia;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TutorDao {

    public boolean create(Tutor entidad) {
        String passTemporal = "Tut@" + entidad.getNomina();

        String sqlUsuario = "INSERT INTO USUARIO (ROL, IDENTIFICADOR, PASS, CORREO_INSTITUCIONAL) VALUES (?, ?, ?, ?)";
        String sqlTutor = "INSERT INTO TUTOR(NOMINA, NOMBRES, APELLIDOS, CORREO_INSTITUCIONAL, TELEFONO, DIVISION_ACADEMICA, ID_USUARIO) VALUES(?, ?, ?, ?, ?, ?, ?)";
        // CORREGIDO: Se cambió ID_ACAMEDIA por NOMINA_TUTOR
        String sqlHorario = "INSERT INTO HORARIO_ATENCION (NOMINA_TUTOR, HORARIO) VALUES (?, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idUsuarioGenerado;
            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario, new String[]{"ID_USUARIO"})) {
                psUsuario.setString(1, "Tutor");
                psUsuario.setString(2, String.valueOf(entidad.getNomina()));
                psUsuario.setString(3, passTemporal);
                psUsuario.setString(4, entidad.getCorreoInstitucional());
                psUsuario.executeUpdate();

                try (ResultSet keys = psUsuario.getGeneratedKeys()) {
                    if (!keys.next()) {
                        con.rollback();
                        return false;
                    }
                    idUsuarioGenerado = keys.getInt(1);
                }
            }

            try (PreparedStatement psTutor = con.prepareStatement(sqlTutor)) {
                psTutor.setInt(1, entidad.getNomina());
                psTutor.setString(2, entidad.getNombres());
                psTutor.setString(3, entidad.getApellidos());
                psTutor.setString(4, entidad.getCorreoInstitucional());
                psTutor.setString(5, entidad.getTelefono());
                psTutor.setInt(6, entidad.getIdAcademia());
                psTutor.setInt(7, idUsuarioGenerado);
                psTutor.executeUpdate();
            }

            if (entidad.getHorariosDispo() != null && !entidad.getHorariosDispo().isEmpty()) {
                try (PreparedStatement psHorario = con.prepareStatement(sqlHorario)) {
                    for (String horario : entidad.getHorariosDispo()) {
                        psHorario.setInt(1, entidad.getNomina());
                        psHorario.setString(2, horario);
                        psHorario.addBatch();
                    }
                    psHorario.executeBatch();
                }
            }

            con.commit();
            entidad.setIdUsuario(idUsuarioGenerado);
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

    public boolean existeNomina(int nomina) {
        String sql = "SELECT COUNT(*) FROM TUTOR WHERE NOMINA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nomina);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean existeCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM TUTOR WHERE CORREO_INSTITUCIONAL = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean existeTelefono(String telefono) {
        String sql = "SELECT COUNT(*) FROM TUTOR WHERE TELEFONO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, telefono);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean existeNomina(int nomina, int idTutorActual) {
        String sql = "SELECT COUNT(*) FROM TUTOR WHERE NOMINA = ? AND ID_TUTOR <> ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nomina);
            ps.setInt(2, idTutorActual);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean existeCorreo(String correo, int idTutorActual) {
        String sql = "SELECT COUNT(*) FROM TUTOR WHERE CORREO_INSTITUCIONAL = ? AND ID_TUTOR <> ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setInt(2, idTutorActual);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean existeTelefono(String telefono, int idTutorActual) {
        String sql = "SELECT COUNT(*) FROM TUTOR WHERE TELEFONO = ? AND ID_TUTOR <> ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, telefono);
            ps.setInt(2, idTutorActual);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Tutor> getAll() {
        List<Tutor> lista = new ArrayList<>();
        String sql = "SELECT ID_TUTOR, NOMINA, NOMBRES, APELLIDOS, CORREO_INSTITUCIONAL, TELEFONO, DIVISION_ACADEMICA, ID_USUARIO FROM TUTOR";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Tutor tutor = new Tutor();
                tutor.setIdTutor(rs.getInt("ID_TUTOR"));
                tutor.setNomina(rs.getInt("NOMINA"));
                tutor.setNombres(rs.getString("NOMBRES"));
                tutor.setApellidos(rs.getString("APELLIDOS"));
                tutor.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
                tutor.setTelefono(rs.getString("TELEFONO"));
                tutor.setIdAcademia(rs.getInt("DIVISION_ACADEMICA"));
                tutor.setIdUsuario(rs.getInt("ID_USUARIO"));

                lista.add(tutor);
            }
        } catch (SQLException e) {
            System.out.println("Error en getAll: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    public Tutor getByNomina(int nomina) {
        String sql = "SELECT * FROM TUTOR WHERE NOMINA = ?";
        String sqlHorarios = "SELECT HORARIO FROM HORARIO_ATENCION WHERE NOMINA_TUTOR = ?";
        Tutor tutor = null;

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nomina);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tutor = mapearTutor(rs); // CORREGIDO: Se asigna sin retornar inmediatamente
                }
            }

            if (tutor != null) {
                try (PreparedStatement psH = con.prepareStatement(sqlHorarios)) {
                    psH.setInt(1, nomina);
                    try (ResultSet rsH = psH.executeQuery()) {
                        List<String> horarios = new ArrayList<>();
                        while (rsH.next()) {
                            horarios.add(rsH.getString("HORARIO"));
                        }
                        tutor.setHorariosDispo(horarios);
                    }
                }
            }

        } catch (SQLException e) { e.printStackTrace(); }
        return tutor; // Retorna el objeto tutor completo con sus horarios
    }

    public boolean update(Tutor entidad) {
        // CORREGIDO: Se cambió ID_ACADEMIA por DIVISION_ACADEMICA
        String sqlTutor = "UPDATE TUTOR SET NOMBRES = ?, APELLIDOS = ?, CORREO_INSTITUCIONAL = ?, TELEFONO = ?, DIVISION_ACADEMICA = ? WHERE NOMINA = ?";
        String sqlUsuario = "UPDATE USUARIO SET CORREO_INSTITUCIONAL = ? WHERE ID_USUARIO = ?";
        String sqlDeleteHorarios = "DELETE FROM HORARIO_ATENCION WHERE NOMINA_TUTOR = ?";
        String sqlInsertHorario = "INSERT INTO HORARIO_ATENCION (NOMINA_TUTOR, HORARIO) VALUES (?, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement psTutor = con.prepareStatement(sqlTutor)) {
                psTutor.setString(1, entidad.getNombres());
                psTutor.setString(2, entidad.getApellidos());
                psTutor.setString(3, entidad.getCorreoInstitucional());
                psTutor.setString(4, entidad.getTelefono());
                psTutor.setInt(5, entidad.getIdAcademia());
                psTutor.setInt(6, entidad.getNomina());
                psTutor.executeUpdate();
            }

            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario)) {
                psUsuario.setString(1, entidad.getCorreoInstitucional());
                psUsuario.setInt(2, entidad.getIdUsuario());
                psUsuario.executeUpdate();
            }

            try (PreparedStatement psDel = con.prepareStatement(sqlDeleteHorarios)) {
                psDel.setInt(1, entidad.getNomina());
                psDel.executeUpdate();
            }

            if (entidad.getHorariosDispo() != null && !entidad.getHorariosDispo().isEmpty()) {
                try (PreparedStatement psIns = con.prepareStatement(sqlInsertHorario)) {
                    for (String horario : entidad.getHorariosDispo()) {
                        psIns.setInt(1, entidad.getNomina());
                        psIns.setString(2, horario);
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
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

    public boolean delete(int nomina) {
        String sql = "DELETE FROM TUTOR WHERE NOMINA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nomina);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Academia> getAllAcademias() {
        List<Academia> lista = new ArrayList<>();
        String sql = "SELECT ID_ACADEMIA, NOMBRE FROM ACADEMIA";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Academia(rs.getInt("ID_ACADEMIA"), rs.getString("NOMBRE")));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private Tutor mapearTutor(ResultSet rs) throws SQLException {
        Tutor tutor = new Tutor();
        tutor.setIdTutor(rs.getInt("ID_TUTOR"));
        tutor.setNomina(rs.getInt("NOMINA"));
        tutor.setNombres(rs.getString("NOMBRES"));
        tutor.setApellidos(rs.getString("APELLIDOS"));
        tutor.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
        tutor.setTelefono(rs.getString("TELEFONO"));
        // CORREGIDO: Se cambió ID_ACADEMIA por DIVISION_ACADEMICA
        tutor.setIdAcademia(rs.getInt("DIVISION_ACADEMICA"));
        tutor.setIdUsuario(rs.getInt("ID_USUARIO"));
        return tutor;
    }

    public List<Tutor> findAll() {
        List<Tutor> lista = new ArrayList<>();
        String sql = "SELECT ID_TUTOR, NOMBRES, APELLIDOS FROM ADMIN.TUTOR";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Tutor t = new Tutor();
                t.setIdTutor(rs.getInt("ID_TUTOR"));
                t.setNombres(rs.getString("NOMBRES"));
                t.setApellidos(rs.getString("APELLIDOS"));
                lista.add(t);
            }

        } catch (Exception e) {
            System.err.println("Error al obtener los tutores: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }
}
