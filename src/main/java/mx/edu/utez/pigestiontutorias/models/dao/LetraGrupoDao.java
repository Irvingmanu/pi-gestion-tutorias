package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.LetraGrupo;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de acceso a datos para el catálogo de letras de grupo (ADMIN.LETRA_GRUPO).
 * Solo implementa la consulta del catálogo completo; el resto de operaciones
 * del contrato {@link Dao} no están implementadas.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-22
 */
public class LetraGrupoDao implements Dao<LetraGrupo, Integer> {

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @param entidad la letra de grupo a crear
     * @return siempre {@code false}
     */
    @Override
    public boolean create(LetraGrupo entidad) {
        return false;
    }

    /**
     * Obtiene el catálogo completo de letras de grupo registradas en el sistema.
     * @return la lista de letras de grupo; vacía si no hay registros o si ocurre un error de base de datos
     */
    @Override
    public List<LetraGrupo> getAll() {
        List<LetraGrupo> lista = new ArrayList<>();
        String sql = "SELECT ID_LETRA, LETRA FROM ADMIN.LETRA_GRUPO";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LetraGrupo lg = new LetraGrupo();
                lg.setIdLetra(rs.getInt("ID_LETRA"));
                lg.setLetra(rs.getString("LETRA"));
                lista.add(lg);
            }

        } catch (Exception e) {
            System.err.println("Error al obtener las letras de grupo: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @param id el identificador de la letra de grupo
     * @return siempre {@code null}
     */
    @Override
    public LetraGrupo getById(Integer id) {
        return null;
    }

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @param entidad la letra de grupo a actualizar
     * @return siempre {@code false}
     */
    @Override
    public boolean update(LetraGrupo entidad) {
        return false;
    }

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @param id el identificador de la letra de grupo
     * @return siempre {@code false}
     */
    @Override
    public boolean delete(Integer id) {
        return false;
    }
}
