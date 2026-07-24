package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.SesionIndividual;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class SesionIndividualDao {

    public boolean crear(SesionIndividual s) {
        String sql = "INSERT INTO SESION_INDIVIDUAL " +
                "(ID_TUTOR, MATRICULA, FECHA, TEMAS_TRATADOS, ACUERDOS, ID_CANALIZACION, ESTADO) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, s.getIdTutor());
            ps.setString(2, s.getMatricula());
            ps.setDate(3, s.getFecha());
            ps.setString(4, s.getTemasTratados());
            ps.setString(5, s.getAcuerdos());

            if (s.getIdCanalizacion() != null) {
                ps.setInt(6, s.getIdCanalizacion());
            } else {
                ps.setNull(6, Types.NUMERIC);
            }

            ps.setString(7, s.getEstado() != null ? s.getEstado() : "Registrada");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}