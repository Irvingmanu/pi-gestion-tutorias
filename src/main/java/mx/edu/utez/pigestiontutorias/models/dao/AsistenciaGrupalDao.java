package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.AsistenciaFilaDTO;
import mx.edu.utez.pigestiontutorias.models.CeldaAsistenciaDTO;
import mx.edu.utez.pigestiontutorias.models.SesionGrupal;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// ALUMNO ya no tiene ID_ALUMNO surrogate ni ID_CARRERA/ID_CUATRIMESTRE/ID_LETRA_GRUPO
// propios: todo se resuelve via ID_GRUPO, y MATRICULA es la unica PK/identificador.
public class AsistenciaGrupalDao {

    private static final double UMBRAL_RIESGO = 80.0;

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

    // Columnas de la cuadricula: cada SESION_GRUPAL "Completado" del grupo dentro del
    // rango del periodo escolar al que pertenece ese grupo, ordenadas por fecha.
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

    // Filas de la cuadricula: un renglon por alumno del grupo (ordenados por apellidos),
    // con su estatus en cada sesion recibida y los totales/% ya calculados. Una celda sin
    // registro en ASISTENCIA para una sesion que si existe se toma como "Falta" (no marcado
    // = ausente, mismo criterio que ya usaba el checklist anterior).
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

        // matricula -> (idSesionGrupal -> estatus)
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

    // Guardado en lote de la cuadricula completa: agrupa las celdas por sesion y, por cada
    // una, reemplaza (DELETE + INSERT batch) su asistencia -- mismo patron delete+insert que
    // ya usaba el registro de una sola fecha, pero iterado sobre todas las sesiones recibidas
    // en una unica transaccion.
    public boolean guardarCeldas(List<CeldaAsistenciaDTO> celdas) {
        if (celdas == null || celdas.isEmpty()) {
            return true;
        }

        Map<Integer, List<CeldaAsistenciaDTO>> porSesion = new LinkedHashMap<>();
        for (CeldaAsistenciaDTO celda : celdas) {
            porSesion.computeIfAbsent(celda.getIdSesionGrupal(), k -> new ArrayList<>()).add(celda);
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
}
