package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Solicitud;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class SolicitudDao {

    // ---------------------------------------------------------------
    // 1. Insertar una nueva solicitud (la crea el alumno)
    // ---------------------------------------------------------------
    public boolean insertar(Solicitud solicitud) {
        String sql = "INSERT INTO ADMIN.SOLICITUD_TUTORIA " +
                "(MATRICULA, ID_TUTOR, ID_HORARIO, ASUNTO, DESCRIPCION, ESTATUS, FECHA_PROPUESTA, DURACION, HORA_PROPUESTA) " +
                "VALUES (?, ?, ?, ?, ?, 'Pendiente', ?, ?, ?)";

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
            if (solicitud.getFechaPropuesta() != null) {
                ps.setDate(6, solicitud.getFechaPropuesta());
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }
            if (solicitud.getDuracion() != null) {
                ps.setInt(7, solicitud.getDuracion());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            ps.setString(8, solicitud.getHoraPropuesta());

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
                "s.ASUNTO, s.DESCRIPCION, s.ESTATUS, s.FECHA_PROPUESTA, s.NUEVA_FECHA, s.NUEVA_HORA, " +
                "s.DURACION, s.HORA_PROPUESTA, s.FECHA_REGISTRO, " +
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
                "s.ASUNTO, s.DESCRIPCION, s.ESTATUS, s.FECHA_PROPUESTA, s.NUEVA_FECHA, s.NUEVA_HORA, " +
                "s.DURACION, s.HORA_PROPUESTA, s.FECHA_REGISTRO, " +
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
    // 5. Reprogramar: el tutor propone una nueva fecha (contrapropuesta)
    // ---------------------------------------------------------------
    public boolean reprogramar(int idSolicitud, Date nuevaFecha, String nuevaHora) {
        String sql = "UPDATE ADMIN.SOLICITUD_TUTORIA " +
                "SET ESTATUS = 'Reprogramada', NUEVA_FECHA = ?, NUEVA_HORA = ?, FECHA_RESPUESTA = SYSDATE " +
                "WHERE ID_SOLICITUD = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, nuevaFecha);
            ps.setString(2, nuevaHora);
            ps.setInt(3, idSolicitud);

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (Exception e) {
            System.err.println("Error al reprogramar la solicitud: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ---------------------------------------------------------------
    // 6. Horas ya ocupadas del tutor en un rango de fechas, cruzando las
    // solicitudes ya confirmadas y las sesiones individuales agendadas.
    // Se usa para calcular la disponibilidad real que ve el alumno.
    // ---------------------------------------------------------------
    public Map<LocalDate, Set<String>> getHorasOcupadas(int idTutor, LocalDate desde, LocalDate hasta) {
        Map<LocalDate, Set<String>> ocupadas = new HashMap<>();

        String sqlSolicitudes = "SELECT FECHA_PROPUESTA, HORA_PROPUESTA, DURACION " +
                "FROM ADMIN.SOLICITUD_TUTORIA " +
                "WHERE ID_TUTOR = ? AND ESTATUS = 'Confirmada' " +
                "AND FECHA_PROPUESTA BETWEEN ? AND ?";

        // SESION_INDIVIDUAL no guarda la hora de la cita, solo la fecha:
        // si el tutor ya tiene una sesión ese día, se bloquea el día completo
        // para no arriesgarnos a empalmar horarios que no podemos verificar.
        String sqlSesiones = "SELECT FECHA FROM SESION_INDIVIDUAL " +
                "WHERE ID_TUTOR = ? AND FECHA BETWEEN ? AND ?";

        try (Connection con = SQLConnector.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(sqlSolicitudes)) {
                ps.setInt(1, idTutor);
                ps.setDate(2, Date.valueOf(desde));
                ps.setDate(3, Date.valueOf(hasta));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String horaInicio = rs.getString("HORA_PROPUESTA");
                        if (horaInicio == null || rs.getDate("FECHA_PROPUESTA") == null) {
                            continue;
                        }

                        LocalDate fecha = rs.getDate("FECHA_PROPUESTA").toLocalDate();
                        int duracion = rs.getInt("DURACION");
                        int bloques = duracion > 0 ? duracion : 1;

                        String[] partes = horaInicio.split(":");
                        int horaBase = Integer.parseInt(partes[0]);
                        String minutos = partes[1];

                        Set<String> horasDia = ocupadas.computeIfAbsent(fecha, f -> new HashSet<>());
                        for (int i = 0; i < bloques; i++) {
                            horasDia.add(String.format("%02d:%s", horaBase + i, minutos));
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlSesiones)) {
                ps.setInt(1, idTutor);
                ps.setDate(2, Date.valueOf(desde));
                ps.setDate(3, Date.valueOf(hasta));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        LocalDate fecha = rs.getDate("FECHA").toLocalDate();
                        Set<String> horasDia = ocupadas.computeIfAbsent(fecha, f -> new HashSet<>());
                        for (int hora = 0; hora < 24; hora++) {
                            horasDia.add(String.format("%02d:00", hora));
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener las horas ocupadas del tutor: " + e.getMessage());
            e.printStackTrace();
        }

        return ocupadas;
    }

    // ---------------------------------------------------------------
    // 7. Historial de solicitudes de un alumno (vista "Mis Solicitudes")
    // ---------------------------------------------------------------
    public List<Solicitud> getSolicitudesByAlumno(int idAlumno) {
        List<Solicitud> lista = new ArrayList<>();
        String sql = "SELECT s.ID_SOLICITUD, s.MATRICULA, s.ID_TUTOR, " +
                "s.ASUNTO, s.DESCRIPCION, s.ESTATUS, s.FECHA_PROPUESTA, s.NUEVA_FECHA, s.NUEVA_HORA, " +
                "s.DURACION, s.HORA_PROPUESTA, s.FECHA_REGISTRO, " +
                "a.NOMBRES, a.APELLIDOS " +
                "FROM ADMIN.SOLICITUD_TUTORIA s " +
                "JOIN ADMIN.ALUMNO a ON a.MATRICULA = s.MATRICULA " +
                "WHERE a.ID_ALUMNO = ? " +
                "ORDER BY s.FECHA_REGISTRO DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAlumno);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSolicitud(rs));
                }
            }

        } catch (Exception e) {
            System.err.println("Error al obtener las solicitudes del alumno: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
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
        solicitud.setNuevaFecha(rs.getDate("NUEVA_FECHA"));
        solicitud.setNuevaHora(rs.getString("NUEVA_HORA"));
        int duracion = rs.getInt("DURACION");
        solicitud.setDuracion(rs.wasNull() ? null : duracion);
        solicitud.setHoraPropuesta(rs.getString("HORA_PROPUESTA"));
        solicitud.setFechaRegistro(rs.getTimestamp("FECHA_REGISTRO"));
        solicitud.setNombreAlumno(rs.getString("NOMBRES"));
        solicitud.setApellidosAlumno(rs.getString("APELLIDOS"));
        return solicitud;
    }
}
