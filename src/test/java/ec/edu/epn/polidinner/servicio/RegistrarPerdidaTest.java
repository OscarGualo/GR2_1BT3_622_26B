package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.DescarteRechazadoException;
import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.persistencia.JPAUtil;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;

/**
 * Comprobación de soporte de HU20 (no es una de las 12 pruebas TDD), con H2 y Hibernate reales:
 * la pérdida y el stock se guardan juntos, la misma confirmación no se registra dos veces y una cantidad
 * mayor al stock no cambia nada.
 */
class RegistrarPerdidaTest extends BaseDatosPrueba {

    private static final LocalDateTime AHORA = LUNES.atTime(15, 30);

    private final ServicioPerdidas perdidas = new ServicioPerdidas();
    private final PerdidaCaducidadDAO dao = new PerdidaCaducidadDAO();
    private Producto yogur;
    private PersonalComedor maria;

    @BeforeEach
    void preparar() {
        prepararBase();
        maria = new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "comedor123");
        yogur = Producto.registrar("Yogur 150 g", 0.90, "Vaso", 6, java.sql.Date.valueOf(LUNES.minusDays(1)));
        guardar(maria, yogur);
    }

    @AfterEach
    void cerrar() {
        cerrarBase();
    }

    @Test
    void registraLaPerdidaYDescuentaElStockUnaSolaVezPorConfirmacion() {
        assertTrue(perdidas.registrar(yogur.getId(), 6, maria, "op-1", AHORA));
        assertFalse(perdidas.registrar(yogur.getId(), 6, maria, "op-1", AHORA));

        assertEquals(0, stock(yogur));
        List<PerdidaCaducidad> historial = dao.listarRecientes();
        assertEquals(1, historial.size());
        assertEquals(6, historial.get(0).getUnidades());
        assertEquals("Yogur 150 g", historial.get(0).getNombreProducto());
        assertEquals("1700000001", historial.get(0).getResponsable().getCedula());
        assertEquals("15/11/2026", historial.get(0).getCaducidadTexto());
    }

    @Test
    void unaCantidadMayorAlStockNoCambiaNada() {
        DescarteRechazadoException error = assertThrows(DescarteRechazadoException.class,
                () -> perdidas.registrar(yogur.getId(), 8, maria, "op-2", AHORA));

        assertEquals("La cantidad supera las 6 unidades disponibles", error.getMessage());
        assertEquals(6, stock(yogur));
        assertTrue(dao.listarRecientes().isEmpty());
    }

    private void guardar(Object... entidades) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            for (Object entidad : entidades) {
                em.persist(entidad);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}