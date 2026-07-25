package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Canalizacion;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CanalizacionDao {

    // Inserta la canalización y devuelve el ID generado, para enlazarlo con SESION_INDIVIDUAL
    public int crearYObtenerId(Canalizacion c) {
        String sql = "INSERT INTO CANALIZACION(ID_AREA, MATRICULA, FECHA_CANALIZACION, ESTATUS, OBSERVACIONES) " +
                "VALUES(?, ?, SYSDATE, 'Pendiente', ?)";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_CANALIZACION"})) {

            ps.setInt(1, c.getIdArea());
            ps.setString(2, c.getMatricula());
            ps.setString(3, c.getObservaciones());

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
}