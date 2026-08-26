package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Horario;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de acceso a datos para los horarios de atención de los tutores.
 * Las operaciones genéricas del contrato {@link Dao} no están implementadas;
 * únicamente expone la consulta de horarios disponibles por tutor.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-23
 */
public class HorarioDao implements Dao<Horario, Integer> {

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @param entidad el horario a crear
     * @return siempre {@code false}
     */
    @Override
    public boolean create(Horario entidad) {
        return false;
    }

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @return siempre {@code null}
     */
    @Override
    public List<Horario> getAll() {
        return null;
    }

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @param id el identificador del horario
     * @return siempre {@code null}
     */
    @Override
    public Horario getById(Integer id) {
        return null;
    }

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @param entidad el horario a actualizar
     * @return siempre {@code false}
     */
    @Override
    public boolean update(Horario entidad) {
        return false;
    }

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @param id el identificador del horario
     * @return siempre {@code false}
     */
    @Override
    public boolean delete(Integer id) {
        return false;
    }

    /**
     * Obtiene los horarios de atención activos de un tutor, ordenados por identificador.
     * @param idTutor el identificador del tutor
     * @return la lista de horarios disponibles del tutor; vacía si no tiene o si ocurre un error de base de datos
     */
    public List<Horario> findDisponiblesByTutor(int idTutor) {
        List<Horario> lista = new ArrayList<>();

        String sql = "SELECT ID_HORARIO, ID_TUTOR, DIA_SEMANA, " +
                "EXTRACT(HOUR FROM HORA_DESDE) AS HD_HORA, EXTRACT(MINUTE FROM HORA_DESDE) AS HD_MIN, " +
                "EXTRACT(HOUR FROM HORA_HASTA) AS HH_HORA, EXTRACT(MINUTE FROM HORA_HASTA) AS HH_MIN " +
                "FROM HORARIO_ATENCION " +
                "WHERE ID_TUTOR = ? AND ESTADO = 'S' " +
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
                    h.setEstado("S");
                    lista.add(h);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener los horarios del tutor: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Da formato "HH:mm" a una hora y minuto dados, rellenando con ceros a la izquierda.
     * @param hora la hora (0-23)
     * @param minuto el minuto (0-59)
     * @return la hora formateada como cadena "HH:mm"
     */
    private String formatear(int hora, int minuto) {
        return String.format("%02d:%02d", hora, minuto);
    }
}
