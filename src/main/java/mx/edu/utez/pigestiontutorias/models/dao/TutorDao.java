package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Academia;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.utils.PasswordUtil;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO de acceso a datos para los tutores (tabla TUTOR) y sus horarios de atención
 * asociados, con operaciones CRUD, alta individual y masiva, validaciones de
 * duplicados, baja lógica/reactivación y consultas auxiliares para el login y
 * la gestión de tutores.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-22
 */
public class TutorDao implements Dao<Tutor, Integer> {

    /**
     * Inserta un nuevo tutor junto con sus horarios de atención, de forma transaccional.
     * Si no se indica contraseña, genera una por defecto a partir del número de empleado.
     * @param entidad el tutor a crear, incluyendo sus horarios disponibles en texto
     * @return {@code true} si la operación se completó correctamente; {@code false} en caso contrario
     */
    @Override
    public boolean create(Tutor entidad) {
        String sqlTutor = "INSERT INTO TUTOR(NUMERO_EMPLEADO, NOMBRES, APELLIDO_PATERNO, APELLIDO_MATERNO, CORREO_INSTITUCIONAL, TELEFONO, ID_ACADEMIA, PASS) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            String pass = (entidad.getPass() != null && !entidad.getPass().isBlank())
                    ? entidad.getPass() : "Tut@" + entidad.getNumeroEmpleado();

            try (PreparedStatement psTutor = con.prepareStatement(sqlTutor)) {
                psTutor.setInt(1, entidad.getNumeroEmpleado());
                psTutor.setString(2, entidad.getNombres());
                psTutor.setString(3, entidad.getApellidoPaterno());
                psTutor.setString(4, entidad.getApellidoMaterno());
                psTutor.setString(5, entidad.getCorreoInstitucional());
                psTutor.setString(6, entidad.getTelefono());
                psTutor.setInt(7, entidad.getIdAcademia());
                psTutor.setString(8, PasswordUtil.hash(pass));
                psTutor.executeUpdate();
            }

            insertarHorarios(con, entidad.getNumeroEmpleado(), entidad.getHorariosDispo());

            con.commit();
            return true;

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
     * Inserta en lote una lista de tutores junto con sus respectivos horarios de atención,
     * de forma transaccional (todo o nada), generando la contraseña por defecto para cada uno.
     * @param tutores la lista de tutores a crear
     * @return el número de tutores insertados correctamente; 0 si la lista es nula/vacía o si ocurre un error
     */
    public int crearMasivo(List<Tutor> tutores) {
        if (tutores == null || tutores.isEmpty()) return 0;

        String sqlTutor = "INSERT INTO TUTOR(NUMERO_EMPLEADO, NOMBRES, APELLIDO_PATERNO, APELLIDO_MATERNO, CORREO_INSTITUCIONAL, TELEFONO, ID_ACADEMIA, PASS) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int insertados = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlTutor)) {
                for (Tutor t : tutores) {
                    ps.setInt(1, t.getNumeroEmpleado());
                    ps.setString(2, t.getNombres());
                    ps.setString(3, t.getApellidoPaterno());
                    ps.setString(4, t.getApellidoMaterno());
                    ps.setString(5, t.getCorreoInstitucional());
                    ps.setString(6, t.getTelefono());
                    ps.setInt(7, t.getIdAcademia());
                    ps.setString(8, PasswordUtil.hash("Tut@" + t.getNumeroEmpleado()));
                    ps.addBatch();
                }

                int[] resultados = ps.executeBatch();
                for (int resultado : resultados) {
                    if (resultado > 0 || resultado == Statement.SUCCESS_NO_INFO) insertados++;
                }
            }

            for (Tutor t : tutores) {
                insertarHorarios(con, t.getNumeroEmpleado(), t.getHorariosDispo());
            }

            con.commit();
            return insertados;
        } catch (SQLException e) {
            System.err.println("Error en la carga masiva de tutores: " + e.getMessage());
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
     * Inserta en lote los horarios de atención de un tutor, extrayendo día de la semana y
     * horas de inicio/fin desde cada cadena de texto mediante expresiones regulares.
     * @param con la conexión abierta a la base de datos, ya en la transacción activa
     * @param numeroEmpleado el número de empleado del tutor dueño de los horarios
     * @param horarios la lista de horarios en formato de texto libre (p. ej. "Lunes 08:00 - 09:00"); si es nula o vacía no hace nada
     * @throws SQLException si ocurre un error al insertar los horarios
     */
    private void insertarHorarios(Connection con, int numeroEmpleado, List<String> horarios) throws SQLException {
        if (horarios == null || horarios.isEmpty()) return;

        String sqlHorario = "INSERT INTO HORARIO_ATENCION (ID_TUTOR, DIA_SEMANA, HORA_DESDE, HORA_HASTA) VALUES (?, ?, TO_DSINTERVAL('0 ' || ? || ':00'), TO_DSINTERVAL('0 ' || ? || ':00'))";

        try (PreparedStatement psHorario = con.prepareStatement(sqlHorario)) {
            for (String horarioStr : horarios) {
                String dia = "Lunes";
                String desde = "00:00";
                String hasta = "00:00";

                java.util.regex.Matcher mDia = java.util.regex.Pattern.compile("(Lunes|Martes|Mi[eé]rcoles|Jueves|Viernes)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(horarioStr);
                if (mDia.find()) dia = normalizarDiaSemana(mDia.group(1));

                java.util.regex.Matcher mHoras = java.util.regex.Pattern.compile("([0-2][0-9]:[0-5][0-9])").matcher(horarioStr);
                if (mHoras.find()) desde = mHoras.group(1);
                if (mHoras.find()) hasta = mHoras.group(1);

                psHorario.setInt(1, numeroEmpleado);
                psHorario.setString(2, dia);
                psHorario.setString(3, desde);
                psHorario.setString(4, hasta);
                psHorario.addBatch();
            }
            psHorario.executeBatch();
        }
    }

    /**
     * Obtiene el siguiente número de nómina (número de empleado) disponible para un nuevo tutor,
     * con un piso mínimo de 1000.
     * @return el siguiente número de nómina disponible
     */
    public int obtenerSiguienteNomina() {
        String sql = "SELECT NVL(MAX(NUMERO_EMPLEADO), 999) + 1 AS SIGUIENTE FROM TUTOR";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Math.max(rs.getInt("SIGUIENTE"), 1000);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1000;
    }

    /**
     * Normaliza el nombre de un día de la semana a su forma canónica sin acentos usada en base de datos.
     * @param dia el nombre del día a normalizar
     * @return el día normalizado, o el mismo valor recibido si no coincide con ninguno conocido o es {@code null}
     */
    private String normalizarDiaSemana(String dia) {
        if (dia == null) return dia;
        return switch (dia.toLowerCase()) {
            case "miércoles", "miercoles" -> "Miercoles";
            case "lunes" -> "Lunes";
            case "martes" -> "Martes";
            case "jueves" -> "Jueves";
            case "viernes" -> "Viernes";
            default -> dia;
        };
    }

    /**
     * Verifica si ya existe un tutor con el número de empleado indicado.
     * @param numeroEmpleado el número de empleado a validar
     * @return {@code true} si ya existe un tutor con ese número de empleado; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean existeNomina(int numeroEmpleado) {
        String sql = "SELECT COUNT(*) FROM TUTOR WHERE NUMERO_EMPLEADO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, numeroEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Verifica si ya existe un tutor con el correo institucional indicado.
     * @param correo el correo institucional a validar
     * @return {@code true} si ya existe un tutor con ese correo; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean existeCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM TUTOR WHERE CORREO_INSTITUCIONAL = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Verifica si ya existe un tutor con el teléfono indicado.
     * @param telefono el teléfono a validar
     * @return {@code true} si ya existe un tutor con ese teléfono; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean existeTelefono(String telefono) {
        String sql = "SELECT COUNT(*) FROM TUTOR WHERE TELEFONO = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, telefono);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Verifica si ya existe otro tutor (distinto al indicado) con el mismo correo institucional.
     * @param correo el correo institucional a validar
     * @param numeroEmpleadoExcluido el número de empleado del tutor que debe excluirse de la comparación
     * @return {@code true} si existe otro tutor con ese correo; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean existeCorreo(String correo, int numeroEmpleadoExcluido) {
        String sql = "SELECT COUNT(*) FROM TUTOR WHERE CORREO_INSTITUCIONAL = ? AND NUMERO_EMPLEADO <> ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setInt(2, numeroEmpleadoExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Verifica si ya existe otro tutor (distinto al indicado) con el mismo teléfono.
     * @param telefono el teléfono a validar
     * @param numeroEmpleadoExcluido el número de empleado del tutor que debe excluirse de la comparación
     * @return {@code true} si existe otro tutor con ese teléfono; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean existeTelefono(String telefono, int numeroEmpleadoExcluido) {
        String sql = "SELECT COUNT(*) FROM TUTOR WHERE TELEFONO = ? AND NUMERO_EMPLEADO <> ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, telefono);
            ps.setInt(2, numeroEmpleadoExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Obtiene todos los tutores registrados en el sistema, sin filtrar por estado.
     * @return la lista completa de tutores; vacía si no hay registros o si ocurre un error de base de datos
     */
    @Override
    public List<Tutor> getAll() {
        List<Tutor> lista = new ArrayList<>();

        String sql = "SELECT * FROM TUTOR";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearTutor(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error en getAll: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene todos los tutores con la lista de grupos activos que tienen asignados actualmente,
     * agrupando las filas del join en un solo objeto {@link Tutor} por número de empleado.
     * @return la lista de tutores con sus grupos asignados; vacía si no hay registros o si ocurre un error de base de datos
     */
    public List<Tutor> getAllConGrupo() {
        Map<Integer, Tutor> tutoresPorNomina = new LinkedHashMap<>();
        String sql = "SELECT t.*, car.NOMBRE AS NOMBRE_CARRERA, g.CUATRIMESTRE, g.LETRA, g.GENERACION " +
                "FROM TUTOR t " +
                "LEFT JOIN ASIGNACION_TUTOR a ON a.ID_TUTOR = t.NUMERO_EMPLEADO AND a.ESTADO = 'S' " +
                "LEFT JOIN GRUPO g ON g.ID_GRUPO = a.ID_GRUPO AND g.ESTADO = 'S' " +
                "LEFT JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA " +
                "ORDER BY t.NOMBRES, t.NUMERO_EMPLEADO";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int numeroEmpleado = rs.getInt("NUMERO_EMPLEADO");
                Tutor tutor = tutoresPorNomina.get(numeroEmpleado);
                if (tutor == null) {
                    tutor = mapearTutor(rs);
                    tutor.setGruposAsignados(new ArrayList<>());
                    tutoresPorNomina.put(numeroEmpleado, tutor);
                }

                String nombreCarrera = rs.getString("NOMBRE_CARRERA");
                if (nombreCarrera != null) {

                    String generacion = rs.getString("GENERACION");
                    String etiquetaGeneracion = (generacion != null && !generacion.isBlank()) ? generacion : "Sin generación";
                    tutor.getGruposAsignados().add(nombreCarrera + " - " + rs.getInt("CUATRIMESTRE") + "° "
                            + rs.getString("LETRA") + " (Gen " + etiquetaGeneracion + ")");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en getAllConGrupo: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>(tutoresPorNomina.values());
    }

    /**
     * Busca un tutor por su número de empleado, incluyendo sus horarios de atención formateados
     * y los datos completos de su academia.
     * @param numeroEmpleado el número de empleado del tutor
     * @return el tutor encontrado con horarios y academia cargados, o {@code null} si no existe o si ocurre un error de base de datos
     */
    @Override
    public Tutor getById(Integer numeroEmpleado) {
        String sql = "SELECT * FROM TUTOR WHERE NUMERO_EMPLEADO = ?";

        String sqlHorarios = "SELECT DIA_SEMANA, " +
                "TO_CHAR(EXTRACT(HOUR FROM HORA_DESDE), 'FM00') || ':' || TO_CHAR(EXTRACT(MINUTE FROM HORA_DESDE), 'FM00') AS DESDE, " +
                "TO_CHAR(EXTRACT(HOUR FROM HORA_HASTA), 'FM00') || ':' || TO_CHAR(EXTRACT(MINUTE FROM HORA_HASTA), 'FM00') AS HASTA " +
                "FROM HORARIO_ATENCION WHERE ID_TUTOR = ?";
        Tutor tutor = null;

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, numeroEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tutor = mapearTutor(rs);
                }
            }

            if (tutor != null) {
                try (PreparedStatement psH = con.prepareStatement(sqlHorarios)) {
                    psH.setInt(1, tutor.getNumeroEmpleado());
                    try (ResultSet rsH = psH.executeQuery()) {
                        List<String> horarios = new ArrayList<>();
                        while (rsH.next()) {
                            String dia = rsH.getString("DIA_SEMANA");
                            String desde = rsH.getString("DESDE");
                            String hasta = rsH.getString("HASTA");

                            horarios.add(dia + " " + desde + " - " + hasta);
                        }
                        tutor.setHorariosDispo(horarios);
                    }
                }

                for (Academia a : getAllAcademias()) {
                    if (a.getIdAcademia() == tutor.getIdAcademia()) {
                        tutor.setAcademia(a);
                        break;
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tutor;
    }

    /**
     * Busca un tutor por su número de nómina (equivalente a su número de empleado).
     * @param numeroEmpleado el número de empleado/nómina del tutor
     * @return el tutor encontrado, o {@code null} si no existe
     */
    public Tutor getByNomina(int numeroEmpleado) {
        return getById(numeroEmpleado);
    }

    /**
     * Busca un tutor por su correo institucional, sin distinguir mayúsculas/minúsculas.
     * @param correo el correo institucional a buscar
     * @return el tutor encontrado, o {@code null} si no existe o si ocurre un error de base de datos
     */
    public Tutor findByCorreo(String correo) {
        String sql = "SELECT * FROM TUTOR WHERE UPPER(CORREO_INSTITUCIONAL) = UPPER(?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearTutor(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Actualiza los datos generales de un tutor y reemplaza por completo sus horarios de
     * atención, de forma transaccional (elimina los horarios previos e inserta los nuevos).
     * @param entidad el tutor con el número de empleado y los nuevos datos y horarios a aplicar
     * @return {@code true} si la operación se completó correctamente; {@code false} en caso contrario
     */
    @Override
    public boolean update(Tutor entidad) {
        String sqlTutor = "UPDATE TUTOR SET NOMBRES = ?, APELLIDO_PATERNO = ?, APELLIDO_MATERNO = ?, CORREO_INSTITUCIONAL = ?, TELEFONO = ?, ID_ACADEMIA = ? WHERE NUMERO_EMPLEADO = ?";
        String sqlDeleteHorarios = "DELETE FROM HORARIO_ATENCION WHERE ID_TUTOR = ?";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement psTutor = con.prepareStatement(sqlTutor)) {
                psTutor.setString(1, entidad.getNombres());
                psTutor.setString(2, entidad.getApellidoPaterno());
                psTutor.setString(3, entidad.getApellidoMaterno());
                psTutor.setString(4, entidad.getCorreoInstitucional());
                psTutor.setString(5, entidad.getTelefono());
                psTutor.setInt(6, entidad.getIdAcademia());
                psTutor.setInt(7, entidad.getNumeroEmpleado());
                psTutor.executeUpdate();
            }

            try (PreparedStatement psDel = con.prepareStatement(sqlDeleteHorarios)) {
                psDel.setInt(1, entidad.getNumeroEmpleado());
                psDel.executeUpdate();
            }

            insertarHorarios(con, entidad.getNumeroEmpleado(), entidad.getHorariosDispo());

            con.commit();
            return true;
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
     * Da de baja lógica a un tutor, marcándolo como inactivo (ESTADO = 'N').
     * @param numeroEmpleado el número de empleado del tutor a desactivar
     * @return {@code true} si la actualización afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    @Override
    public boolean delete(Integer numeroEmpleado) {

        String sql = "UPDATE TUTOR SET ESTADO = 'N' WHERE NUMERO_EMPLEADO = ?";
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
     * Verifica si un tutor tiene registros pendientes que impidan su eliminación: asignaciones
     * activas, solicitudes pendientes, o sesiones individuales o grupales pendientes.
     * @param numeroEmpleado el número de empleado del tutor
     * @return {@code true} si tiene al menos un registro pendiente asociado; {@code false} en caso contrario
     */
    public boolean tienePendientes(int numeroEmpleado) {
        return existeRegistro("SELECT COUNT(1) FROM ASIGNACION_TUTOR WHERE ID_TUTOR = ? AND ESTADO = 'S' AND ROWNUM = 1", numeroEmpleado)
                || existeRegistro("SELECT COUNT(1) FROM SOLICITUD_TUTORIA WHERE ID_TUTOR = ? AND ESTATUS = 'Pendiente' AND ROWNUM = 1", numeroEmpleado)
                || existeRegistro("SELECT COUNT(1) FROM SESION_INDIVIDUAL WHERE ID_TUTOR = ? AND ESTADO = 'Pendiente' AND ROWNUM = 1", numeroEmpleado)
                || existeRegistro("SELECT COUNT(1) FROM SESION_GRUPAL WHERE ID_TUTOR = ? AND ESTADO = 'Pendiente' AND ROWNUM = 1", numeroEmpleado);
    }

    /**
     * Ejecuta una consulta de conteo parametrizada con el identificador del tutor y determina
     * si existe al menos un registro. Ante un error de base de datos, retorna {@code true} como
     * medida preventiva para evitar eliminar un tutor cuyo estado real no pudo verificarse.
     * @param sql la sentencia SQL de conteo a ejecutar, con un único parámetro posicional para el id del tutor
     * @param idTutor el identificador (número de empleado) del tutor
     * @return {@code true} si existe al menos un registro o si ocurre un error al consultar; {@code false} en caso contrario
     */
    private boolean existeRegistro(String sql, int idTutor) {
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTutor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al validar pendientes del tutor: " + e.getMessage());
            e.printStackTrace();

            return true;
        }
    }

    /**
     * Reactiva un tutor previamente dado de baja, marcándolo como activo (ESTADO = 'S').
     * @param numeroEmpleado el número de empleado del tutor a reactivar
     * @return {@code true} si la actualización afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean reactivar(int numeroEmpleado) {
        String sql = "UPDATE TUTOR SET ESTADO = 'S' WHERE NUMERO_EMPLEADO = ?";
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
     * Actualiza la contraseña de un tutor, almacenando su valor ya cifrado (hash).
     * @param numeroEmpleado el número de empleado del tutor
     * @param nuevaPassword la nueva contraseña en texto plano, que se cifra antes de guardarse
     * @return {@code true} si la actualización afectó al menos una fila; {@code false} en caso contrario o si ocurre un error de base de datos
     */
    public boolean actualizarPassword(int numeroEmpleado, String nuevaPassword) {
        String sql = "UPDATE TUTOR SET PASS = ? WHERE NUMERO_EMPLEADO = ?";
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
     * Obtiene el catálogo completo de academias, delegando en {@link AcademiaDao}.
     * @return la lista de academias registradas
     */
    public List<Academia> getAllAcademias() {
        return new AcademiaDao().getAll();
    }

    /**
     * Construye un objeto {@link Tutor} a partir de la fila actual de un ResultSet.
     * @param rs el ResultSet posicionado en la fila a mapear
     * @return el tutor mapeado con sus campos principales
     * @throws SQLException si ocurre un error al leer las columnas del ResultSet
     */
    private Tutor mapearTutor(ResultSet rs) throws SQLException {
        Tutor tutor = new Tutor();
        tutor.setNumeroEmpleado(rs.getInt("NUMERO_EMPLEADO"));
        tutor.setNombres(rs.getString("NOMBRES"));
        tutor.setApellidoPaterno(rs.getString("APELLIDO_PATERNO"));
        tutor.setApellidoMaterno(rs.getString("APELLIDO_MATERNO"));
        tutor.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
        tutor.setTelefono(rs.getString("TELEFONO"));
        tutor.setIdAcademia(rs.getInt("ID_ACADEMIA"));
        tutor.setPass(rs.getString("PASS"));
        tutor.setEstado(rs.getString("ESTADO"));
        return tutor;
    }

    /**
     * Obtiene los tutores activos con datos básicos (identificador, nombre y academia), usado
     * para listados ligeros como selects de formularios.
     * @return la lista de tutores activos; vacía si no hay o si ocurre un error de base de datos
     */
    public List<Tutor> findAll() {
        List<Tutor> lista = new ArrayList<>();
        String sql = "SELECT NUMERO_EMPLEADO, NOMBRES, APELLIDO_PATERNO, APELLIDO_MATERNO, ID_ACADEMIA FROM TUTOR WHERE ESTADO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Tutor t = new Tutor();
                t.setNumeroEmpleado(rs.getInt("NUMERO_EMPLEADO"));
                t.setNombres(rs.getString("NOMBRES"));
                t.setApellidoPaterno(rs.getString("APELLIDO_PATERNO"));
                t.setApellidoMaterno(rs.getString("APELLIDO_MATERNO"));
                t.setIdAcademia(rs.getInt("ID_ACADEMIA"));
                lista.add(t);
            }

        } catch (Exception e) {
            System.err.println("Error al obtener los tutores: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }
}
