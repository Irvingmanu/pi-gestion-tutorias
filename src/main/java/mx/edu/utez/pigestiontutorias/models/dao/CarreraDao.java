package mx.edu.utez.pigestiontutorias.models.dao;import mx.edu.utez.pigestiontutorias.models.Carrera;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CarreraDao {

    public List<Carrera> findAll() {
        List<Carrera> lista = new ArrayList<>();
        String sql = "SELECT ID_CARRERA, NOMBRE FROM ADMIN.CARRERA";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Carrera c = new Carrera();
                c.setIdCarrera(rs.getInt("ID_CARRERA"));
                c.setNombre(rs.getString("NOMBRE"));
                lista.add(c);
            }

        } catch (Exception e) {
            System.err.println("Error al obtener las carreras: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }
}