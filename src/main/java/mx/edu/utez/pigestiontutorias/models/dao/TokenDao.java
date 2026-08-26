package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO de acceso a datos para los tokens de recuperación de contraseña (tabla TOKENS),
 * usados por alumnos, tutores y coordinadores para restablecer su contraseña.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-08-14
 */
public class TokenDao {

    /**
     * Estructura de resultado con la información resuelta de un token vigente:
     * su identificador, el rol del dueño y su matrícula o número de empleado.
     */
    public static class TokenInfo {
        public int idToken;
        public String rol;
        public String matricula;
        public Integer numeroEmpleado;
    }

    /**
     * Crea un token de recuperación de contraseña asociado a un alumno.
     * @param token el valor del token generado
     * @param matricula la matrícula del alumno dueño del token
     * @return {@code true} si la inserción afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean crearParaAlumno(String token, String matricula) {
        return crear(token, "ID_ALUMNO", matricula);
    }

    /**
     * Crea un token de recuperación de contraseña asociado a un tutor.
     * @param token el valor del token generado
     * @param numeroEmpleado el número de empleado del tutor dueño del token
     * @return {@code true} si la inserción afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean crearParaTutor(String token, int numeroEmpleado) {
        return crear(token, "ID_TUTOR", numeroEmpleado);
    }

    /**
     * Crea un token de recuperación de contraseña asociado a un coordinador.
     * @param token el valor del token generado
     * @param numeroEmpleado el número de empleado del coordinador dueño del token
     * @return {@code true} si la inserción afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean crearParaCoordinador(String token, int numeroEmpleado) {
        return crear(token, "ID_COORDINADOR", numeroEmpleado);
    }

    /**
     * Inserta un nuevo token de recuperación en la tabla TOKENS, asociándolo a la columna
     * dueño (alumno, tutor o coordinador) indicada.
     * @param token el valor del token generado
     * @param columnaDueno el nombre de la columna dueño (ID_ALUMNO, ID_TUTOR o ID_COORDINADOR) a la que se asocia el token
     * @param valorDueno el valor del dueño (matrícula como {@code String} o número de empleado como {@code Integer})
     * @return {@code true} si la inserción afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
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

    /**
     * Busca un token de recuperación vigente (de tipo "Recuperacion" y aún no utilizado) y
     * resuelve a qué rol de usuario pertenece.
     * @param token el valor del token a buscar
     * @return la información del token vigente con su rol y matrícula/número de empleado, o {@code null} si no existe, ya fue usado o si ocurre un error de base de datos
     */
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

    /**
     * Marca un token de recuperación como utilizado, para que no pueda volver a usarse.
     * @param idToken el identificador del token
     * @return {@code true} si la actualización afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
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
