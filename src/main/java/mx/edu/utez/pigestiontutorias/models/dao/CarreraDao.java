package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Carrera;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CarreraDao implements Dao<Carrera, Integer> {

    @Override
    public boolean create(Carrera entidad) {
        return false;
    }

    @Override
    public List<Carrera> getAll() {
        List<Carrera> lista = new ArrayList<>();
        String sql = "SELECT ID_CARRERA, NOMBRE, NIVEL, ID_ACADEMIA FROM CARRERA ORDER BY NOMBRE";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener las carreras: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    public List<Carrera> getByIdAcademia(int idAcademia) {
        List<Carrera> lista = new ArrayList<>();
        String sql = "SELECT ID_CARRERA, NOMBRE, NIVEL, ID_ACADEMIA FROM CARRERA WHERE ID_ACADEMIA = ? ORDER BY NOMBRE";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAcademia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener las carreras de la academia: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    @Override
    public Carrera getById(Integer id) {
        String sql = "SELECT ID_CARRERA, NOMBRE, NIVEL, ID_ACADEMIA FROM CARRERA WHERE ID_CARRERA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Carrera entidad) {
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        return false;
    }

    private Carrera mapear(ResultSet rs) throws SQLException {
        Carrera c = new Carrera();
        c.setIdCarrera(rs.getInt("ID_CARRERA"));
        c.setNombre(rs.getString("NOMBRE"));
        c.setNivel(rs.getString("NIVEL"));
        c.setIdAcademia(rs.getInt("ID_ACADEMIA"));
        return c;
    }
}
