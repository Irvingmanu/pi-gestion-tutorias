package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Solicitud;
import mx.edu.utez.pigestiontutorias.models.SolicitudPendienteDTO;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

/**
 * DAO de acceso a datos para las solicitudes de tutoría individual (tabla SOLICITUD_TUTORIA):
 * creación, consulta por tutor/alumno/estatus, cambios de estatus, reprogramación, cancelación
 * automática de solicitudes vencidas y cálculo de horarios ocupados de un tutor.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-23
 */
public class SolicitudDao implements Dao<Solicitud, Integer> {

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @return siempre {@code null}
     */
    @Override
    public List<Solicitud> getAll() {
        return null;
    }

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @param entidad la solicitud a actualizar
     * @return siempre {@code false}
     */
    @Override
    public boolean update(Solicitud entidad) {
        return false;
    }

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @param id el identificador de la solicitud
     * @return siempre {@code false}
     */
    @Override
    public boolean delete(Integer id) {
        return false;
    }

    /**
     * Inserta una nueva solicitud de tutoría individual con estatus inicial "Pendiente".
     * @param solicitud la solicitud a crear, con matrícula, tutor, horario, asunto, descripción, fecha, duración y hora propuestas
     * @return {@code true} si la inserción afectó al menos una fila; {@code false} en caso contrario o si ocurre un error
     */
    @Override
    public boolean create(Solicitud solicitud) {
        String sql = "INSERT INTO SOLICITUD_TUTORIA " +
                "(MATRICULA, ID_TUTOR, ID_HORARIO, ASUNTO, DESCRIPCION, ESTATUS, FECHA_PROPUESTA, DURACION, HORA_PROPUESTA) " +
                "VALUES (?, ?, ?, ?, ?, 'Pendiente', ?, ?, ?)";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, solicitud.getMatricula());
            ps.setInt(2, solicitud.getIdTutor());
            if (solicitud.getIdHorario() != null) {
                ps.setInt(3, solicitud.getIdHorario());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, solicitud.getAsunto());
            ps.setString(5, solicitud.getDescripcion());
            if (solicitud.getFechaPropuesta() != null) {
                ps.setDate(6, solicitud.getFechaPropuesta());
            } else {
                ps.setNull(6, Types.DATE);
            }
            if (solicitud.getDuracion() != null) {
                ps.setInt(7, solicitud.getDuracion());
            } else {
                ps.setNull(7, Types.INTEGER);
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

    /**
     * Verifica si un alumno registró alguna solicitud de tutoría en los últimos 7 días.
     * @param matricula la matrícula del alumno
     * @return {@code true} si tiene una solicitud reciente; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean tieneSolicitudReciente(String matricula) {
        String sql = "SELECT COUNT(*) FROM SOLICITUD_TUTORIA " +
                "WHERE MATRICULA = ? AND FECHA_REGISTRO >= (SYSDATE - 7)";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, matricula);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar solicitudes recientes: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtiene todas las solicitudes de tutoría dirigidas a un tutor, con el nombre del alumno solicitante,
     * ordenadas por identificador descendente.
     * @param idTutor el identificador del tutor
     * @return la lista de solicitudes del tutor; vacía si no hay o si ocurre un error de base de datos
     */
    public List<Solicitud> findByTutor(int idTutor) {
        List<Solicitud> lista = new ArrayList<>();
        String sql = "SELECT s.ID_SOLICITUD, s.MATRICULA, s.ID_TUTOR, " +
                "s.ASUNTO, s.DESCRIPCION, s.ESTATUS, s.FECHA_PROPUESTA, s.NUEVA_FECHA, s.NUEVA_HORA, " +
                "s.DURACION, s.HORA_PROPUESTA, s.FECHA_REGISTRO, " +
                "a.NOMBRES, a.APELLIDO_PATERNO, a.APELLIDO_MATERNO " +
                "FROM SOLICITUD_TUTORIA s " +
                "JOIN ALUMNO a ON a.MATRICULA = s.MATRICULA " +
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

    /**
     * Busca una solicitud de tutoría por su identificador, con el nombre del alumno solicitante.
     * @param idSolicitud el identificador de la solicitud
     * @return la solicitud encontrada, o {@code null} si no existe o si ocurre un error de base de datos
     */
    @Override
    public Solicitud getById(Integer idSolicitud) {
        String sql = "SELECT s.ID_SOLICITUD, s.MATRICULA, s.ID_TUTOR, " +
                "s.ASUNTO, s.DESCRIPCION, s.ESTATUS, s.FECHA_PROPUESTA, s.NUEVA_FECHA, s.NUEVA_HORA, " +
                "s.DURACION, s.HORA_PROPUESTA, s.FECHA_REGISTRO, " +
                "a.NOMBRES, a.APELLIDO_PATERNO, a.APELLIDO_MATERNO " +
                "FROM SOLICITUD_TUTORIA s " +
                "JOIN ALUMNO a ON a.MATRICULA = s.MATRICULA " +
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

    /**
     * Actualiza el estatus de una solicitud de tutoría y registra la fecha de respuesta actual.
     * @param idSolicitud el identificador de la solicitud
     * @param nuevoEstatus el nuevo estatus a asignar
     * @return {@code true} si la actualización afectó al menos una fila; {@code false} en caso contrario o si ocurre un error
     */
    public boolean actualizarEstatus(int idSolicitud, String nuevoEstatus) {
        String sql = "UPDATE SOLICITUD_TUTORIA " +
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

    /**
     * Reprograma una solicitud de tutoría a una nueva fecha y hora, cambiando su estatus a "Reprogramada".
     * @param idSolicitud el identificador de la solicitud
     * @param nuevaFecha la nueva fecha propuesta
     * @param nuevaHora la nueva hora propuesta
     * @return {@code true} si la actualización afectó al menos una fila; {@code false} en caso contrario o si ocurre un error
     */
    public boolean reprogramar(int idSolicitud, Date nuevaFecha, String nuevaHora) {
        String sql = "UPDATE SOLICITUD_TUTORIA " +
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

    /**
     * Cancela automáticamente las solicitudes pendientes cuya fecha y hora propuestas ya se
     * encuentran a menos de un día de distancia (o ya pasaron), marcándolas como "Cancelada".
     * @return el número de solicitudes canceladas por esta operación
     */
    public int cancelarSolicitudesVencidas() {
        String sql = "UPDATE SOLICITUD_TUTORIA " +
                "SET ESTATUS = 'Cancelada', FECHA_RESPUESTA = SYSDATE " +
                "WHERE ESTATUS = 'Pendiente' " +
                "AND (FECHA_PROPUESTA " +
                "     + NVL(TO_NUMBER(SUBSTR(HORA_PROPUESTA, 1, 2)), 0) / 24 " +
                "     + NVL(TO_NUMBER(SUBSTR(HORA_PROPUESTA, 4, 2)), 0) / 1440" +
                ") <= (SYSDATE + 1)";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int filas = ps.executeUpdate();
            if (filas > 0) {
                System.out.println("Cancelación automática: " + filas
                        + " solicitud(es) pendiente(s) vencida(s) cancelada(s).");
            }
            return filas;

        } catch (SQLException e) {
            System.err.println("Error al cancelar solicitudes vencidas: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Calcula, para un tutor y un rango de fechas, el conjunto de horas ya ocupadas por
     * solicitudes confirmadas (considerando su duración en bloques de una hora) y por sesiones
     * individuales ya registradas (que bloquean el día completo).
     * @param idTutor el identificador del tutor
     * @param desde la fecha inicial del rango
     * @param hasta la fecha final del rango
     * @return un mapa de fecha hacia el conjunto de horas ("HH:mm") ocupadas en esa fecha
     */
    public Map<LocalDate, Set<String>> getHorasOcupadas(int idTutor, LocalDate desde, LocalDate hasta) {
        Map<LocalDate, Set<String>> ocupadas = new HashMap<>();

        String sqlSolicitudes = "SELECT FECHA_PROPUESTA, HORA_PROPUESTA, DURACION " +
                "FROM SOLICITUD_TUTORIA " +
                "WHERE ID_TUTOR = ? AND ESTATUS = 'Confirmada' " +
                "AND FECHA_PROPUESTA BETWEEN ? AND ?";

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

    /**
     * Obtiene todas las solicitudes de tutoría registradas por un alumno, ordenadas de la más reciente a la más antigua.
     * @param matricula la matrícula del alumno
     * @return la lista de solicitudes del alumno; vacía si no hay o si ocurre un error de base de datos
     */
    public List<Solicitud> getSolicitudesByAlumno(String matricula) {
        List<Solicitud> lista = new ArrayList<>();
        String sql = "SELECT s.ID_SOLICITUD, s.MATRICULA, s.ID_TUTOR, " +
                "s.ASUNTO, s.DESCRIPCION, s.ESTATUS, s.FECHA_PROPUESTA, s.NUEVA_FECHA, s.NUEVA_HORA, " +
                "s.DURACION, s.HORA_PROPUESTA, s.FECHA_REGISTRO, " +
                "a.NOMBRES, a.APELLIDO_PATERNO, a.APELLIDO_MATERNO " +
                "FROM SOLICITUD_TUTORIA s " +
                "JOIN ALUMNO a ON a.MATRICULA = s.MATRICULA " +
                "WHERE s.MATRICULA = ? " +
                "ORDER BY s.FECHA_REGISTRO DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, matricula);

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

    /**
     * Obtiene las solicitudes pendientes de todo el sistema registradas dentro de un rango de fechas,
     * con los filtros opcionales indicados, sin filtrar por matrícula.
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param desde la fecha inicial del rango de registro
     * @param hasta la fecha final del rango de registro
     * @return la lista de solicitudes pendientes encontradas, ordenada por fecha propuesta ascendente
     */
    public List<SolicitudPendienteDTO> getSolicitudesPendientesGlobal(Integer idTutor, Integer idCarrera,
                                                                      Integer cuatrimestre, String letra,
                                                                      Date desde, Date hasta) {
        return getSolicitudesPendientesGlobal(idTutor, idCarrera, cuatrimestre, letra, desde, hasta, null);
    }

    /**
     * Obtiene las solicitudes pendientes de todo el sistema registradas dentro de un rango de fechas,
     * con los filtros opcionales indicados, incluyendo datos del alumno, su grupo y el tutor destinatario.
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param desde la fecha inicial del rango de registro
     * @param hasta la fecha final del rango de registro
     * @param matricula la matrícula del alumno a filtrar, o {@code null}/vacía para no filtrar
     * @return la lista de solicitudes pendientes encontradas, ordenada por fecha propuesta ascendente
     */
    public List<SolicitudPendienteDTO> getSolicitudesPendientesGlobal(Integer idTutor, Integer idCarrera,
                                                                      Integer cuatrimestre, String letra,
                                                                      Date desde, Date hasta, String matricula) {
        List<SolicitudPendienteDTO> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT s.ID_SOLICITUD, s.MATRICULA, s.ASUNTO, s.DESCRIPCION, s.ESTATUS, " +
                        "s.FECHA_PROPUESTA, s.HORA_PROPUESTA, s.DURACION, " +
                        "a.NOMBRES AS NOMBRES_ALUMNO, a.APELLIDO_PATERNO AS AP_ALUMNO, a.APELLIDO_MATERNO AS AM_ALUMNO, " +
                        "car.NOMBRE AS NOMBRE_CARRERA, g.CUATRIMESTRE, g.LETRA, " +
                        "t.NUMERO_EMPLEADO AS ID_TUTOR, t.NOMBRES AS NOMBRES_TUTOR, " +
                        "t.APELLIDO_PATERNO AS AP_TUTOR, t.APELLIDO_MATERNO AS AM_TUTOR, " +
                        "t.CORREO_INSTITUCIONAL AS CORREO_TUTOR " +
                        "FROM SOLICITUD_TUTORIA s " +
                        "JOIN ALUMNO a ON a.MATRICULA = s.MATRICULA " +
                        "JOIN GRUPO g ON g.ID_GRUPO = a.ID_GRUPO " +
                        "JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA " +
                        "JOIN TUTOR t ON t.NUMERO_EMPLEADO = s.ID_TUTOR " +
                        "WHERE s.ESTATUS = 'Pendiente' AND TRUNC(s.FECHA_REGISTRO) BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);

        if (idTutor != null) {
            sql.append(" AND s.ID_TUTOR = ? ");
            params.add(idTutor);
        }
        if (idCarrera != null) {
            sql.append(" AND g.ID_CARRERA = ? ");
            params.add(idCarrera);
        }
        if (cuatrimestre != null) {
            sql.append(" AND g.CUATRIMESTRE = ? ");
            params.add(cuatrimestre);
        }
        if (letra != null && !letra.isBlank()) {
            sql.append(" AND g.LETRA = ? ");
            params.add(letra);
        }
        if (matricula != null && !matricula.isBlank()) {
            sql.append(" AND s.MATRICULA = ? ");
            params.add(matricula);
        }
        sql.append(" ORDER BY s.FECHA_PROPUESTA ASC");

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object valor = params.get(i);
                if (valor instanceof Date) {
                    ps.setDate(i + 1, (Date) valor);
                } else if (valor instanceof String) {
                    ps.setString(i + 1, (String) valor);
                } else {
                    ps.setInt(i + 1, (Integer) valor);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSolicitudPendiente(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener las solicitudes pendientes: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Obtiene el detalle completo de una solicitud (alumno, grupo y tutor) para construir el correo
     * recordatorio o de notificación asociado.
     * @param idSolicitud el identificador de la solicitud
     * @return el detalle de la solicitud, o {@code null} si no existe o si ocurre un error de base de datos
     */
    public SolicitudPendienteDTO getDetalleParaRecordatorio(int idSolicitud) {
        String sql = "SELECT s.ID_SOLICITUD, s.MATRICULA, s.ASUNTO, s.DESCRIPCION, s.ESTATUS, " +
                "s.FECHA_PROPUESTA, s.HORA_PROPUESTA, s.DURACION, " +
                "a.NOMBRES AS NOMBRES_ALUMNO, a.APELLIDO_PATERNO AS AP_ALUMNO, a.APELLIDO_MATERNO AS AM_ALUMNO, " +
                "car.NOMBRE AS NOMBRE_CARRERA, g.CUATRIMESTRE, g.LETRA, " +
                "t.NUMERO_EMPLEADO AS ID_TUTOR, t.NOMBRES AS NOMBRES_TUTOR, " +
                "t.APELLIDO_PATERNO AS AP_TUTOR, t.APELLIDO_MATERNO AS AM_TUTOR, " +
                "t.CORREO_INSTITUCIONAL AS CORREO_TUTOR " +
                "FROM SOLICITUD_TUTORIA s " +
                "JOIN ALUMNO a ON a.MATRICULA = s.MATRICULA " +
                "JOIN GRUPO g ON g.ID_GRUPO = a.ID_GRUPO " +
                "JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA " +
                "JOIN TUTOR t ON t.NUMERO_EMPLEADO = s.ID_TUTOR " +
                "WHERE s.ID_SOLICITUD = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearSolicitudPendiente(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener el detalle de la solicitud: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Construye un objeto {@link SolicitudPendienteDTO} a partir de la fila actual de un ResultSet,
     * combinando los datos de la solicitud con los del alumno, su grupo y el tutor destinatario.
     * @param rs el ResultSet posicionado en la fila a mapear
     * @return el DTO de solicitud pendiente mapeado
     * @throws SQLException si ocurre un error al leer las columnas del ResultSet
     */
    private SolicitudPendienteDTO mapearSolicitudPendiente(ResultSet rs) throws SQLException {
        SolicitudPendienteDTO dto = new SolicitudPendienteDTO();
        dto.setIdSolicitud(rs.getInt("ID_SOLICITUD"));
        dto.setMatricula(rs.getString("MATRICULA"));
        dto.setAsunto(rs.getString("ASUNTO"));
        dto.setDescripcion(rs.getString("DESCRIPCION"));
        dto.setEstatus(rs.getString("ESTATUS"));
        dto.setFechaPropuesta(rs.getDate("FECHA_PROPUESTA"));
        dto.setHoraPropuesta(rs.getString("HORA_PROPUESTA"));
        int duracion = rs.getInt("DURACION");
        dto.setDuracion(rs.wasNull() ? null : duracion);

        String apMaternoAlumno = rs.getString("AM_ALUMNO");
        String apellidosAlumno = rs.getString("AP_ALUMNO")
                + (apMaternoAlumno != null && !apMaternoAlumno.isBlank() ? " " + apMaternoAlumno : "");
        dto.setNombreAlumno(rs.getString("NOMBRES_ALUMNO") + " " + apellidosAlumno);
        dto.setGrupoAsignado(rs.getString("NOMBRE_CARRERA") + " " + rs.getInt("CUATRIMESTRE") + "°" + rs.getString("LETRA"));

        dto.setIdTutor(rs.getInt("ID_TUTOR"));
        String apMaternoTutor = rs.getString("AM_TUTOR");
        String apellidosTutor = rs.getString("AP_TUTOR")
                + (apMaternoTutor != null && !apMaternoTutor.isBlank() ? " " + apMaternoTutor : "");
        dto.setNombreTutor(rs.getString("NOMBRES_TUTOR") + " " + apellidosTutor);
        dto.setCorreoTutor(rs.getString("CORREO_TUTOR"));

        return dto;
    }

    /**
     * Construye un objeto {@link Solicitud} a partir de la fila actual de un ResultSet,
     * incluyendo el nombre completo del alumno solicitante.
     * @param rs el ResultSet posicionado en la fila a mapear
     * @return la solicitud mapeada con sus campos principales y el nombre del alumno
     * @throws Exception si ocurre un error al leer las columnas del ResultSet
     */
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

        String apellidoM = rs.getString("APELLIDO_MATERNO");
        String apellidos = rs.getString("APELLIDO_PATERNO") + (apellidoM != null && !apellidoM.isBlank() ? " " + apellidoM : "");
        solicitud.setApellidosAlumno(apellidos);

        return solicitud;
    }
}