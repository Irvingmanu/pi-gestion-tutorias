import mx.edu.utez.pigestiontutorias.models.Carrera;
import mx.edu.utez.pigestiontutorias.models.Grupo;
import mx.edu.utez.pigestiontutorias.models.PeriodoEscolar;
import mx.edu.utez.pigestiontutorias.models.dao.CarreraDao;
import mx.edu.utez.pigestiontutorias.models.dao.GrupoDao;
import mx.edu.utez.pigestiontutorias.models.dao.PeriodoEscolarDao;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class GrupoDaoTest {

    private GrupoDao grupoDao;
    private Integer idGrupoCreado;

    @BeforeEach
    void setUp() {
        grupoDao = new GrupoDao();
        idGrupoCreado = null;
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (idGrupoCreado == null) return;
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM GRUPO WHERE ID_GRUPO = ?")) {
            ps.setInt(1, idGrupoCreado);
            ps.executeUpdate();
        }
    }

    @Test
    @DisplayName("CRUD: create -> getAll -> getById; update() y delete() no soportados")
    void crud() {
        List<Carrera> carreras = new CarreraDao().getAll();
        List<PeriodoEscolar> periodos = new PeriodoEscolarDao().getAll();
        assumeTrue(!carreras.isEmpty() && !periodos.isEmpty(),
                "Se requiere al menos una carrera y un periodo escolar existentes para crear un grupo de prueba");

        Grupo nuevo = new Grupo();
        nuevo.setIdCarrera(carreras.get(0).getIdCarrera());
        nuevo.setIdPeriodo(periodos.get(0).getIdPeriodo());
        nuevo.setCuatrimestre(11);
        nuevo.setLetra("F");
        nuevo.setGeneracion("2099-2100");

        assertTrue(grupoDao.create(nuevo), "create() debe insertar el grupo y asignar su ID generado");
        idGrupoCreado = nuevo.getIdGrupo();
        assertTrue(idGrupoCreado > 0, "create() debe asignar un ID_GRUPO válido a la entidad");

        assertNotNull(grupoDao.getAll());

        Grupo encontrado = grupoDao.getById(idGrupoCreado);
        assertNotNull(encontrado, "getById() debe encontrar el grupo recién creado");
        assertEquals(nuevo.getGeneracion(), encontrado.getGeneracion());

        assertFalse(grupoDao.update(encontrado), "update() no está soportado por este DAO");
        assertFalse(grupoDao.delete(idGrupoCreado), "delete() no está soportado por este DAO");
    }

    @Test
    @DisplayName("getById() retorna null para un identificador inexistente")
    void getById_idInexistente_retornaNull() {
        assertNull(grupoDao.getById(-1));
    }
}
