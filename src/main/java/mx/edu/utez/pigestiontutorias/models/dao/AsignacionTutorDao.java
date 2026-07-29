package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.AsignacionTutor;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AsignacionTutorDao {

    // Regla de negocio: un grupo+cuatrimestre solo puede tener un tutor
    // activo a la vez. Se consulta antes del INSERT para no depender de
    // una restricción UNIQUE en la base de datos.
    public boolean existeAsignacionActiva(int idLetraGrupo, int idCuatrimestre) {
        String sql = "SELECT COUNT(*) FROM ASIGNACION_TUTOR " +
                "WHERE ID_LETRA_GRUPO = ? AND ID_CUATRIMESTRE = ? AND ACTIVO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idLetraGrupo);
            ps.setInt(2, idCuatrimestre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al validar la asignación existente: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

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

    // Necesario para el módulo de Solicitud: dado el grupo y cuatrimestre
    // de un alumno, obtenemos el ID_TUTOR que tiene asignado (regla de
    // negocio: un alumno tiene un solo tutor activo por grupo+cuatrimestre).
    public Integer findIdTutorByGrupoYCuatrimestre(int idLetraGrupo, int idCuatrimestre) {
        String sql = "SELECT ID_TUTOR FROM ASIGNACION_TUTOR " +
                "WHERE ID_LETRA_GRUPO = ? AND ID_CUATRIMESTRE = ? AND ACTIVO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idLetraGrupo);
            ps.setInt(2, idCuatrimestre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID_TUTOR");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar el tutor asignado: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Lista de asignaciones activas para el panel de "Asignación de Tutores",
    // con los datos ya legibles del tutor, grupo y cuatrimestre (via JOIN).
    public List<AsignacionTutor> findAllActivas() {
        List<AsignacionTutor> lista = new ArrayList<>();
        String sql = "SELECT a.ID_ASIGNACION, a.ID_TUTOR, a.ID_LETRA_GRUPO, a.ID_CUATRIMESTRE, a.ACTIVO, " +
                "t.NOMBRES, t.APELLIDOS, lg.LETRA, c.NUMERO " +
                "FROM ASIGNACION_TUTOR a " +
                "JOIN ADMIN.TUTOR t ON t.ID_TUTOR = a.ID_TUTOR " +
                "JOIN ADMIN.LETRA_GRUPO lg ON lg.ID_LETRA = a.ID_LETRA_GRUPO " +
                "JOIN ADMIN.CUATRIMESTRE c ON c.ID_CUATRIMESTRE = a.ID_CUATRIMESTRE " +
                "WHERE a.ACTIVO = 'S' " +
                "ORDER BY a.ID_ASIGNACION DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AsignacionTutor asignacion = new AsignacionTutor();
                asignacion.setIdAsignacion(rs.getInt("ID_ASIGNACION"));
                asignacion.setIdTutor(rs.getInt("ID_TUTOR"));
                asignacion.setIdLetraGrupo(rs.getInt("ID_LETRA_GRUPO"));
                asignacion.setIdCuatrimestre(rs.getInt("ID_CUATRIMESTRE"));
                asignacion.setNombresTutor(rs.getString("NOMBRES"));
                asignacion.setApellidosTutor(rs.getString("APELLIDOS"));
                asignacion.setLetraGrupo(rs.getString("LETRA"));
                asignacion.setNumeroCuatrimestre(rs.getInt("NUMERO"));
                lista.add(asignacion);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener las asignaciones: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    // Borrado lógico: la asignación deja de estar activa pero se conserva
    // el historial (igual patrón que ALUMNO/TUTOR con ACTIVO = 'N').
    public boolean desactivar(int idAsignacion) {
        String sql = "UPDATE ASIGNACION_TUTOR SET ACTIVO = 'N' WHERE ID_ASIGNACION = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAsignacion);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al desactivar la asignación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
