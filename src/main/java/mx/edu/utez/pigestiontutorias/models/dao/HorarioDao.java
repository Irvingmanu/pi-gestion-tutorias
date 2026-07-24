package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Horario;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HorarioDao {

    // Lista los horarios activos de un tutor, para el <select> del alumno
    // al llenar la solicitud.
    public List<Horario> findDisponiblesByTutor(int idTutor) {
        List<Horario> lista = new ArrayList<>();

        String sql = "SELECT ID_HORARIO, ID_TUTOR, DIA_SEMANA, " +
                "EXTRACT(HOUR FROM HORA_DESDE) AS HD_HORA, EXTRACT(MINUTE FROM HORA_DESDE) AS HD_MIN, " +
                "EXTRACT(HOUR FROM HORA_HASTA) AS HH_HORA, EXTRACT(MINUTE FROM HORA_HASTA) AS HH_MIN " +
                "FROM HORARIO_ATENCION " +
                "WHERE ID_TUTOR = ? AND ACTIVO = 'S' " +
                "ORDER BY ID_HORARIO";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTutor);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Horario h = new Horario();
                    h.setIdHorario(rs.getInt("ID_HORARIO"));
                    h.setIdTutor(rs.getInt("ID_TUTOR"));
                    h.setDiaSemana(rs.getString("DIA_SEMANA"));
                    h.setHoraDesde(formatear(rs.getInt("HD_HORA"), rs.getInt("HD_MIN")));
                    h.setHoraHasta(formatear(rs.getInt("HH_HORA"), rs.getInt("HH_MIN")));
                    h.setActivo("S");
                    lista.add(h);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener los horarios del tutor: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    private String formatear(int hora, int minuto) {
        return String.format("%02d:%02d", hora, minuto);
    }
}
