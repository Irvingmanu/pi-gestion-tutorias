package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.AlumnoBusquedaDTO;
import mx.edu.utez.pigestiontutorias.models.Carrera;
import mx.edu.utez.pigestiontutorias.models.EventoAgenda;
import mx.edu.utez.pigestiontutorias.models.Genero;
import mx.edu.utez.pigestiontutorias.utils.PasswordUtil;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlumnoDAO implements Dao<Alumno, String> {

    private final GrupoDao grupoDao = new GrupoDao();

    @Override
    public boolean create(Alumno entidad) {
        String sql = "INSERT INTO ALUMNO(MATRICULA, NOMBRES, APELLIDO_PATERNO, APELLIDO_MATERNO, CORREO_INSTITUCIONAL, TELEFONO, ID_GENERO, ID_GRUPO, PASS) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String pass = (entidad.getPass() != null && !entidad.getPass().isBlank())
                    ? entidad.getPass() : "Tut@" + entidad.getMatricula();

            ps.setString(1, entidad.getMatricula());
            ps.setString(2, entidad.getNombres());
            ps.setString(3, entidad.getApellidoPaterno());
            ps.setString(4, entidad.getApellidoMaterno());
            ps.setString(5, entidad.getCorreoInstitucional());
            ps.setString(6, entidad.getTelefono());
            ps.setInt(7, entidad.getIdGenero());
            ps.setInt(8, entidad.getIdGrupo());
            ps.setString(9, PasswordUtil.hash(pass));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Alta masiva (carga de Excel via AlumnoServlet + Apache POI, accion=cargaMasivaAlumnos):
    // un solo batch/commit para todos los alumnos ya validados por el servlet, mas un
    // segundo batch en la MISMA transaccion para dejar registro en ALUMNO_GRUPO_HISTORICO
    // (alta inicial del alumno en idGrupo). idGrupo es el mismo para todo el lote: la carga
    // masiva es "un archivo = un grupo", elegido en el modal antes de subir el Excel.
    // Usa PasswordUtil.hash(...) igual que create(), para que un alumno de carga masiva
    // pueda iniciar sesion exactamente igual que uno dado de alta a mano.
    public int crearMasivo(List<Alumno> alumnos, int idGrupo) {
        if (alumnos == null || alumnos.isEmpty()) return 0;

        String sqlAlumno = "INSERT INTO ALUMNO(MATRICULA, NOMBRES, APELLIDO_PATERNO, APELLIDO_MATERNO, CORREO_INSTITUCIONAL, TELEFONO, ID_GENERO, ID_GRUPO, PASS) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlHistorico = "INSERT INTO ALUMNO_GRUPO_HISTORICO (MATRICULA, ID_GRUPO, FECHA_INICIO, MOTIVO_CAMBIO) VALUES (?, ?, SYSDATE, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int insertados = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlAlumno)) {
                for (Alumno a : alumnos) {
                    String pass = (a.getPass() != null && !a.getPass().isBlank())
                            ? a.getPass() : "Tut@" + a.getMatricula();

                    ps.setString(1, a.getMatricula());
                    ps.setString(2, a.getNombres());
                    ps.setString(3, a.getApellidoPaterno());
                    ps.setString(4, a.getApellidoMaterno());
                    ps.setString(5, a.getCorreoInstitucional());
                    ps.setString(6, a.getTelefono());
                    ps.setInt(7, a.getIdGenero());
                    ps.setInt(8, idGrupo);
                    ps.setString(9, PasswordUtil.hash(pass));
                    ps.addBatch();
                }

                int[] resultados = ps.executeBatch();
                for (int resultado : resultados) {
                    if (resultado > 0 || resultado == Statement.SUCCESS_NO_INFO) insertados++;
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlHistorico)) {
                for (Alumno a : alumnos) {
                    ps.setString(1, a.getMatricula());
                    ps.setInt(2, idGrupo);
                    ps.setString(3, "Alta por carga masiva");
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
            return insertados;
        } catch (SQLException e) {
            System.err.println("Error en la carga masiva de alumnos: " + e.getMessage());
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return 0;
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException ex) { ex.printStackTrace(); }
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

    // MATRICULA ya es la PK real de ALUMNO (no hay ID_ALUMNO surrogate), asi que estas
    // variantes de edicion excluyen por MATRICULA en vez de un id numerico aparte.
    public boolean existeCorreo(String correo, String matriculaExcluida) {
        String sql = "SELECT COUNT(*) FROM ALUMNO WHERE CORREO_INSTITUCIONAL = ? AND MATRICULA <> ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, matriculaExcluida);
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

    public boolean existeTelefono(String telefono, String matriculaExcluida) {
        String sql = "SELECT COUNT(*) FROM ALUMNO WHERE TELEFONO = ? AND MATRICULA <> ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, telefono);
            ps.setString(2, matriculaExcluida);
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

    // Login: busca por correo institucional (ver LoginServlet, que intenta ALUMNO
    // antes que TUTOR y COORDINADOR). Incluye PASS para que el servlet valide la
    // contraseña; ESTADO se revisa aparte para no dejar entrar a alguien dado de baja.
    public Alumno findByCorreo(String correo) {
        String sql = "SELECT * FROM ALUMNO WHERE UPPER(CORREO_INSTITUCIONAL) = UPPER(?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearAlumno(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Perfil del alumno para la sesión: además de sus datos, resuelve el Grupo
    // (Carrera + Cuatrimestre + Letra + Periodo, ya unificados en GRUPO) como objeto
    // para poder usarlo directamente en EL (ej. ${alumno.grupo.nombreGrupo}) sin scriptlets.
    public Alumno getPerfilCompleto(String matricula) {
        Alumno alumno = getById(matricula);
        if (alumno == null) return null;

        if (alumno.getIdGrupo() != null) {
            alumno.setGrupo(grupoDao.getById(alumno.getIdGrupo()));
        }
        return alumno;
    }

    @Override
    public boolean update(Alumno entidad) {
        String sql = "UPDATE ALUMNO SET NOMBRES = ?, APELLIDO_PATERNO = ?, APELLIDO_MATERNO = ?, " +
                "CORREO_INSTITUCIONAL = ?, TELEFONO = ?, ID_GENERO = ?, ID_GRUPO = ? WHERE MATRICULA = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, entidad.getNombres());
            ps.setString(2, entidad.getApellidoPaterno());
            ps.setString(3, entidad.getApellidoMaterno());
            ps.setString(4, entidad.getCorreoInstitucional());
            ps.setString(5, entidad.getTelefono());
            ps.setInt(6, entidad.getIdGenero());
            ps.setInt(7, entidad.getIdGrupo());
            ps.setString(8, entidad.getMatricula());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String matricula) {
        // Baja logica: preserva el historial de asistencias/sesiones y bloquea el acceso del alumno.
        String sql = "UPDATE ALUMNO SET ESTADO = 'N' WHERE MATRICULA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Reactiva a un alumno dado de baja y restaura su acceso al sistema.
    public boolean reactivar(String matricula) {
        String sql = "UPDATE ALUMNO SET ESTADO = 'S' WHERE MATRICULA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarPassword(String matricula, String nuevaPassword) {
        String sql = "UPDATE ALUMNO SET PASS = ? WHERE MATRICULA = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hash(nuevaPassword));
            ps.setString(2, matricula);
            return ps.executeUpdate() > 0;
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
        return new CarreraDao().getAll();
    }

    // Siguiente numero de 3 digitos disponible para una MATRICULA nueva (formato
    // AnioPeriodoSigla + Contador, ver GenerarCredencialesServlet). Revisa las matriculas
    // que ya empiezan con ese prefijo (Anio+Periodo+Sigla, en MAYUSCULAS porque
    // AlumnoServlet siempre guarda la matricula en mayusculas), se queda con el sufijo
    // numerico de cada una y regresa el mayor + 1 (o 1 si todavia no hay ninguna).
    // Se resuelve en Java (no con TO_NUMBER(SUBSTR(...)) en SQL) para no reventar la
    // consulta si algun dato viejo/manual no trae un sufijo numerico.
    public int obtenerSiguienteContador(String prefijo) {
        String sql = "SELECT MATRICULA FROM ALUMNO WHERE MATRICULA LIKE ?";
        int mayor = 0;

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, prefijo + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String matricula = rs.getString("MATRICULA");
                    if (matricula == null || matricula.length() <= prefijo.length()) continue;

                    try {
                        mayor = Math.max(mayor, Integer.parseInt(matricula.substring(prefijo.length())));
                    } catch (NumberFormatException ignorada) {
                        // Matricula con sufijo no numerico: no cuenta para el correlativo.
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al calcular el siguiente contador de matricula: " + e.getMessage());
            e.printStackTrace();
        }

        return mayor + 1;
    }

    // Buscador de alumnos del dashboard de Reportes (por nombre completo o matricula).
    // idTutor == null -> busqueda global (vista Coordinador, ve a todos los alumnos).
    // idTutor != null -> solo alumnos cuyo grupo esta asignado a ese tutor (via
    // ASIGNACION_TUTOR), para que un tutor jamas pueda ver/buscar alumnos ajenos.
    public List<AlumnoBusquedaDTO> buscarAlumnos(String texto, Integer idTutor) {
        List<AlumnoBusquedaDTO> lista = new ArrayList<>();
        if (texto == null || texto.isBlank()) return lista;

        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT al.MATRICULA, al.NOMBRES, al.APELLIDO_PATERNO, al.APELLIDO_MATERNO, " +
                        "car.NOMBRE AS NOMBRE_CARRERA, g.CUATRIMESTRE, g.LETRA " +
                        "FROM ALUMNO al " +
                        "JOIN GRUPO g ON g.ID_GRUPO = al.ID_GRUPO " +
                        "JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA ");
        if (idTutor != null) {
            sql.append("JOIN ASIGNACION_TUTOR asg ON asg.ID_GRUPO = al.ID_GRUPO AND asg.ESTADO = 'S' AND asg.ID_TUTOR = ? ");
        }
        sql.append("WHERE al.ESTADO = 'S' AND (UPPER(al.MATRICULA) LIKE UPPER(?) " +
                "OR UPPER(al.NOMBRES || ' ' || al.APELLIDO_PATERNO || ' ' || al.APELLIDO_MATERNO) LIKE UPPER(?)) " +
                "ORDER BY al.APELLIDO_PATERNO, al.APELLIDO_MATERNO, al.NOMBRES " +
                "FETCH FIRST 20 ROWS ONLY");

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int idx = 1;
            if (idTutor != null) {
                ps.setInt(idx++, idTutor);
            }
            String comodin = "%" + texto.trim() + "%";
            ps.setString(idx++, comodin);
            ps.setString(idx, comodin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String apellidoM = rs.getString("APELLIDO_MATERNO");
                    String nombreCompleto = rs.getString("NOMBRES") + " " + rs.getString("APELLIDO_PATERNO")
                            + (apellidoM != null && !apellidoM.isBlank() ? " " + apellidoM : "");
                    String grupoAsignado = rs.getString("NOMBRE_CARRERA") + " " + rs.getInt("CUATRIMESTRE") + "°" + rs.getString("LETRA");
                    lista.add(new AlumnoBusquedaDTO(rs.getString("MATRICULA"), nombreCompleto, grupoAsignado));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar alumnos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    private Alumno mapearAlumno(ResultSet rs) throws SQLException {
        Alumno alumno = new Alumno();
        alumno.setMatricula(rs.getString("MATRICULA"));
        alumno.setNombres(rs.getString("NOMBRES"));
        alumno.setApellidoPaterno(rs.getString("APELLIDO_PATERNO"));
        alumno.setApellidoMaterno(rs.getString("APELLIDO_MATERNO"));
        alumno.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
        alumno.setTelefono(rs.getString("TELEFONO"));
        alumno.setIdGenero(rs.getInt("ID_GENERO"));
        alumno.setIdGrupo(rs.getInt("ID_GRUPO"));
        alumno.setPass(rs.getString("PASS"));
        alumno.setEstado(rs.getString("ESTADO"));
        return alumno;
    }

    // idCarrera/idGrupo definen el grupo real del alumno: sin el grupo correcto, dos
    // carreras que comparten letra+cuatrimestre podrian filtrarse sesiones grupales ajenas.
    public List<EventoAgenda> getAgendaAlumno(String matricula, int idGrupo) {
        List<EventoAgenda> listaEventos = new ArrayList<>();

        String query =
                "SELECT 'Individual' AS TIPO, " +
                        "       t.NOMBRES || ' ' || t.APELLIDO_PATERNO AS DESCRIPCION, " +
                        "       si.FECHA AS FECHA, " +
                        "       si.HORA AS HORA, " +
                        "       COALESCE(si.ESTATUS_ASISTENCIA, 'Falta') AS ESTATUS_ASISTENCIA " +
                        "FROM SESION_INDIVIDUAL si " +
                        "JOIN TUTOR t ON si.ID_TUTOR = t.NUMERO_EMPLEADO " +
                        "WHERE TRIM(si.MATRICULA) = TRIM(?) " +
                        "UNION ALL " +
                        "SELECT 'Grupal' AS TIPO, " +
                        "       'Grupo ' || g.CUATRIMESTRE || '°' || g.LETRA AS DESCRIPCION, " +
                        "       sg.FECHA AS FECHA, " +
                        "       sg.HORA AS HORA, " +
                        "       COALESCE(a.ESTATUS_ASISTENCIA, 'Falta') AS ESTATUS_ASISTENCIA " +
                        "FROM SESION_GRUPAL sg " +
                        "JOIN GRUPO g ON g.ID_GRUPO = sg.ID_GRUPO " +
                        "LEFT JOIN ASISTENCIA a ON a.ID_SESION_GRUPAL = sg.ID_SESION_GRUPAL AND TRIM(a.MATRICULA) = TRIM(?) " +
                        "WHERE sg.ID_GRUPO = ? " +
                        "ORDER BY FECHA ASC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            String matriculaTrim = matricula != null ? matricula.trim() : "";
            ps.setString(1, matriculaTrim);
            ps.setString(2, matriculaTrim);
            ps.setInt(3, idGrupo);

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
