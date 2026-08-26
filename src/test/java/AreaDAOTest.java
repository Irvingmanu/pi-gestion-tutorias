import mx.edu.utez.pigestiontutorias.models.Area;
import mx.edu.utez.pigestiontutorias.models.dao.AreaDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AreaDAOTest {

    private AreaDAO areaDAO;

    @BeforeEach
    void setUp() {
        areaDAO = new AreaDAO();
    }

    private Area construirArea(String nombre) {
        Area a = new Area();
        a.setNombre(nombre);
        a.setNombresEncargado("Encargado");
        a.setApellidoPaternoEncargado("Prueba");
        a.setApellidoMaternoEncargado("Unitaria");
        a.setCorreoContacto("encargado.test@utez.edu.mx");
        a.setEnlaceCita("https://ejemplo.com/cita");
        return a;
    }

    @Test
    @DisplayName("CRUD completo: create -> getById -> update -> delete")
    void crudCompleto() {
        String nombreUnico = "Area Test " + System.nanoTime();
        int idGenerado = areaDAO.createAndGetId(construirArea(nombreUnico));
        assertTrue(idGenerado > 0, "create() debe generar un ID_AREA válido");

        try {
            Area encontrada = areaDAO.getById(idGenerado);
            assertNotNull(encontrada, "getById() debe encontrar el área recién creada");
            assertEquals(nombreUnico, encontrada.getNombre());

            encontrada.setNombre(nombreUnico + " Editada");
            encontrada.setEnlaceCita("https://ejemplo.com/cita-editada");
            assertTrue(areaDAO.update(encontrada), "update() debe afectar al menos una fila");

            Area actualizada = areaDAO.getById(idGenerado);
            assertEquals(nombreUnico + " Editada", actualizada.getNombre());
        } finally {
            assertTrue(areaDAO.delete(idGenerado), "delete() debe eliminar el área creada");
        }

        assertNull(areaDAO.getById(idGenerado), "El área no debe existir después de eliminarla");
    }

    @Test
    @DisplayName("getAll() nunca retorna null y create() la incorpora al catálogo")
    void getAll_incluyeElRegistroCreado() {
        assertNotNull(areaDAO.getAll());

        String nombreUnico = "Area Catalogo " + System.nanoTime();
        assertTrue(areaDAO.create(construirArea(nombreUnico)), "create() debe insertar el área");

        boolean encontrada = areaDAO.getAll().stream().anyMatch(a -> nombreUnico.equals(a.getNombre()));
        assertTrue(encontrada, "getAll() debe incluir el área recién creada");

        areaDAO.getAll().stream()
                .filter(a -> nombreUnico.equals(a.getNombre()))
                .findFirst()
                .ifPresent(a -> areaDAO.delete(a.getIdArea()));
    }

    @Test
    @DisplayName("getById() retorna null para un identificador inexistente")
    void getById_idInexistente_retornaNull() {
        assertNull(areaDAO.getById(-1));
    }
}
