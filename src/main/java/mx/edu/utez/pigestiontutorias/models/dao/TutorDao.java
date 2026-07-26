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

        String sqlHorario = "INSERT INTO HORARIO_ATENCION (ID_TUTOR, DIA_SEMANA, HORA_DESDE, HORA_HASTA) VALUES (?, ?, TO_DSINTERVAL('0 ' || ? || ':00'), TO_DSINTERVAL('0 ' || ? || ':00'))";
        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idUsuarioGenerado = 0;
            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario, new String[]{"ID_USUARIO"})) {
                psUsuario.setString(1, "Tutor");
                psUsuario.setString(2, String.valueOf(entidad.getNomina()));
                psUsuario.setString(3, passTemporal);
                psUsuario.setString(4, entidad.getCorreoInstitucional());
                psUsuario.executeUpdate();

                try (ResultSet keys = psUsuario.getGeneratedKeys()) {
                    if (keys.next()) idUsuarioGenerado = keys.getInt(1);
                    else { con.rollback(); return false; }
                }
            }

            int idTutorGenerado = 0;
            try (PreparedStatement psTutor = con.prepareStatement(sqlTutor, new String[]{"ID_TUTOR"})) {
                psTutor.setString(1, String.valueOf(entidad.getNomina()));
                psTutor.setString(2, entidad.getNombres());
                psTutor.setString(3, entidad.getApellidos());
                psTutor.setString(4, entidad.getCorreoInstitucional());
                psTutor.setString(5, entidad.getTelefono());
                psTutor.setInt(6, entidad.getIdAcademia());
                psTutor.setInt(7, idUsuarioGenerado);
                psTutor.executeUpdate();

                try (ResultSet keys = psTutor.getGeneratedKeys()) {
                    if (keys.next()) idTutorGenerado = keys.getInt(1);
                    else { con.rollback(); return false; }
                }
            }

            if (entidad.getHorariosDispo() != null && !entidad.getHorariosDispo().isEmpty()) {
                try (PreparedStatement psHorario = con.prepareStatement(sqlHorario)) {
                    for (String horarioStr : entidad.getHorariosDispo()) {
                        String dia = "Lunes";
                        String desde = "00:00";
                        String hasta = "00:00";

                        java.util.regex.Matcher mDia = java.util.regex.Pattern.compile("(Lunes|Martes|Mi[eé]rcoles|Jueves|Viernes)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(horarioStr);
                        if (mDia.find()) dia = mDia.group(1);

                        java.util.regex.Matcher mHoras = java.util.regex.Pattern.compile("([0-2][0-9]:[0-5][0-9])").matcher(horarioStr);
                        if (mHoras.find()) desde = mHoras.group(1);
                        if (mHoras.find()) hasta = mHoras.group(1);

                        psHorario.setInt(1, idTutorGenerado);
                        psHorario.setString(2, dia);
                        psHorario.setString(3, desde);
                        psHorario.setString(4, hasta);
                        psHorario.addBatch();
                    }
                    psHorario.executeBatch();
                }
            }

            con.commit();
            entidad.setIdUsuario(idUsuarioGenerado);
            entidad.setIdTutor(idTutorGenerado);
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
            ps.setString(1, String.valueOf(nomina)); // CORRECCIÓN: Era setInt, cambiado a setString
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
            ps.setString(1, String.valueOf(nomina)); // CORRECCIÓN: Era setInt, cambiado a setString
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
        // ... (Se mantiene igual)
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
                try {
                    Tutor tutor = new Tutor();
                    tutor.setIdTutor(rs.getInt("ID_TUTOR"));

                    // CORRECCIÓN: Se añade trim() para evitar fallos si la DB agrega espacios invisibles
                    String nominaStr = rs.getString("NOMINA");
                    tutor.setNomina(nominaStr != null ? Integer.parseInt(nominaStr.trim()) : 0);

                    tutor.setNombres(rs.getString("NOMBRES"));
                    tutor.setApellidos(rs.getString("APELLIDOS"));
                    tutor.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
                    tutor.setTelefono(rs.getString("TELEFONO"));

                    // CORRECCIÓN: También sanitizamos este parseo con trim()
                    String divAcadStr = rs.getString("DIVISION_ACADEMICA");
                    tutor.setIdAcademia(divAcadStr != null && !divAcadStr.trim().isEmpty() ? Integer.parseInt(divAcadStr.trim()) : 0);

                    tutor.setIdUsuario(rs.getInt("ID_USUARIO"));

                    lista.add(tutor);
                } catch (NumberFormatException ex) {
                    System.out.println("Fila ignorada por datos incompatibles: " + ex.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en getAll: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    public Tutor getByNomina(int nomina) {
        String sql = "SELECT * FROM TUTOR WHERE NOMINA = ?";
        String sqlHorarios = "SELECT DIA_SEMANA, " +
                "TO_CHAR(EXTRACT(HOUR FROM HORA_DESDE), 'FM00') || ':' || TO_CHAR(EXTRACT(MINUTE FROM HORA_DESDE), 'FM00') AS DESDE, " +
                "TO_CHAR(EXTRACT(HOUR FROM HORA_HASTA), 'FM00') || ':' || TO_CHAR(EXTRACT(MINUTE FROM HORA_HASTA), 'FM00') AS HASTA " +
                "FROM HORARIO_ATENCION WHERE ID_TUTOR = ?";
        Tutor tutor = null;

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(nomina));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tutor = mapearTutor(rs);
                }
            }

            if (tutor != null) {
                try (PreparedStatement psH = con.prepareStatement(sqlHorarios)) {
                    psH.setInt(1, tutor.getIdTutor());
                    try (ResultSet rsH = psH.executeQuery()) {
                        List<String> horarios = new ArrayList<>();
                        while (rsH.next()) {
                            String dia = rsH.getString("DIA_SEMANA");
                            String desde = rsH.getString("DESDE");
                            String hasta = rsH.getString("HASTA");
                            horarios.add(dia + " " + desde + " - " + hasta);
                        }
                        tutor.setHorariosDispo(horarios);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tutor;
    }

    public boolean update(Tutor entidad) {
        String sqlTutor = "UPDATE TUTOR SET NOMBRES = ?, APELLIDOS = ?, CORREO_INSTITUCIONAL = ?, TELEFONO = ?, DIVISION_ACADEMICA = ? WHERE NOMINA = ?";
        String sqlUsuario = "UPDATE USUARIO SET CORREO_INSTITUCIONAL = ? WHERE ID_USUARIO = ?";

        String sqlDeleteHorarios = "DELETE FROM HORARIO_ATENCION WHERE ID_TUTOR = ?";
        String sqlInsertHorario = "INSERT INTO HORARIO_ATENCION (ID_TUTOR, DIA_SEMANA, HORA_DESDE, HORA_HASTA) VALUES (?, ?, TO_DSINTERVAL('0 ' || ? || ':00'), TO_DSINTERVAL('0 ' || ? || ':00'))";

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
                psTutor.setString(6, String.valueOf(entidad.getNomina()));
                psTutor.executeUpdate();
            }

            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario)) {
                psUsuario.setString(1, entidad.getCorreoInstitucional());
                psUsuario.setInt(2, entidad.getIdUsuario());
                psUsuario.executeUpdate();
            }

            try (PreparedStatement psDel = con.prepareStatement(sqlDeleteHorarios)) {
                psDel.setInt(1, entidad.getIdTutor());
                psDel.executeUpdate();
            }

            if (entidad.getHorariosDispo() != null && !entidad.getHorariosDispo().isEmpty()) {
                try (PreparedStatement psIns = con.prepareStatement(sqlInsertHorario)) {
                    for (String horarioStr : entidad.getHorariosDispo()) {
                        String dia = "Lunes";
                        String desde = "00:00";
                        String hasta = "00:00";

                        java.util.regex.Matcher mDia = java.util.regex.Pattern.compile("(Lunes|Martes|Mi[eé]rcoles|Jueves|Viernes)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(horarioStr);
                        if (mDia.find()) dia = mDia.group(1);

                        java.util.regex.Matcher mHoras = java.util.regex.Pattern.compile("([0-2][0-9]:[0-5][0-9])").matcher(horarioStr);
                        if (mHoras.find()) desde = mHoras.group(1);
                        if (mHoras.find()) hasta = mHoras.group(1);

                        psIns.setInt(1, entidad.getIdTutor());
                        psIns.setString(2, dia);
                        psIns.setString(3, desde);
                        psIns.setString(4, hasta);
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
        String selectSql = "SELECT ID_TUTOR, ID_USUARIO FROM TUTOR WHERE NOMINA = ?";
        String deleteHorarios = "DELETE FROM HORARIO_ATENCION WHERE ID_TUTOR = ?";
        String deleteTutor = "DELETE FROM TUTOR WHERE NOMINA = ?";
        String deleteUsuario = "DELETE FROM USUARIO WHERE ID_USUARIO = ?";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idTutor = 0;
            int idUsuario = 0;

            try (PreparedStatement psSel = con.prepareStatement(selectSql)) {
                // CORRECCIÓN: Cambiado de setInt a setString para que el WHERE concuerde
                psSel.setString(1, String.valueOf(nomina));
                try (ResultSet rs = psSel.executeQuery()) {
                    if (rs.next()) {
                        idTutor = rs.getInt("ID_TUTOR");
                        idUsuario = rs.getInt("ID_USUARIO");
                    } else {
                        return false;
                    }
                }
            }

            try (PreparedStatement psDelH = con.prepareStatement(deleteHorarios)) {
                psDelH.setInt(1, idTutor);
                psDelH.executeUpdate();
            }

            try (PreparedStatement psDelT = con.prepareStatement(deleteTutor)) {
                // CORRECCIÓN: Cambiado de setInt a setString
                psDelT.setString(1, String.valueOf(nomina));
                psDelT.executeUpdate();
            }

            if (idUsuario > 0) {
                try (PreparedStatement psDelU = con.prepareStatement(deleteUsuario)) {
                    psDelU.setInt(1, idUsuario);
                    psDelU.executeUpdate();
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

        String nominaStr = rs.getString("NOMINA");
        tutor.setNomina(nominaStr != null ? Integer.parseInt(nominaStr.trim()) : 0);

        tutor.setNombres(rs.getString("NOMBRES"));
        tutor.setApellidos(rs.getString("APELLIDOS"));
        tutor.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
        tutor.setTelefono(rs.getString("TELEFONO"));
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

    public Tutor findByIdUsuario(int idUsuario) {
        String sql = "SELECT ID_TUTOR, NOMBRES, APELLIDOS FROM ADMIN.TUTOR WHERE ID_USUARIO = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Tutor t = new Tutor();
                    t.setIdTutor(rs.getInt("ID_TUTOR"));
                    t.setNombres(rs.getString("NOMBRES"));
                    t.setApellidos(rs.getString("APELLIDOS"));
                    return t;
                }
            }

        } catch (Exception e) {
            System.err.println("Error al obtener el tutor por idUsuario: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}