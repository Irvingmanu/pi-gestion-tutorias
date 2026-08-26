import mx.edu.utez.pigestiontutorias.models.Area;
import mx.edu.utez.pigestiontutorias.models.Motivo;
import mx.edu.utez.pigestiontutorias.models.dao.AreaDAO;
import mx.edu.utez.pigestiontutorias.models.dao.MotivoDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MotivoDAOTest {

    private final AreaDAO areaDAO = new AreaDAO();
    private final MotivoDAO motivoDAO = new MotivoDAO();
    private int idAreaTemporal;

    @BeforeEach
    void setUp() {
        Area area = new Area();
        area.setNombre("Area para Motivo Test " + System.nanoTime());
        area.setNombresEncargado("Encargado");
        area.setApellidoPaternoEncargado("Prueba");
        area.setApellidoMaternoEncargado("Unitaria");
        area.setCorreoContacto("encargado.test@utez.edu.mx");
        idAreaTemporal = areaDAO.createAndGetId(area);
        assertTrue(idAreaTemporal > 0, "No se pudo crear el área temporal requerida para las pruebas");
    }

    @AfterEach
    void tearDown() {
        areaDAO.delete(idAreaTemporal);
    }

    @Test
    @DisplayName("CRUD completo: create -> getById -> update -> delete")
    void crudCompleto() {
        Motivo nuevo = new Motivo();
        nuevo.setIdArea(idAreaTemporal);
        nuevo.setNombreMotivo("Motivo Test " + System.nanoTime());

        int idGenerado = motivoDAO.createAndGetId(nuevo);
        assertTrue(idGenerado > 0, "create() debe generar un ID_MOTIVO válido");

        Motivo encontrado = motivoDAO.getById(idGenerado);
        assertNotNull(encontrado, "getById() debe encontrar el motivo recién creado");
        assertEquals(nuevo.getNombreMotivo(), encontrado.getNombreMotivo());
        assertEquals(idAreaTemporal, encontrado.getIdArea());

        encontrado.setNombreMotivo(nuevo.getNombreMotivo() + " Editado");
        assertTrue(motivoDAO.update(encontrado), "update() debe afectar al menos una fila");

        Motivo actualizado = motivoDAO.getById(idGenerado);
        assertEquals(nuevo.getNombreMotivo() + " Editado", actualizado.getNombreMotivo());

        assertTrue(motivoDAO.delete(idGenerado), "delete() debe eliminar el motivo creado");
        assertNull(motivoDAO.getById(idGenerado), "El motivo no debe existir después de eliminarlo");
    }

    @Test
    @DisplayName("getAll() y getByIdArea() nunca retornan null")
    void getAll_yGetByIdArea_noRetornanNull() {
        assertNotNull(motivoDAO.getAll());
        assertNotNull(motivoDAO.getByIdArea(idAreaTemporal));
    }

    @Test
    @DisplayName("getById() retorna null para un identificador inexistente")
    void getById_idInexistente_retornaNull() {
        assertNull(motivoDAO.getById(-1));
    }
}
