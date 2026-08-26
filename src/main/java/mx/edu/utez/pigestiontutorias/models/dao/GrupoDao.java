package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Grupo;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsable del acceso a datos de los grupos (GRUPO), incluyendo su búsqueda o creación
 * automática, la consulta de grupos disponibles para asignación de tutor y los grupos asignados
 * a un tutor específico.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-08-14
 */
public class GrupoDao implements Dao<Grupo, Integer> {

    private static final String SELECT_BASE =
            "SELECT g.ID_GRUPO, g.ID_CARRERA, g.CUATRIMESTRE, g.LETRA, g.ID_PERIODO, g.GENERACION, g.ESTADO, " +
                    "car.NOMBRE AS NOMBRE_CARRERA, car.ID_ACADEMIA " +
                    "FROM GRUPO g JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA ";

    /**
     * Crea un nuevo grupo con estado activo y asigna a la entidad el identificador generado.
     * @param entidad el grupo a crear
     * @return {@code true} si la inserción generó un identificador válido; {@code false} en caso contrario
     */
    @Override
    public boolean create(Grupo entidad) {
        String sql = "INSERT INTO GRUPO (ID_CARRERA, CUATRIMESTRE, LETRA, ID_PERIODO, GENERACION, ESTADO) VALUES (?, ?, ?, ?, ?, 'S')";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_GRUPO"})) {

            ps.setInt(1, entidad.getIdCarrera());
            ps.setInt(2, entidad.getCuatrimestre());
            ps.setString(3, entidad.getLetra());
            ps.setInt(4, entidad.getIdPeriodo());
            ps.setString(5, entidad.getGeneracion());

            if (ps.executeUpdate() == 0) return false;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entidad.setIdGrupo(keys.getInt(1));
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error al crear el grupo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todos los grupos activos, ordenados por carrera, cuatrimestre y letra.
     * @return la lista de todos los grupos activos
     */
    @Override
    public List<Grupo> getAll() {
        List<Grupo> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE g.ESTADO = 'S' ORDER BY car.NOMBRE, g.CUATRIMESTRE, g.LETRA";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener los grupos: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Obtiene un grupo por su identificador.
     * @param id el identificador (ID_GRUPO) del grupo buscado
     * @return el grupo encontrado, o {@code null} si no existe
     */
    @Override
    public Grupo getById(Integer id) {
        String sql = SELECT_BASE + "WHERE g.ID_GRUPO = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener el grupo: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Operación no soportada; los grupos no se actualizan directamente mediante este DAO.
     * @param entidad el grupo a actualizar (no utilizado)
     * @return siempre {@code false}
     */
    @Override
    public boolean update(Grupo entidad) {
        return false;
    }

    /**
     * Operación no soportada; los grupos no se eliminan mediante este DAO.
     * @param id el identificador del grupo (no utilizado)
     * @return siempre {@code false}
     */
    @Override
    public boolean delete(Integer id) {
        return false;
    }

    /**
     * Busca un grupo existente por carrera, cuatrimestre, letra y periodo; si no existe, lo crea.
     * @param idCarrera el identificador de la carrera
     * @param cuatrimestre el número de cuatrimestre
     * @param letra la letra del grupo
     * @param idPeriodo el identificador del periodo escolar
     * @param generacion la generación a asignar si el grupo debe crearse
     * @return el identificador del grupo encontrado o creado, o {@code null} si ocurre un error o la creación falla
     */
    public Integer findOrCreate(int idCarrera, int cuatrimestre, String letra, int idPeriodo, String generacion) {
        String sqlBuscar = "SELECT ID_GRUPO FROM GRUPO WHERE ID_CARRERA = ? AND CUATRIMESTRE = ? AND LETRA = ? AND ID_PERIODO = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlBuscar)) {

            ps.setInt(1, idCarrera);
            ps.setInt(2, cuatrimestre);
            ps.setString(3, letra);
            ps.setInt(4, idPeriodo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID_GRUPO");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar el grupo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        Grupo nuevo = new Grupo();
        nuevo.setIdCarrera(idCarrera);
        nuevo.setCuatrimestre(cuatrimestre);
        nuevo.setLetra(letra);
        nuevo.setIdPeriodo(idPeriodo);
        nuevo.setGeneracion(generacion);

        return create(nuevo) ? nuevo.getIdGrupo() : null;
    }

    /**
     * Verifica si ya existe un grupo activo con la combinación de carrera, cuatrimestre, letra y periodo indicados.
     * @param idCarrera el identificador de la carrera
     * @param cuatrimestre el número de cuatrimestre
     * @param letra la letra del grupo
     * @param idPeriodo el identificador del periodo escolar
     * @return {@code true} si el grupo ya existe o si ocurre un error de base de datos; {@code false} en caso contrario
     */
    public boolean existeGrupo(int idCarrera, int cuatrimestre, String letra, int idPeriodo) {
        String sql = "SELECT 1 FROM GRUPO WHERE ID_CARRERA = ? AND CUATRIMESTRE = ? AND LETRA = ? AND ID_PERIODO = ? AND ESTADO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCarrera);
            ps.setInt(2, cuatrimestre);
            ps.setString(3, letra);
            ps.setInt(4, idPeriodo);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar el grupo: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Obtiene los grupos activos que tienen al menos un alumno activo y aún no tienen
     * un tutor asignado activamente, es decir, los disponibles para asignación de tutor.
     * @return la lista de grupos disponibles para asignación
     */
    public List<Grupo> getDisponiblesParaAsignacion() {
        List<Grupo> lista = new ArrayList<>();
        String sql = SELECT_BASE +
                "WHERE g.ESTADO = 'S' AND EXISTS (SELECT 1 FROM ALUMNO a WHERE a.ID_GRUPO = g.ID_GRUPO AND a.ESTADO = 'S') " +
                "AND NOT EXISTS (SELECT 1 FROM ASIGNACION_TUTOR at WHERE at.ID_GRUPO = g.ID_GRUPO AND at.ESTADO = 'S') " +
                "ORDER BY car.NOMBRE, g.CUATRIMESTRE, g.LETRA";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener los grupos disponibles para asignacion: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Verifica si un grupo tiene al menos un alumno activo.
     * @param idGrupo el identificador del grupo
     * @return {@code true} si el grupo tiene al menos un alumno activo; {@code false} en caso contrario o si ocurre un error
     */
    public boolean tieneAlumnosActivos(int idGrupo) {
        String sql = "SELECT 1 FROM ALUMNO WHERE ID_GRUPO = ? AND ESTADO = 'S' AND ROWNUM = 1";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idGrupo);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar los alumnos activos del grupo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene los grupos activos asignados actualmente a un tutor, ordenados por cuatrimestre, letra y carrera.
     * @param idTutor el identificador del tutor
     * @return la lista de grupos activos asignados al tutor
     */
    public List<Grupo> getGruposByTutor(int idTutor) {
        List<Grupo> lista = new ArrayList<>();
        String sql = "SELECT g.ID_GRUPO, g.ID_CARRERA, g.CUATRIMESTRE, g.LETRA, g.ID_PERIODO, g.GENERACION, g.ESTADO, " +
                "car.NOMBRE AS NOMBRE_CARRERA, car.ID_ACADEMIA " +
                "FROM ASIGNACION_TUTOR a " +
                "JOIN GRUPO g ON g.ID_GRUPO = a.ID_GRUPO " +
                "JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA " +
                "WHERE a.ID_TUTOR = ? AND a.ESTADO = 'S' AND g.ESTADO = 'S' " +
                "ORDER BY g.CUATRIMESTRE, g.LETRA, car.NOMBRE";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTutor);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener los grupos del tutor: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Construye una entidad {@link Grupo} a partir de la fila actual de un {@link ResultSet},
     * incluyendo un nombre de grupo legible compuesto por carrera, cuatrimestre y letra.
     * @param rs el conjunto de resultados posicionado en la fila a mapear
     * @return el grupo construido con los datos de la fila
     * @throws SQLException si ocurre un error al leer las columnas del resultado
     */
    private Grupo mapear(ResultSet rs) throws SQLException {
        Grupo g = new Grupo();
        g.setIdGrupo(rs.getInt("ID_GRUPO"));
        g.setIdCarrera(rs.getInt("ID_CARRERA"));
        g.setCuatrimestre(rs.getInt("CUATRIMESTRE"));
        g.setLetra(rs.getString("LETRA"));
        g.setIdPeriodo(rs.getInt("ID_PERIODO"));
        g.setGeneracion(rs.getString("GENERACION"));
        g.setEstado(rs.getString("ESTADO"));
        g.setNombreCarrera(rs.getString("NOMBRE_CARRERA"));
        g.setNombreGrupo(rs.getString("NOMBRE_CARRERA") + " " + rs.getInt("CUATRIMESTRE") + "°" + rs.getString("LETRA"));
        g.setIdAcademia(rs.getInt("ID_ACADEMIA"));
        return g;
    }
}
