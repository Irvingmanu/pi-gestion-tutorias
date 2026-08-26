import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.Genero;
import mx.edu.utez.pigestiontutorias.models.Grupo;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.GrupoDao;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AlumnoDAOTest {

    private AlumnoDAO alumnoDAO;
    private String matriculaPrueba;

    @BeforeEach
    void setUp() {
        alumnoDAO = new AlumnoDAO();
        matriculaPrueba = "TST" + (System.nanoTime() % 100000000L);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection con = SQLConnector.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ALUMNO_GRUPO_HISTORICO WHERE MATRICULA = ?")) {
                ps.setString(1, matriculaPrueba);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ALUMNO WHERE MATRICULA = ?")) {
                ps.setString(1, matriculaPrueba);
                ps.executeUpdate();
            }
        }
    }

    @Test
    @DisplayName("CRUD completo: create -> getById -> update -> delete (baja lógica)")
    void crudCompleto() {
        List<Genero> generos = alumnoDAO.getAllGeneros();
        List<Grupo> grupos = new GrupoDao().getAll();
        assumeTrue(!generos.isEmpty() && !grupos.isEmpty(),
                "Se requiere al menos un género y un grupo activo existentes para crear un alumno de prueba");

        Alumno nuevo = new Alumno();
        nuevo.setMatricula(matriculaPrueba);
        nuevo.setNombres("Alumno");
        nuevo.setApellidoPaterno("Prueba");
        nuevo.setApellidoMaterno("Unitaria");
        nuevo.setCorreoInstitucional(matriculaPrueba.toLowerCase() + "@utez.edu.mx");
        nuevo.setTelefono(String.format("77%08d", System.nanoTime() % 100000000L));
        nuevo.setIdGenero(generos.get(0).getId());
        nuevo.setIdGrupo(grupos.get(0).getIdGrupo());

        assertTrue(alumnoDAO.create(nuevo), "create() debe insertar el alumno");

        Alumno encontrado = alumnoDAO.getById(matriculaPrueba);
        assertNotNull(encontrado, "getById() debe encontrar el alumno recién creado");
        assertEquals("Alumno", encontrado.getNombres());

        String telefonoActualizado = String.format("78%08d", System.nanoTime() % 100000000L);
        encontrado.setTelefono(telefonoActualizado);
        assertTrue(alumnoDAO.update(encontrado), "update() debe afectar al menos una fila");

        Alumno actualizado = alumnoDAO.getById(matriculaPrueba);
        assertEquals(telefonoActualizado, actualizado.getTelefono());

        assertTrue(alumnoDAO.delete(matriculaPrueba), "delete() debe afectar al menos una fila");
        Alumno deBaja = alumnoDAO.getById(matriculaPrueba);
        assertNotNull(deBaja, "delete() es una baja lógica: el registro sigue existiendo");
        assertEquals("N", deBaja.getEstado(), "delete() debe marcar ESTADO = 'N'");
    }

    @Test
    @DisplayName("getAll() nunca retorna null")
    void getAll_noRetornaNull() {
        assertNotNull(alumnoDAO.getAll());
    }
}
