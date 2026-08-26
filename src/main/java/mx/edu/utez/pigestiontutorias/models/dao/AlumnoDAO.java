package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.*;
import mx.edu.utez.pigestiontutorias.utils.PasswordUtil;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsable del acceso a datos de los alumnos (ALUMNO), incluyendo su alta individual
 * y masiva, la gestión de su historial de cambios de grupo (ALUMNO_GRUPO_HISTORICO), la
 * generación de matrículas, la búsqueda para tutores/coordinadores y la consulta de su agenda
 * de sesiones.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-21
 */
public class AlumnoDAO implements Dao<Alumno, String> {

    private final GrupoDao grupoDao = new GrupoDao();

    /**
     * Crea un nuevo alumno y registra su alta inicial en el histórico de grupos, dentro de una transacción.
     * Si no se proporciona contraseña, genera una por defecto a partir de la matrícula.
     * @param entidad el alumno a crear
     * @return {@code true} si la inserción del alumno se realizó con éxito; {@code false} en caso contrario
     */
    @Override
    public boolean create(Alumno entidad) {
        String sqlAlumno = "INSERT INTO ALUMNO(MATRICULA, NOMBRES, APELLIDO_PATERNO, APELLIDO_MATERNO, CORREO_INSTITUCIONAL, TELEFONO, ID_GENERO, ID_GRUPO, PASS) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlHistorico = "INSERT INTO ALUMNO_GRUPO_HISTORICO (MATRICULA, ID_GRUPO, FECHA_INICIO, MOTIVO_CAMBIO) VALUES (?, ?, SYSDATE, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            String pass = (entidad.getPass() != null && !entidad.getPass().isBlank())
                    ? entidad.getPass() : "Tut@" + entidad.getMatricula();

            boolean insertado;
            try (PreparedStatement ps = con.prepareStatement(sqlAlumno)) {
                ps.setString(1, entidad.getMatricula());
                ps.setString(2, entidad.getNombres());
                ps.setString(3, entidad.getApellidoPaterno());
                ps.setString(4, entidad.getApellidoMaterno());
                ps.setString(5, entidad.getCorreoInstitucional());
                ps.setString(6, entidad.getTelefono());
                ps.setInt(7, entidad.getIdGenero());
                ps.setInt(8, entidad.getIdGrupo());
                ps.setString(9, PasswordUtil.hash(pass));
                insertado = ps.executeUpdate() > 0;
            }

            if (insertado) {
                try (PreparedStatement ps = con.prepareStatement(sqlHistorico)) {
                    ps.setString(1, entidad.getMatricula());
                    ps.setInt(2, entidad.getIdGrupo());
                    ps.setString(3, "Alta inicial");
                    ps.executeUpdate();
                }
            }

            con.commit();
            return insertado;
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

    /**
     * Crea en bloque una lista de alumnos y registra su alta por carga masiva en el histórico de grupos,
     * usando inserciones por lote (batch) dentro de una única transacción.
     * @param alumnos la lista de alumnos a crear
     * @param idGrupo el identificador del grupo al que se asignarán todos los alumnos
     * @return la cantidad de alumnos insertados con éxito, o 0 si la lista está vacía/nula o si ocurre un error
     */
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

    /**
     * Verifica si ya existe un alumno registrado con la matrícula indicada.
     * @param matricula la matrícula a verificar
     * @return {@code true} si la matrícula ya existe; {@code false} en caso contrario
     */
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

    /**
     * Verifica si ya existe un alumno registrado con el correo institucional indicado.
     * @param correo el correo institucional a verificar
     * @return {@code true} si el correo ya existe; {@code false} en caso contrario
     */
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

    /**
     * Verifica si ya existe un alumno registrado con el teléfono indicado.
     * @param telefono el teléfono a verificar
     * @return {@code true} si el teléfono ya existe; {@code false} en caso contrario
     */
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

    /**
     * Verifica si ya existe otro alumno (distinto al de la matrícula excluida) registrado con el correo indicado,
     * útil para validar duplicados al editar un alumno existente.
     * @param correo el correo institucional a verificar
     * @param matriculaExcluida la matrícula del alumno que se está editando, excluida de la comparación
     * @return {@code true} si el correo ya existe en otro alumno; {@code false} en caso contrario
     */
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

    /**
     * Verifica si ya existe otro alumno (distinto al de la matrícula excluida) registrado con el teléfono indicado,
     * útil para validar duplicados al editar un alumno existente.
     * @param telefono el teléfono a verificar
     * @param matriculaExcluida la matrícula del alumno que se está editando, excluida de la comparación
     * @return {@code true} si el teléfono ya existe en otro alumno; {@code false} en caso contrario
     */
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

    /**
     * Obtiene todos los alumnos registrados, sin importar su estado.
     * @return la lista de todos los alumnos
     */
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

    /**
     * Obtiene un alumno por su matrícula.
     * @param matricula la matrícula del alumno buscado
     * @return el alumno encontrado, o {@code null} si no existe
     */
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

    /**
     * Busca un alumno por su correo institucional, sin distinguir mayúsculas/minúsculas.
     * @param correo el correo institucional a buscar
     * @return el alumno encontrado, o {@code null} si no existe
     */
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

    /**
     * Obtiene un alumno por su matrícula junto con los datos completos de su grupo asignado.
     * @param matricula la matrícula del alumno buscado
     * @return el alumno con su grupo cargado, o {@code null} si el alumno no existe
     */
    public Alumno getPerfilCompleto(String matricula) {
        Alumno alumno = getById(matricula);
        if (alumno == null) return null;

        if (alumno.getIdGrupo() != null) {
            alumno.setGrupo(grupoDao.getById(alumno.getIdGrupo()));
        }
        return alumno;
    }

    /**
     * Actualiza los datos de un alumno existente. Si el grupo cambia respecto al actual,
     * cierra el registro histórico vigente y abre uno nuevo con motivo "Cambio de grupo",
     * todo dentro de una transacción.
     * @param entidad el alumno con los datos actualizados (debe incluir su matrícula)
     * @return {@code true} si se actualizó al menos una fila del alumno; {@code false} en caso contrario
     */
    @Override
    public boolean update(Alumno entidad) {
        String sqlGrupoAnterior = "SELECT ID_GRUPO FROM ALUMNO WHERE MATRICULA = ?";
        String sqlCerrarHistorico = "UPDATE ALUMNO_GRUPO_HISTORICO SET FECHA_FIN = SYSDATE " +
                "WHERE MATRICULA = ? AND ID_GRUPO = ? AND FECHA_FIN IS NULL";
        String sqlAbrirHistorico = "INSERT INTO ALUMNO_GRUPO_HISTORICO (MATRICULA, ID_GRUPO, FECHA_INICIO, MOTIVO_CAMBIO) " +
                "VALUES (?, ?, SYSDATE, ?)";
        String sqlAlumno = "UPDATE ALUMNO SET NOMBRES = ?, APELLIDO_PATERNO = ?, APELLIDO_MATERNO = ?, " +
                "CORREO_INSTITUCIONAL = ?, TELEFONO = ?, ID_GENERO = ?, ID_GRUPO = ? WHERE MATRICULA = ?";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            Integer idGrupoAnterior = null;
            try (PreparedStatement ps = con.prepareStatement(sqlGrupoAnterior)) {
                ps.setString(1, entidad.getMatricula());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) idGrupoAnterior = rs.getInt(1);
                }
            }

            if (idGrupoAnterior != null && !idGrupoAnterior.equals(entidad.getIdGrupo())) {
                try (PreparedStatement ps = con.prepareStatement(sqlCerrarHistorico)) {
                    ps.setString(1, entidad.getMatricula());
                    ps.setInt(2, idGrupoAnterior);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlAbrirHistorico)) {
                    ps.setString(1, entidad.getMatricula());
                    ps.setInt(2, entidad.getIdGrupo());
                    ps.setString(3, "Cambio de grupo");
                    ps.executeUpdate();
                }
            }

            boolean actualizado;
            try (PreparedStatement ps = con.prepareStatement(sqlAlumno)) {
                ps.setString(1, entidad.getNombres());
                ps.setString(2, entidad.getApellidoPaterno());
                ps.setString(3, entidad.getApellidoMaterno());
                ps.setString(4, entidad.getCorreoInstitucional());
                ps.setString(5, entidad.getTelefono());
                ps.setInt(6, entidad.getIdGenero());
                ps.setInt(7, entidad.getIdGrupo());
                ps.setString(8, entidad.getMatricula());
                actualizado = ps.executeUpdate() > 0;
            }

            con.commit();
            return actualizado;
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

    /**
     * Obtiene la trayectoria histórica de grupos de un alumno (carrera, cuatrimestre, letra,
     * generación y fechas de inicio/fin), ordenada cronológicamente.
     * @param matricula la matrícula del alumno
     * @return la lista de registros de trayectoria del alumno
     */
    public List<TrayectoriaGrupoDTO> getTrayectoriaPorAlumno(String matricula) {
        List<TrayectoriaGrupoDTO> lista = new ArrayList<>();
        String sql = "SELECT c.NOMBRE AS NOMBRE_CARRERA, c.NIVEL, g.CUATRIMESTRE, g.LETRA, g.GENERACION, " +
                "h.FECHA_INICIO, h.FECHA_FIN, h.MOTIVO_CAMBIO " +
                "FROM ALUMNO_GRUPO_HISTORICO h " +
                "JOIN GRUPO g ON g.ID_GRUPO = h.ID_GRUPO " +
                "JOIN CARRERA c ON c.ID_CARRERA = g.ID_CARRERA " +
                "WHERE h.MATRICULA = ? ORDER BY h.FECHA_INICIO ASC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TrayectoriaGrupoDTO dto = new TrayectoriaGrupoDTO();
                    dto.setNombreCarrera(rs.getString("NOMBRE_CARRERA"));
                    dto.setNivel(rs.getString("NIVEL"));
                    dto.setCuatrimestre(rs.getInt("CUATRIMESTRE"));
                    dto.setLetra(rs.getString("LETRA"));
                    dto.setGeneracion(rs.getString("GENERACION"));
                    dto.setFechaInicio(rs.getDate("FECHA_INICIO"));
                    dto.setFechaFin(rs.getDate("FECHA_FIN"));
                    dto.setMotivoCambio(rs.getString("MOTIVO_CAMBIO"));
                    lista.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Da de baja lógica a un alumno, marcando su ESTADO como 'N'.
     * @param matricula la matrícula del alumno a dar de baja
     * @return {@code true} si se actualizó al menos una fila; {@code false} en caso contrario
     */
    @Override
    public boolean delete(String matricula) {

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

    /**
     * Reactiva a un alumno dado de baja, marcando su ESTADO como 'S'.
     * @param matricula la matrícula del alumno a reactivar
     * @return {@code true} si se actualizó al menos una fila; {@code false} en caso contrario
     */
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

    /**
     * Actualiza la contraseña de un alumno, almacenándola con hash.
     * @param matricula la matrícula del alumno
     * @param nuevaPassword la nueva contraseña en texto plano, que será hasheada antes de guardarse
     * @return {@code true} si se actualizó al menos una fila; {@code false} en caso contrario
     */
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

    /**
     * Obtiene todos los géneros del catálogo.
     * @return la lista de todos los géneros
     */
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

    /**
     * Obtiene todas las carreras del catálogo, delegando en {@link CarreraDao}.
     * @return la lista de todas las carreras
     */
    public List<Carrera> getAllCarreras() {
        return new CarreraDao().getAll();
    }

    /**
     * Calcula el siguiente número consecutivo disponible para generar matrículas con un prefijo dado,
     * revisando las matrículas existentes que comienzan con ese prefijo y tomando el mayor consecutivo + 1.
     * @param prefijo el prefijo de matrícula a buscar
     * @return el siguiente número consecutivo disponible (1 si no hay matrículas previas con ese prefijo)
     */
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

                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al calcular el siguiente contador de matricula: " + e.getMessage());
            e.printStackTrace();
        }

        return mayor + 1;
    }

    /**
     * Busca alumnos activos por coincidencia de matrícula o nombre completo, opcionalmente
     * restringido a los alumnos de los grupos asignados a un tutor específico. Limita el
     * resultado a 20 registros.
     * @param texto el texto a buscar en la matrícula o el nombre completo del alumno
     * @param idTutor el identificador del tutor para restringir la búsqueda a sus grupos, o {@code null} para no restringir
     * @return la lista de alumnos encontrados (vacía si el texto es nulo o está en blanco)
     */
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

    /**
     * Construye una entidad {@link Alumno} a partir de la fila actual de un {@link ResultSet}.
     * @param rs el conjunto de resultados posicionado en la fila a mapear
     * @return el alumno construido con los datos de la fila
     * @throws SQLException si ocurre un error al leer las columnas del resultado
     */
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

    /**
     * Obtiene la agenda de eventos de un alumno, combinando sus sesiones individuales con las
     * sesiones grupales de su grupo, incluyendo el estatus de asistencia de cada una (por defecto "Falta").
     * @param matricula la matrícula del alumno
     * @param idGrupo el identificador del grupo del alumno
     * @return la lista de eventos de agenda del alumno, ordenados por fecha ascendente
     */
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
                    Timestamp fecha = rs.getTimestamp("FECHA");
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
