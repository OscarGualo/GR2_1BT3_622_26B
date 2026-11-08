package ec.edu.epn.polidinner.modelo;

import java.util.List;

/**
 * HU18 (escenario 18.3.1 y regla 4): conserva solo las ventas de pedidos que siguen siendo válidos.
 * Origen TDD: FiltroVentasValidasTest (PU03).
 */
public class FiltroVentasValidas {

    public List<VentaProducto> filtrar(List<VentaProducto> ventas) {
        return ventas.stream()
                .filter(this::esVentaValida)
                .toList();
    }

    private boolean esVentaValida(VentaProducto venta) {
        return "Pagado".equals(venta.getEstadoPedido()) ||
                "Entregado".equals(venta.getEstadoPedido());
    }
}