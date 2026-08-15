package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// TOKENS reemplaza el antiguo CODIGO_RECUPERACION embebido en USUARIO: guarda tokens de
// Recuperacion/Confirmacion para exactamente uno de ALUMNO/TUTOR/COORDINADOR (ver
// CK_TOKENS_UN_DUENO en BD_INTEGRADORA_SGT.sql). Solo se usa el TIPO='Recuperacion' aqui;
// CANALIZACION tiene su propio ID_TOKEN independiente para confirmaciones de area.
public class TokenDao {

    public static class TokenInfo {
        public int idToken;
        public String rol;             // "Alumno", "Tutor" o "Coordinador"
        public String matricula;       // solo si rol = Alumno
        public Integer numeroEmpleado; // solo si rol = Tutor/Coordinador
    }

    public boolean crearParaAlumno(String token, String matricula) {
        return crear(token, "ID_ALUMNO", matricula);
    }

    public boolean crearParaTutor(String token, int numeroEmpleado) {
        return crear(token, "ID_TUTOR", numeroEmpleado);
    }

    public boolean crearParaCoordinador(String token, int numeroEmpleado) {
        return crear(token, "ID_COORDINADOR", numeroEmpleado);
    }

    private boolean crear(String token, String columnaDueno, Object valorDueno) {
        String sql = "INSERT INTO TOKENS (TOKEN, " + columnaDueno + ", FECHA_CREACION, TIPO, UTILIZADO) " +
                "VALUES (?, ?, SYSTIMESTAMP, 'Recuperacion', 'N')";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            if (valorDueno instanceof Integer) {
                ps.setInt(2, (Integer) valorDueno);
            } else {
                ps.setString(2, (String) valorDueno);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Busca un token de recuperacion vigente (no utilizado todavia).
    public TokenInfo buscarVigente(String token) {
        String sql = "SELECT ID_TOKEN, ID_ALUMNO, ID_TUTOR, ID_COORDINADOR FROM TOKENS " +
                "WHERE TOKEN = ? AND TIPO = 'Recuperacion' AND UTILIZADO = 'N'";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                TokenInfo info = new TokenInfo();
                info.idToken = rs.getInt("ID_TOKEN");

                String matricula = rs.getString("ID_ALUMNO");
                int idTutor = rs.getInt("ID_TUTOR");
                boolean tutorNull = rs.wasNull();
                int idCoordinador = rs.getInt("ID_COORDINADOR");
                boolean coordNull = rs.wasNull();

                if (matricula != null) {
                    info.rol = "Alumno";
                    info.matricula = matricula;
                } else if (!tutorNull) {
                    info.rol = "Tutor";
                    info.numeroEmpleado = idTutor;
                } else if (!coordNull) {
                    info.rol = "Coordinador";
                    info.numeroEmpleado = idCoordinador;
                }
                return info;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean marcarUtilizado(int idToken) {
        String sql = "UPDATE TOKENS SET UTILIZADO = 'S' WHERE ID_TOKEN = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idToken);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
