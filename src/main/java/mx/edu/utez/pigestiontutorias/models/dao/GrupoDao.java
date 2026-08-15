package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Grupo;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// GRUPO reemplaza el antiguo cruce de 3 catalogos (CARRERA + CUATRIMESTRE + LETRA_GRUPO):
// CUATRIMESTRE y LETRA ahora son columnas propias de GRUPO, no FKs a tablas catalogo.
public class GrupoDao implements Dao<Grupo, Integer> {

    private static final String SELECT_BASE =
            "SELECT g.ID_GRUPO, g.ID_CARRERA, g.CUATRIMESTRE, g.LETRA, g.ID_PERIODO, g.ESTADO, " +
                    "car.NOMBRE AS NOMBRE_CARRERA, car.ID_ACADEMIA " +
                    "FROM GRUPO g JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA ";

    @Override
    public boolean create(Grupo entidad) {
        String sql = "INSERT INTO GRUPO (ID_CARRERA, CUATRIMESTRE, LETRA, ID_PERIODO, ESTADO) VALUES (?, ?, ?, ?, 'S')";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_GRUPO"})) {

            ps.setInt(1, entidad.getIdCarrera());
            ps.setInt(2, entidad.getCuatrimestre());
            ps.setString(3, entidad.getLetra());
            ps.setInt(4, entidad.getIdPeriodo());

            if (ps.executeUpdate() == 0) return false;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entidad.setIdGrupo(keys.getInt(1));
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error al crear el grupo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Grupo> getAll() {
        List<Grupo> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE g.ESTADO = 'S' ORDER BY car.NOMBRE, g.CUATRIMESTRE, g.LETRA";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener los grupos: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    @Override
    public Grupo getById(Integer id) {
        String sql = SELECT_BASE + "WHERE g.ID_GRUPO = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener el grupo: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean update(Grupo entidad) {
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        return false;
    }

    // Busca el GRUPO (Carrera+Cuatrimestre+Letra+Periodo) que corresponde a esa combinacion
    // exacta; si nadie lo ha usado todavia, lo crea. GRUPO tiene UQ_GRUPO sobre estas 4
    // columnas, asi que nunca se duplica. La usan los formularios que capturan
    // Carrera+Cuatrimestre+Letra (alta de alumno, asignacion de tutor) para resolver el
    // ID_GRUPO real que hay que guardar.
    public Integer findOrCreate(int idCarrera, int cuatrimestre, String letra, int idPeriodo) {
        String sqlBuscar = "SELECT ID_GRUPO FROM GRUPO WHERE ID_CARRERA = ? AND CUATRIMESTRE = ? AND LETRA = ? AND ID_PERIODO = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlBuscar)) {

            ps.setInt(1, idCarrera);
            ps.setInt(2, cuatrimestre);
            ps.setString(3, letra);
            ps.setInt(4, idPeriodo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID_GRUPO");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar el grupo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        Grupo nuevo = new Grupo();
        nuevo.setIdCarrera(idCarrera);
        nuevo.setCuatrimestre(cuatrimestre);
        nuevo.setLetra(letra);
        nuevo.setIdPeriodo(idPeriodo);

        return create(nuevo) ? nuevo.getIdGrupo() : null;
    }

    // Grupos que un tutor tiene asignados actualmente, con la etiqueta amigable lista
    // para pintar el <select> de "Tutoria Grupal"/"Tutoria Espontanea" (ej. "DSM 3°A").
    public List<Grupo> getGruposByTutor(int idTutor) {
        List<Grupo> lista = new ArrayList<>();
        String sql = "SELECT g.ID_GRUPO, g.ID_CARRERA, g.CUATRIMESTRE, g.LETRA, g.ID_PERIODO, g.ESTADO, " +
                "car.NOMBRE AS NOMBRE_CARRERA, car.ID_ACADEMIA " +
                "FROM ASIGNACION_TUTOR a " +
                "JOIN GRUPO g ON g.ID_GRUPO = a.ID_GRUPO " +
                "JOIN CARRERA car ON car.ID_CARRERA = g.ID_CARRERA " +
                "WHERE a.ID_TUTOR = ? AND a.ESTADO = 'S' AND g.ESTADO = 'S' " +
                "ORDER BY g.CUATRIMESTRE, g.LETRA, car.NOMBRE";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTutor);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener los grupos del tutor: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    private Grupo mapear(ResultSet rs) throws SQLException {
        Grupo g = new Grupo();
        g.setIdGrupo(rs.getInt("ID_GRUPO"));
        g.setIdCarrera(rs.getInt("ID_CARRERA"));
        g.setCuatrimestre(rs.getInt("CUATRIMESTRE"));
        g.setLetra(rs.getString("LETRA"));
        g.setIdPeriodo(rs.getInt("ID_PERIODO"));
        g.setEstado(rs.getString("ESTADO"));
        g.setNombreCarrera(rs.getString("NOMBRE_CARRERA"));
        g.setNombreGrupo(rs.getString("NOMBRE_CARRERA") + " " + rs.getInt("CUATRIMESTRE") + "°" + rs.getString("LETRA"));
        g.setIdAcademia(rs.getInt("ID_ACADEMIA"));
        return g;
    }
}
