package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Motivo;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MotivoDAO implements Dao<Motivo, Integer> {

    public List<Motivo> getByIdArea(int idArea) {
        List<Motivo> listaMotivos = new ArrayList<>();
        String sql = "SELECT * FROM MOTIVO_AREA WHERE ID_AREA = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idArea);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listaMotivos.add(mapearMotivo(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaMotivos;
    }

    @Override
    public boolean create(Motivo entidad) {
        String sql = "INSERT INTO MOTIVO_AREA(ID_AREA, NOMBRE_MOTIVO) VALUES(?, ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, entidad.getIdArea());
            ps.setString(2, entidad.getNombreMotivo());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Motivo> getAll() {
        List<Motivo> listaMotivos = new ArrayList<>();
        String sql = "SELECT * FROM MOTIVO_AREA";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaMotivos.add(mapearMotivo(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaMotivos;
    }

    @Override
    public Motivo getById(Integer id) {
        String sql = "SELECT * FROM MOTIVO_AREA WHERE ID_MOTIVO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearMotivo(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Motivo entidad) {
        String sql = "UPDATE MOTIVO_AREA SET NOMBRE_MOTIVO = ? WHERE ID_MOTIVO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, entidad.getNombreMotivo());
            ps.setInt(2, entidad.getIdMotivo());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM MOTIVO_AREA WHERE ID_MOTIVO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Motivo mapearMotivo(ResultSet rs) throws SQLException {
        Motivo m = new Motivo();
        m.setIdMotivo(rs.getInt("ID_MOTIVO"));
        m.setIdArea(rs.getInt("ID_AREA"));
        m.setNombreMotivo(rs.getString("NOMBRE_MOTIVO"));
        return m;
    }
}
