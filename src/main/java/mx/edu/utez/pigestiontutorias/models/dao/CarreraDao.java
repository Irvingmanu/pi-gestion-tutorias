package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Carrera;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsable del acceso de solo lectura al catálogo de carreras (CARRERA).
 * Las operaciones de escritura del contrato {@link Dao} no están implementadas ya que este
 * catálogo se considera fijo.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-22
 */
public class CarreraDao implements Dao<Carrera, Integer> {

    /**
     * Operación no soportada para este catálogo de solo lectura.
     * @param entidad la carrera a crear (no utilizado)
     * @return siempre {@code false}
     */
    @Override
    public boolean create(Carrera entidad) {
        return false;
    }

    /**
     * Obtiene todas las carreras del catálogo, ordenadas alfabéticamente por nombre.
     * @return la lista de todas las carreras
     */
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

    /**
     * Obtiene las carreras pertenecientes a una academia específica, ordenadas alfabéticamente por nombre.
     * @param idAcademia el identificador de la academia
     * @return la lista de carreras de la academia indicada
     */
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

    /**
     * Obtiene una carrera por su identificador.
     * @param id el identificador (ID_CARRERA) de la carrera buscada
     * @return la carrera encontrada, o {@code null} si no existe
     */
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

    /**
     * Operación no soportada para este catálogo de solo lectura.
     * @param entidad la carrera a actualizar (no utilizado)
     * @return siempre {@code false}
     */
    @Override
    public boolean update(Carrera entidad) {
        return false;
    }

    /**
     * Operación no soportada para este catálogo de solo lectura.
     * @param id el identificador de la carrera (no utilizado)
     * @return siempre {@code false}
     */
    @Override
    public boolean delete(Integer id) {
        return false;
    }

    /**
     * Construye una entidad {@link Carrera} a partir de la fila actual de un {@link ResultSet}.
     * @param rs el conjunto de resultados posicionado en la fila a mapear
     * @return la carrera construida con los datos de la fila
     * @throws SQLException si ocurre un error al leer las columnas del resultado
     */
    private Carrera mapear(ResultSet rs) throws SQLException {
        Carrera c = new Carrera();
        c.setIdCarrera(rs.getInt("ID_CARRERA"));
        c.setNombre(rs.getString("NOMBRE"));
        c.setNivel(rs.getString("NIVEL"));
        c.setIdAcademia(rs.getInt("ID_ACADEMIA"));
        return c;
    }
}
