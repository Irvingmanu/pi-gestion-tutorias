package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.PeriodoEscolar;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de acceso a datos para los periodos escolares (tabla PERIODO_ESCOLAR),
 * con operaciones CRUD, baja lógica y reactivación, y consultas específicas
 * como periodos activos, del año actual y el periodo vigente.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-08-10
 */
public class PeriodoEscolarDao implements Dao<PeriodoEscolar, Integer> {

    /**
     * Inserta un nuevo periodo escolar.
     * @param p el periodo a crear, con nombre, fechas, estado (por defecto "S") y asistencias grupales
     * @return {@code true} si la inserción afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    @Override
    public boolean create(PeriodoEscolar p) {
        String sql = "INSERT INTO PERIODO_ESCOLAR (NOMBRE, FECHA_INICIO, FECHA_FIN, ESTADO, ASISTENCIASGRUPALES) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDate(2, p.getFechaInicio());
            ps.setDate(3, p.getFechaFin());
            ps.setString(4, p.getEstado() != null ? p.getEstado() : "S");
            ps.setInt(5, p.getAsistenciasGrupales());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear el periodo escolar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todos los periodos escolares registrados, ordenados por fecha de inicio descendente.
     * @return la lista completa de periodos escolares; vacía si no hay registros o si ocurre un error de base de datos
     */
    @Override
    public List<PeriodoEscolar> getAll() {
        List<PeriodoEscolar> lista = new ArrayList<>();
        String sql = "SELECT * FROM PERIODO_ESCOLAR ORDER BY FECHA_INICIO DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener los periodos escolares: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Busca un periodo escolar por su identificador.
     * @param id el identificador del periodo escolar
     * @return el periodo encontrado, o {@code null} si no existe o si ocurre un error de base de datos
     */
    @Override
    public PeriodoEscolar getById(Integer id) {
        String sql = "SELECT * FROM PERIODO_ESCOLAR WHERE ID_PERIODO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener el periodo escolar: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Actualiza nombre, fechas y asistencias grupales de un periodo escolar existente.
     * @param p el periodo con el identificador y los nuevos valores a aplicar
     * @return {@code true} si la actualización afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    @Override
    public boolean update(PeriodoEscolar p) {
        String sql = "UPDATE PERIODO_ESCOLAR SET NOMBRE = ?, FECHA_INICIO = ?, FECHA_FIN = ?, ASISTENCIASGRUPALES = ? WHERE ID_PERIODO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDate(2, p.getFechaInicio());
            ps.setDate(3, p.getFechaFin());
            ps.setInt(4, p.getAsistenciasGrupales());
            ps.setInt(5, p.getIdPeriodo());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar el periodo escolar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Da de baja lógica un periodo escolar, marcándolo como inactivo (ESTADO = 'N').
     * @param id el identificador del periodo a desactivar
     * @return {@code true} si la actualización afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    @Override
    public boolean delete(Integer id) {
        String sql = "UPDATE PERIODO_ESCOLAR SET ESTADO = 'N' WHERE ID_PERIODO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al desactivar el periodo escolar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reactiva un periodo escolar previamente dado de baja, marcándolo como activo (ESTADO = 'S').
     * @param id el identificador del periodo a reactivar
     * @return {@code true} si la actualización afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean reactivar(int id) {
        String sql = "UPDATE PERIODO_ESCOLAR SET ESTADO = 'S' WHERE ID_PERIODO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al reactivar el periodo escolar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene los periodos escolares activos cuya fecha de inicio corresponde al año actual.
     * @return la lista de periodos del año actual; vacía si no hay registros o si ocurre un error de base de datos
     */
    public List<PeriodoEscolar> getDelAnioActual() {
        List<PeriodoEscolar> lista = new ArrayList<>();
        String sql = "SELECT * FROM PERIODO_ESCOLAR WHERE ESTADO = 'S' " +
                "AND EXTRACT(YEAR FROM FECHA_INICIO) = EXTRACT(YEAR FROM SYSDATE) " +
                "ORDER BY FECHA_INICIO";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener los periodos del año actual: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene todos los periodos escolares activos, ordenados por fecha de inicio descendente.
     * @return la lista de periodos activos; vacía si no hay registros o si ocurre un error de base de datos
     */
    public List<PeriodoEscolar> getActivos() {
        List<PeriodoEscolar> lista = new ArrayList<>();
        String sql = "SELECT * FROM PERIODO_ESCOLAR WHERE ESTADO = 'S' ORDER BY FECHA_INICIO DESC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener los periodos activos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene el periodo escolar activo cuya fecha actual del sistema cae dentro de su rango de fechas.
     * @return el periodo vigente, o {@code null} si no hay ninguno vigente o si ocurre un error de base de datos
     */
    public PeriodoEscolar getPeriodoVigente() {
        String sql = "SELECT * FROM PERIODO_ESCOLAR WHERE ESTADO = 'S' " +
                "AND TRUNC(SYSDATE) BETWEEN FECHA_INICIO AND FECHA_FIN " +
                "ORDER BY FECHA_INICIO DESC FETCH FIRST 1 ROW ONLY";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener el periodo vigente: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Verifica si ya existe otro periodo escolar (distinto al indicado) con el mismo nombre, sin distinguir mayúsculas/minúsculas.
     * @param nombre el nombre a validar
     * @param idPeriodoExcluir el identificador del periodo que debe excluirse de la comparación
     * @return {@code true} si existe otro periodo con ese nombre; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean existeNombreParaOtro(String nombre, int idPeriodoExcluir) {
        String sql = "SELECT COUNT(*) FROM PERIODO_ESCOLAR WHERE UPPER(NOMBRE) = UPPER(?) AND ID_PERIODO <> ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idPeriodoExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al validar el nombre del periodo: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Verifica si ya existe un periodo escolar con el nombre indicado, sin distinguir mayúsculas/minúsculas.
     * @param nombre el nombre a validar
     * @return {@code true} si ya existe un periodo con ese nombre; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean existeNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM PERIODO_ESCOLAR WHERE UPPER(NOMBRE) = UPPER(?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al validar el nombre del periodo: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Construye un objeto {@link PeriodoEscolar} a partir de la fila actual de un ResultSet.
     * @param rs el ResultSet posicionado en la fila a mapear
     * @return el periodo escolar mapeado con todos sus campos
     * @throws SQLException si ocurre un error al leer las columnas del ResultSet
     */
    private PeriodoEscolar mapear(ResultSet rs) throws SQLException {
        PeriodoEscolar p = new PeriodoEscolar();
        p.setIdPeriodo(rs.getInt("ID_PERIODO"));
        p.setNombre(rs.getString("NOMBRE"));
        p.setFechaInicio(rs.getDate("FECHA_INICIO"));
        p.setFechaFin(rs.getDate("FECHA_FIN"));
        p.setEstado(rs.getString("ESTADO"));
        p.setAsistenciasGrupales(rs.getInt("ASISTENCIASGRUPALES"));
        return p;
    }
}
