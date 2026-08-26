package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.AsistenciaFilaDTO;
import mx.edu.utez.pigestiontutorias.models.CeldaAsistenciaDTO;
import mx.edu.utez.pigestiontutorias.models.SesionGrupal;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

/**
 * DAO responsable de construir y persistir la matriz de asistencia grupal (tabla ASISTENCIA)
 * a partir de las sesiones grupales completadas de un grupo, incluyendo el cálculo de
 * porcentajes de asistencia y la detección de alumnos en riesgo por baja asistencia.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-25
 */
public class AsistenciaGrupalDao {

    private static final double UMBRAL_RIESGO = 80.0;

    /**
     * Obtiene los alumnos activos pertenecientes a un grupo.
     * @param idGrupo el identificador del grupo
     * @return la lista de alumnos activos del grupo
     */
    public List<Alumno> getAlumnosPorGrupo(int idGrupo) {
        List<Alumno> lista = new ArrayList<>();
        String sql = "SELECT * FROM ALUMNO WHERE ID_GRUPO = ? AND ESTADO = 'S'";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idGrupo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Alumno alumno = new Alumno();
                    alumno.setMatricula(rs.getString("MATRICULA"));
                    alumno.setNombres(rs.getString("NOMBRES"));
                    alumno.setApellidoPaterno(rs.getString("APELLIDO_PATERNO"));
                    alumno.setApellidoMaterno(rs.getString("APELLIDO_MATERNO"));
                    alumno.setCorreoInstitucional(rs.getString("CORREO_INSTITUCIONAL"));
                    alumno.setTelefono(rs.getString("TELEFONO"));
                    alumno.setIdGenero(rs.getInt("ID_GENERO"));
                    alumno.setIdGrupo(rs.getInt("ID_GRUPO"));
                    alumno.setEstado(rs.getString("ESTADO"));
                    lista.add(alumno);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene las sesiones grupales completadas de un grupo dentro de un rango de fechas, ordenadas cronológicamente.
     * @param idGrupo el identificador del grupo
     * @param fechaInicio la fecha inicial del rango
     * @param fechaFin la fecha final del rango
     * @return la lista de sesiones grupales completadas dentro del rango
     */
    public List<SesionGrupal> getSesionesDelGrupoEnPeriodo(int idGrupo, Date fechaInicio, Date fechaFin) {
        List<SesionGrupal> lista = new ArrayList<>();
        String sql = "SELECT * FROM SESION_GRUPAL WHERE ID_GRUPO = ? AND ESTADO = 'Completado' " +
                "AND FECHA BETWEEN ? AND ? ORDER BY FECHA ASC";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idGrupo);
            ps.setDate(2, fechaInicio);
            ps.setDate(3, fechaFin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SesionGrupal s = new SesionGrupal();
                    s.setIdSesionGrupal(rs.getInt("ID_SESION_GRUPAL"));
                    s.setIdGrupo(rs.getInt("ID_GRUPO"));
                    s.setIdTutor(rs.getInt("ID_TUTOR"));
                    s.setFecha(rs.getDate("FECHA"));
                    s.setHora(rs.getString("HORA"));
                    s.setTemasTratados(rs.getString("TEMAS_TRATADOS"));
                    s.setAcuerdos(rs.getString("ACUERDOS"));
                    s.setAsesoriasGrupales(rs.getString("ASESORIAS_GRUPALES"));
                    s.setEstado(rs.getString("ESTADO"));
                    lista.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Construye la matriz de asistencia (una fila por alumno del grupo) con el estatus de cada
     * alumno en cada sesión indicada, calculando el porcentaje de asistencia (excluyendo faltas
     * justificadas del denominador) y marcando en riesgo a quienes queden por debajo del umbral.
     * @param idGrupo el identificador del grupo cuyos alumnos se incluirán en la matriz
     * @param sesiones las sesiones grupales completadas a considerar en el cálculo
     * @return la lista de filas de asistencia, una por cada alumno activo del grupo
     */
    public List<AsistenciaFilaDTO> construirFilasAsistencia(int idGrupo, List<SesionGrupal> sesiones) {
        List<AsistenciaFilaDTO> filas = new ArrayList<>();

        String sqlAlumnos = "SELECT * FROM ALUMNO WHERE ID_GRUPO = ? AND ESTADO = 'S' " +
                "ORDER BY APELLIDO_PATERNO, APELLIDO_MATERNO, NOMBRES";

        List<Alumno> alumnos = new ArrayList<>();
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlAlumnos)) {
            ps.setInt(1, idGrupo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Alumno a = new Alumno();
                    a.setMatricula(rs.getString("MATRICULA"));
                    a.setNombres(rs.getString("NOMBRES"));
                    a.setApellidoPaterno(rs.getString("APELLIDO_PATERNO"));
                    a.setApellidoMaterno(rs.getString("APELLIDO_MATERNO"));
                    alumnos.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return filas;
        }

        if (alumnos.isEmpty() || sesiones.isEmpty()) {
            for (Alumno a : alumnos) {
                filas.add(mapearFilaVacia(a));
            }
            return filas;
        }

        Map<String, Map<Integer, String>> matriz = new HashMap<>();

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < sesiones.size(); i++) {
            if (i > 0) inClause.append(",");
            inClause.append("?");
        }
        String sqlAsistencia = "SELECT MATRICULA, ID_SESION_GRUPAL, ESTATUS_ASISTENCIA FROM ASISTENCIA " +
                "WHERE ID_SESION_GRUPAL IN (" + inClause + ")";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlAsistencia)) {
            int idx = 1;
            for (SesionGrupal s : sesiones) {
                ps.setInt(idx++, s.getIdSesionGrupal());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String matricula = rs.getString("MATRICULA");
                    int idSesion = rs.getInt("ID_SESION_GRUPAL");
                    String estatus = rs.getString("ESTATUS_ASISTENCIA");
                    matriz.computeIfAbsent(matricula, k -> new HashMap<>()).put(idSesion, estatus);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Alumno alumno : alumnos) {
            Map<Integer, String> estatusAlumno = matriz.getOrDefault(alumno.getMatricula(), new HashMap<>());

            Map<Integer, String> estatusPorSesion = new LinkedHashMap<>();
            int presentes = 0;
            int justificadas = 0;

            for (SesionGrupal s : sesiones) {
                String estatus = estatusAlumno.getOrDefault(s.getIdSesionGrupal(), "Falta");
                estatusPorSesion.put(s.getIdSesionGrupal(), estatus);

                if ("Presente".equals(estatus)) {
                    presentes++;
                } else if ("Justificado".equals(estatus)) {
                    justificadas++;
                }
            }

            int totalSesiones = sesiones.size();
            int denominador = totalSesiones - justificadas;
            double porcentaje = denominador <= 0 ? 100.0 : (presentes * 100.0) / denominador;

            AsistenciaFilaDTO fila = new AsistenciaFilaDTO();
            fila.setMatricula(alumno.getMatricula());
            fila.setNombreCompleto(alumno.getApellidos() + " " + alumno.getNombres());
            fila.setEstatusPorSesion(estatusPorSesion);
            fila.setTotalSesiones(totalSesiones);
            fila.setTotalPresentes(presentes);
            fila.setTotalJustificadas(justificadas);
            fila.setPorcentaje(porcentaje);
            fila.setRiesgo(porcentaje < UMBRAL_RIESGO);
            filas.add(fila);
        }

        return filas;
    }

    /**
     * Construye una fila de asistencia vacía (sin sesiones registradas) para un alumno,
     * con porcentaje 100% y sin marca de riesgo.
     * @param alumno el alumno para el cual construir la fila vacía
     * @return la fila de asistencia inicializada sin datos de sesiones
     */
    private AsistenciaFilaDTO mapearFilaVacia(Alumno alumno) {
        AsistenciaFilaDTO fila = new AsistenciaFilaDTO();
        fila.setMatricula(alumno.getMatricula());
        fila.setNombreCompleto(alumno.getApellidos() + " " + alumno.getNombres());
        fila.setEstatusPorSesion(new LinkedHashMap<>());
        fila.setTotalSesiones(0);
        fila.setTotalPresentes(0);
        fila.setTotalJustificadas(0);
        fila.setPorcentaje(100.0);
        fila.setRiesgo(false);
        return fila;
    }

    /**
     * Persiste en bloque las celdas de asistencia capturadas, agrupándolas por sesión grupal.
     * Para sesiones de fechas pasadas, preserva el estatus "Presente" ya registrado y convierte
     * cualquier otro estatus no capturado previamente a "Justificado"; para sesiones del día actual,
     * degrada cualquier celda marcada como "Justificado" a "Falta". Reemplaza los registros existentes
     * de cada sesión (borra e inserta) dentro de una transacción.
     * @param celdas las celdas de asistencia a guardar, con matrícula, sesión y estatus
     * @return {@code true} si la operación se completó con éxito (o si la lista está vacía/nula); {@code false} si ocurre un error
     */
    public boolean guardarCeldas(List<CeldaAsistenciaDTO> celdas) {
        if (celdas == null || celdas.isEmpty()) {
            return true;
        }

        Map<Integer, List<CeldaAsistenciaDTO>> porSesion = new LinkedHashMap<>();
        for (CeldaAsistenciaDTO celda : celdas) {
            porSesion.computeIfAbsent(celda.getIdSesionGrupal(), k -> new ArrayList<>()).add(celda);
        }

        Map<Integer, LocalDate> fechasPorSesion = obtenerFechasDeSesiones(porSesion.keySet());
        LocalDate hoy = LocalDate.now();
        for (Map.Entry<Integer, List<CeldaAsistenciaDTO>> entrySesion : porSesion.entrySet()) {
            int idSesionGrupal = entrySesion.getKey();
            LocalDate fechaSesion = fechasPorSesion.get(idSesionGrupal);
            if (fechaSesion == null) {
                continue;
            }

            if (fechaSesion.isBefore(hoy)) {
                Map<String, String> estatusPrevio = obtenerEstatusExistente(idSesionGrupal);
                for (CeldaAsistenciaDTO celda : entrySesion.getValue()) {
                    String previo = estatusPrevio.get(celda.getMatricula());
                    if (previo == null) {

                        continue;
                    }
                    if ("Presente".equals(previo)) {
                        celda.setEstatus("Presente");
                    } else if (!"Justificado".equals(celda.getEstatus())) {
                        celda.setEstatus("Justificado");
                    }
                }
            } else if (fechaSesion.isEqual(hoy)) {
                for (CeldaAsistenciaDTO celda : entrySesion.getValue()) {
                    if ("Justificado".equals(celda.getEstatus())) {
                        celda.setEstatus("Falta");
                    }
                }
            }
        }

        String deleteSql = "DELETE FROM ASISTENCIA WHERE ID_SESION_GRUPAL = ?";
        String insertSql = "INSERT INTO ASISTENCIA (ID_SESION_GRUPAL, MATRICULA, ESTATUS_ASISTENCIA) VALUES (?, ?, ?)";

        try (Connection con = SQLConnector.getConnection()) {
            con.setAutoCommit(false);

            for (Map.Entry<Integer, List<CeldaAsistenciaDTO>> entry : porSesion.entrySet()) {
                int idSesionGrupal = entry.getKey();

                try (PreparedStatement psDel = con.prepareStatement(deleteSql)) {
                    psDel.setInt(1, idSesionGrupal);
                    psDel.executeUpdate();
                }

                try (PreparedStatement psIns = con.prepareStatement(insertSql)) {
                    for (CeldaAsistenciaDTO celda : entry.getValue()) {
                        psIns.setInt(1, idSesionGrupal);
                        psIns.setString(2, celda.getMatricula());
                        psIns.setString(3, celda.getEstatus());
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene la fecha de cada sesión grupal indicada.
     * @param idsSesion el conjunto de identificadores de sesión grupal a consultar
     * @return un mapa de identificador de sesión a su fecha, vacío si el conjunto es nulo o está vacío
     */
    private Map<Integer, LocalDate> obtenerFechasDeSesiones(java.util.Set<Integer> idsSesion) {
        Map<Integer, LocalDate> fechas = new HashMap<>();
        if (idsSesion == null || idsSesion.isEmpty()) {
            return fechas;
        }

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < idsSesion.size(); i++) {
            if (i > 0) inClause.append(",");
            inClause.append("?");
        }
        String sql = "SELECT ID_SESION_GRUPAL, FECHA FROM SESION_GRUPAL WHERE ID_SESION_GRUPAL IN (" + inClause + ")";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            for (Integer idSesion : idsSesion) {
                ps.setInt(idx++, idSesion);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    fechas.put(rs.getInt("ID_SESION_GRUPAL"), rs.getDate("FECHA").toLocalDate());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fechas;
    }

    /**
     * Obtiene el estatus de asistencia ya registrado por alumno para una sesión grupal específica.
     * @param idSesionGrupal el identificador de la sesión grupal
     * @return un mapa de matrícula a estatus de asistencia registrado
     */
    private Map<String, String> obtenerEstatusExistente(int idSesionGrupal) {
        Map<String, String> estatus = new HashMap<>();
        String sql = "SELECT MATRICULA, ESTATUS_ASISTENCIA FROM ASISTENCIA WHERE ID_SESION_GRUPAL = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSesionGrupal);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    estatus.put(rs.getString("MATRICULA"), rs.getString("ESTATUS_ASISTENCIA"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return estatus;
    }
}
