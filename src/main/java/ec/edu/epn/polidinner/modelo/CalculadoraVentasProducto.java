package ec.edu.epn.polidinner.modelo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HU18 (criterio 18.1 y regla 6): resume las ventas válidas en una fila por referencia.
 * Origen TDD: CalculadoraVentasProductoTest (PU01 y PU02).
 */
public class CalculadoraVentasProducto {

    public List<FilaVentaProducto> resumir(List<VentaProducto> ventas) {
        return new ArrayList<>(acumularUnidadesPorReferencia(ventas).values());
    }

    private Map<Integer, FilaVentaProducto> acumularUnidadesPorReferencia(List<VentaProducto> ventas) {
        Map<Integer, FilaVentaProducto> filas = new LinkedHashMap<>();
        for (VentaProducto venta : ventas) {
            FilaVentaProducto actual = filas.get(venta.getReferencia());
            int unidades = venta.getCantidad() + (actual == null ? 0 : actual.getUnidades());
            filas.put(venta.getReferencia(), new FilaVentaProducto(venta.getReferencia(), venta.getNombre(),
                    venta.getFechaCaducidad(), unidades));
        }
        return filas;
    }
}