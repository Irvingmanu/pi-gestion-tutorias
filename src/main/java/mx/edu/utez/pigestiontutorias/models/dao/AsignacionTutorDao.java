package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.AsignacionTutor;
import mx.edu.utez.pigestiontutorias.models.Grupo;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AsignacionTutorDao implements Dao<AsignacionTutor, Integer> {

    @Override
    public boolean create(AsignacionTutor entidad) {
        String sql = "INSERT INTO ASIGNACION_TUTOR (ID_TUTOR, ID_GRUPO, ESTADO) VALUES (?, ?, 'S')";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, entidad.getIdTutor());
            ps.setInt(2, entidad.getIdGrupo());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar la asignación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<AsignacionTutor> getAll() {
        List<AsignacionTutor> lista = new ArrayList<>();
        String sql = "SELECT a.ID_ASIGNACION, a.ID_TUTOR, a.ID_GRUPO, a.ESTADO, " +
                "t.NOMBRES, t.APELLIDO_PATERNO, t.APELLIDO_MATERNO, " +
                "car.NOMBRE AS NOMBRE_CARRERA, car.ID_ACADEMIA, g.CUATRIMESTRE, g.LETRA, g.GENERACION " +
                "FROM ASIGNACION_TUTOR a " +
                "JOIN TUTOR t ON t.NUMERO_EMPLEADO = a.ID_TUTOR " +
                "JOIN GRUPO g ON g.ID_GRUPO = a.ID_GRUPO " +
                "JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA " +
                "WHERE a.ESTADO = 'S' " +
                "ORDER BY a.ID_ASIGNACION DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AsignacionTutor asignacion = new AsignacionTutor();
                asignacion.setIdAsignacion(rs.getInt("ID_ASIGNACION"));
                asignacion.setIdTutor(rs.getInt("ID_TUTOR"));
                asignacion.setIdGrupo(rs.getInt("ID_GRUPO"));
                asignacion.setEstado(rs.getString("ESTADO"));
                asignacion.setNombresTutor(rs.getString("NOMBRES"));

                String apellidoM = rs.getString("APELLIDO_MATERNO");
                String apellidos = rs.getString("APELLIDO_PATERNO") + (apellidoM != null && !apellidoM.isBlank() ? " " + apellidoM : "");
                asignacion.setApellidosTutor(apellidos);

                String generacion = rs.getString("GENERACION");
                String etiquetaGeneracion = (generacion != null && !generacion.isBlank()) ? generacion : "Sin generación";
                asignacion.setNombreGrupo(rs.getString("NOMBRE_CARRERA") + " - " + rs.getInt("CUATRIMESTRE") + "° "
                        + rs.getString("LETRA") + " (Gen " + etiquetaGeneracion + ")");
                asignacion.setIdAcademia(rs.getInt("ID_ACADEMIA"));
                lista.add(asignacion);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener las asignaciones: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    @Override
    public AsignacionTutor getById(Integer id) {
        String sql = "SELECT ID_ASIGNACION, ID_TUTOR, ID_GRUPO, ESTADO FROM ASIGNACION_TUTOR WHERE ID_ASIGNACION = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AsignacionTutor asignacion = new AsignacionTutor();
                    asignacion.setIdAsignacion(rs.getInt("ID_ASIGNACION"));
                    asignacion.setIdTutor(rs.getInt("ID_TUTOR"));
                    asignacion.setIdGrupo(rs.getInt("ID_GRUPO"));
                    asignacion.setEstado(rs.getString("ESTADO"));
                    return asignacion;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar la asignación: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean update(AsignacionTutor entidad) {
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "UPDATE ASIGNACION_TUTOR SET ESTADO = 'N' WHERE ID_ASIGNACION = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al desactivar la asignación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean existeAsignacionActiva(int idGrupo) {
        String sql = "SELECT COUNT(*) FROM ASIGNACION_TUTOR WHERE ID_GRUPO = ? AND ESTADO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idGrupo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al validar la asignación existente: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public Integer findIdTutorByGrupo(int idGrupo) {
        String sql = "SELECT ID_TUTOR FROM ASIGNACION_TUTOR WHERE ID_GRUPO = ? AND ESTADO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idGrupo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID_TUTOR");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar el tutor asignado: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<Grupo> obtenerGruposPorTutor(int idTutor, int idPeriodoVigente) {
        List<Grupo> lista = new ArrayList<>();
        String sql = "SELECT g.ID_GRUPO, g.ID_CARRERA, g.CUATRIMESTRE, g.LETRA, g.ID_PERIODO, g.ESTADO, " +
                "car.NOMBRE AS NOMBRE_CARRERA " +
                "FROM ASIGNACION_TUTOR a " +
                "JOIN GRUPO g ON g.ID_GRUPO = a.ID_GRUPO " +
                "JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA " +
                "WHERE a.ID_TUTOR = ? AND g.ID_PERIODO = ? AND a.ESTADO = 'S' " +
                "ORDER BY car.NOMBRE, g.CUATRIMESTRE, g.LETRA";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTutor);
            ps.setInt(2, idPeriodoVigente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Grupo g = new Grupo();
                    g.setIdGrupo(rs.getInt("ID_GRUPO"));
                    g.setIdCarrera(rs.getInt("ID_CARRERA"));
                    g.setCuatrimestre(rs.getInt("CUATRIMESTRE"));
                    g.setLetra(rs.getString("LETRA"));
                    g.setIdPeriodo(rs.getInt("ID_PERIODO"));
                    g.setEstado(rs.getString("ESTADO"));
                    g.setNombreCarrera(rs.getString("NOMBRE_CARRERA"));
                    g.setNombreGrupo(rs.getString("NOMBRE_CARRERA") + " " + rs.getInt("CUATRIMESTRE") + "°" + rs.getString("LETRA"));
                    lista.add(g);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener los grupos del tutor: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    public boolean existeAsignacionParaTutor(int idTutor, int idGrupo) {
        String sql = "SELECT COUNT(*) FROM ASIGNACION_TUTOR WHERE ID_TUTOR = ? AND ID_GRUPO = ? AND ESTADO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTutor);
            ps.setInt(2, idGrupo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al validar la asignación del tutor: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean alumnoPerteneceATutor(int idTutor, String matricula) {
        String sql = "SELECT COUNT(*) FROM ALUMNO al " +
                "JOIN ASIGNACION_TUTOR a ON a.ID_GRUPO = al.ID_GRUPO " +
                "WHERE al.MATRICULA = ? AND a.ID_TUTOR = ? AND a.ESTADO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, matricula);
            ps.setInt(2, idTutor);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al validar la pertenencia del alumno al tutor: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean existeAsignacionEnPeriodoActivo(int idTutor) {
        String sql = "SELECT COUNT(*) FROM ASIGNACION_TUTOR a " +
                "JOIN GRUPO g ON g.ID_GRUPO = a.ID_GRUPO " +
                "JOIN PERIODO_ESCOLAR per ON per.ID_PERIODO = g.ID_PERIODO " +
                "WHERE a.ID_TUTOR = ? AND a.ESTADO = 'S' AND per.ESTADO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTutor);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al validar asignación en periodo activo: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean tienePendientesEnGrupo(int idTutor, int idGrupo) {
        return existeRegistro(
                "SELECT COUNT(1) FROM SESION_GRUPAL " +
                        "WHERE ID_TUTOR = ? AND ID_GRUPO = ? AND ESTADO = 'Pendiente' AND ROWNUM = 1",
                idTutor, idGrupo)
                || existeRegistro(
                "SELECT COUNT(1) FROM SESION_INDIVIDUAL si " +
                        "JOIN ALUMNO al ON al.MATRICULA = si.MATRICULA " +
                        "WHERE si.ID_TUTOR = ? AND al.ID_GRUPO = ? AND si.ESTADO = 'Pendiente' AND ROWNUM = 1",
                idTutor, idGrupo)
                || existeRegistro(
                "SELECT COUNT(1) FROM SOLICITUD_TUTORIA st " +
                        "JOIN ALUMNO al ON al.MATRICULA = st.MATRICULA " +
                        "WHERE st.ID_TUTOR = ? AND al.ID_GRUPO = ? AND st.ESTATUS = 'Pendiente' AND ROWNUM = 1",
                idTutor, idGrupo);
    }

    private boolean existeRegistro(String sql, int idTutor, int idGrupo) {
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTutor);
            ps.setInt(2, idGrupo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al validar pendientes de la asignación: " + e.getMessage());
            e.printStackTrace();

            return true;
        }
    }
}
