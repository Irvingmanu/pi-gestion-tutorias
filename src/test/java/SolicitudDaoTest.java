import mx.edu.utez.pigestiontutorias.models.Academia;
import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.Solicitud;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.AcademiaDao;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.SolicitudDao;
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

class SolicitudDaoTest {

    private final SolicitudDao solicitudDao = new SolicitudDao();
    private int numeroEmpleadoTutor;
    private Integer idSolicitudCreada;

    @BeforeEach
    void setUp() {
        numeroEmpleadoTutor = 900000 + (int) (System.nanoTime() % 90000);
        idSolicitudCreada = null;
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection con = SQLConnector.getConnection()) {
            if (idSolicitudCreada != null) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM SOLICITUD_TUTORIA WHERE ID_SOLICITUD = ?")) {
                    ps.setInt(1, idSolicitudCreada);
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
                "Se requiere al menos un alumno y una academia existentes para crear la solicitud de prueba");

        Tutor tutor = new Tutor();
        tutor.setNumeroEmpleado(numeroEmpleadoTutor);
        tutor.setNombres("Tutor");
        tutor.setApellidoPaterno("Solicitud");
        tutor.setApellidoMaterno("Prueba");
        tutor.setCorreoInstitucional("tutor.solicitud" + numeroEmpleadoTutor + "@utez.edu.mx");
        tutor.setTelefono("7771233211");
        tutor.setIdAcademia(academias.get(0).getIdAcademia());
        tutor.setPass("Tutor@Test1");
        assertTrue(new TutorDao().create(tutor), "No se pudo crear el tutor temporal requerido para la prueba");

        String matricula = alumnos.get(0).getMatricula();
        String asuntoUnico = "Asunto Test " + System.nanoTime();

        Solicitud nueva = new Solicitud();
        nueva.setMatricula(matricula);
        nueva.setIdTutor(numeroEmpleadoTutor);
        nueva.setAsunto(asuntoUnico);
        nueva.setDescripcion("Descripción generada por prueba unitaria");
        nueva.setFechaPropuesta(java.sql.Date.valueOf(java.time.LocalDate.now().plusDays(3)));
        nueva.setDuracion(1);
        nueva.setHoraPropuesta("10:00");

        assertTrue(solicitudDao.create(nueva), "create() debe insertar la solicitud");

        Optional<Solicitud> creada = solicitudDao.getSolicitudesByAlumno(matricula).stream()
                .filter(s -> asuntoUnico.equals(s.getAsunto()))
                .findFirst();
        assertTrue(creada.isPresent(), "No se encontró la solicitud recién creada");
        idSolicitudCreada = creada.get().getIdSolicitud();

        Solicitud encontrada = solicitudDao.getById(idSolicitudCreada);
        assertNotNull(encontrada, "getById() debe encontrar la solicitud recién creada");
        assertEquals(asuntoUnico, encontrada.getAsunto());
        assertEquals("Pendiente", encontrada.getEstatus());

        assertNull(solicitudDao.getAll(), "getAll() no está implementado en este DAO");
        assertFalse(solicitudDao.update(encontrada), "update() no está soportado por este DAO");
        assertFalse(solicitudDao.delete(idSolicitudCreada), "delete() no está soportado por este DAO");
    }
}
