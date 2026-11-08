package ec.edu.epn.polidinner.persistencia;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import ec.edu.epn.polidinner.modelo.VentaProducto;

/**
 * Soporte técnico NO modelado - HU18: líneas de pedido de Productos cuyo comprobante se generó en el periodo.
 * Contrato de la iteración: pendiente (Oscar, HU18).
 */
public class VentaProductoDAO {

    /** Ventas de productos con Comprobante.fechaGeneracion en [desde, hasta). */
    public List<VentaProducto> buscarPorFechaDePago(Date desde, Date hasta) {
        return Collections.emptyList();
    }
}