package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.Area;
import mx.edu.utez.pigestiontutorias.models.Canalizacion;
import mx.edu.utez.pigestiontutorias.models.CanalizacionAlumnoDTO;
import mx.edu.utez.pigestiontutorias.models.CanalizacionRecordatorioDTO;
import mx.edu.utez.pigestiontutorias.utils.EmailSender;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CanalizacionDao implements Dao<Canalizacion, Integer> {

    private final AreaDAO areaDAO = new AreaDAO();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final EmailSender emailSender = new EmailSender();

    @Override
    public boolean create(Canalizacion entidad) {
        // Sin baseUrl no hay forma de armar el link de confirmacion, asi que este camino
        // (que en la practica nadie usa, ver comentario de crearYObtenerId) no manda correo.
        return crearYObtenerId(entidad, null) > 0;
    }

    @Override
    public List<Canalizacion> getAll() {
        return null;
    }

    @Override
    public Canalizacion getById(Integer id) {
        return null;
    }

    @Override
    public boolean update(Canalizacion entidad) {
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        return false;
    }

    // Inserta la canalización (con su propia conexión/commit) y devuelve el ID generado, para
    // enlazarlo con SESION_INDIVIDUAL (create() de la interfaz no sirve aquí porque
    // SesionIndividualServlet y TutoriaServlet necesitan el ID generado para enlazarlo a
    // SESION_INDIVIDUAL.ID_CANALIZACION). Si la inserción tuvo éxito y hay baseUrl, dispara
    // el correo de confirmación al encargado del área.
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

    // Variante que reutiliza la conexión/transacción del llamador (no hace commit ni la cierra):
    // la usa SesionIndividualDao.completarSesion() para que la canalización quede en la misma
    // transacción que el UPDATE de SESION_INDIVIDUAL. Genera el ID_TOKEN y lo deja en
    // el objeto "c", pero NO manda el correo aquí — eso debe esperar a que el llamador confirme
    // el commit, para no notificar al encargado de un registro que después se puede revertir.
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

    // Manda el correo de confirmación al encargado del área. Publico porque tambien lo llama
    // SesionIndividualDao.completarSesion() despues de su propio commit exitoso.
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

    // Usado por ConfirmarCanalizacionServlet cuando el encargado del área da clic en el link
    // del correo. Devuelve "ok", "ya_confirmada" (el link ya se habia usado) o "invalido"
    // (el token no existe).
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

    private String generarToken() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(48);
        for (int i = 0; i < 48; i++) {
            sb.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return sb.toString();
    }

    // Usado por CanalizacionesServlet para que el alumno vea a donde fue canalizado:
    // trae, ademas de la canalizacion, el nombre/encargado/correo/enlace de cita del area
    // y el nombre del motivo (si tiene uno asociado).
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

    // Modal "Alumnos Canalizados" del reporte del coordinador: una fila por canalizacion,
    // con el alumno/grupo destino, el tutor que la registro (via SESION_INDIVIDUAL, unica
    // forma en que se crea una CANALIZACION, ver TutoriaServlet/SesionIndividualServlet) y el
    // area/motivo/estatus. Reutiliza los mismos filtros opcionales de carrera/cuatrimestre/letra
    // que ReportesDao para que el desglose sea coherente con el KPI de la tarjeta; el filtro de
    // idTutor se aplica sobre quien registro la canalizacion (si.ID_TUTOR).
    public List<CanalizacionAlumnoDTO> getCanalizacionesDetalladas(Integer idTutor, Integer idCarrera,
                                                                   Integer cuatrimestre, String letra,
                                                                   Date desde, Date hasta) {
        return getCanalizacionesDetalladas(idTutor, idCarrera, cuatrimestre, letra, desde, hasta, null);
    }

    // Sobrecarga con matricula: cuando el buscador de alumnos del dashboard selecciona un
    // alumno, el modal "Canalizados" se acota a ese alumno en vez del filtro/tutor general.
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

    // Boton "Enviar correo de recordatorio" del modal "Canalizados" del tutor: vuelve a
    // consultar por ID_CANALIZACION (no confia en lo que mande el cliente) y exige que la
    // canalizacion haya sido registrada por ese mismo tutor (via SESION_INDIVIDUAL.ID_TUTOR),
    // para que un tutor no pueda mandar recordatorios de canalizaciones ajenas.
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
