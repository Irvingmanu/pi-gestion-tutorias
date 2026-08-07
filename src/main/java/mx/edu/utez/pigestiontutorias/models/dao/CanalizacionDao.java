package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.Area;
import mx.edu.utez.pigestiontutorias.models.Canalizacion;
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
    // transacción que el UPDATE de SESION_INDIVIDUAL. Genera el TOKEN_CONFIRMACION y lo deja en
    // el objeto "c", pero NO manda el correo aquí — eso debe esperar a que el llamador confirme
    // el commit, para no notificar al encargado de un registro que después se puede revertir.
    public int crearEnTransaccion(Connection con, Canalizacion c) throws SQLException {
        String token = generarToken();
        String sql = "INSERT INTO CANALIZACION(ID_AREA, ID_MOTIVO, MATRICULA, FECHA_CANALIZACION, ESTATUS, OBSERVACIONES, TOKEN_CONFIRMACION) " +
                "VALUES(?, ?, ?, SYSDATE, 'Pendiente', ?, ?)";

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
                        c.setTokenConfirmacion(token);
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
        if (area == null || area.getCorreoContacto() == null || c.getTokenConfirmacion() == null) {
            return;
        }

        Alumno alumno = alumnoDAO.getById(c.getMatricula());
        String nombreAlumno = alumno != null ? (alumno.getNombres() + " " + alumno.getApellidos()) : "N/D";
        String motivoODetalle = c.getNombreMotivo() != null ? c.getNombreMotivo()
                : (c.getObservaciones() != null ? c.getObservaciones() : "Sin especificar");
        String link = baseUrl + "/confirmar-canalizacion?token=" + c.getTokenConfirmacion();

        emailSender.enviarConfirmacionCanalizacion(area.getCorreoContacto(), area.getEncargado(), area.getNombre(),
                nombreAlumno, c.getMatricula(), motivoODetalle, link);
    }

    // Usado por ConfirmarCanalizacionServlet cuando el encargado del área da clic en el link
    // del correo. Devuelve "ok", "ya_confirmada" (el link ya se habia usado) o "invalido"
    // (el token no existe).
    public String confirmarPorToken(String token) {
        String sqlSelect = "SELECT ESTATUS FROM CANALIZACION WHERE TOKEN_CONFIRMACION = ?";
        String sqlUpdate = "UPDATE CANALIZACION SET ESTATUS = 'Atendida' WHERE TOKEN_CONFIRMACION = ?";

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
            if (!"Pendiente".equals(estatusActual)) {
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
                "ar.NOMBRE AS NOMBRE_AREA, ar.ENCARGADO AS ENCARGADO_AREA, " +
                "ar.CORREO_CONTACTO AS CORREO_CONTACTO_AREA, ar.ENLACE_CITA AS ENLACE_CITA_AREA, " +
                "m.NOMBRE_MOTIVO " +
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
                    c.setEncargadoArea(rs.getString("ENCARGADO_AREA"));
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
}