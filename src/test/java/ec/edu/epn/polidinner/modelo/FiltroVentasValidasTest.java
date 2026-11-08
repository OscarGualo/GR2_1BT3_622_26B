package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/** HU18 Consultar ventas de productos - TDD de FiltroVentasValidas (PU03). */
class FiltroVentasValidasTest {

    private static final Date MANANA = Date.valueOf(LocalDate.of(2026, 11, 9));

    private final FiltroVentasValidas filtro = new FiltroVentasValidas();

    private static VentaProducto venta(int cantidad, String estado) {
        return new VentaProducto(103, "Galletas 100 g", MANANA, cantidad, estado, new java.util.Date());
    }

    /** PU03 - Escenario 18.3.1: solo cuentan los pedidos Pagados o Entregados. */
    @Test
    void conservaSoloLasVentasDePedidosPagadosOEntregados() {
        List<VentaProducto> ventas = List.of(
                venta(2, "Pagado"),
                venta(3, "Entregado"),
                venta(4, "Cancelado"),
                venta(1, "Creado"));

        List<VentaProducto> validas = filtro.filtrar(ventas);

        assertEquals(List.of("Pagado", "Entregado"),
                validas.stream().map(VentaProducto::getEstadoPedido).toList());
        assertEquals(5, validas.stream().mapToInt(VentaProducto::getCantidad).sum());
    }
}