package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Coordinador;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CoordinadorDAO implements Dao<Coordinador, Integer> {

    @Override
    public boolean create(Coordinador entidad) {
        String sql = "INSERT INTO COORDINADOR(NUMERO_EMPLEADO, NOMBRES, APELLIDO_PATERNO, APELLIDO_MATERNO, CORREO_INSTITUCIONAL, TELEFONO, PASS) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String pass = (entidad.getPass() != null && !entidad.getPass().isBlank())
                    ? entidad.getPass() : "Tut@" + entidad.getNumeroEmpleado();

            ps.setInt(1, entidad.getNumeroEmpleado());
            ps.setString(2, entidad.getNombres());
            ps.setString(3, entidad.getApellidoPaterno());
            ps.setString(4, entidad.getApellidoMaterno());
            ps.setString(5, entidad.getCorreoInstitucional());
            ps.setString(6, entidad.getTelefono());
            ps.setString(7, PasswordUtil.hash(pass));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Coordinador> getAll() {
        List<Coordinador> lista = new ArrayList<>();
        String sql = "SELECT * FROM COORDINADOR";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearCoordinador(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Coordinador getById(Integer numeroEmpleado) {
        String sql = "SELECT * FROM COORDINADOR WHERE NUMERO_EMPLEADO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, numeroEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCoordinador(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Login: busca por correo institucional (ver LoginServlet, ultimo intento tras
    // ALUMNO y TUTOR). Incluye PASS para que el servlet valide la contraseña.
    public Coordinador findByCorreo(String correo) {
        String sql = "SELECT * FROM COORDINADOR WHERE UPPER(CORREO_INSTITUCIONAL) = UPPER(?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCoordinador(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Coordinador entidad) {
        String sql = "UPDATE COORDINADOR SET NOMBRES = ?, APELLIDO_PATERNO = ?, APELLIDO_MATERNO = ?, CORREO_INSTITUCIONAL = ?, TELEFONO = ? WHERE NUMERO_EMPLEADO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombres());
            ps.setString(2, entidad.getApellidoPaterno());
            ps.setString(3, entidad.getApellidoMaterno());
            ps.setString(4, entidad.getCorreoInstitucional());
            ps.setString(5, entidad.getTelefono());
            ps.setInt(6, entidad.getNumeroEmpleado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Integer numeroEmpleado) {
        String sql = "UPDATE COORDINADOR SET ESTADO = 'N' WHERE NUMERO_EMPLEADO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, numeroEmpleado);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarPassword(int numeroEmpleado, String nuevaPassword) {
        String sql = "UPDATE COORDINADOR SET PASS = ? WHERE NUMERO_EMPLEADO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hash(nuevaPassword));
            ps.setInt(2, numeroEmpleado);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Coordinador mapearCoordinador(ResultSet rs) throws SQLException {
        Coordinador coordinador = new Coordinador();
        coordinador.setNumeroEmpleado(rs.getInt("NUMERO_EMPLEADO"));
        coordinador.setNombres(rs.getString("NOMBRES"));
        coordinador.setApellidoPaterno(rs.getString("APELLIDO_PATERNO"));
        coordinador.setApellidoMaterno(rs.getString("APELLIDO_MATERNO"));
        coordinador.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
        coordinador.setTelefono(rs.getString("TELEFONO"));
        coordinador.setPass(rs.getString("PASS"));
        coordinador.setEstado(rs.getString("ESTADO"));
        return coordinador;
    }
}
