package ec.edu.epn.polidinner.persistencia;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;

import ec.edu.epn.polidinner.modelo.VentaProducto;

/**
 * Soporte técnico NO modelado - HU18: líneas de pedido de Productos cuyo comprobante se generó en el periodo.
 * La fecha de pago es Comprobante.fechaGeneracion (se crea en Pedido.confirmarPago()); los Platos no se
 * incluyen porque solo se cruzan los ItemMenu que son Producto. El estado del pedido se filtra después.
 */
public class VentaProductoDAO {

    private static final String VENTAS_POR_FECHA_DE_PAGO =
            "SELECT pr.id, pr.nombre, pr.fechaCaducidad, d.cantidad, p.estado, c.fechaGeneracion "
                    + "FROM Pedido p JOIN p.detalles d JOIN p.comprobante c, Producto pr "
                    + "WHERE pr.id = d.item.id AND c.fechaGeneracion >= :desde AND c.fechaGeneracion < :hasta "
                    + "ORDER BY p.id, d.id";

    /** Ventas de productos con Comprobante.fechaGeneracion en [desde, hasta). */
    public List<VentaProducto> buscarPorFechaDePago(Date desde, Date hasta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Object[]> filas = em.createQuery(VENTAS_POR_FECHA_DE_PAGO, Object[].class)
                    .setParameter("desde", desde, TemporalType.TIMESTAMP)
                    .setParameter("hasta", hasta, TemporalType.TIMESTAMP)
                    .getResultList();
            return filas.stream()
                    .map(f -> new VentaProducto((Integer) f[0], (String) f[1], (Date) f[2], (Integer) f[3],
                            (String) f[4], (Date) f[5]))
                    .toList();
        } finally {
            em.close();
        }
    }
}
