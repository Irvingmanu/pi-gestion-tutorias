package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.AvanceTutorGrupal;
import mx.edu.utez.pigestiontutorias.models.SesionGrupal;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SesionGrupalDao implements Dao<SesionGrupal, Integer> {

    // Inserta la SESION_GRUPAL y, con el ID generado, la ASISTENCIA de cada matricula
    // recibida en entidad.getAsistentes() (checkboxes marcados en el registro). Todo en
    // una sola transaccion: si falla la asistencia, tampoco debe quedar la sesion huerfana.
    @Override
    public boolean create(SesionGrupal entidad) {
        String sqlSesion = "INSERT INTO SESION_GRUPAL " +
                "(ID_GRUPO, ID_TUTOR, FECHA, HORA, TEMAS_TRATADOS, ACUERDOS, ASESORIAS_GRUPALES, ESTADO) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlAsistencia = "INSERT INTO ASISTENCIA (ID_SESION_GRUPAL, MATRICULA, ESTATUS_ASISTENCIA) VALUES (?, ?, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idSesionGrupal;
            try (PreparedStatement psSesion = con.prepareStatement(sqlSesion, new String[]{"ID_SESION_GRUPAL"})) {
                psSesion.setInt(1, entidad.getIdGrupo());
                psSesion.setInt(2, entidad.getIdTutor());
                psSesion.setDate(3, entidad.getFecha());
                psSesion.setString(4, entidad.getHora());
                psSesion.setString(5, entidad.getTemasTratados());
                psSesion.setString(6, entidad.getAcuerdos());

                if (entidad.getAsesoriasGrupales() != null) {
                    psSesion.setString(7, entidad.getAsesoriasGrupales());
                } else {
                    psSesion.setNull(7, Types.CLOB);
                }

                psSesion.setString(8, entidad.getEstado() != null ? entidad.getEstado() : "Completado");

                if (psSesion.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }

                try (ResultSet keys = psSesion.getGeneratedKeys()) {
                    if (!keys.next()) {
                        con.rollback();
                        return false;
                    }
                    idSesionGrupal = keys.getInt(1);
                }
            }

            String[] asistentes = entidad.getAsistentes();
            if (asistentes != null && asistentes.length > 0) {
                try (PreparedStatement psAsistencia = con.prepareStatement(sqlAsistencia)) {
                    for (String matricula : asistentes) {
                        if (matricula == null || matricula.isBlank()) {
                            continue;
                        }
                        psAsistencia.setInt(1, idSesionGrupal);
                        psAsistencia.setString(2, matricula.trim());
                        psAsistencia.setString(3, "Presente");
                        psAsistencia.addBatch();
                    }
                    psAsistencia.executeBatch();
                }
            }

            con.commit();
            return true;
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

    @Override
    public List<SesionGrupal> getAll() {
        return null;
    }

    @Override
    public SesionGrupal getById(Integer id) {
        return null;
    }

    @Override
    public boolean update(SesionGrupal entidad) {
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        return false;
    }

    public List<SesionGrupal> getAcuerdosPorAlumno(String matricula) {
        List<SesionGrupal> lista = new ArrayList<>();
        String sql = "SELECT sg.* FROM SESION_GRUPAL sg " +
                "INNER JOIN ALUMNO a ON sg.ID_GRUPO = a.ID_GRUPO " +
                "WHERE a.MATRICULA = ? AND sg.ESTADO = 'Completado' ORDER BY sg.FECHA DESC";

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

    // Historial: sesiones grupales YA REALIZADAS (Completado) del tutor, filtrables por
    // rango de fechas (cualquiera de los dos limites puede venir null/blank).
    public List<SesionGrupal> getHistorialByTutor(int idTutor, String fechaInicio, String fechaFin) {
        List<SesionGrupal> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM SESION_GRUPAL WHERE ID_TUTOR = ? AND ESTADO = 'Completado'");

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

    private SesionGrupal mapearSesion(ResultSet rs) throws SQLException {
        SesionGrupal s = new SesionGrupal();
        s.setIdSesionGrupal(rs.getInt("ID_SESION_GRUPAL"));
        s.setIdGrupo(rs.getInt("ID_GRUPO"));
        s.setIdTutor(rs.getInt("ID_TUTOR"));
        s.setFecha(rs.getDate("FECHA"));
        s.setHora(rs.getString("HORA"));
        s.setTemasTratados(rs.getString("TEMAS_TRATADOS"));
        s.setAcuerdos(rs.getString("ACUERDOS"));
        s.setAsesoriasGrupales(rs.getString("ASESORIAS_GRUPALES"));
        s.setEstado(rs.getString("ESTADO"));
        return s;
    }

    // Modal "Seguimiento de Tutorias Grupales" del reporte del coordinador: una fila por
    // cada asignacion tutor-grupo activa dentro del periodo, con el conteo de sesiones
    // "Completado" que ese tutor ya registro en ese grupo durante el rango del periodo.
    // Ordenado de MENOR a MAYOR avance, tal como lo pide el reporte.
    public List<AvanceTutorGrupal> getAvancePorPeriodo(int idPeriodo, Date fechaInicio, Date fechaFin, int objetivo) {
        List<AvanceTutorGrupal> lista = new ArrayList<>();
        String sql = "SELECT asg.ID_TUTOR, t.NOMBRES, t.APELLIDO_PATERNO, t.APELLIDO_MATERNO, t.CORREO_INSTITUCIONAL, " +
                "asg.ID_GRUPO, car.NOMBRE AS NOMBRE_CARRERA, g.CUATRIMESTRE, g.LETRA, " +
                "(SELECT COUNT(*) FROM SESION_GRUPAL sg WHERE sg.ID_TUTOR = asg.ID_TUTOR AND sg.ID_GRUPO = asg.ID_GRUPO " +
                "AND sg.ESTADO = 'Completado' AND sg.FECHA BETWEEN ? AND ?) AS REALIZADAS " +
                "FROM ASIGNACION_TUTOR asg " +
                "JOIN TUTOR t ON t.NUMERO_EMPLEADO = asg.ID_TUTOR " +
                "JOIN GRUPO g ON g.ID_GRUPO = asg.ID_GRUPO " +
                "JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA " +
                "WHERE asg.ESTADO = 'S' AND g.ID_PERIODO = ? " +
                "ORDER BY REALIZADAS ASC, t.APELLIDO_PATERNO ASC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, fechaInicio);
            ps.setDate(2, fechaFin);
            ps.setInt(3, idPeriodo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AvanceTutorGrupal a = new AvanceTutorGrupal();
                    a.setIdTutor(rs.getInt("ID_TUTOR"));

                    String apellidoMaterno = rs.getString("APELLIDO_MATERNO");
                    String apellidos = rs.getString("APELLIDO_PATERNO")
                            + (apellidoMaterno != null && !apellidoMaterno.isBlank() ? " " + apellidoMaterno : "");
                    a.setNombreTutor(rs.getString("NOMBRES") + " " + apellidos);
                    a.setCorreoTutor(rs.getString("CORREO_INSTITUCIONAL"));

                    a.setIdGrupo(rs.getInt("ID_GRUPO"));
                    a.setGrupoAsignado(rs.getString("NOMBRE_CARRERA") + " " + rs.getInt("CUATRIMESTRE") + "°" + rs.getString("LETRA"));

                    int realizadas = rs.getInt("REALIZADAS");
                    a.setRealizadas(realizadas);
                    a.setObjetivo(objetivo);
                    a.setEstatus(calcularEstatus(realizadas, objetivo));

                    lista.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener el avance de tutorias grupales: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // Umbral de negocio: sin avance o por debajo de la mitad del objetivo se marca
    // "En Riesgo" para que el coordinador priorice el envio de alertas a esos tutores.
    private String calcularEstatus(int realizadas, int objetivo) {
        if (objetivo <= 0) return "SIN_OBJETIVO";
        if (realizadas == 0 || realizadas < objetivo / 2.0) return "RIESGO";
        return "AL_DIA";
    }

    // "Ver detalles" del modal de seguimiento: sesiones que un tutor especifico registro
    // en un grupo especifico, dentro del rango de fechas del periodo.
    public List<SesionGrupal> getSesionesPorTutorYGrupo(int idTutor, int idGrupo, Date fechaInicio, Date fechaFin) {
        List<SesionGrupal> lista = new ArrayList<>();
        String sql = "SELECT * FROM SESION_GRUPAL WHERE ID_TUTOR = ? AND ID_GRUPO = ? AND ESTADO = 'Completado' " +
                "AND FECHA BETWEEN ? AND ? ORDER BY FECHA DESC, HORA DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTutor);
            ps.setInt(2, idGrupo);
            ps.setDate(3, fechaInicio);
            ps.setDate(4, fechaFin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSesion(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener las sesiones del tutor: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }
}
