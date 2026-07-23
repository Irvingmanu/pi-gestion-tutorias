package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.AsignacionTutor;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AsignacionTutorDao {

    public boolean insertar(AsignacionTutor asignacion) {
        boolean resultado = false;
        String sql = "INSERT INTO ASIGNACION_TUTOR (ID_TUTOR, ID_LETRA_GRUPO, ID_CUATRIMESTRE, ACTIVO) VALUES (?, ?, ?, ?)";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, asignacion.getIdTutor());
            ps.setInt(2, asignacion.getIdLetraGrupo());
            ps.setInt(3, asignacion.getIdCuatrimestre());
            ps.setString(4, "S"); // Cambiado a 'S' según la restricción de Oracle

            resultado = ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("=== ERROR SQL AL INSERTAR ASIGNACIÓN ===");
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("Código de error: " + e.getErrorCode());
            e.printStackTrace();
        }

        return resultado;
    }
}