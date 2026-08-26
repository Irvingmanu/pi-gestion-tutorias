package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Area;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsable del acceso a datos de las áreas de apoyo (AREA_APOYO), incluyendo
 * su relación con los motivos de canalización y el conteo de alumnos canalizados a cada área.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-17
 */
public class AreaDAO implements Dao<Area, Integer> {

    private final MotivoDAO motivoDAO = new MotivoDAO();

    /**
     * Crea una nueva área de apoyo.
     * @param entidad el área a crear
     * @return {@code true} si la inserción generó un identificador válido; {@code false} en caso contrario
     */
    @Override
    public boolean create(Area entidad) {
        return createAndGetId(entidad) > 0;
    }

    /**
     * Inserta una nueva área de apoyo y devuelve el identificador generado.
     * @param entidad el área a crear
     * @return el ID_AREA generado, o -1 si la inserción falla
     */
    public int createAndGetId(Area entidad) {
        String sql = "INSERT INTO AREA_APOYO(NOMBRE, NOMBRES, APELLIDO_PATERNO, APELLIDO_MATERNO, CORREO_CONTACTO, ENLACE_CITA) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_AREA"})) {

            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getNombresEncargado());
            ps.setString(3, entidad.getApellidoPaternoEncargado());
            ps.setString(4, entidad.getApellidoMaternoEncargado());
            ps.setString(5, entidad.getCorreoContacto());
            ps.setString(6, entidad.getEnlaceCita());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Verifica si ya existe un área de apoyo registrada con el nombre indicado.
     * @param nombre el nombre a verificar
     * @return {@code true} si el nombre ya existe; {@code false} en caso contrario
     */
    public boolean existeNombreArea(String nombre) {
        String sql = "SELECT COUNT(*) FROM AREA_APOYO WHERE NOMBRE = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Verifica si ya existe otra área de apoyo (distinta a la actual) registrada con el nombre indicado,
     * útil para validar duplicados al editar un área existente.
     * @param nombre el nombre a verificar
     * @param idAreaActual el ID_AREA del área que se está editando, excluido de la comparación
     * @return {@code true} si el nombre ya existe en otra área; {@code false} en caso contrario
     */
    public boolean existeNombreArea(String nombre, int idAreaActual) {
        String sql = "SELECT COUNT(*) FROM AREA_APOYO WHERE NOMBRE = ? AND ID_AREA <> ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, idAreaActual);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtiene todas las áreas de apoyo registradas, ordenadas de la más reciente a la más antigua.
     * @return la lista de todas las áreas de apoyo
     */
    @Override
    public List<Area> getAll() {
        List<Area> listaAreas = new ArrayList<>();
        String sql = "SELECT * FROM AREA_APOYO ORDER BY ID_AREA DESC";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaAreas.add(mapearArea(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaAreas;
    }

    /**
     * Obtiene todas las áreas de apoyo, cargando además la lista de motivos asociada a cada una.
     * @return la lista de áreas de apoyo con sus motivos cargados
     */
    public List<Area> getAllConMotivos() {
        List<Area> areas = getAll();
        for (Area area : areas) {
            area.setMotivos(motivoDAO.getByIdArea(area.getIdArea()));
        }
        return areas;
    }

    /**
     * Obtiene un área de apoyo por su identificador, incluyendo sus motivos asociados
     * y el conteo de alumnos canalizados a ella.
     * @param id el identificador (ID_AREA) del área buscada
     * @return el área encontrada con sus datos completos, o {@code null} si no existe
     */
    @Override
    public Area getById(Integer id) {
        String sql = "SELECT * FROM AREA_APOYO WHERE ID_AREA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Area a = mapearArea(rs);
                    a.setMotivos(motivoDAO.getByIdArea(a.getIdArea()));
                    a.setAlumnosCanalizados(contarCanalizados(a.getIdArea()));
                    return a;
                }
            }
        } catch (SQLException e)    {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cuenta cuántas canalizaciones existen registradas hacia un área de apoyo específica.
     * @param idArea el identificador del área a consultar
     * @return la cantidad de canalizaciones registradas hacia el área
     */
    public int contarCanalizados(int idArea) {
        String sql = "SELECT COUNT(*) FROM CANALIZACION WHERE ID_AREA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idArea);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Actualiza los datos de un área de apoyo existente.
     * @param entidad el área con los datos actualizados (debe incluir su ID_AREA)
     * @return {@code true} si se actualizó al menos una fila; {@code false} en caso contrario
     */
    @Override
    public boolean update(Area entidad) {
        String sql = "UPDATE AREA_APOYO SET NOMBRE = ?, NOMBRES = ?, APELLIDO_PATERNO = ?, APELLIDO_MATERNO = ?, CORREO_CONTACTO = ?, " +
                "ENLACE_CITA = ? WHERE ID_AREA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getNombresEncargado());
            ps.setString(3, entidad.getApellidoPaternoEncargado());
            ps.setString(4, entidad.getApellidoMaternoEncargado());
            ps.setString(5, entidad.getCorreoContacto());
            ps.setString(6, entidad.getEnlaceCita());
            ps.setInt(7, entidad.getIdArea());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un área de apoyo junto con sus motivos asociados, dentro de una transacción.
     * @param id el identificador (ID_AREA) del área a eliminar
     * @return {@code true} si el área fue eliminada; {@code false} en caso contrario
     */
    @Override
    public boolean delete(Integer id) {
        String sqlMotivos = "DELETE FROM MOTIVO_AREA WHERE ID_AREA = ?";
        String sqlArea = "DELETE FROM AREA_APOYO WHERE ID_AREA = ?";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement psMotivos = con.prepareStatement(sqlMotivos)) {
                psMotivos.setInt(1, id);
                psMotivos.executeUpdate();
            }

            int filasAfectadas;
            try (PreparedStatement psArea = con.prepareStatement(sqlArea)) {
                psArea.setInt(1, id);
                filasAfectadas = psArea.executeUpdate();
            }

            con.commit();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Construye una entidad {@link Area} a partir de la fila actual de un {@link ResultSet}.
     * @param rs el conjunto de resultados posicionado en la fila a mapear
     * @return el área construida con los datos de la fila
     * @throws SQLException si ocurre un error al leer las columnas del resultado
     */
    private Area mapearArea(ResultSet rs) throws SQLException {
        Area a = new Area();
        a.setIdArea(rs.getInt("ID_AREA"));
        a.setNombre(rs.getString("NOMBRE"));
        a.setNombresEncargado(rs.getString("NOMBRES"));
        a.setApellidoPaternoEncargado(rs.getString("APELLIDO_PATERNO"));
        a.setApellidoMaternoEncargado(rs.getString("APELLIDO_MATERNO"));
        a.setCorreoContacto(rs.getString("CORREO_CONTACTO"));
        a.setEnlaceCita(rs.getString("ENLACE_CITA"));
        return a;
    }
}
