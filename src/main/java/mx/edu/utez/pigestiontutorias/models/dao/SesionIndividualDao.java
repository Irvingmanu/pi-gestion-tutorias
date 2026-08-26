package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.AtencionAlumnoDTO;
import mx.edu.utez.pigestiontutorias.models.Canalizacion;
import mx.edu.utez.pigestiontutorias.models.SesionIndividual;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de acceso a datos para las sesiones individuales de tutoría (tabla SESION_INDIVIDUAL),
 * incluyendo su registro, consulta de historial y pendientes, y el proceso transaccional de
 * completar una sesión generando las canalizaciones correspondientes a sus motivos.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-23
 */
public class SesionIndividualDao implements Dao<SesionIndividual, Integer> {

    private final CanalizacionDao canalizacionDao = new CanalizacionDao();

    /**
     * Inserta una nueva sesión individual de tutoría.
     * @param s la sesión individual a crear, con tutor, matrícula, fecha, hora, temas y acuerdos
     * @return {@code true} si la inserción afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    @Override
    public boolean create(SesionIndividual s) {
        String sql = "INSERT INTO SESION_INDIVIDUAL " +
                "(ID_TUTOR, MATRICULA, FECHA, HORA, TEMAS_TRATADOS, ACUERDOS, ID_CANALIZACION, ESTADO, ESTATUS_ASISTENCIA, ORIGEN) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, s.getIdTutor());
            ps.setString(2, s.getMatricula());
            ps.setDate(3, s.getFecha());
            ps.setString(4, s.getHora());
            ps.setString(5, s.getTemasTratados());
            ps.setString(6, s.getAcuerdos());

            if (s.getIdCanalizacion() != null) {
                ps.setInt(7, s.getIdCanalizacion());
            } else {
                ps.setNull(7, Types.NUMERIC);
            }

            ps.setString(8, s.getEstado() != null ? s.getEstado() : "Completado");
            ps.setString(9, s.getEstatusAsistencia() != null ? s.getEstatusAsistencia() : "Presente");
            ps.setString(10, s.getOrigen() != null ? s.getOrigen() : "Espontanea");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @return siempre {@code null}
     */
    @Override
    public List<SesionIndividual> getAll() {
        return null;
    }

    /**
     * Busca una sesión individual por su identificador.
     * @param id el identificador de la sesión individual
     * @return la sesión encontrada, o {@code null} si no existe o si ocurre un error de base de datos
     */
    @Override
    public SesionIndividual getById(Integer id) {
        String sql = "SELECT * FROM SESION_INDIVIDUAL WHERE ID_SESION_INDIVIDUAL = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearSesion(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @param entidad la sesión individual a actualizar
     * @return siempre {@code false}
     */
    @Override
    public boolean update(SesionIndividual entidad) {
        return false;
    }

    /**
     * Operación no implementada del contrato {@link Dao}.
     * @param id el identificador de la sesión individual
     * @return siempre {@code false}
     */
    @Override
    public boolean delete(Integer id) {
        return false;
    }

    /**
     * Obtiene el historial de sesiones individuales completadas de un alumno, ordenadas de la más reciente a la más antigua.
     * @param matricula la matrícula del alumno
     * @return la lista de sesiones individuales completadas del alumno; vacía si no hay o si ocurre un error de base de datos
     */
    public List<SesionIndividual> getAcuerdosPorAlumno(String matricula) {
        List<SesionIndividual> lista = new ArrayList<>();
        String sql = "SELECT * FROM SESION_INDIVIDUAL WHERE MATRICULA = ? AND ESTADO = 'Completado' ORDER BY FECHA DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, matricula);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSesion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene las sesiones individuales pendientes (programadas) de un tutor, ordenadas por fecha ascendente.
     * @param idTutor el identificador del tutor
     * @return la lista de sesiones pendientes del tutor; vacía si no hay o si ocurre un error de base de datos
     */
    public List<SesionIndividual> getSesionesProgramadasByTutor(int idTutor) {
        List<SesionIndividual> lista = new ArrayList<>();
        String sql = "SELECT * FROM SESION_INDIVIDUAL WHERE ID_TUTOR = ? AND ESTADO = 'Pendiente' ORDER BY FECHA";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTutor);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSesion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene el historial de sesiones individuales completadas de un tutor, con filtros opcionales
     * de origen y rango de fechas, ordenadas de la más reciente a la más antigua.
     * @param idTutor el identificador del tutor
     * @param origen el origen de la sesión ("Programada" o "Espontanea") a filtrar, o {@code null}/vacío para no filtrar
     * @param fechaInicio la fecha inicial del rango en formato ISO (yyyy-MM-dd), o {@code null}/vacía para no filtrar
     * @param fechaFin la fecha final del rango en formato ISO (yyyy-MM-dd), o {@code null}/vacía para no filtrar
     * @return la lista de sesiones individuales del tutor; vacía si no hay o si ocurre un error de base de datos
     */
    public List<SesionIndividual> getHistorialByTutor(int idTutor, String origen, String fechaInicio, String fechaFin) {
        List<SesionIndividual> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM SESION_INDIVIDUAL WHERE ID_TUTOR = ? AND ESTADO = 'Completado'");

        if (origen != null && !origen.isBlank()) {
            sql.append(" AND ORIGEN = ?");
        }
        if (fechaInicio != null && !fechaInicio.isBlank()) {
            sql.append(" AND FECHA >= ?");
        }
        if (fechaFin != null && !fechaFin.isBlank()) {
            sql.append(" AND FECHA <= ?");
        }
        sql.append(" ORDER BY FECHA DESC, HORA DESC");

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setInt(idx++, idTutor);
            if (origen != null && !origen.isBlank()) {
                ps.setString(idx++, origen);
            }
            if (fechaInicio != null && !fechaInicio.isBlank()) {
                ps.setDate(idx++, Date.valueOf(fechaInicio));
            }
            if (fechaFin != null && !fechaFin.isBlank()) {
                ps.setDate(idx++, Date.valueOf(fechaFin));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSesion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Marca una sesión individual como completada, de forma transaccional: registra los temas,
     * acuerdos y asistencia, genera una canalización por cada motivo seleccionado (resolviendo su
     * área correspondiente), enlaza la sesión con la primera canalización creada y, al confirmar,
     * envía los correos de confirmación de las canalizaciones generadas.
     * @param idSesion el identificador de la sesión individual a completar
     * @param temas los temas tratados durante la sesión
     * @param acuerdos los acuerdos alcanzados durante la sesión
     * @param idMotivos los identificadores de los motivos de canalización seleccionados, como cadenas
     * @param estatusAsistencia el estatus de asistencia del alumno a la sesión
     * @param baseUrl la URL base usada para construir los enlaces de los correos de confirmación, o {@code null} para omitir el envío
     * @return {@code true} si la actualización de la sesión afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean completarSesion(int idSesion, String temas, String acuerdos, String[] idMotivos, String estatusAsistencia, String baseUrl) {
        String sqlMatricula = "SELECT MATRICULA FROM SESION_INDIVIDUAL WHERE ID_SESION_INDIVIDUAL = ?";
        String sqlMotivoArea = "SELECT ID_AREA FROM MOTIVO_AREA WHERE ID_MOTIVO = ?";
        String sqlUpdate = "UPDATE SESION_INDIVIDUAL SET ESTADO = 'Completado', TEMAS_TRATADOS = ?, ACUERDOS = ?, ID_CANALIZACION = ?, ESTATUS_ASISTENCIA = ? " +
                "WHERE ID_SESION_INDIVIDUAL = ?";

        List<Canalizacion> canalizacionesCreadas = new ArrayList<>();
        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            String matricula = null;
            try (PreparedStatement ps = con.prepareStatement(sqlMatricula)) {
                ps.setInt(1, idSesion);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        matricula = rs.getString("MATRICULA");
                    }
                }
            }

            Integer idCanalizacionPrincipal = null;

            if (idMotivos != null && matricula != null) {
                for (String idMotivoStr : idMotivos) {
                    if (idMotivoStr == null || idMotivoStr.isBlank()) {
                        continue;
                    }

                    int idMotivo = Integer.parseInt(idMotivoStr.trim());
                    Integer idArea = null;

                    try (PreparedStatement psArea = con.prepareStatement(sqlMotivoArea)) {
                        psArea.setInt(1, idMotivo);
                        try (ResultSet rs = psArea.executeQuery()) {
                            if (rs.next()) {
                                idArea = rs.getInt("ID_AREA");
                            }
                        }
                    }

                    if (idArea == null) {
                        continue;
                    }

                    Canalizacion c = new Canalizacion();
                    c.setIdArea(idArea);
                    c.setIdMotivo(idMotivo);
                    c.setMatricula(matricula);
                    c.setObservaciones("Canalización registrada al completar la sesión");

                    int idGenerado = canalizacionDao.crearEnTransaccion(con, c);
                    if (idGenerado > 0) {
                        c.setIdCanalizacion(idGenerado);
                        canalizacionesCreadas.add(c);
                        if (idCanalizacionPrincipal == null) {
                            idCanalizacionPrincipal = idGenerado;
                        }
                    }
                }
            }

            boolean actualizado;
            try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                psUpdate.setString(1, temas);
                psUpdate.setString(2, acuerdos);
                if (idCanalizacionPrincipal != null) {
                    psUpdate.setInt(3, idCanalizacionPrincipal);
                } else {
                    psUpdate.setNull(3, Types.NUMERIC);
                }
                psUpdate.setString(4, estatusAsistencia);
                psUpdate.setInt(5, idSesion);
                actualizado = psUpdate.executeUpdate() > 0;
            }

            con.commit();

            if (baseUrl != null) {
                for (Canalizacion creada : canalizacionesCreadas) {
                    canalizacionDao.enviarCorreoConfirmacion(creada, baseUrl);
                }
            }

            return actualizado;
        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    /**
     * Obtiene el detalle de las atenciones individuales completadas dentro de un rango de fechas,
     * con los filtros opcionales indicados, sin filtrar por matrícula.
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param desde la fecha inicial del rango
     * @param hasta la fecha final del rango
     * @return la lista de atenciones individuales encontradas, ordenada por fecha y hora descendente
     */
    public List<AtencionAlumnoDTO> getAtencionesIndividuales(Integer idTutor, Integer idCarrera, Integer cuatrimestre,
                                                             String letra, Date desde, Date hasta) {
        return getAtencionesIndividuales(idTutor, idCarrera, cuatrimestre, letra, desde, hasta, null);
    }

    /**
     * Obtiene el detalle de las atenciones individuales completadas dentro de un rango de fechas,
     * con los filtros opcionales indicados, incluyendo datos del alumno, su grupo y la
     * canalización asociada (área y motivo) si la sesión originó una.
     * @param idTutor el identificador del tutor a filtrar, o {@code null} para no filtrar
     * @param idCarrera el identificador de la carrera a filtrar, o {@code null} para no filtrar
     * @param cuatrimestre el cuatrimestre a filtrar, o {@code null} para no filtrar
     * @param letra la letra de grupo a filtrar, o {@code null}/vacía para no filtrar
     * @param desde la fecha inicial del rango
     * @param hasta la fecha final del rango
     * @param matricula la matrícula del alumno a filtrar, o {@code null}/vacía para no filtrar
     * @return la lista de atenciones individuales encontradas, ordenada por fecha y hora descendente
     */
    public List<AtencionAlumnoDTO> getAtencionesIndividuales(Integer idTutor, Integer idCarrera, Integer cuatrimestre,
                                                             String letra, Date desde, Date hasta, String matricula) {
        List<AtencionAlumnoDTO> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT si.ID_SESION_INDIVIDUAL, si.ORIGEN, si.FECHA, si.HORA, si.ESTADO, " +
                        "si.TEMAS_TRATADOS, si.ACUERDOS, si.MATRICULA, " +
                        "a.NOMBRES AS NOMBRES_ALUMNO, a.APELLIDO_PATERNO AS AP_ALUMNO, a.APELLIDO_MATERNO AS AM_ALUMNO, " +
                        "car.NOMBRE AS NOMBRE_CARRERA, g.CUATRIMESTRE, g.LETRA, " +
                        "ar.NOMBRE AS NOMBRE_AREA, m.NOMBRE AS NOMBRE_MOTIVO " +
                        "FROM SESION_INDIVIDUAL si " +
                        "JOIN ALUMNO a ON a.MATRICULA = si.MATRICULA " +
                        "JOIN GRUPO g ON g.ID_GRUPO = a.ID_GRUPO " +
                        "JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA " +
                        "LEFT JOIN CANALIZACION c ON c.ID_CANALIZACION = si.ID_CANALIZACION " +
                        "LEFT JOIN AREA_APOYO ar ON ar.ID_AREA = c.ID_AREA " +
                        "LEFT JOIN MOTIVO_AREA m ON m.ID_MOTIVO = c.ID_MOTIVO " +
                        "WHERE si.ESTADO = 'Completado' AND si.FECHA BETWEEN ? AND ? ");

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
            sql.append(" AND si.MATRICULA = ? ");
            params.add(matricula);
        }
        sql.append(" ORDER BY si.FECHA DESC, si.HORA DESC");

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
                    AtencionAlumnoDTO dto = new AtencionAlumnoDTO();
                    dto.setIdSesion(rs.getInt("ID_SESION_INDIVIDUAL"));
                    dto.setTipo("Programada".equals(rs.getString("ORIGEN")) ? "Individual" : "Espontánea");
                    dto.setFecha(rs.getDate("FECHA"));
                    dto.setHora(rs.getString("HORA"));
                    dto.setEstado(rs.getString("ESTADO"));
                    dto.setTemasTratados(rs.getString("TEMAS_TRATADOS"));
                    dto.setAcuerdos(rs.getString("ACUERDOS"));

                    dto.setGrupoAsignado(rs.getString("NOMBRE_CARRERA") + " " + rs.getInt("CUATRIMESTRE") + "°" + rs.getString("LETRA"));

                    dto.setMatricula(rs.getString("MATRICULA"));
                    String apellidoMaterno = rs.getString("AM_ALUMNO");
                    String apellidos = rs.getString("AP_ALUMNO")
                            + (apellidoMaterno != null && !apellidoMaterno.isBlank() ? " " + apellidoMaterno : "");
                    dto.setNombreAlumno(rs.getString("NOMBRES_ALUMNO") + " " + apellidos);

                    String nombreArea = rs.getString("NOMBRE_AREA");
                    if (nombreArea != null) {
                        String nombreMotivo = rs.getString("NOMBRE_MOTIVO");
                        dto.setVinculoDirecto(nombreArea + (nombreMotivo != null && !nombreMotivo.isBlank() ? " — " + nombreMotivo : ""));
                    }

                    lista.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener las atenciones individuales: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Construye un objeto {@link SesionIndividual} a partir de la fila actual de un ResultSet.
     * @param rs el ResultSet posicionado en la fila a mapear
     * @return la sesión individual mapeada con sus campos principales, incluyendo el identificador de canalización si existe
     * @throws SQLException si ocurre un error al leer las columnas del ResultSet
     */
    private SesionIndividual mapearSesion(ResultSet rs) throws SQLException {
        SesionIndividual s = new SesionIndividual();
        s.setIdSesionIndividual(rs.getInt("ID_SESION_INDIVIDUAL"));
        s.setIdTutor(rs.getInt("ID_TUTOR"));
        s.setMatricula(rs.getString("MATRICULA"));
        s.setFecha(rs.getDate("FECHA"));
        s.setHora(rs.getString("HORA"));
        s.setTemasTratados(rs.getString("TEMAS_TRATADOS"));
        s.setAcuerdos(rs.getString("ACUERDOS"));
        int idCanalizacion = rs.getInt("ID_CANALIZACION");
        s.setIdCanalizacion(rs.wasNull() ? null : idCanalizacion);
        s.setEstado(rs.getString("ESTADO"));
        s.setOrigen(rs.getString("ORIGEN"));
        return s;
    }
}