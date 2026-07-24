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

    // Necesario para el módulo de Solicitud: la sesión solo guarda idUsuario,
    // así que hay que traducirlo al ID_TUTOR real de la tabla TUTOR.
    public Tutor findByIdUsuario(int idUsuario) {
        String sql = "SELECT ID_TUTOR, NOMBRES, APELLIDOS FROM ADMIN.TUTOR WHERE ID_USUARIO = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Tutor t = new Tutor();
                    t.setIdTutor(rs.getInt("ID_TUTOR"));
                    t.setNombres(rs.getString("NOMBRES"));
                    t.setApellidos(rs.getString("APELLIDOS"));
                    return t;
                }
            }

        } catch (Exception e) {
            System.err.println("Error al obtener el tutor por idUsuario: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}