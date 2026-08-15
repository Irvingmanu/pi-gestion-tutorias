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

    private final MotivoDAO motivoDAO = new MotivoDAO();

    @Override
    public boolean create(Area entidad) {
        return createAndGetId(entidad) > 0;
    }

    // Devuelve el ID_AREA generado para poder insertar sus motivos hijos
    public int createAndGetId(Area entidad) {
        String sql = "INSERT INTO AREA_APOYO(NOMBRE, NOMBRES, APELLIDO_PATERNO, APELLIDO_MATERNO, CORREO_CONTACTO, ENLACE_CITA) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_AREA"})) {

            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getNombresEncargado());
            ps.setString(3, entidad.getApellidoPaternoEncargado());
            ps.setString(4, entidad.getApellidoMaternoEncargado());
            ps.setString(5, entidad.getCorreoContacto());
            ps.setString(6, entidad.getEnlaceCita());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean existeNombreArea(String nombre) {
        String sql = "SELECT COUNT(*) FROM AREA_APOYO WHERE NOMBRE = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
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

    public boolean existeNombreArea(String nombre, int idAreaActual) {
        String sql = "SELECT COUNT(*) FROM AREA_APOYO WHERE NOMBRE = ? AND ID_AREA <> ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, idAreaActual);
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

    @Override
    public List<Area> getAll() {
        List<Area> listaAreas = new ArrayList<>();
        String sql = "SELECT * FROM AREA_APOYO";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaAreas.add(mapearArea(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaAreas;
    }

    // Usado por el formulario de tutoria individual: cada Area trae ya cargados sus Motivos
    // para poder generar un <select> por Area sin hacer una consulta aparte por cada una.
    public List<Area> getAllConMotivos() {
        List<Area> areas = getAll();
        for (Area area : areas) {
            area.setMotivos(motivoDAO.getByIdArea(area.getIdArea()));
        }
        return areas;
    }

    @Override
    public Area getById(Integer id) {
        String sql = "SELECT * FROM AREA_APOYO WHERE ID_AREA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Area a = mapearArea(rs);
                    a.setMotivos(motivoDAO.getByIdArea(a.getIdArea()));
                    return a;
                }
            }
        } catch (SQLException e)    {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Area entidad) {
        // ENLACE_CITA usa COALESCE porque el formulario de coordinador (formulario-area.jsp)
        // todavia no tiene un campo para capturarlo: si viene null aqui, se conserva el valor
        // que ya estaba en BD en vez de borrarlo en cada edicion de nombre/encargado/correo.
        String sql = "UPDATE AREA_APOYO SET NOMBRE = ?, NOMBRES = ?, APELLIDO_PATERNO = ?, APELLIDO_MATERNO = ?, CORREO_CONTACTO = ?, " +
                "ENLACE_CITA = COALESCE(?, ENLACE_CITA) WHERE ID_AREA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getNombresEncargado());
            ps.setString(3, entidad.getApellidoPaternoEncargado());
            ps.setString(4, entidad.getApellidoMaternoEncargado());
            ps.setString(5, entidad.getCorreoContacto());
            ps.setString(6, entidad.getEnlaceCita());
            ps.setInt(7, entidad.getIdArea());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sqlMotivos = "DELETE FROM MOTIVO_AREA WHERE ID_AREA = ?";
        String sqlArea = "DELETE FROM AREA_APOYO WHERE ID_AREA = ?";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement psMotivos = con.prepareStatement(sqlMotivos)) {
                psMotivos.setInt(1, id);
                psMotivos.executeUpdate();
            }

            int filasAfectadas;
            try (PreparedStatement psArea = con.prepareStatement(sqlArea)) {
                psArea.setInt(1, id);
                filasAfectadas = psArea.executeUpdate();
            }

            con.commit();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private Area mapearArea(ResultSet rs) throws SQLException {
        Area a = new Area();
        a.setIdArea(rs.getInt("ID_AREA"));
        a.setNombre(rs.getString("NOMBRE"));
        a.setNombresEncargado(rs.getString("NOMBRES"));
        a.setApellidoPaternoEncargado(rs.getString("APELLIDO_PATERNO"));
        a.setApellidoMaternoEncargado(rs.getString("APELLIDO_MATERNO"));
        a.setCorreoContacto(rs.getString("CORREO_CONTACTO"));
        a.setEnlaceCita(rs.getString("ENLACE_CITA"));
        return a;
    }
}
