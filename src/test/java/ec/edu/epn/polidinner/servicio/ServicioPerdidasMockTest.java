package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.modelo.ValidadorDescarte;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;

/**
 * HU20 Registrar pérdidas por caducidad - TDD con mocks de ServicioPerdidas (PM02).
 * El Producto y el ValidadorDescarte son objetos simulados con Mockito.
 */
class ServicioPerdidasMockTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 8, 15, 30);

    /** PM02 - Escenario 20.2.1: valida la existencia y la cantidad antes de descontar y armar la pérdida. */
    @Test
    void validaLaExistenciaYLaCantidadAntesDeDescontarElStock() {
        Producto yogur = Mockito.mock(Producto.class);
        ValidadorDescarte validador = Mockito.mock(ValidadorDescarte.class);
        PersonalComedor maria = new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "comedor123");
        ServicioPerdidas servicio = new ServicioPerdidas(Mockito.mock(PerdidaCaducidadDAO.class),
                Mockito.mock(ProductoDAO.class), validador);

        Mockito.when(yogur.getNombre()).thenReturn("Yogur 150 g");
        Mockito.when(yogur.getStockActual()).thenReturn(6);
        Mockito.when(validador.admiteDescarte(yogur, HOY)).thenReturn(true);

        PerdidaCaducidad perdida = servicio.aplicarDescarte(yogur, 6, maria, AHORA, "op-1");

        InOrder orden = Mockito.inOrder(validador, yogur);
        orden.verify(validador).admiteDescarte(yogur, HOY);
        orden.verify(validador).validarCantidad(yogur, 6);
        orden.verify(yogur).setStockActual(0);
        assertEquals(6, perdida.getUnidades());
        assertSame(maria, perdida.getResponsable());
        assertEquals(AHORA, perdida.getFechaRegistro());
        assertEquals("op-1", perdida.getTokenOperacion());
    }
}