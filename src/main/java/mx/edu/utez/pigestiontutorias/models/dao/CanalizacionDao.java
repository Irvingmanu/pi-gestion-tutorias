package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.*;
import mx.edu.utez.pigestiontutorias.utils.EmailSender;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsable del acceso a datos de las canalizaciones de alumnos a áreas de apoyo
 * (CANALIZACION), incluyendo la generación de tokens de confirmación, el envío del correo
 * de confirmación al área, la confirmación por token y las consultas detalladas para
 * reportes y recordatorios.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-23
 */
public class CanalizacionDao implements Dao<Canalizacion, Integer> {

    private final AreaDAO areaDAO = new AreaDAO();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final EmailSender emailSender = new EmailSender();

    /**
     * Crea una nueva canalización sin enviar correo de confirmación.
     * @param entidad la canalización a crear
     * @return {@code true} si la inserción generó un identificador válido; {@code false} en caso contrario
     */
    @Override
    public boolean create(Canalizacion entidad) {

        return crearYObtenerId(entidad, null) > 0;
    }

    /**
     * Operación no soportada; las canalizaciones se consultan mediante métodos específicos
     * como {@link #getByMatricula(String)} o {@link #getCanalizacionesDetalladas}.
     * @return siempre {@code null}
     */
    @Override
    public List<Canalizacion> getAll() {
        return null;
    }

    /**
     * Operación no soportada; las canalizaciones se consultan mediante métodos específicos.
     * @param id el identificador de la canalización (no utilizado)
     * @return siempre {@code null}
     */
    @Override
    public Canalizacion getById(Integer id) {
        return null;
    }

    /**
     * Operación no soportada; las canalizaciones no se actualizan directamente, solo cambian de estatus
     * mediante {@link #confirmarPorToken(String)}.
     * @param entidad la canalización a actualizar (no utilizado)
     * @return siempre {@code false}
     */
    @Override
    public boolean update(Canalizacion entidad) {
        return false;
    }

    /**
     * Operación no soportada; las canalizaciones no se eliminan.
     * @param id el identificador de la canalización (no utilizado)
     * @return siempre {@code false}
     */
    @Override
    public boolean delete(Integer id) {
        return false;
    }

    /**
     * Crea una canalización y, si se proporciona una URL base, envía el correo de confirmación
     * al área de apoyo correspondiente.
     * @param c la canalización a crear
     * @param baseUrl la URL base de la aplicación usada para construir el enlace de confirmación,
     *                o {@code null} si no se desea enviar correo
     * @return el ID_CANALIZACION generado, o -1 si la creación falla
     */
    public int crearYObtenerId(Canalizacion c, String baseUrl) {
        try (Connection con = SQLConnector.getConnection()) {
            int idGenerado = crearEnTransaccion(con, c);
            if (idGenerado > 0 && baseUrl != null) {
                enviarCorreoConfirmacion(c, baseUrl);
            }
            return idGenerado;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Inserta una canalización dentro de una transacción externa dada, generando un token
     * único de confirmación que se asigna a la entidad tras la inserción exitosa.
     * @param con la conexión activa sobre la cual se ejecuta la inserción
     * @param c la canalización a crear
     * @return el ID_CANALIZACION generado, o -1 si la inserción no afecta filas
     * @throws SQLException si ocurre un error al ejecutar la inserción
     */
    public int crearEnTransaccion(Connection con, Canalizacion c) throws SQLException {
        String token = generarToken();
        String sql = "INSERT INTO CANALIZACION(ID_AREA, ID_MOTIVO, MATRICULA, FECHA_CANALIZACION, ESTATUS, OBSERVACIONES, ID_TOKEN) " +
                "VALUES(?, ?, ?, SYSDATE, 'En proceso', ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_CANALIZACION"})) {
            ps.setInt(1, c.getIdArea());
            if (c.getIdMotivo() != null) {
                ps.setInt(2, c.getIdMotivo());
            } else {
                ps.setNull(2, Types.NUMERIC);
            }
            ps.setString(3, c.getMatricula());
            ps.setString(4, c.getObservaciones());
            ps.setString(5, token);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        c.setIdToken(token);
                        return keys.getInt(1);
                    }
                }
            }
            return -1;
        }
    }

    /**
     * Envía al correo de contacto del área el correo de confirmación de una canalización,
     * construyendo el enlace de confirmación a partir del token generado. No hace nada si
     * el área, su correo de contacto o el token no están disponibles.
     * @param c la canalización cuyo correo de confirmación se enviará
     * @param baseUrl la URL base de la aplicación usada para construir el enlace de confirmación
     */
    public void enviarCorreoConfirmacion(Canalizacion c, String baseUrl) {
        Area area = areaDAO.getById(c.getIdArea());
        if (area == null || area.getCorreoContacto() == null || c.getIdToken() == null) {
            return;
        }

        Alumno alumno = alumnoDAO.getById(c.getMatricula());
        String nombreAlumno = alumno != null ? (alumno.getNombres() + " " + alumno.getApellidos()) : "N/D";
        String motivoODetalle = c.getNombreMotivo() != null ? c.getNombreMotivo()
                : (c.getObservaciones() != null ? c.getObservaciones() : "Sin especificar");
        String link = baseUrl + "/confirmar-canalizacion?token=" + c.getIdToken();

        emailSender.enviarConfirmacionCanalizacion(area.getCorreoContacto(), area.getEncargado(), area.getNombre(),
                nombreAlumno, c.getMatricula(), motivoODetalle, link);
    }

    /**
     * Confirma una canalización a partir de su token, cambiando su estatus a "Atendido"
     * únicamente si estaba "En proceso".
     * @param token el token de confirmación de la canalización
     * @return "ok" si se confirmó correctamente, "ya_confirmada" si ya no estaba en proceso,
     *         o "invalido" si el token no existe o si ocurre un error
     */
    public String confirmarPorToken(String token) {
        String sqlSelect = "SELECT ESTATUS FROM CANALIZACION WHERE ID_TOKEN = ?";
        String sqlUpdate = "UPDATE CANALIZACION SET ESTATUS = 'Atendido' WHERE ID_TOKEN = ?";

        try (Connection con = SQLConnector.getConnection()) {
            String estatusActual = null;
            try (PreparedStatement ps = con.prepareStatement(sqlSelect)) {
                ps.setString(1, token);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        estatusActual = rs.getString("ESTATUS");
                    }
                }
            }

            if (estatusActual == null) {
                return "invalido";
            }
            if (!"En proceso".equals(estatusActual)) {
                return "ya_confirmada";
            }

            try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                ps.setString(1, token);
                ps.executeUpdate();
            }
            return "ok";
        } catch (SQLException e) {
            e.printStackTrace();
            return "invalido";
        }
    }

    /**
     * Genera un token aleatorio y criptográficamente seguro de 48 caracteres alfanuméricos,
     * usado para confirmar canalizaciones por enlace de correo.
     * @return el token generado
     */
    private String generarToken() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(48);
        for (int i = 0; i < 48; i++) {
            sb.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return sb.toString();
    }

    /**
     * Obtiene todas las canalizaciones de un alumno, incluyendo los datos del área, su encargado
     * y el motivo asociado, ordenadas de la más reciente a la más antigua.
     * @param matricula la matrícula del alumno
     * @return la lista de canalizaciones del alumno
     */
    public List<Canalizacion> getByMatricula(String matricula) {
        List<Canalizacion> lista = new ArrayList<>();
        String sql = "SELECT c.ID_CANALIZACION, c.ID_AREA, c.ID_MOTIVO, c.MATRICULA, c.FECHA_CANALIZACION, " +
                "c.ESTATUS, c.OBSERVACIONES, " +
                "ar.NOMBRE AS NOMBRE_AREA, ar.NOMBRES AS NOMBRES_ENCARGADO, " +
                "ar.APELLIDO_PATERNO AS APELLIDO_PATERNO_ENCARGADO, ar.APELLIDO_MATERNO AS APELLIDO_MATERNO_ENCARGADO, " +
                "ar.CORREO_CONTACTO AS CORREO_CONTACTO_AREA, ar.ENLACE_CITA AS ENLACE_CITA_AREA, " +
                "m.NOMBRE AS NOMBRE_MOTIVO " +
                "FROM CANALIZACION c " +
                "JOIN AREA_APOYO ar ON ar.ID_AREA = c.ID_AREA " +
                "LEFT JOIN MOTIVO_AREA m ON m.ID_MOTIVO = c.ID_MOTIVO " +
                "WHERE c.MATRICULA = ? " +
                "ORDER BY c.FECHA_CANALIZACION DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, matricula);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Canalizacion c = new Canalizacion();
                    c.setIdCanalizacion(rs.getInt("ID_CANALIZACION"));
                    c.setIdArea(rs.getInt("ID_AREA"));

                    int idMotivo = rs.getInt("ID_MOTIVO");
                    c.setIdMotivo(rs.wasNull() ? null : idMotivo);

                    c.setMatricula(rs.getString("MATRICULA"));
                    c.setFechaCanalizacion(rs.getDate("FECHA_CANALIZACION"));
                    c.setEstatus(rs.getString("ESTATUS"));
                    c.setObservaciones(rs.getString("OBSERVACIONES"));

                    c.setNombreArea(rs.getString("NOMBRE_AREA"));

                    String nombresEncargado = rs.getString("NOMBRES_ENCARGADO");
                    String apPaterno = rs.getString("APELLIDO_PATERNO_ENCARGADO");
                    String apMaterno = rs.getString("APELLIDO_MATERNO_ENCARGADO");
                    StringBuilder encargado = new StringBuilder(nombresEncargado != null ? nombresEncargado : "");
                    if (apPaterno != null && !apPaterno.isBlank()) encargado.append(' ').append(apPaterno);
                    if (apMaterno != null && !apMaterno.isBlank()) encargado.append(' ').append(apMaterno);
                    c.setEncargadoArea(encargado.toString());

                    c.setCorreoContactoArea(rs.getString("CORREO_CONTACTO_AREA"));
                    c.setEnlaceCitaArea(rs.getString("ENLACE_CITA_AREA"));
                    c.setNombreMotivo(rs.getString("NOMBRE_MOTIVO"));

                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene canalizaciones detalladas (con datos del alumno, grupo, área, motivo y tutor)
     * filtradas por tutor, carrera, cuatrimestre, letra de grupo y rango de fechas, sin filtro de matrícula.
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacío para no filtrar
     * @param desde la fecha inicial del rango de canalización
     * @param hasta la fecha final del rango de canalización
     * @return la lista de canalizaciones detalladas que cumplen los filtros
     */
    public List<CanalizacionAlumnoDTO> getCanalizacionesDetalladas(Integer idTutor, Integer idCarrera,
                                                                   Integer cuatrimestre, String letra,
                                                                   Date desde, Date hasta) {
        return getCanalizacionesDetalladas(idTutor, idCarrera, cuatrimestre, letra, desde, hasta, null);
    }

    /**
     * Obtiene canalizaciones detalladas (con datos del alumno, grupo, área, motivo y tutor)
     * filtradas por tutor, carrera, cuatrimestre, letra de grupo, rango de fechas y matrícula,
     * construyendo la consulta dinámicamente según los filtros no nulos recibidos.
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacío para no filtrar
     * @param desde la fecha inicial del rango de canalización
     * @param hasta la fecha final del rango de canalización
     * @param matricula la matrícula del alumno a filtrar, o {@code null}/vacío para no filtrar
     * @return la lista de canalizaciones detalladas que cumplen los filtros
     */
    public List<CanalizacionAlumnoDTO> getCanalizacionesDetalladas(Integer idTutor, Integer idCarrera,
                                                                   Integer cuatrimestre, String letra,
                                                                   Date desde, Date hasta, String matricula) {
        List<CanalizacionAlumnoDTO> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.ID_CANALIZACION, c.MATRICULA, c.FECHA_CANALIZACION, c.ESTATUS, c.OBSERVACIONES, " +
                        "a.NOMBRES AS NOMBRES_ALUMNO, a.APELLIDO_PATERNO AS AP_ALUMNO, a.APELLIDO_MATERNO AS AM_ALUMNO, " +
                        "car.NOMBRE AS NOMBRE_CARRERA, g.CUATRIMESTRE, g.LETRA, " +
                        "ar.NOMBRE AS NOMBRE_AREA, m.NOMBRE AS NOMBRE_MOTIVO, " +
                        "t.NOMBRES AS NOMBRES_TUTOR, t.APELLIDO_PATERNO AS AP_TUTOR, t.APELLIDO_MATERNO AS AM_TUTOR " +
                        "FROM CANALIZACION c " +
                        "JOIN ALUMNO a ON a.MATRICULA = c.MATRICULA " +
                        "JOIN GRUPO g ON g.ID_GRUPO = a.ID_GRUPO " +
                        "JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA " +
                        "JOIN AREA_APOYO ar ON ar.ID_AREA = c.ID_AREA " +
                        "LEFT JOIN MOTIVO_AREA m ON m.ID_MOTIVO = c.ID_MOTIVO " +
                        "LEFT JOIN SESION_INDIVIDUAL si ON si.ID_CANALIZACION = c.ID_CANALIZACION " +
                        "LEFT JOIN TUTOR t ON t.NUMERO_EMPLEADO = si.ID_TUTOR " +
                        "WHERE TRUNC(c.FECHA_CANALIZACION) BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(desde);
        params.add(hasta);

        if (idTutor != null) {
            sql.append(" AND si.ID_TUTOR = ? ");
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
            sql.append(" AND c.MATRICULA = ? ");
            params.add(matricula);
        }
        sql.append(" ORDER BY c.FECHA_CANALIZACION DESC");

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
                    CanalizacionAlumnoDTO dto = new CanalizacionAlumnoDTO();
                    dto.setIdCanalizacion(rs.getInt("ID_CANALIZACION"));
                    dto.setMatricula(rs.getString("MATRICULA"));
                    dto.setFechaCanalizacion(rs.getDate("FECHA_CANALIZACION"));
                    dto.setEstatus(rs.getString("ESTATUS"));
                    dto.setObservaciones(rs.getString("OBSERVACIONES"));
                    dto.setNombreArea(rs.getString("NOMBRE_AREA"));
                    dto.setNombreMotivo(rs.getString("NOMBRE_MOTIVO"));

                    String apMaternoAlumno = rs.getString("AM_ALUMNO");
                    String apellidosAlumno = rs.getString("AP_ALUMNO")
                            + (apMaternoAlumno != null && !apMaternoAlumno.isBlank() ? " " + apMaternoAlumno : "");
                    dto.setNombreAlumno(rs.getString("NOMBRES_ALUMNO") + " " + apellidosAlumno);
                    dto.setGrupoAsignado(rs.getString("NOMBRE_CARRERA") + " " + rs.getInt("CUATRIMESTRE") + "°" + rs.getString("LETRA"));

                    String nombresTutor = rs.getString("NOMBRES_TUTOR");
                    if (nombresTutor != null) {
                        String apMaternoTutor = rs.getString("AM_TUTOR");
                        String apellidosTutor = rs.getString("AP_TUTOR")
                                + (apMaternoTutor != null && !apMaternoTutor.isBlank() ? " " + apMaternoTutor : "");
                        dto.setNombreTutor(nombresTutor + " " + apellidosTutor);
                    }

                    lista.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener las canalizaciones detalladas: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene el detalle de una canalización necesario para reenviar el recordatorio de confirmación,
     * validando que la canalización esté asociada a una sesión individual del tutor indicado.
     * @param idCanalizacion el identificador de la canalización
     * @param idTutorSesion el identificador del tutor de la sesión individual asociada
     * @return el detalle de la canalización para el recordatorio, o {@code null} si no se encuentra o no coincide el tutor
     */
    public CanalizacionRecordatorioDTO getDetalleParaRecordatorio(int idCanalizacion, int idTutorSesion) {
        String sql = "SELECT c.ID_TOKEN, c.ESTATUS, c.OBSERVACIONES, c.MATRICULA, " +
                "a.NOMBRES AS NOMBRES_ALUMNO, a.APELLIDO_PATERNO AS AP_ALUMNO, a.APELLIDO_MATERNO AS AM_ALUMNO, " +
                "ar.NOMBRE AS NOMBRE_AREA, ar.CORREO_CONTACTO, " +
                "ar.NOMBRES AS NOMBRES_ENCARGADO, ar.APELLIDO_PATERNO AS AP_ENCARGADO, ar.APELLIDO_MATERNO AS AM_ENCARGADO, " +
                "m.NOMBRE AS NOMBRE_MOTIVO " +
                "FROM CANALIZACION c " +
                "JOIN ALUMNO a ON a.MATRICULA = c.MATRICULA " +
                "JOIN AREA_APOYO ar ON ar.ID_AREA = c.ID_AREA " +
                "LEFT JOIN MOTIVO_AREA m ON m.ID_MOTIVO = c.ID_MOTIVO " +
                "JOIN SESION_INDIVIDUAL si ON si.ID_CANALIZACION = c.ID_CANALIZACION " +
                "WHERE c.ID_CANALIZACION = ? AND si.ID_TUTOR = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCanalizacion);
            ps.setInt(2, idTutorSesion);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CanalizacionRecordatorioDTO dto = new CanalizacionRecordatorioDTO();
                    dto.setIdToken(rs.getString("ID_TOKEN"));
                    dto.setEstatus(rs.getString("ESTATUS"));
                    dto.setMatricula(rs.getString("MATRICULA"));
                    dto.setNombreArea(rs.getString("NOMBRE_AREA"));
                    dto.setCorreoContactoArea(rs.getString("CORREO_CONTACTO"));

                    String apMaternoAlumno = rs.getString("AM_ALUMNO");
                    String apellidosAlumno = rs.getString("AP_ALUMNO")
                            + (apMaternoAlumno != null && !apMaternoAlumno.isBlank() ? " " + apMaternoAlumno : "");
                    dto.setNombreAlumno(rs.getString("NOMBRES_ALUMNO") + " " + apellidosAlumno);

                    String nombresEncargado = rs.getString("NOMBRES_ENCARGADO");
                    String apPaternoEnc = rs.getString("AP_ENCARGADO");
                    String apMaternoEnc = rs.getString("AM_ENCARGADO");
                    StringBuilder encargado = new StringBuilder(nombresEncargado != null ? nombresEncargado : "");
                    if (apPaternoEnc != null && !apPaternoEnc.isBlank()) encargado.append(' ').append(apPaternoEnc);
                    if (apMaternoEnc != null && !apMaternoEnc.isBlank()) encargado.append(' ').append(apMaternoEnc);
                    dto.setEncargadoArea(encargado.toString());

                    String nombreMotivo = rs.getString("NOMBRE_MOTIVO");
                    dto.setMotivoODetalle(nombreMotivo != null ? nombreMotivo : rs.getString("OBSERVACIONES"));

                    return dto;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener el detalle de la canalización para recordatorio: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
