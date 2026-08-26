package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Usuario;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO de acceso a datos para la tabla USUARIO, usada para la autenticación de acceso al
 * sistema y el flujo de recuperación de contraseña por código.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-17
 */
public class UsuarioDao {

    /**
     * Verifica si existe un usuario cuyo identificador y contraseña coincidan exactamente con los indicados.
     * @param identificador el identificador del usuario (correo o matrícula/nómina)
     * @param pass la contraseña a validar (ya cifrada, tal como se almacena)
     * @return {@code true} si existe coincidencia; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean login(String identificador, String pass) {
        String sql = "SELECT COUNT(*) FROM USUARIO WHERE IDENTIFICADOR = ? AND PASS = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, identificador.trim());
            ps.setString(2, pass);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Busca un usuario por su correo institucional o su identificador (matrícula/nómina), sin
     * distinguir mayúsculas/minúsculas.
     * @param dato el correo institucional o identificador a buscar
     * @return el usuario encontrado, o {@code null} si no existe o si ocurre un error de base de datos
     */
    public Usuario buscarPorCorreoOMatricula(String dato) {
        Usuario usuario = null;
        String sql = "SELECT * FROM USUARIO WHERE UPPER(CORREO_INSTITUCIONAL) = UPPER(?) OR UPPER(IDENTIFICADOR) = UPPER(?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dato);
            ps.setString(2, dato);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuario;
    }

    /**
     * Guarda el código de recuperación de contraseña generado para un usuario.
     * @param idUsuario el identificador del usuario
     * @param codigo el código de recuperación a guardar
     * @return {@code true} si la actualización afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean guardarCodigoRecuperacion(int idUsuario, String codigo) {
        String sql = "UPDATE USUARIO SET CODIGO_RECUPERACION = ? WHERE ID_USUARIO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Busca un usuario a partir de un código de recuperación de contraseña vigente.
     * @param codigo el código de recuperación a verificar
     * @return el usuario dueño del código, o {@code null} si no existe o si ocurre un error de base de datos
     */
    public Usuario verificarCodigo(String codigo) {
        Usuario usuario = null;
        String sql = "SELECT * FROM USUARIO WHERE CODIGO_RECUPERACION = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuario;
    }

    /**
     * Actualiza la contraseña de un usuario y limpia su código de recuperación, finalizando el flujo.
     * @param idUsuario el identificador del usuario
     * @param nuevaPassword la nueva contraseña a guardar (ya en el formato de almacenamiento esperado)
     * @return {@code true} si la actualización afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean actualizarPasswordLimpiaCodigo(int idUsuario, String nuevaPassword) {
        String sql = "UPDATE USUARIO SET PASS = ?, CODIGO_RECUPERACION = NULL WHERE ID_USUARIO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevaPassword);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Construye un objeto {@link Usuario} a partir de la fila actual de un ResultSet.
     * @param rs el ResultSet posicionado en la fila a mapear
     * @return el usuario mapeado con sus campos principales
     * @throws SQLException si ocurre un error al leer las columnas del ResultSet
     */
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("ID_USUARIO"));
        u.setRol(rs.getString("ROL"));
        u.setIdentificador(rs.getString("IDENTIFICADOR"));
        u.setPass(rs.getString("PASS"));
        u.setIntentosFallidos(rs.getInt("INTENTOS_FALLIDOS"));
        u.setCodigoRecuperacion(rs.getString("CODIGO_RECUPERACION"));
        u.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
        return u;
    }

    /**
     * Autentica a un usuario activo por correo institucional o identificador y contraseña exactos.
     * @param usuarioInput el correo institucional o identificador ingresado
     * @param password la contraseña ingresada (ya cifrada, tal como se almacena)
     * @return el usuario autenticado, o {@code null} si las credenciales no coinciden, el usuario está inactivo, o si ocurre un error de base de datos
     */
    public Usuario autenticar(String usuarioInput, String password) {
        Usuario usuario = null;
        String sql = "SELECT * FROM USUARIO " +
                "WHERE (UPPER(CORREO_INSTITUCIONAL) = UPPER(?) OR UPPER(IDENTIFICADOR) = UPPER(?)) " +
                "AND PASS = ? AND ACTIVO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuarioInput);
            ps.setString(2, usuarioInput);
            ps.setString(3, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuario;
    }
}