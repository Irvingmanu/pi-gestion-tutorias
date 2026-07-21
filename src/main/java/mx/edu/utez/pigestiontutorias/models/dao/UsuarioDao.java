package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Usuario;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDao {

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

    public Usuario buscarPorCorreoOMatricula(String dato) {
        Usuario usuario = null;
        String sql = "SELECT * FROM USUARIO WHERE CORREO_INSTITUCIONAL = ? OR IDENTIFICADOR = ?";
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

    public Usuario autenticar(String usuarioInput, String password) {
        Usuario usuario = null;
        String sql = "SELECT * FROM USUARIO " +
                "WHERE (UPPER(CORREO_INSTITUCIONAL) = UPPER(?) OR UPPER(IDENTIFICADOR) = UPPER(?)) " +
                "AND PASS = ?";

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