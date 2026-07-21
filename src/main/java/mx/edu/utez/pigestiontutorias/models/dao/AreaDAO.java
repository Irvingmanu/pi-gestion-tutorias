package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Area;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AreaDAO implements Dao<Area, Integer> {

    @Override
    public boolean create(Area entidad) {
        String sql = "INSERT INTO AREA_APOYO(NOMBRE, ENCARGADO, CORREO_CONTACTO) VALUES(?, ?, ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getEncargado());
            ps.setString(3, entidad.getCorreoContacto());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Area> getAll() {
        List<Area> listaAreas = new ArrayList<>();
        String sql = "SELECT * FROM AREA_APOYO";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Area a = new Area();
                a.setIdArea(rs.getInt("ID_AREA"));
                a.setNombre(rs.getString("NOMBRE"));
                a.setEncargado(rs.getString("ENCARGADO"));
                a.setCorreoContacto(rs.getString("CORREO_CONTACTO"));
                listaAreas.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaAreas;
    }

    @Override
    public Area getById(Integer id) {
        String sql = "SELECT * FROM AREA_APOYO WHERE ID_AREA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Area a = new Area();
                    a.setIdArea(rs.getInt("ID_AREA"));
                    a.setNombre(rs.getString("NOMBRE"));
                    a.setEncargado(rs.getString("ENCARGADO"));
                    a.setCorreoContacto(rs.getString("CORREO_CONTACTO"));
                    return a;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Area entidad) {
        String sql = "UPDATE AREA_APOYO SET NOMBRE = ?, ENCARGADO = ?, CORREO_CONTACTO = ? WHERE ID_AREA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getEncargado());
            ps.setString(3, entidad.getCorreoContacto());
            ps.setInt(4, entidad.getIdArea());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM AREA_APOYO WHERE ID_AREA = ?";
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

    public boolean existeNombreCorreo(String nombre, String correo) {
        boolean existe = false;
        String sql = "SELECT COUNT(*) FROM AREA_APOYO WHERE nombre = ? OR CORREO_CONTACTO = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, correo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    existe = rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return existe;
    }

    public boolean Duplicado(String nombre, String correo, Integer idArea) {
        int id = (idArea != null) ? idArea : 0;
        String sql = "SELECT COUNT(*) FROM AREA_APOYO WHERE (NOMBRE = ? OR CORREO_CONTACTO = ?) AND ID_AREA != ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.setInt(3, id);

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
}
