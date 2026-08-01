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
        // Trae activos e inactivos: la pantalla de gestion decide que mostrar
        // segun el filtro "mostrar dados de baja".
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

    // Perfil del alumno para la sesión: además de sus datos, resuelve
    // Carrera/Cuatrimestre/LetraGrupo como objetos para poder usarlos
    // directamente en EL (ej. ${alumno.carrera.nombre}) sin scriptlets.
    public Alumno getPerfilCompleto(int idUsuario) {
        Alumno alumno = getByIdUsuario(idUsuario);
        if (alumno == null) return null;

        if (alumno.getIdCarrera() != null) {
            for (Carrera c : getAllCarreras()) {
                if (c.getIdCarrera() == alumno.getIdCarrera()) {
                    alumno.setCarrera(c);
                    break;
                }
            }
        }
        if (alumno.getIdCuatrimestre() != null) {
            for (Cuatrimestre c : getAllCuatrimestres()) {
                if (c.getIdCuatrimestre() == alumno.getIdCuatrimestre()) {
                    alumno.setCuatrimestre(c);
                    break;
                }
            }
        }
        if (alumno.getIdLetraGrupo() != null) {
            for (LetraGrupo lg : getAllLetrasGrupo()) {
                if (lg.getIdLetra() == alumno.getIdLetraGrupo()) {
                    alumno.setLetraGrupo(lg);
                    break;
                }
            }
        }
        return alumno;
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
        // Baja logica: preserva el historial de asistencias/sesiones y bloquea el acceso del alumno.
        String sqlSelect = "SELECT ID_USUARIO FROM ALUMNO WHERE MATRICULA = ?";
        String sqlAlumno = "UPDATE ALUMNO SET ACTIVO = 'N' WHERE MATRICULA = ?";
        String sqlUsuario = "UPDATE USUARIO SET ACTIVO = 'N' WHERE ID_USUARIO = ?";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idUsuario = 0;
            try (PreparedStatement psSel = con.prepareStatement(sqlSelect)) {
                psSel.setString(1, matricula);
                try (ResultSet rs = psSel.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    idUsuario = rs.getInt("ID_USUARIO");
                }
            }

            int filasAfectadas;
            try (PreparedStatement psAlumno = con.prepareStatement(sqlAlumno)) {
                psAlumno.setString(1, matricula);
                filasAfectadas = psAlumno.executeUpdate();
            }

            if (idUsuario > 0) {
                try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario)) {
                    psUsuario.setInt(1, idUsuario);
                    psUsuario.executeUpdate();
                }
            }

            con.commit();
            return filasAfectadas > 0;
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
        alumno.setActivo(rs.getString("ACTIVO"));
        return alumno;
    }

    // Reactiva a un alumno dado de baja y restaura su acceso al sistema.
    public boolean reactivar(String matricula) {
        String sqlSelect = "SELECT ID_USUARIO FROM ALUMNO WHERE MATRICULA = ?";
        String sqlAlumno = "UPDATE ALUMNO SET ACTIVO = 'S' WHERE MATRICULA = ?";
        String sqlUsuario = "UPDATE USUARIO SET ACTIVO = 'S' WHERE ID_USUARIO = ?";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idUsuario = 0;
            try (PreparedStatement psSel = con.prepareStatement(sqlSelect)) {
                psSel.setString(1, matricula);
                try (ResultSet rs = psSel.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    idUsuario = rs.getInt("ID_USUARIO");
                }
            }

            int filasAfectadas;
            try (PreparedStatement psAlumno = con.prepareStatement(sqlAlumno)) {
                psAlumno.setString(1, matricula);
                filasAfectadas = psAlumno.executeUpdate();
            }

            if (idUsuario > 0) {
                try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario)) {
                    psUsuario.setInt(1, idUsuario);
                    psUsuario.executeUpdate();
                }
            }

            con.commit();
            return filasAfectadas > 0;
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

    // idCarrera/idCuatrimestre/idLetraGrupo definen el grupo real del alumno: sin los tres,
    // dos carreras que comparten letra+cuatrimestre podrian filtrarse sesiones grupales
    // ajenas (mismo problema de contaminacion cruzada ya corregido en getAcuerdosPorAlumno).
    public List<EventoAgenda> getAgendaAlumno(String matricula, int idCarrera, int idCuatrimestre, int idLetraGrupo) {
        List<EventoAgenda> listaEventos = new ArrayList<>();

        String query =
                "SELECT 'Individual' AS TIPO, " +
                        "       t.NOMBRES || ' ' || t.APELLIDOS AS DESCRIPCION, " +
                        "       si.FECHA AS FECHA, " +
                        "       si.HORA AS HORA, " +
                        "       COALESCE(si.ESTATUS_ASISTENCIA, 'Falta') AS ESTATUS_ASISTENCIA " +
                        "FROM SESION_INDIVIDUAL si " +
                        "JOIN TUTOR t ON si.ID_TUTOR = t.ID_TUTOR " +
                        "WHERE TRIM(si.MATRICULA) = TRIM(?) " +
                        "UNION ALL " +
                        "SELECT 'Grupal' AS TIPO, " +
                        "       'Grupo ' || c.NUMERO || '°' || lg.LETRA AS DESCRIPCION, " +
                        "       sg.FECHA AS FECHA, " +
                        "       sg.HORA AS HORA, " +
                        "       COALESCE(a.ESTATUS_ASISTENCIA, 'Falta') AS ESTATUS_ASISTENCIA " +
                        "FROM SESION_GRUPAL sg " +
                        "JOIN LETRA_GRUPO lg ON sg.ID_LETRA_GRUPO = lg.ID_LETRA " +
                        "JOIN CUATRIMESTRE c ON sg.ID_CUATRIMESTRE = c.ID_CUATRIMESTRE " +
                        "LEFT JOIN ASISTENCIA a ON a.ID_SESION_GRUPAL = sg.ID_SESION_GRUPAL AND TRIM(a.MATRICULA) = TRIM(?) " +
                        "WHERE sg.ID_LETRA_GRUPO = ? AND sg.ID_CARRERA = ? AND sg.ID_CUATRIMESTRE = ? " +
                        "ORDER BY FECHA ASC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            String matriculaTrim = matricula != null ? matricula.trim() : "";
            ps.setString(1, matriculaTrim);
            ps.setString(2, matriculaTrim);
            ps.setInt(3, idLetraGrupo);
            ps.setInt(4, idCarrera);
            ps.setInt(5, idCuatrimestre);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp fecha = rs.getTimestamp("FECHA");
                    listaEventos.add(new EventoAgenda(
                            rs.getString("TIPO"),
                            rs.getString("DESCRIPCION"),
                            fecha,
                            rs.getString("HORA"),
                            rs.getString("ESTATUS_ASISTENCIA")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaEventos;
    }
}