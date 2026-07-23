package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Cuatrimestre;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CuatrimestreDao {

    public List<Cuatrimestre> findAll() {
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
}