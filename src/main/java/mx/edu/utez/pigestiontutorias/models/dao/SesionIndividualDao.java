package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Canalizacion;
import mx.edu.utez.pigestiontutorias.models.SesionIndividual;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SesionIndividualDao implements Dao<SesionIndividual, Integer> {

    private final CanalizacionDao canalizacionDao = new CanalizacionDao();

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

            ps.setString(8, s.getEstado() != null ? s.getEstado() : "Registrada");
            ps.setString(9, s.getEstatusAsistencia() != null ? s.getEstatusAsistencia() : "Presente");
            ps.setString(10, s.getOrigen() != null ? s.getOrigen() : "Espontanea");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<SesionIndividual> getAll() {
        return null;
    }

    @Override
    public SesionIndividual getById(Integer id) {
        return null;
    }

    @Override
    public boolean update(SesionIndividual entidad) {
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        return false;
    }

    public List<SesionIndividual> getAcuerdosPorAlumno(String matricula) {
        List<SesionIndividual> lista = new ArrayList<>();
        String sql = "SELECT * FROM SESION_INDIVIDUAL WHERE MATRICULA = ? AND ESTADO = 'Tomada' ORDER BY FECHA DESC";

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

    // Citas que ya salieron de una Solicitud aceptada y siguen pendientes de que el tutor
    // capture temas/acuerdos (ver SolicitudServlet, accion=aceptar).
    public List<SesionIndividual> getSesionesProgramadasByTutor(int idTutor) {
        List<SesionIndividual> lista = new ArrayList<>();
        String sql = "SELECT * FROM SESION_INDIVIDUAL WHERE ID_TUTOR = ? AND ESTADO = 'Programada' ORDER BY FECHA";

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

    // Historial: sesiones individuales YA REALIZADAS (Tomada) del tutor, filtrables por
    // origen ('Programada' o 'Espontanea') y por rango de fechas. Cualquiera de los
    // filtros puede venir null/blank, en cuyo caso no se aplica esa condicion.
    public List<SesionIndividual> getHistorialByTutor(int idTutor, String origen, String fechaInicio, String fechaFin) {
        List<SesionIndividual> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM SESION_INDIVIDUAL WHERE ID_TUTOR = ? AND ESTADO = 'Tomada'");

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

    // Cierra una sesion programada: la marca 'Tomada', guarda temas/acuerdos y registra
    // en CANALIZACION cada motivo de vinculo directo seleccionado en el modal "Completar".
    // La primera canalizacion creada queda enlazada a la sesion via ID_CANALIZACION;
    // las demas quedan igual en CANALIZACION (por matricula/area) porque SESION_INDIVIDUAL
    // solo tiene una columna de enlace.
    // baseUrl se usa para armar el link del correo de confirmacion (ver CanalizacionDao);
    // los correos se mandan DESPUES del commit, para no notificar al encargado de una
    // canalizacion que la transaccion pudiera revertir mas adelante.
    public boolean completarSesion(int idSesion, String temas, String acuerdos, String[] idMotivos, String estatusAsistencia, String baseUrl) {
        String sqlMatricula = "SELECT MATRICULA FROM SESION_INDIVIDUAL WHERE ID_SESION_INDIVIDUAL = ?";
        String sqlMotivoArea = "SELECT ID_AREA FROM MOTIVO_AREA WHERE ID_MOTIVO = ?";
        String sqlUpdate = "UPDATE SESION_INDIVIDUAL SET ESTADO = 'Tomada', TEMAS_TRATADOS = ?, ACUERDOS = ?, ID_CANALIZACION = ?, ESTATUS_ASISTENCIA = ? " +
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