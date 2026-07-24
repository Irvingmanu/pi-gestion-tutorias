package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.*;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlumnoDAO implements Dao<Alumno, String> {

    @Override
    public boolean create(Alumno entidad) {
        String passTemporal = "Tut@" + entidad.getMatricula();

        String sqlUsuario = "INSERT INTO USUARIO (ROL, IDENTIFICADOR, PASS, CORREO_INSTITUCIONAL) VALUES (?, ?, ?, ?)";
        String sqlAlumno = "INSERT INTO ALUMNO(MATRICULA, NOMBRES, APELLIDOS, CORREO_INSTITUCIONAL, TELEFONO, ID_GENERO, ID_CARRERA, ID_CUATRIMESTRE, ID_LETRA_GRUPO, ID_USUARIO) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idUsuarioGenerado;
            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario, new String[]{"ID_USUARIO"})) {
                psUsuario.setString(1, "Alumno");
                psUsuario.setString(2, entidad.getMatricula());
                psUsuario.setString(3, passTemporal);
                psUsuario.setString(4, entidad.getCorreoInstitucional());
                psUsuario.executeUpdate();

                try (ResultSet keys = psUsuario.getGeneratedKeys()) {
                    if (!keys.next()) {
                        con.rollback();
                        return false;
                    }
                    idUsuarioGenerado = keys.getInt(1);
                }
            }

            try (PreparedStatement psAlumno = con.prepareStatement(sqlAlumno)) {
                psAlumno.setString(1, entidad.getMatricula());
                psAlumno.setString(2, entidad.getNombres());
                psAlumno.setString(3, entidad.getApellidos());
                psAlumno.setString(4, entidad.getCorreoInstitucional());
                psAlumno.setString(5, entidad.getTelefono());
                psAlumno.setInt(6, entidad.getIdGenero());
                psAlumno.setInt(7, entidad.getIdCarrera());
                psAlumno.setInt(8, entidad.getIdCuatrimestre());
                psAlumno.setInt(9, entidad.getIdLetraGrupo());
                psAlumno.setInt(10, idUsuarioGenerado);

                int filasAfectadas = psAlumno.executeUpdate();
                con.commit();
                entidad.setIdUsuario(idUsuarioGenerado);
                return filasAfectadas > 0;
            }
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

    public boolean existeMatricula(String matricula) {
        String sql = "SELECT COUNT(*) FROM ALUMNO WHERE MATRICULA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
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

    public boolean existeCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM ALUMNO WHERE CORREO_INSTITUCIONAL = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
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

    public boolean existeTelefono(String telefono) {
        String sql = "SELECT COUNT(*) FROM ALUMNO WHERE TELEFONO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, telefono);
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

    public boolean existeMatricula(String matricula, int idAlumnoActual) {
        String sql = "SELECT COUNT(*) FROM ALUMNO WHERE MATRICULA = ? AND ID_ALUMNO <> ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ps.setInt(2, idAlumnoActual);
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

    public boolean existeCorreo(String correo, int idAlumnoActual) {
        String sql = "SELECT COUNT(*) FROM ALUMNO WHERE CORREO_INSTITUCIONAL = ? AND ID_ALUMNO <> ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setInt(2, idAlumnoActual);
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

    public boolean existeTelefono(String telefono, int idAlumnoActual) {
        String sql = "SELECT COUNT(*) FROM ALUMNO WHERE TELEFONO = ? AND ID_ALUMNO <> ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, telefono);
            ps.setInt(2, idAlumnoActual);
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

    @Override
    public List<Alumno> getAll() {
        List<Alumno> listaAlumnos = new ArrayList<>();
        String sql = "SELECT * FROM ALUMNO";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaAlumnos.add(mapearAlumno(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return listaAlumnos;
    }

    @Override
    public Alumno getById(String matricula) {
        String sql = "SELECT * FROM ALUMNO WHERE MATRICULA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearAlumno(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Necesario para el módulo de Solicitud: la sesión solo guarda idUsuario,
    // así que hay que traducirlo a la MATRICULA real de la tabla ALUMNO.
    public Alumno getByIdUsuario(int idUsuario) {
        String sql = "SELECT * FROM ALUMNO WHERE ID_USUARIO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearAlumno(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean update(Alumno entidad) {
        String sqlAlumno = "UPDATE ALUMNO SET NOMBRES = ?, APELLIDOS = ?, CORREO_INSTITUCIONAL = ?, TELEFONO = ?, ID_GENERO = ?, ID_CARRERA = ?, ID_CUATRIMESTRE = ?, ID_LETRA_GRUPO = ? WHERE MATRICULA = ?";
        String sqlUsuario = "UPDATE USUARIO SET CORREO_INSTITUCIONAL = ? WHERE ID_USUARIO = ?";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int filasAfectadas;
            try (PreparedStatement psAlumno = con.prepareStatement(sqlAlumno)) {
                psAlumno.setString(1, entidad.getNombres());
                psAlumno.setString(2, entidad.getApellidos());
                psAlumno.setString(3, entidad.getCorreoInstitucional());
                psAlumno.setString(4, entidad.getTelefono());
                psAlumno.setInt(5, entidad.getIdGenero());
                psAlumno.setInt(6, entidad.getIdCarrera());
                psAlumno.setInt(7, entidad.getIdCuatrimestre());
                psAlumno.setInt(8, entidad.getIdLetraGrupo());
                psAlumno.setString(9, entidad.getMatricula());
                filasAfectadas = psAlumno.executeUpdate();
            }

            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario)) {
                psUsuario.setString(1, entidad.getCorreoInstitucional());
                psUsuario.setInt(2, entidad.getIdUsuario());
                psUsuario.executeUpdate();
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

    @Override
    public boolean delete(String matricula) {
        String sql = "DELETE FROM ALUMNO WHERE MATRICULA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Genero> getAllGeneros() {
        List<Genero> lista = new ArrayList<>();
        String sql = "SELECT ID_GENERO, NOMBRE FROM GENERO";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Genero(rs.getInt("ID_GENERO"), rs.getString("NOMBRE")));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<Carrera> getAllCarreras() {
        List<Carrera> lista = new ArrayList<>();
        String sql = "SELECT ID_CARRERA, NOMBRE FROM CARRERA";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Carrera(rs.getInt("ID_CARRERA"), rs.getString("NOMBRE")));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<Cuatrimestre> getAllCuatrimestres() {
        List<Cuatrimestre> lista = new ArrayList<>();
        String sql = "SELECT ID_CUATRIMESTRE, NUMERO FROM CUATRIMESTRE";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Cuatrimestre(rs.getInt("ID_CUATRIMESTRE"), rs.getInt("NUMERO")));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<LetraGrupo> getAllLetrasGrupo() {
        List<LetraGrupo> lista = new ArrayList<>();
        String sql = "SELECT ID_LETRA, LETRA FROM LETRA_GRUPO";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new LetraGrupo(rs.getInt("ID_LETRA"), rs.getString("LETRA")));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private Alumno mapearAlumno(ResultSet rs) throws SQLException {
        Alumno alumno = new Alumno();
        alumno.setIdAlumno(rs.getInt("ID_ALUMNO"));
        alumno.setMatricula(rs.getString("MATRICULA"));
        alumno.setNombres(rs.getString("NOMBRES"));
        alumno.setApellidos(rs.getString("APELLIDOS"));
        alumno.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
        alumno.setTelefono(rs.getString("TELEFONO"));
        alumno.setIdGenero(rs.getInt("ID_GENERO"));
        alumno.setIdCarrera(rs.getInt("ID_CARRERA"));
        alumno.setIdCuatrimestre(rs.getInt("ID_CUATRIMESTRE"));
        alumno.setIdLetraGrupo(rs.getInt("ID_LETRA_GRUPO"));
        alumno.setIdUsuario(rs.getInt("ID_USUARIO"));
        return alumno;
    }
}