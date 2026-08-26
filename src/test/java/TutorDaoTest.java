import mx.edu.utez.pigestiontutorias.models.Academia;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.AcademiaDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class TutorDaoTest {

    private TutorDao tutorDao;
    private int numeroEmpleadoPrueba;

    @BeforeEach
    void setUp() {
        tutorDao = new TutorDao();
        numeroEmpleadoPrueba = 900000 + (int) (System.nanoTime() % 90000);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM TUTOR WHERE NUMERO_EMPLEADO = ?")) {
            ps.setInt(1, numeroEmpleadoPrueba);
            ps.executeUpdate();
        }
    }

    @Test
    @DisplayName("CRUD completo: create -> getById -> update -> delete (baja lógica)")
    void crudCompleto() {
        List<Academia> academias = new AcademiaDao().getAll();
        assumeTrue(!academias.isEmpty(), "Se requiere al menos una academia existente para crear un tutor de prueba");

        Tutor nuevo = new Tutor();
        nuevo.setNumeroEmpleado(numeroEmpleadoPrueba);
        nuevo.setNombres("Tutor");
        nuevo.setApellidoPaterno("Prueba");
        nuevo.setApellidoMaterno("Unitaria");
        nuevo.setCorreoInstitucional("tutor.test" + numeroEmpleadoPrueba + "@utez.edu.mx");
        nuevo.setTelefono("7771230990");
        nuevo.setIdAcademia(academias.get(0).getIdAcademia());
        nuevo.setPass("Tutor@Test1");

        assertTrue(tutorDao.create(nuevo), "create() debe insertar el tutor");

        Tutor encontrado = tutorDao.getById(numeroEmpleadoPrueba);
        assertNotNull(encontrado, "getById() debe encontrar el tutor recién creado");
        assertEquals("Tutor", encontrado.getNombres());

        encontrado.setTelefono("7770987654");
        assertTrue(tutorDao.update(encontrado), "update() debe afectar al menos una fila");

        Tutor actualizado = tutorDao.getById(numeroEmpleadoPrueba);
        assertEquals("7770987654", actualizado.getTelefono());

        assertTrue(tutorDao.delete(numeroEmpleadoPrueba), "delete() debe afectar al menos una fila");
        Tutor deBaja = tutorDao.getById(numeroEmpleadoPrueba);
        assertNotNull(deBaja, "delete() es una baja lógica: el registro sigue existiendo");
        assertEquals("N", deBaja.getEstado(), "delete() debe marcar ESTADO = 'N'");
    }

    @Test
    @DisplayName("getAll() nunca retorna null")
    void getAll_noRetornaNull() {
        assertNotNull(tutorDao.getAll());
    }
}
