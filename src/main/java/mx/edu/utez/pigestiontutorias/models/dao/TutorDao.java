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

public class TutorDao implements Dao<Tutor, Integer> {

    @Override
    public boolean create(Tutor entidad) {
        String sqlTutor = "INSERT INTO TUTOR(NUMERO_EMPLEADO, NOMBRES, APELLIDO_PATERNO, APELLIDO_MATERNO, CORREO_INSTITUCIONAL, TELEFONO, ID_ACADEMIA, PASS) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlHorario = "INSERT INTO HORARIO_ATENCION (ID_TUTOR, DIA_SEMANA, HORA_DESDE, HORA_HASTA) VALUES (?, ?, TO_DSINTERVAL('0 ' || ? || ':00'), TO_DSINTERVAL('0 ' || ? || ':00'))";

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

            if (entidad.getHorariosDispo() != null && !entidad.getHorariosDispo().isEmpty()) {
                try (PreparedStatement psHorario = con.prepareStatement(sqlHorario)) {
                    for (String horarioStr : entidad.getHorariosDispo()) {

                        // Extractor inteligente: Busca el día y las horas en el texto que manda el JS
                        String dia = "Lunes";
                        String desde = "00:00";
                        String hasta = "00:00";

                        java.util.regex.Matcher mDia = java.util.regex.Pattern.compile("(Lunes|Martes|Mi[eé]rcoles|Jueves|Viernes)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(horarioStr);
                        if (mDia.find()) dia = normalizarDiaSemana(mDia.group(1));

                        java.util.regex.Matcher mHoras = java.util.regex.Pattern.compile("([0-2][0-9]:[0-5][0-9])").matcher(horarioStr);
                        if (mHoras.find()) desde = mHoras.group(1);
                        if (mHoras.find()) hasta = mHoras.group(1);

                        psHorario.setInt(1, entidad.getNumeroEmpleado());
                        psHorario.setString(2, dia);
                        psHorario.setString(3, desde);
                        psHorario.setString(4, hasta);
                        psHorario.addBatch();
                    }
                    psHorario.executeBatch();
                }
            }

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

    // La nomina (NUMERO_EMPLEADO) ya no la captura el coordinador a mano: se asigna
    // automaticamente a partir de 1000 (primer tutor = 1000, luego MAX(NUMERO_EMPLEADO) + 1).
    // La usan tanto el alta individual (formulario-tutor.jsp) como la carga masiva por Excel.
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

    // Alta masiva (carga de Excel via TutoresServlet + Apache POI): un solo batch/commit
    // para todos los tutores validados por el servlet, en vez de una transaccion por fila.
    // No inserta horarios de atencion: el Excel no los captura, el tutor puede agregarlos
    // despues editando su registro.
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
                    String pass = (t.getPass() != null && !t.getPass().isBlank())
                            ? t.getPass() : "Tut@" + t.getNumeroEmpleado();

                    ps.setInt(1, t.getNumeroEmpleado());
                    ps.setString(2, t.getNombres());
                    ps.setString(3, t.getApellidoPaterno());
                    ps.setString(4, t.getApellidoMaterno());
                    ps.setString(5, t.getCorreoInstitucional());
                    ps.setString(6, t.getTelefono());
                    ps.setInt(7, t.getIdAcademia());
                    ps.setString(8, PasswordUtil.hash(pass));
                    ps.addBatch();
                }

                int[] resultados = ps.executeBatch();
                for (int resultado : resultados) {
                    if (resultado > 0 || resultado == Statement.SUCCESS_NO_INFO) insertados++;
                }
            }

            con.commit();
            return insertados;
        } catch (SQLException e) {
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

    // CK_HORARIO_DIA (BD_INTEGRADORA_SGT.sql) solo acepta los dias SIN acento
    // ('Lunes','Martes','Miercoles','Jueves','Viernes'), pero el <select> del
    // formulario manda "Miércoles" con acento; se normaliza antes de guardar
    // para no violar el constraint.
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

    @Override
    public List<Tutor> getAll() {
        List<Tutor> lista = new ArrayList<>();
        // Trae activos e inactivos: la pantalla de gestion decide que mostrar
        // segun el filtro "mostrar dados de baja".
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

    // Igual que getAll(), pero resuelve ademas el/los grupos que cada tutor tiene
    // asignados actualmente via JOIN con ASIGNACION_TUTOR + GRUPO + CARRERA (LEFT JOIN
    // para no perder tutores sin ningun grupo asignado). La usa gestion-tutores.jsp para
    // pintar la columna "Grupo" sin una consulta aparte por cada fila de la tabla.
    public List<Tutor> getAllConGrupo() {
        Map<Integer, Tutor> tutoresPorNomina = new LinkedHashMap<>();
        String sql = "SELECT t.*, car.NOMBRE AS NOMBRE_CARRERA, g.CUATRIMESTRE, g.LETRA " +
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
                    tutor.getGruposAsignados().add(nombreCarrera + " " + rs.getInt("CUATRIMESTRE") + "°" + rs.getString("LETRA"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en getAllConGrupo: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>(tutoresPorNomina.values());
    }

    // El PK de TUTOR ahora es NUMERO_EMPLEADO directamente (ya no hay ID_TUTOR sintetico
    // ni USUARIO de por medio), asi que getById() sirve tanto para el login/sesion como
    // para el CRUD de gestion-tutores.
    @Override
    public Tutor getById(Integer numeroEmpleado) {
        String sql = "SELECT * FROM TUTOR WHERE NUMERO_EMPLEADO = ?";

        // Extraemos el intervalo de Oracle y lo formateamos de vuelta a texto "HH:mm"
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

                            // Juntamos los datos para que el HTML los pinte como un solo texto
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

    public Tutor getByNomina(int numeroEmpleado) {
        return getById(numeroEmpleado);
    }

    // Login: busca por correo institucional (ver LoginServlet). Incluye PASS para que
    // el servlet valide la contraseña; ESTADO se revisa aparte.
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

    @Override
    public boolean update(Tutor entidad) {
        String sqlTutor = "UPDATE TUTOR SET NOMBRES = ?, APELLIDO_PATERNO = ?, APELLIDO_MATERNO = ?, CORREO_INSTITUCIONAL = ?, TELEFONO = ?, ID_ACADEMIA = ? WHERE NUMERO_EMPLEADO = ?";
        String sqlDeleteHorarios = "DELETE FROM HORARIO_ATENCION WHERE ID_TUTOR = ?";
        String sqlInsertHorario = "INSERT INTO HORARIO_ATENCION (ID_TUTOR, DIA_SEMANA, HORA_DESDE, HORA_HASTA) VALUES (?, ?, TO_DSINTERVAL('0 ' || ? || ':00'), TO_DSINTERVAL('0 ' || ? || ':00'))";

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

            if (entidad.getHorariosDispo() != null && !entidad.getHorariosDispo().isEmpty()) {
                try (PreparedStatement psIns = con.prepareStatement(sqlInsertHorario)) {
                    for (String horarioStr : entidad.getHorariosDispo()) {

                        // Mismo extractor inteligente que usamos al crear
                        String dia = "Lunes";
                        String desde = "00:00";
                        String hasta = "00:00";

                        java.util.regex.Matcher mDia = java.util.regex.Pattern.compile("(Lunes|Martes|Mi[eé]rcoles|Jueves|Viernes)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(horarioStr);
                        if (mDia.find()) dia = normalizarDiaSemana(mDia.group(1));

                        java.util.regex.Matcher mHoras = java.util.regex.Pattern.compile("([0-2][0-9]:[0-5][0-9])").matcher(horarioStr);
                        if (mHoras.find()) desde = mHoras.group(1);
                        if (mHoras.find()) hasta = mHoras.group(1);

                        psIns.setInt(1, entidad.getNumeroEmpleado());
                        psIns.setString(2, dia);
                        psIns.setString(3, desde);
                        psIns.setString(4, hasta);
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }

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

    @Override
    public boolean delete(Integer numeroEmpleado) {
        // Baja logica: preserva horarios, asignaciones y sesiones vinculadas y bloquea el acceso del tutor.
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

    // Reactiva a un tutor dado de baja y restaura su acceso al sistema.
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

    public List<Academia> getAllAcademias() {
        return new AcademiaDao().getAll();
    }

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
