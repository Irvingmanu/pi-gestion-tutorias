import mx.edu.utez.pigestiontutorias.models.Horario;
import mx.edu.utez.pigestiontutorias.models.dao.HorarioDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class HorarioDaoTest {

    private HorarioDao horarioDao;

    @BeforeEach
    void setUp() {
        horarioDao = new HorarioDao();
    }

    @Test
    @DisplayName("create(), getAll(), getById(), update() y delete() no están implementados")
    void crud_noImplementado() {
        assertFalse(horarioDao.create(new Horario()));
        assertNull(horarioDao.getAll());
        assertNull(horarioDao.getById(-1));
        assertFalse(horarioDao.update(new Horario()));
        assertFalse(horarioDao.delete(-1));
    }

    @Test
    @DisplayName("findDisponiblesByTutor() nunca retorna null para un tutor inexistente")
    void findDisponiblesByTutor_noRetornaNull() {
        assertNotNull(horarioDao.findDisponiblesByTutor(-1));
    }
}
