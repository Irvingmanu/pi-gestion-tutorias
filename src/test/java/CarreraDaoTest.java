import mx.edu.utez.pigestiontutorias.models.Carrera;
import mx.edu.utez.pigestiontutorias.models.dao.CarreraDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CarreraDaoTest {

    private CarreraDao carreraDao;

    @BeforeEach
    void setUp() {
        carreraDao = new CarreraDao();
    }

    @Test
    @DisplayName("getAll() nunca retorna null; getById() encuentra un registro existente")
    void getAll_yGetById_consistentes() {
        List<Carrera> carreras = carreraDao.getAll();
        assertNotNull(carreras);
        assumeTrue(!carreras.isEmpty(), "No hay carreras registradas en la BD para validar getById()");

        Carrera primera = carreras.get(0);
        Carrera encontrada = carreraDao.getById(primera.getIdCarrera());
        assertNotNull(encontrada);
        assertEquals(primera.getNombre(), encontrada.getNombre());
    }

    @Test
    @DisplayName("create(), update() y delete() no están soportados y siempre retornan false")
    void operacionesDeEscritura_noSoportadas() {
        Carrera dummy = new Carrera(-1, "No aplica");
        assertFalse(carreraDao.create(dummy));
        assertFalse(carreraDao.update(dummy));
        assertFalse(carreraDao.delete(-1));
    }
}
