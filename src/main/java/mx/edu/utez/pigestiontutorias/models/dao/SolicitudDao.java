package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Solicitud;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class SolicitudDao {

    // ---------------------------------------------------------------
    // 1. Insertar una nueva solicitud (la crea el alumno)
    // ---------------------------------------------------------------
    public boolean insertar(Solicitud solicitud) {
        String sql = "INSERT INTO ADMIN.SOLICITUD_TUTORIA " +
                "(MATRICULA, ID_TUTOR, ID_HORARIO, ASUNTO, DESCRIPCION, ESTATUS) " +
                "VALUES (?, ?, ?, ?, ?, 'Pendiente')";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, solicitud.getMatricula());
            ps.setInt(2, solicitud.getIdTutor());
            if (solicitud.getIdHorario() != null) {
                ps.setInt(3, solicitud.getIdHorario());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setString(4, solicitud.getAsunto());
            ps.setString(5, solicitud.getDescripcion());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (Exception e) {
            System.err.println("Error al insertar la solicitud: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ---------------------------------------------------------------
    // 2. Listar todas las solicitudes de un tutor (con datos del alumno)
    // ---------------------------------------------------------------
    public List<Solicitud> findByTutor(int idTutor) {
        List<Solicitud> lista = new ArrayList<>();
        String sql = "SELECT s.ID_SOLICITUD, s.MATRICULA, s.ID_TUTOR, " +
                "s.ASUNTO, s.DESCRIPCION, s.ESTATUS, s.FECHA_PROPUESTA, " +
                "a.NOMBRES, a.APELLIDOS " +
                "FROM ADMIN.SOLICITUD_TUTORIA s " +
                "JOIN ADMIN.ALUMNO a ON a.MATRICULA = s.MATRICULA " +
                "WHERE s.ID_TUTOR = ? " +
                "ORDER BY s.ID_SOLICITUD DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTutor);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSolicitud(rs));
                }
            }

        } catch (Exception e) {
            System.err.println("Error al obtener las solicitudes del tutor: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    // ---------------------------------------------------------------
    // 3. Obtener una sola solicitud por su id (pantalla de detalle)
    // ---------------------------------------------------------------
    public Solicitud findById(int idSolicitud) {
        String sql = "SELECT s.ID_SOLICITUD, s.MATRICULA, s.ID_TUTOR, " +
                "s.ASUNTO, s.DESCRIPCION, s.ESTATUS, s.FECHA_PROPUESTA, " +
                "a.NOMBRES, a.APELLIDOS " +
                "FROM ADMIN.SOLICITUD_TUTORIA s " +
                "JOIN ADMIN.ALUMNO a ON a.MATRICULA = s.MATRICULA " +
                "WHERE s.ID_SOLICITUD = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearSolicitud(rs);
                }
            }

        } catch (Exception e) {
            System.err.println("Error al obtener la solicitud: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // ---------------------------------------------------------------
    // 4. Aceptar o rechazar una solicitud (actualiza estatus)
    // ---------------------------------------------------------------
    public boolean actualizarEstatus(int idSolicitud, String nuevoEstatus) {
        String sql = "UPDATE ADMIN.SOLICITUD_TUTORIA " +
                "SET ESTATUS = ?, FECHA_RESPUESTA = SYSDATE " +
                "WHERE ID_SOLICITUD = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstatus);
            ps.setInt(2, idSolicitud);

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (Exception e) {
            System.err.println("Error al actualizar el estatus de la solicitud: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Método privado de apoyo para no repetir el mapeo de columnas
    // ---------------------------------------------------------------
    private Solicitud mapearSolicitud(ResultSet rs) throws Exception {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(rs.getInt("ID_SOLICITUD"));
        solicitud.setMatricula(rs.getString("MATRICULA"));
        solicitud.setIdTutor(rs.getInt("ID_TUTOR"));
        solicitud.setAsunto(rs.getString("ASUNTO"));
        solicitud.setDescripcion(rs.getString("DESCRIPCION"));
        solicitud.setEstatus(rs.getString("ESTATUS"));
        solicitud.setFechaPropuesta(rs.getDate("FECHA_PROPUESTA"));
        solicitud.setNombreAlumno(rs.getString("NOMBRES"));
        solicitud.setApellidosAlumno(rs.getString("APELLIDOS"));
        return solicitud;
    }
}
