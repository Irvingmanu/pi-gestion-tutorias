package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.SesionIndividual;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SesionIndividualDao implements Dao<SesionIndividual, Integer> {

    @Override
    public boolean create(SesionIndividual s) {
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

    @Override
    public List<SesionIndividual> getAll() {
        return null;
    }

    @Override
    public SesionIndividual getById(Integer id) {
        return null;
    }

    @Override
    public boolean update(SesionIndividual entidad) {
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        return false;
    }

    public List<SesionIndividual> getAcuerdosPorAlumno(String matricula) {
        List<SesionIndividual> lista = new ArrayList<>();
        String sql = "SELECT * FROM SESION_INDIVIDUAL WHERE MATRICULA = ? AND ESTADO = 'Tomada' ORDER BY FECHA DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, matricula);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SesionIndividual s = new SesionIndividual();
                    s.setIdSesionIndividual(rs.getInt("ID_SESION_INDIVIDUAL"));
                    s.setIdTutor(rs.getInt("ID_TUTOR"));
                    s.setMatricula(rs.getString("MATRICULA"));
                    s.setFecha(rs.getDate("FECHA"));
                    s.setTemasTratados(rs.getString("TEMAS_TRATADOS"));
                    s.setAcuerdos(rs.getString("ACUERDOS"));
                    int idCanalizacion = rs.getInt("ID_CANALIZACION");
                    s.setIdCanalizacion(rs.wasNull() ? null : idCanalizacion);
                    s.setEstado(rs.getString("ESTADO"));
                    lista.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
