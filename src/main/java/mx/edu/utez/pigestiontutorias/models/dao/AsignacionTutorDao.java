package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.AsignacionDTO;
import mx.edu.utez.pigestiontutorias.models.AsignacionTutor;
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
        boolean resultado = false;
        String sql = "INSERT INTO ASIGNACION_TUTOR (ID_TUTOR, ID_CARRERA, ID_LETRA_GRUPO, ID_CUATRIMESTRE, ACTIVO) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, entidad.getIdTutor());
            ps.setInt(2, entidad.getIdCarrera());
            ps.setInt(3, entidad.getIdLetraGrupo());
            ps.setInt(4, entidad.getIdCuatrimestre());
            ps.setString(5, "S"); // Cambiado a 'S' según la restricción de Oracle

            resultado = ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("=== ERROR SQL AL INSERTAR ASIGNACIÓN ===");
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("Código de error: " + e.getErrorCode());
            e.printStackTrace();
        }

        return resultado;
    }

    // Lista de asignaciones activas para el panel de "Asignación de Tutores",
    // con los datos ya legibles del tutor, carrera, grupo y cuatrimestre (via JOIN).
    @Override
    public List<AsignacionTutor> getAll() {
        List<AsignacionTutor> lista = new ArrayList<>();
        String sql = "SELECT a.ID_ASIGNACION, a.ID_TUTOR, a.ID_CARRERA, a.ID_LETRA_GRUPO, a.ID_CUATRIMESTRE, a.ACTIVO, " +
                "t.NOMBRES, t.APELLIDOS, car.NOMBRE AS NOMBRE_CARRERA, lg.LETRA, c.NUMERO " +
                "FROM ASIGNACION_TUTOR a " +
                "JOIN ADMIN.TUTOR t ON t.ID_TUTOR = a.ID_TUTOR " +
                "JOIN ADMIN.CARRERA car ON car.ID_CARRERA = a.ID_CARRERA " +
                "JOIN ADMIN.LETRA_GRUPO lg ON lg.ID_LETRA = a.ID_LETRA_GRUPO " +
                "JOIN ADMIN.CUATRIMESTRE c ON c.ID_CUATRIMESTRE = a.ID_CUATRIMESTRE " +
                "WHERE a.ACTIVO = 'S' " +
                "ORDER BY a.ID_ASIGNACION DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AsignacionTutor asignacion = new AsignacionTutor();
                asignacion.setIdAsignacion(rs.getInt("ID_ASIGNACION"));
                asignacion.setIdTutor(rs.getInt("ID_TUTOR"));
                asignacion.setIdCarrera(rs.getInt("ID_CARRERA"));
                asignacion.setIdLetraGrupo(rs.getInt("ID_LETRA_GRUPO"));
                asignacion.setIdCuatrimestre(rs.getInt("ID_CUATRIMESTRE"));
                asignacion.setNombresTutor(rs.getString("NOMBRES"));
                asignacion.setApellidosTutor(rs.getString("APELLIDOS"));
                asignacion.setNombreCarrera(rs.getString("NOMBRE_CARRERA"));
                asignacion.setLetraGrupo(rs.getString("LETRA"));
                asignacion.setNumeroCuatrimestre(rs.getInt("NUMERO"));
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
        return null;
    }

    @Override
    public boolean update(AsignacionTutor entidad) {
        return false;
    }

    // Borrado lógico: la asignación deja de estar activa pero se conserva
    // el historial (igual patrón que ALUMNO/TUTOR con ACTIVO = 'N').
    @Override
    public boolean delete(Integer id) {
        String sql = "UPDATE ASIGNACION_TUTOR SET ACTIVO = 'N' WHERE ID_ASIGNACION = ?";

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

    // Regla de negocio: un grupo real (Carrera+Cuatrimestre+Letra) solo puede tener
    // un tutor activo a la vez. Se consulta antes del INSERT para no depender de
    // una restricción UNIQUE en la base de datos. Incluye ID_CARRERA porque una
    // misma letra+cuatrimestre puede repetirse en carreras distintas (son grupos
    // reales distintos, ej. "DSM - 1° A" vs "TI - 1° A").
    public boolean existeAsignacionActiva(int idLetraGrupo, int idCarrera, int idCuatrimestre) {
        String sql = "SELECT COUNT(*) FROM ASIGNACION_TUTOR " +
                "WHERE ID_LETRA_GRUPO = ? AND ID_CARRERA = ? AND ID_CUATRIMESTRE = ? AND ACTIVO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idLetraGrupo);
            ps.setInt(2, idCarrera);
            ps.setInt(3, idCuatrimestre);

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

    // Necesario para el módulo de Solicitud: dado el grupo y cuatrimestre
    // de un alumno, obtenemos el ID_TUTOR que tiene asignado (regla de
    // negocio: un alumno tiene un solo tutor activo por grupo+cuatrimestre).
    public Integer findIdTutorByGrupoYCuatrimestre(int idLetraGrupo, int idCuatrimestre) {
        String sql = "SELECT ID_TUTOR FROM ASIGNACION_TUTOR " +
                "WHERE ID_LETRA_GRUPO = ? AND ID_CUATRIMESTRE = ? AND ACTIVO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idLetraGrupo);
            ps.setInt(2, idCuatrimestre);

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

    // Grupos reales (Carrera + Cuatrimestre + Letra) que tiene asignados un tutor.
    // Alimenta el <select> de registro-grupal.jsp: un tutor solo debe poder
    // registrar tutoria/asistencia de los grupos que aparecen aqui.
    public List<AsignacionDTO> obtenerAsignacionesPorTutor(int idTutor) {
        List<AsignacionDTO> lista = new ArrayList<>();
        String sql = "SELECT a.ID_CARRERA, c.NOMBRE AS NOMBRE_CARRERA, " +
                "a.ID_CUATRIMESTRE, cu.NUMERO AS NUMERO_CUATRIMESTRE, " +
                "a.ID_LETRA_GRUPO, lg.LETRA " +
                "FROM ASIGNACION_TUTOR a " +
                "JOIN ADMIN.CARRERA c ON c.ID_CARRERA = a.ID_CARRERA " +
                "JOIN ADMIN.CUATRIMESTRE cu ON cu.ID_CUATRIMESTRE = a.ID_CUATRIMESTRE " +
                "JOIN ADMIN.LETRA_GRUPO lg ON lg.ID_LETRA = a.ID_LETRA_GRUPO " +
                "WHERE a.ID_TUTOR = ? AND a.ACTIVO = 'S' " +
                "ORDER BY c.NOMBRE, cu.NUMERO, lg.LETRA";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTutor);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new AsignacionDTO(
                            rs.getInt("ID_CARRERA"),
                            rs.getString("NOMBRE_CARRERA"),
                            rs.getInt("ID_CUATRIMESTRE"),
                            rs.getInt("NUMERO_CUATRIMESTRE"),
                            rs.getInt("ID_LETRA_GRUPO"),
                            rs.getString("LETRA")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener las asignaciones del tutor: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    // Blindaje de servidor: aunque el <select> del formulario solo liste los grupos
    // del tutor, el POST se valida de nuevo aqui para que nadie pueda registrar
    // tutoria/asistencia de un grupo que no le pertenece manipulando el request.
    public boolean existeAsignacionParaTutor(int idTutor, int idCarrera, int idCuatrimestre, int idLetraGrupo) {
        String sql = "SELECT COUNT(*) FROM ASIGNACION_TUTOR " +
                "WHERE ID_TUTOR = ? AND ID_CARRERA = ? AND ID_CUATRIMESTRE = ? AND ID_LETRA_GRUPO = ? AND ACTIVO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTutor);
            ps.setInt(2, idCarrera);
            ps.setInt(3, idCuatrimestre);
            ps.setInt(4, idLetraGrupo);

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

    // Blindaje de Tutoria Espontanea: el campo de matricula queda libre por UX, pero aqui se
    // verifica que el alumno realmente pertenezca a alguno de los grupos (Carrera+Cuatrimestre+
    // Letra) asignados al tutor, cruzando ALUMNO -> ASIGNACION_TUTOR, antes de dejarlo registrar
    // una sesion individual con esa matricula.
    public boolean alumnoPerteneceATutor(int idTutor, String matricula) {
        String sql = "SELECT COUNT(*) FROM ALUMNO al " +
                "JOIN ASIGNACION_TUTOR a ON a.ID_CARRERA = al.ID_CARRERA " +
                "AND a.ID_CUATRIMESTRE = al.ID_CUATRIMESTRE " +
                "AND a.ID_LETRA_GRUPO = al.ID_LETRA_GRUPO " +
                "WHERE al.MATRICULA = ? AND a.ID_TUTOR = ? AND a.ACTIVO = 'S'";

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
}
