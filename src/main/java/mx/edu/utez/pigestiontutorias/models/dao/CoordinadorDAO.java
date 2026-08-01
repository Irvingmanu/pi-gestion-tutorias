package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Coordinador;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CoordinadorDAO implements Dao<Coordinador, Integer> {

    @Override
    public boolean create(Coordinador entidad) {
        return false;
    }

    @Override
    public List<Coordinador> getAll() {
        return null;
    }

    @Override
    public Coordinador getById(Integer id) {
        return null;
    }

    @Override
    public boolean update(Coordinador entidad) {
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        return false;
    }

    public Coordinador getCoordinadorTemporal() {
        String sql = "SELECT * FROM COORDINADOR WHERE ID_COORDINADOR = 1";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapearCoordinador(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Coordinador mapearCoordinador(ResultSet rs) throws SQLException {
        Coordinador coordinador = new Coordinador();
        coordinador.setIdCoordinador(rs.getInt("ID_COORDINADOR"));
        coordinador.setIdUsuario(rs.getInt("ID_USUARIO"));
        coordinador.setNombres(rs.getString("NOMBRES"));
        coordinador.setApellidos(rs.getString("APELLIDOS"));
        coordinador.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
        return coordinador;
    }
}
