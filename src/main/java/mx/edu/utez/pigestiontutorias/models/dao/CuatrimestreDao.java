package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Cuatrimestre;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsable del acceso de solo lectura al catálogo de cuatrimestres (ADMIN.CUATRIMESTRE).
 * Las operaciones de escritura del contrato {@link Dao} no están implementadas ya que este
 * catálogo se considera fijo.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-22
 */
public class CuatrimestreDao implements Dao<Cuatrimestre, Integer> {

    /**
     * Operación no soportada para este catálogo de solo lectura.
     * @param entidad el cuatrimestre a crear (no utilizado)
     * @return siempre {@code false}
     */
    @Override
    public boolean create(Cuatrimestre entidad) {
        return false;
    }

    /**
     * Obtiene todos los cuatrimestres del catálogo.
     * @return la lista de todos los cuatrimestres registrados
     */
    @Override
    public List<Cuatrimestre> getAll() {
        List<Cuatrimestre> lista = new ArrayList<>();
        String sql = "SELECT ID_CUATRIMESTRE, NUMERO FROM ADMIN.CUATRIMESTRE";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cuatrimestre cuatrimestre = new Cuatrimestre();
                cuatrimestre.setIdCuatrimestre(rs.getInt("ID_CUATRIMESTRE"));
                cuatrimestre.setNumero(rs.getInt("NUMERO"));
                lista.add(cuatrimestre);
            }

        } catch (Exception e) {
            System.err.println("Error al obtener los cuatrimestres: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Operación no soportada para este catálogo de solo lectura.
     * @param id el identificador del cuatrimestre (no utilizado)
     * @return siempre {@code null}
     */
    @Override
    public Cuatrimestre getById(Integer id) {
        return null;
    }

    /**
     * Operación no soportada para este catálogo de solo lectura.
     * @param entidad el cuatrimestre a actualizar (no utilizado)
     * @return siempre {@code false}
     */
    @Override
    public boolean update(Cuatrimestre entidad) {
        return false;
    }

    /**
     * Operación no soportada para este catálogo de solo lectura.
     * @param id el identificador del cuatrimestre (no utilizado)
     * @return siempre {@code false}
     */
    @Override
    public boolean delete(Integer id) {
        return false;
    }
}
