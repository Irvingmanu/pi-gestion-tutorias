import mx.edu.utez.pigestiontutorias.models.Coordinador;
import mx.edu.utez.pigestiontutorias.models.dao.CoordinadorDAO;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoordinadorDAOTest {

    private CoordinadorDAO coordinadorDAO;
    private int numeroEmpleadoPrueba;

    @BeforeEach
    void setUp() {
        coordinadorDAO = new CoordinadorDAO();
        numeroEmpleadoPrueba = 900000 + (int) (System.nanoTime() % 90000);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM COORDINADOR WHERE NUMERO_EMPLEADO = ?")) {
            ps.setInt(1, numeroEmpleadoPrueba);
            ps.executeUpdate();
        }
    }

    private Coordinador construirCoordinador() {
        Coordinador c = new Coordinador();
        c.setNumeroEmpleado(numeroEmpleadoPrueba);
        c.setNombres("Coordinador");
        c.setApellidoPaterno("Prueba");
        c.setApellidoMaterno("Unitaria");
        c.setCorreoInstitucional("coordinador.test" + numeroEmpleadoPrueba + "@utez.edu.mx");
        c.setTelefono("7771234567");
        c.setPass("Coord@Test1");
        return c;
    }

    @Test
    @DisplayName("CRUD completo: create -> getById -> update -> delete (baja lógica)")
    void crudCompleto() {
        assertTrue(coordinadorDAO.create(construirCoordinador()), "create() debe insertar el coordinador");

        Coordinador encontrado = coordinadorDAO.getById(numeroEmpleadoPrueba);
        assertNotNull(encontrado, "getById() debe encontrar el coordinador recién creado");
        assertEquals("Coordinador", encontrado.getNombres());

        encontrado.setTelefono("7779876543");
        assertTrue(coordinadorDAO.update(encontrado), "update() debe afectar al menos una fila");

        Coordinador actualizado = coordinadorDAO.getById(numeroEmpleadoPrueba);
        assertEquals("7779876543", actualizado.getTelefono());

        assertTrue(coordinadorDAO.delete(numeroEmpleadoPrueba), "delete() debe afectar al menos una fila");
        Coordinador tresDeBaja = coordinadorDAO.getById(numeroEmpleadoPrueba);
        assertNotNull(tresDeBaja, "delete() es una baja lógica: el registro sigue existiendo");
        assertEquals("N", tresDeBaja.getEstado(), "delete() debe marcar ESTADO = 'N'");
    }

    @Test
    @DisplayName("getAll() nunca retorna null")
    void getAll_noRetornaNull() {
        assertNotNull(coordinadorDAO.getAll());
    }
}
