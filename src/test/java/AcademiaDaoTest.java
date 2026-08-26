import mx.edu.utez.pigestiontutorias.models.Academia;
import mx.edu.utez.pigestiontutorias.models.dao.AcademiaDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AcademiaDaoTest {

    private AcademiaDao academiaDao;

    @BeforeEach
    void setUp() {
        academiaDao = new AcademiaDao();
    }

    @Test
    @DisplayName("getAll() nunca retorna null")
    void getAll_noRetornaNull() {
        assertNotNull(academiaDao.getAll());
    }

    @Test
    @DisplayName("getAll() retorna academias con ID positivo y nombre no vacío")
    void getAll_academiasConDatosValidos() {
        List<Academia> academias = academiaDao.getAll();
        assumeTrue(!academias.isEmpty(), "No hay academias registradas en la BD para validar");

        for (Academia a : academias) {
            assertTrue(a.getIdAcademia() > 0, "El ID_ACADEMIA debe ser positivo");
            assertNotNull(a.getNombre(), "El nombre no debe ser null");
            assertFalse(a.getNombre().isBlank(), "El nombre no debe estar vacío");
        }
    }

    @Test
    @DisplayName("getAll() retorna la lista ordenada alfabéticamente por nombre")
    void getAll_ordenAlfabetico() {
        List<Academia> academias = academiaDao.getAll();
        assumeTrue(academias.size() > 1, "Se requieren al menos 2 academias para validar el orden");

        for (int i = 0; i < academias.size() - 1; i++) {
            String actual = academias.get(i).getNombre();
            String siguiente = academias.get(i + 1).getNombre();
            assertTrue(actual.compareToIgnoreCase(siguiente) <= 0,
                    "El orden alfabético se rompe entre \"" + actual + "\" y \"" + siguiente + "\"");
        }
    }

    @Test
    @DisplayName("getAll() no contiene identificadores de academia duplicados")
    void getAll_sinIdsDuplicados() {
        List<Academia> academias = academiaDao.getAll();
        long idsUnicos = academias.stream().map(Academia::getIdAcademia).distinct().count();
        assertEquals(academias.size(), idsUnicos, "No deben existir ID_ACADEMIA duplicados en el catálogo");
    }

    @Test
    @DisplayName("getAll() es consistente entre llamadas consecutivas")
    void getAll_esConsistenteEntreLlamadas() {
        List<Academia> primeraLlamada = academiaDao.getAll();
        List<Academia> segundaLlamada = academiaDao.getAll();

        assertEquals(primeraLlamada.size(), segundaLlamada.size(),
                "Dos llamadas consecutivas sin cambios en la BD deben retornar la misma cantidad de registros");
    }
}
