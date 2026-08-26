package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Motivo;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de acceso a datos para los motivos de canalización asociados a un área
 * de apoyo (tabla MOTIVO_AREA), con operaciones CRUD y consulta por área.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-23
 */
public class MotivoDAO implements Dao<Motivo, Integer> {

    /**
     * Obtiene los motivos registrados para un área de apoyo específica.
     * @param idArea el identificador del área de apoyo
     * @return la lista de motivos asociados al área; vacía si no hay registros o si ocurre un error de base de datos
     */
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

    /**
     * Inserta un nuevo motivo asociado a un área de apoyo.
     * @param entidad el motivo a crear, con el área y nombre ya asignados
     * @return {@code true} si la inserción afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    @Override
    public boolean create(Motivo entidad) {
        String sql = "INSERT INTO MOTIVO_AREA(ID_AREA, NOMBRE) VALUES(?, ?)";
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

    /**
     * Inserta un nuevo motivo asociado a un área de apoyo y devuelve el identificador generado.
     * @param entidad el motivo a crear, con el área y nombre ya asignados
     * @return el identificador (ID_MOTIVO) generado para el nuevo motivo, o -1 si la inserción falló o no se pudo obtener la clave generada
     */
    public int createAndGetId(Motivo entidad) {
        String sql = "INSERT INTO MOTIVO_AREA(ID_AREA, NOMBRE) VALUES(?, ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_MOTIVO"})) {

            ps.setInt(1, entidad.getIdArea());
            ps.setString(2, entidad.getNombreMotivo());

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

    /**
     * Obtiene todos los motivos registrados en el sistema, de todas las áreas.
     * @return la lista completa de motivos; vacía si no hay registros o si ocurre un error de base de datos
     */
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

    /**
     * Busca un motivo por su identificador.
     * @param id el identificador del motivo
     * @return el motivo encontrado, o {@code null} si no existe o si ocurre un error de base de datos
     */
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

    /**
     * Actualiza el nombre de un motivo existente.
     * @param entidad el motivo con el identificador y el nuevo nombre a aplicar
     * @return {@code true} si la actualización afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    @Override
    public boolean update(Motivo entidad) {
        String sql = "UPDATE MOTIVO_AREA SET NOMBRE = ? WHERE ID_MOTIVO = ?";
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

    /**
     * Elimina un motivo por su identificador.
     * @param id el identificador del motivo a eliminar
     * @return {@code true} si la eliminación afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
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

    /**
     * Construye un objeto {@link Motivo} a partir de la fila actual de un ResultSet.
     * @param rs el ResultSet posicionado en la fila a mapear
     * @return el motivo mapeado con sus campos ID_MOTIVO, ID_AREA y NOMBRE
     * @throws SQLException si ocurre un error al leer las columnas del ResultSet
     */
    private Motivo mapearMotivo(ResultSet rs) throws SQLException {
        Motivo m = new Motivo();
        m.setIdMotivo(rs.getInt("ID_MOTIVO"));
        m.setIdArea(rs.getInt("ID_AREA"));
        m.setNombreMotivo(rs.getString("NOMBRE"));
        return m;
    }
}
