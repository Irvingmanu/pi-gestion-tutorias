import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.Area;
import mx.edu.utez.pigestiontutorias.models.Canalizacion;
import mx.edu.utez.pigestiontutorias.models.Motivo;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.AreaDAO;
import mx.edu.utez.pigestiontutorias.models.dao.CanalizacionDao;
import mx.edu.utez.pigestiontutorias.models.dao.MotivoDAO;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CanalizacionDaoTest {

    private final CanalizacionDao canalizacionDao = new CanalizacionDao();
    private final AreaDAO areaDAO = new AreaDAO();
    private Integer idAreaTemporal;
    private Integer idCanalizacionCreada;

    @BeforeEach
    void setUp() {
        idAreaTemporal = null;
        idCanalizacionCreada = null;
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection con = SQLConnector.getConnection()) {
            if (idCanalizacionCreada != null) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM CANALIZACION WHERE ID_CANALIZACION = ?")) {
                    ps.setInt(1, idCanalizacionCreada);
                    ps.executeUpdate();
                }
            }
        }
        if (idAreaTemporal != null) {
            areaDAO.delete(idAreaTemporal);
        }
    }

    @Test
    @DisplayName("CRUD: create(); getAll(), getById() y update() no soportados; delete() no soportado")
    void crud() throws SQLException {
        List<Alumno> alumnos = new AlumnoDAO().getAll();
        assumeTrue(!alumnos.isEmpty(), "Se requiere al menos un alumno existente para crear la canalización de prueba");

        Area area = new Area();
        area.setNombre("Area Canalizacion Test " + System.nanoTime());
        area.setNombresEncargado("Encargado");
        area.setApellidoPaternoEncargado("Prueba");
        area.setApellidoMaternoEncargado("Unitaria");
        area.setCorreoContacto("encargado.test@utez.edu.mx");
        idAreaTemporal = areaDAO.createAndGetId(area);
        assertTrue(idAreaTemporal > 0, "No se pudo crear el área temporal requerida para la prueba");

        Motivo motivo = new Motivo();
        motivo.setIdArea(idAreaTemporal);
        motivo.setNombreMotivo("Motivo Canalizacion Test " + System.nanoTime());
        int idMotivoTemporal = new MotivoDAO().createAndGetId(motivo);
        assertTrue(idMotivoTemporal > 0, "No se pudo crear el motivo temporal requerido para la prueba");

        String matricula = alumnos.get(0).getMatricula();
        Canalizacion nueva = new Canalizacion();
        nueva.setIdArea(idAreaTemporal);
        nueva.setIdMotivo(idMotivoTemporal);
        nueva.setMatricula(matricula);
        nueva.setObservaciones("Canalizacion Test " + System.nanoTime());

        assertTrue(canalizacionDao.create(nueva), "create() debe insertar la canalización");

        // OBSERVACIONES es CLOB y no admite comparación directa con "=" en SQL, por lo que la
        // canalización recién creada se localiza por área temporal + matrícula (la más reciente).
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT ID_CANALIZACION FROM CANALIZACION WHERE ID_AREA = ? AND MATRICULA = ? " +
                             "ORDER BY ID_CANALIZACION DESC FETCH FIRST 1 ROW ONLY")) {
            ps.setInt(1, idAreaTemporal);
            ps.setString(2, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "No se encontró la canalización recién creada");
                idCanalizacionCreada = rs.getInt("ID_CANALIZACION");
            }
        }

        assertNull(canalizacionDao.getAll(), "getAll() no está implementado en este DAO");
        assertNull(canalizacionDao.getById(idCanalizacionCreada), "getById() no está implementado en este DAO");
        assertFalse(canalizacionDao.update(nueva), "update() no está soportado por este DAO");
        assertFalse(canalizacionDao.delete(idCanalizacionCreada), "delete() no está soportado por este DAO");
    }
}
