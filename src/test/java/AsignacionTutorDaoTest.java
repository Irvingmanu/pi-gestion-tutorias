import mx.edu.utez.pigestiontutorias.models.Academia;
import mx.edu.utez.pigestiontutorias.models.AsignacionTutor;
import mx.edu.utez.pigestiontutorias.models.Carrera;
import mx.edu.utez.pigestiontutorias.models.Grupo;
import mx.edu.utez.pigestiontutorias.models.PeriodoEscolar;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.AcademiaDao;
import mx.edu.utez.pigestiontutorias.models.dao.AsignacionTutorDao;
import mx.edu.utez.pigestiontutorias.models.dao.CarreraDao;
import mx.edu.utez.pigestiontutorias.models.dao.GrupoDao;
import mx.edu.utez.pigestiontutorias.models.dao.PeriodoEscolarDao;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AsignacionTutorDaoTest {

    private final AsignacionTutorDao asignacionTutorDao = new AsignacionTutorDao();
    private int numeroEmpleadoTutor;
    private Integer idGrupoTemporal;
    private Integer idAsignacionCreada;

    @BeforeEach
    void setUp() {
        numeroEmpleadoTutor = 900000 + (int) (System.nanoTime() % 90000);
        idGrupoTemporal = null;
        idAsignacionCreada = null;
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection con = SQLConnector.getConnection()) {
            if (idAsignacionCreada != null) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM ASIGNACION_TUTOR WHERE ID_ASIGNACION = ?")) {
                    ps.setInt(1, idAsignacionCreada);
                    ps.executeUpdate();
                }
            }
            if (idGrupoTemporal != null) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM GRUPO WHERE ID_GRUPO = ?")) {
                    ps.setInt(1, idGrupoTemporal);
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM TUTOR WHERE NUMERO_EMPLEADO = ?")) {
                ps.setInt(1, numeroEmpleadoTutor);
                ps.executeUpdate();
            }
        }
    }

    @Test
    @DisplayName("CRUD: create -> getAll -> getById -> delete (baja lógica); update() no soportado")
    void crud() {
        List<Academia> academias = new AcademiaDao().getAll();
        List<Carrera> carreras = new CarreraDao().getAll();
        List<PeriodoEscolar> periodos = new PeriodoEscolarDao().getAll();
        assumeTrue(!academias.isEmpty() && !carreras.isEmpty() && !periodos.isEmpty(),
                "Se requiere al menos una academia, una carrera y un periodo escolar existentes");

        Tutor tutor = new Tutor();
        tutor.setNumeroEmpleado(numeroEmpleadoTutor);
        tutor.setNombres("Tutor");
        tutor.setApellidoPaterno("Asignacion");
        tutor.setApellidoMaterno("Prueba");
        tutor.setCorreoInstitucional("tutor.asignacion" + numeroEmpleadoTutor + "@utez.edu.mx");
        tutor.setTelefono(String.format("79%08d", numeroEmpleadoTutor));
        tutor.setIdAcademia(academias.get(0).getIdAcademia());
        tutor.setPass("Tutor@Test1");
        assertTrue(new TutorDao().create(tutor), "No se pudo crear el tutor temporal requerido para la prueba");

        Grupo grupo = new Grupo();
        grupo.setIdCarrera(carreras.get(0).getIdCarrera());
        grupo.setIdPeriodo(periodos.get(0).getIdPeriodo());
        grupo.setCuatrimestre(11);
        grupo.setLetra("F");
        grupo.setGeneracion("2098-2099");
        assertTrue(new GrupoDao().create(grupo), "No se pudo crear el grupo temporal requerido para la prueba");
        idGrupoTemporal = grupo.getIdGrupo();

        AsignacionTutor nueva = new AsignacionTutor(numeroEmpleadoTutor, idGrupoTemporal);
        assertTrue(asignacionTutorDao.create(nueva), "create() debe insertar la asignación");

        List<AsignacionTutor> activas = asignacionTutorDao.getAll();
        assertNotNull(activas);
        Optional<AsignacionTutor> creada = activas.stream()
                .filter(a -> a.getIdTutor() == numeroEmpleadoTutor && a.getIdGrupo() == idGrupoTemporal)
                .findFirst();
        assertTrue(creada.isPresent(), "getAll() debe incluir la asignación recién creada");
        idAsignacionCreada = creada.get().getIdAsignacion();

        AsignacionTutor encontrada = asignacionTutorDao.getById(idAsignacionCreada);
        assertNotNull(encontrada, "getById() debe encontrar la asignación recién creada");
        assertEquals("S", encontrada.getEstado());

        assertFalse(asignacionTutorDao.update(encontrada), "update() no está soportado por este DAO");

        assertTrue(asignacionTutorDao.delete(idAsignacionCreada), "delete() debe afectar al menos una fila");
        AsignacionTutor deBaja = asignacionTutorDao.getById(idAsignacionCreada);
        assertNotNull(deBaja, "delete() es una baja lógica: el registro sigue existiendo");
        assertEquals("N", deBaja.getEstado(), "delete() debe marcar ESTADO = 'N'");
    }
}
