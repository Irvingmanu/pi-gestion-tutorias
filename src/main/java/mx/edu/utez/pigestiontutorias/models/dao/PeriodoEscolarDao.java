package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.PeriodoEscolar;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeriodoEscolarDao implements Dao<PeriodoEscolar, Integer> {

    @Override
    public boolean create(PeriodoEscolar p) {
        String sql = "INSERT INTO PERIODO_ESCOLAR (NOMBRE, FECHA_INICIO, FECHA_FIN, ACTIVO) VALUES (?, ?, ?, ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDate(2, p.getFechaInicio());
            ps.setDate(3, p.getFechaFin());
            ps.setString(4, p.getActivo() != null ? p.getActivo() : "S");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear el periodo escolar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

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

    @Override
    public boolean update(PeriodoEscolar p) {
        String sql = "UPDATE PERIODO_ESCOLAR SET NOMBRE = ?, FECHA_INICIO = ?, FECHA_FIN = ? WHERE ID_PERIODO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDate(2, p.getFechaInicio());
            ps.setDate(3, p.getFechaFin());
            ps.setInt(4, p.getIdPeriodo());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar el periodo escolar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Borrado logico, mismo patron que TUTOR/ALUMNO (ACTIVO = 'N')
    @Override
    public boolean delete(Integer id) {
        String sql = "UPDATE PERIODO_ESCOLAR SET ACTIVO = 'N' WHERE ID_PERIODO = ?";
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

    public boolean reactivar(int id) {
        String sql = "UPDATE PERIODO_ESCOLAR SET ACTIVO = 'S' WHERE ID_PERIODO = ?";
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

    // Periodos activos del año ACTUAL (dinamico via EXTRACT(YEAR FROM SYSDATE),
    // asi que el mismo codigo sirve el proximo año sin tocarlo). Alimenta el
    // <select> de "Nueva Asignación" en asignacion.jsp.
    public List<PeriodoEscolar> getDelAnioActual() {
        List<PeriodoEscolar> lista = new ArrayList<>();
        String sql = "SELECT * FROM PERIODO_ESCOLAR WHERE ACTIVO = 'S' " +
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

    // Periodo "vigente": el que contiene la fecha de hoy. Se usa para acotar
    // el <input type="date"> al registrar tutorias (grupal/individual) sin
    // hardcodear un rango de dos semanas ni un año fijo.
    public PeriodoEscolar getPeriodoVigente() {
        String sql = "SELECT * FROM PERIODO_ESCOLAR WHERE ACTIVO = 'S' " +
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

    private PeriodoEscolar mapear(ResultSet rs) throws SQLException {
        PeriodoEscolar p = new PeriodoEscolar();
        p.setIdPeriodo(rs.getInt("ID_PERIODO"));
        p.setNombre(rs.getString("NOMBRE"));
        p.setFechaInicio(rs.getDate("FECHA_INICIO"));
        p.setFechaFin(rs.getDate("FECHA_FIN"));
        p.setActivo(rs.getString("ACTIVO"));
        return p;
    }
}