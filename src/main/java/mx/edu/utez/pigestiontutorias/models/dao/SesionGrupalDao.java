package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.SesionGrupal;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SesionGrupalDao implements Dao<SesionGrupal, Integer> {

    @Override
    public boolean create(SesionGrupal entidad) {
        return false;
    }

    @Override
    public List<SesionGrupal> getAll() {
        return null;
    }

    @Override
    public SesionGrupal getById(Integer id) {
        return null;
    }

    @Override
    public boolean update(SesionGrupal entidad) {
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        return false;
    }

    public List<SesionGrupal> getAcuerdosPorAlumno(String matricula) {
        List<SesionGrupal> lista = new ArrayList<>();
        String sql = "SELECT sg.* FROM SESION_GRUPAL sg " +
                "INNER JOIN ALUMNO a ON sg.ID_CUATRIMESTRE = a.ID_CUATRIMESTRE AND sg.ID_LETRA_GRUPO = a.ID_LETRA_GRUPO " +
                "WHERE a.MATRICULA = ? AND sg.ESTADO = 'Tomada' ORDER BY sg.FECHA DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, matricula);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SesionGrupal s = new SesionGrupal();
                    s.setIdSesionGrupal(rs.getInt("ID_SESION_GRUPAL"));
                    s.setIdLetraGrupo(rs.getInt("ID_LETRA_GRUPO"));
                    s.setIdCuatrimestre(rs.getInt("ID_CUATRIMESTRE"));
                    s.setIdTutor(rs.getInt("ID_TUTOR"));
                    s.setFecha(rs.getDate("FECHA"));
                    s.setTemasTratados(rs.getString("TEMAS_TRATADOS"));
                    s.setAcuerdos(rs.getString("ACUERDOS"));
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
