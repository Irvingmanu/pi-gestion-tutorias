package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.PeriodoEscolar;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PeriodoEscolarDao implements Dao<PeriodoEscolar, Integer> {

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

    // Borrado logico, mismo patron que TUTOR/ALUMNO (ESTADO = 'N')
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

    // Periodos activos del año ACTUAL (dinamico via EXTRACT(YEAR FROM SYSDATE),
    // asi que el mismo codigo sirve el proximo año sin tocarlo). Alimenta el
    // <select> de "Nueva Asignación" en asignacion.jsp.
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

    // Periodos activos (cualquier año), para el <select> de "Periodo Escolar" del modal
    // "Nuevo Grupo" en gestion-grupos.jsp: a diferencia de getDelAnioActual(), no se limita
    // al año en curso, porque un grupo puede crearse para un periodo que ya arranco o que
    // todavia no empieza.
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

    // Periodo "vigente": el que contiene la fecha de hoy. Se usa para acotar
    // el <input type="date"> al registrar tutorias (grupal/individual) sin
    // hardcodear un rango de dos semanas ni un año fijo.
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

    // Igual que existeNombre, pero excluyendo el propio registro: se usa al editar un periodo
    // para no rechazar el nombre contra si mismo cuando no cambio.
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
        p.setEstado(rs.getString("ESTADO"));
        p.setAsistenciasGrupales(rs.getInt("ASISTENCIASGRUPALES"));
        return p;
    }
}
