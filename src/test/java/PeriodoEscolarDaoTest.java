import mx.edu.utez.pigestiontutorias.models.PeriodoEscolar;
import mx.edu.utez.pigestiontutorias.models.dao.PeriodoEscolarDao;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodoEscolarDaoTest {

    private PeriodoEscolarDao periodoEscolarDao;
    private String nombreUnico;
    private Integer idCreado;

    @BeforeEach
    void setUp() {
        periodoEscolarDao = new PeriodoEscolarDao();
        nombreUnico = "Periodo Test " + System.nanoTime();
        idCreado = null;
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (idCreado == null) return;
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM PERIODO_ESCOLAR WHERE ID_PERIODO = ?")) {
            ps.setInt(1, idCreado);
            ps.executeUpdate();
        }
    }

    private PeriodoEscolar construirPeriodo() {
        PeriodoEscolar p = new PeriodoEscolar();
        p.setNombre(nombreUnico);
        p.setFechaInicio(Date.valueOf("2026-01-01"));
        p.setFechaFin(Date.valueOf("2026-04-30"));
        p.setEstado("S");
        p.setAsistenciasGrupales(4);
        return p;
    }

    private int buscarIdPorNombre(String nombre) {
        Optional<PeriodoEscolar> encontrado = periodoEscolarDao.getAll().stream()
                .filter(p -> nombre.equals(p.getNombre()))
                .findFirst();
        assertTrue(encontrado.isPresent(), "No se encontró el periodo recién creado por su nombre");
        return encontrado.get().getIdPeriodo();
    }

    @Test
    @DisplayName("CRUD completo: create -> getById -> update -> delete (baja lógica)")
    void crudCompleto() {
        assertTrue(periodoEscolarDao.create(construirPeriodo()), "create() debe insertar el periodo escolar");
        idCreado = buscarIdPorNombre(nombreUnico);

        PeriodoEscolar encontrado = periodoEscolarDao.getById(idCreado);
        assertNotNull(encontrado, "getById() debe encontrar el periodo recién creado");
        assertEquals(nombreUnico, encontrado.getNombre());

        encontrado.setAsistenciasGrupales(6);
        assertTrue(periodoEscolarDao.update(encontrado), "update() debe afectar al menos una fila");

        PeriodoEscolar actualizado = periodoEscolarDao.getById(idCreado);
        assertEquals(6, actualizado.getAsistenciasGrupales());

        assertTrue(periodoEscolarDao.delete(idCreado), "delete() debe afectar al menos una fila");
        PeriodoEscolar deBaja = periodoEscolarDao.getById(idCreado);
        assertNotNull(deBaja, "delete() es una baja lógica: el registro sigue existiendo");
        assertEquals("N", deBaja.getEstado(), "delete() debe marcar ESTADO = 'N'");
    }

    @Test
    @DisplayName("getAll() nunca retorna null")
    void getAll_noRetornaNull() {
        List<PeriodoEscolar> lista = periodoEscolarDao.getAll();
        assertNotNull(lista);
    }
}
