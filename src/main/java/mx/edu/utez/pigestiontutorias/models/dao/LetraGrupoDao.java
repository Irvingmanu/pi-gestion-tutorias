package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.LetraGrupo;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LetraGrupoDao {

    public List<LetraGrupo> findAll() {
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
}