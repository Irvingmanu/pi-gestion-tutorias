package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TutorDao {

    public List<Tutor> findAll() {
        List<Tutor> lista = new ArrayList<>();
        String sql = "SELECT ID_TUTOR, NOMBRES, APELLIDOS FROM ADMIN.TUTOR";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Tutor t = new Tutor();
                t.setIdTutor(rs.getInt("ID_TUTOR"));
                t.setNombres(rs.getString("NOMBRES"));
                t.setApellidos(rs.getString("APELLIDOS"));
                lista.add(t);
            }

        } catch (Exception e) {
            System.err.println("Error al obtener los tutores: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }
}