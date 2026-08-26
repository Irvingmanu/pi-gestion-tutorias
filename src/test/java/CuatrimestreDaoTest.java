import mx.edu.utez.pigestiontutorias.models.Cuatrimestre;
import mx.edu.utez.pigestiontutorias.models.dao.CuatrimestreDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CuatrimestreDaoTest {

    private CuatrimestreDao cuatrimestreDao;

    @BeforeEach
    void setUp() {
        cuatrimestreDao = new CuatrimestreDao();
    }

    @Test
    @DisplayName("getAll() nunca retorna null")
    void getAll_noRetornaNull() {
        assertNotNull(cuatrimestreDao.getAll());
    }

    @Test
    @DisplayName("create(), getById(), update() y delete() no están implementados")
    void operacionesNoImplementadas() {
        assertFalse(cuatrimestreDao.create(new Cuatrimestre(-1, -1)));
        assertNull(cuatrimestreDao.getById(-1));
        assertFalse(cuatrimestreDao.update(new Cuatrimestre(-1, -1)));
        assertFalse(cuatrimestreDao.delete(-1));
    }
}
