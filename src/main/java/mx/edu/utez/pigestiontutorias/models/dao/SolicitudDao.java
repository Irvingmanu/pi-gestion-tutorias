package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Solicitud;
import mx.edu.utez.pigestiontutorias.models.SolicitudPendienteDTO;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class SolicitudDao implements Dao<Solicitud, Integer> {

    @Override
    public List<Solicitud> getAll() {
        return null;
    }

    @Override
    public boolean update(Solicitud entidad) {
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        return false;
    }

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

    public List<SolicitudPendienteDTO> getSolicitudesPendientesGlobal(Integer idTutor, Integer idCarrera,
                                                                      Integer cuatrimestre, String letra,
                                                                      Date desde, Date hasta) {
        return getSolicitudesPendientesGlobal(idTutor, idCarrera, cuatrimestre, letra, desde, hasta, null);
    }

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