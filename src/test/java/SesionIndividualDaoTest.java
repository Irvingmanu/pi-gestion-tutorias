import mx.edu.utez.pigestiontutorias.models.Academia;
import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.SesionIndividual;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.AcademiaDao;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.SesionIndividualDao;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SesionIndividualDaoTest {

    private final SesionIndividualDao sesionIndividualDao = new SesionIndividualDao();
    private int numeroEmpleadoTutor;
    private Integer idSesionCreada;

    @BeforeEach
    void setUp() {
        numeroEmpleadoTutor = 900000 + (int) (System.nanoTime() % 90000);
        idSesionCreada = null;
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection con = SQLConnector.getConnection()) {
            if (idSesionCreada != null) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM SESION_INDIVIDUAL WHERE ID_SESION_INDIVIDUAL = ?")) {
                    ps.setInt(1, idSesionCreada);
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
    @DisplayName("CRUD: create -> getById; getAll(), update() y delete() no soportados")
    void crud() {
        List<Alumno> alumnos = new AlumnoDAO().getAll();
        List<Academia> academias = new AcademiaDao().getAll();
        assumeTrue(!alumnos.isEmpty() && !academias.isEmpty(),
                "Se requiere al menos un alumno y una academia existentes para crear la sesión de prueba");

        Tutor tutor = new Tutor();
        tutor.setNumeroEmpleado(numeroEmpleadoTutor);
        tutor.setNombres("Tutor");
        tutor.setApellidoPaterno("SesionIndividual");
        tutor.setApellidoMaterno("Prueba");
        tutor.setCorreoInstitucional("tutor.sesionindividual" + numeroEmpleadoTutor + "@utez.edu.mx");
        tutor.setTelefono("7773214567");
        tutor.setIdAcademia(academias.get(0).getIdAcademia());
        tutor.setPass("Tutor@Test1");
        assertTrue(new TutorDao().create(tutor), "No se pudo crear el tutor temporal requerido para la prueba");

        String matricula = alumnos.get(0).getMatricula();
        String temasUnicos = "Temas Test " + System.nanoTime();

        SesionIndividual nueva = new SesionIndividual();
        nueva.setIdTutor(numeroEmpleadoTutor);
        nueva.setMatricula(matricula);
        nueva.setFecha(java.sql.Date.valueOf(java.time.LocalDate.now()));
        nueva.setHora("11:00");
        nueva.setTemasTratados(temasUnicos);
        nueva.setAcuerdos("Acuerdos de prueba unitaria");

        assertTrue(sesionIndividualDao.create(nueva), "create() debe insertar la sesión individual");

        Optional<SesionIndividual> creada = sesionIndividualDao.getAcuerdosPorAlumno(matricula).stream()
                .filter(s -> temasUnicos.equals(s.getTemasTratados()))
                .findFirst();
        assertTrue(creada.isPresent(), "No se encontró la sesión individual recién creada");
        idSesionCreada = creada.get().getIdSesionIndividual();

        SesionIndividual encontrada = sesionIndividualDao.getById(idSesionCreada);
        assertNotNull(encontrada, "getById() debe encontrar la sesión recién creada");
        assertEquals(temasUnicos, encontrada.getTemasTratados());

        assertNull(sesionIndividualDao.getAll(), "getAll() no está implementado en este DAO");
        assertFalse(sesionIndividualDao.update(encontrada), "update() no está soportado por este DAO");
        assertFalse(sesionIndividualDao.delete(idSesionCreada), "delete() no está soportado por este DAO");
    }
}
