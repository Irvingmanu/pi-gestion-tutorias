import mx.edu.utez.pigestiontutorias.models.LetraGrupo;
import mx.edu.utez.pigestiontutorias.models.dao.LetraGrupoDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LetraGrupoDaoTest {

    private LetraGrupoDao letraGrupoDao;

    @BeforeEach
    void setUp() {
        letraGrupoDao = new LetraGrupoDao();
    }

    @Test
    @DisplayName("getAll() nunca retorna null")
    void getAll_noRetornaNull() {
        assertNotNull(letraGrupoDao.getAll());
    }

    @Test
    @DisplayName("create(), getById(), update() y delete() no están implementados")
    void operacionesNoImplementadas() {
        assertFalse(letraGrupoDao.create(new LetraGrupo(-1, "Z")));
        assertNull(letraGrupoDao.getById(-1));
        assertFalse(letraGrupoDao.update(new LetraGrupo(-1, "Z")));
        assertFalse(letraGrupoDao.delete(-1));
    }
}
