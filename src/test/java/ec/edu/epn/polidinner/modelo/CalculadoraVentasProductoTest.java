package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * HU18 Consultar ventas de productos - TDD de CalculadoraVentasProducto (PU01 y PU02).
 * Datos de la demostración: Agua 500 ml (referencias 101 y 102) y Galletas 100 g (103).
 */
class CalculadoraVentasProductoTest {

    private static final Date MANANA = Date.valueOf(LocalDate.of(2026, 11, 9));
    private static final Date HOY_10H = new Date(java.sql.Timestamp.valueOf("2026-11-08 10:00:00").getTime());

    private final CalculadoraVentasProducto calculadora = new CalculadoraVentasProducto();

    private static VentaProducto venta(int referencia, String nombre, int cantidad) {
        return new VentaProducto(referencia, nombre, MANANA, cantidad, "Pagado", HOY_10H);
    }

    /** PU01 - Escenarios 18.1.1 y 18.1.2: suma los pedidos del mismo registro sin mezclar registros. */
    @Test
    void sumaLasUnidadesDeVariosPedidosDelMismoRegistroSinMezclarRegistros() {
        List<VentaProducto> ventas = List.of(
                venta(101, "Agua 500 ml", 6),
                venta(101, "Agua 500 ml", 4),
                venta(102, "Agua 500 ml", 4));

        List<FilaVentaProducto> filas = calculadora.resumir(ventas);

        assertEquals(2, filas.size());
        assertEquals(101, filas.get(0).getReferencia());
        assertEquals(10, filas.get(0).getUnidades());
        assertEquals(102, filas.get(1).getReferencia());
        assertEquals(4, filas.get(1).getUnidades());
    }
}