package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Coordinador;
import mx.edu.utez.pigestiontutorias.utils.PasswordUtil;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsable del acceso a datos de los coordinadores (COORDINADOR), incluyendo
 * su autenticación por correo, la gestión de contraseñas y la baja lógica.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-21
 */
public class CoordinadorDAO implements Dao<Coordinador, Integer> {

    /**
     * Crea un nuevo coordinador. Si no se proporciona contraseña, genera una por defecto
     * a partir del número de empleado, y siempre almacena la contraseña con hash.
     * @param entidad el coordinador a crear
     * @return {@code true} si la inserción afectó al menos una fila; {@code false} en caso contrario
     */
    @Override
    public boolean create(Coordinador entidad) {
        String sql = "INSERT INTO COORDINADOR(NUMERO_EMPLEADO, NOMBRES, APELLIDO_PATERNO, APELLIDO_MATERNO, CORREO_INSTITUCIONAL, TELEFONO, PASS) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String pass = (entidad.getPass() != null && !entidad.getPass().isBlank())
                    ? entidad.getPass() : "Tut@" + entidad.getNumeroEmpleado();

            ps.setInt(1, entidad.getNumeroEmpleado());
            ps.setString(2, entidad.getNombres());
            ps.setString(3, entidad.getApellidoPaterno());
            ps.setString(4, entidad.getApellidoMaterno());
            ps.setString(5, entidad.getCorreoInstitucional());
            ps.setString(6, entidad.getTelefono());
            ps.setString(7, PasswordUtil.hash(pass));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todos los coordinadores registrados.
     * @return la lista de todos los coordinadores
     */
    @Override
    public List<Coordinador> getAll() {
        List<Coordinador> lista = new ArrayList<>();
        String sql = "SELECT * FROM COORDINADOR";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearCoordinador(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene un coordinador por su número de empleado.
     * @param numeroEmpleado el número de empleado del coordinador buscado
     * @return el coordinador encontrado, o {@code null} si no existe
     */
    @Override
    public Coordinador getById(Integer numeroEmpleado) {
        String sql = "SELECT * FROM COORDINADOR WHERE NUMERO_EMPLEADO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, numeroEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCoordinador(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Busca un coordinador por su correo institucional, sin distinguir mayúsculas/minúsculas.
     * @param correo el correo institucional a buscar
     * @return el coordinador encontrado, o {@code null} si no existe
     */
    public Coordinador findByCorreo(String correo) {
        String sql = "SELECT * FROM COORDINADOR WHERE UPPER(CORREO_INSTITUCIONAL) = UPPER(?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCoordinador(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Actualiza los datos de contacto de un coordinador existente (no incluye la contraseña).
     * @param entidad el coordinador con los datos actualizados (debe incluir su número de empleado)
     * @return {@code true} si se actualizó al menos una fila; {@code false} en caso contrario
     */
    @Override
    public boolean update(Coordinador entidad) {
        String sql = "UPDATE COORDINADOR SET NOMBRES = ?, APELLIDO_PATERNO = ?, APELLIDO_MATERNO = ?, CORREO_INSTITUCIONAL = ?, TELEFONO = ? WHERE NUMERO_EMPLEADO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombres());
            ps.setString(2, entidad.getApellidoPaterno());
            ps.setString(3, entidad.getApellidoMaterno());
            ps.setString(4, entidad.getCorreoInstitucional());
            ps.setString(5, entidad.getTelefono());
            ps.setInt(6, entidad.getNumeroEmpleado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Da de baja lógica a un coordinador, marcando su ESTADO como 'N'.
     * @param numeroEmpleado el número de empleado del coordinador a dar de baja
     * @return {@code true} si se actualizó al menos una fila; {@code false} en caso contrario
     */
    @Override
    public boolean delete(Integer numeroEmpleado) {
        String sql = "UPDATE COORDINADOR SET ESTADO = 'N' WHERE NUMERO_EMPLEADO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, numeroEmpleado);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza la contraseña de un coordinador, almacenándola con hash.
     * @param numeroEmpleado el número de empleado del coordinador
     * @param nuevaPassword la nueva contraseña en texto plano, que será hasheada antes de guardarse
     * @return {@code true} si se actualizó al menos una fila; {@code false} en caso contrario
     */
    public boolean actualizarPassword(int numeroEmpleado, String nuevaPassword) {
        String sql = "UPDATE COORDINADOR SET PASS = ? WHERE NUMERO_EMPLEADO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hash(nuevaPassword));
            ps.setInt(2, numeroEmpleado);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Construye una entidad {@link Coordinador} a partir de la fila actual de un {@link ResultSet}.
     * @param rs el conjunto de resultados posicionado en la fila a mapear
     * @return el coordinador construido con los datos de la fila
     * @throws SQLException si ocurre un error al leer las columnas del resultado
     */
    private Coordinador mapearCoordinador(ResultSet rs) throws SQLException {
        Coordinador coordinador = new Coordinador();
        coordinador.setNumeroEmpleado(rs.getInt("NUMERO_EMPLEADO"));
        coordinador.setNombres(rs.getString("NOMBRES"));
        coordinador.setApellidoPaterno(rs.getString("APELLIDO_PATERNO"));
        coordinador.setApellidoMaterno(rs.getString("APELLIDO_MATERNO"));
        coordinador.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
        coordinador.setTelefono(rs.getString("TELEFONO"));
        coordinador.setPass(rs.getString("PASS"));
        coordinador.setEstado(rs.getString("ESTADO"));
        return coordinador;
    }
}
