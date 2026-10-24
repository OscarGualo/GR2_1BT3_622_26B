package ec.edu.epn.polidinner.persistencia;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import ec.edu.epn.polidinner.modelo.DetallePedido;
import ec.edu.epn.polidinner.modelo.ItemMenu;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.modelo.Policuenta;

/**
 * Trazabilidad: Diagrama de secuencia CU03 - participante PedidoDAO
 * (mensajes 34 adjuntar(pedido) y 43 guardar(pedido), este último heredado de GenericDAO).
 */
public class PedidoDAO extends GenericDAO<Pedido, Integer> {

    public PedidoDAO() {
        super(Pedido.class);
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU03 - mensaje 34 adjuntar(pedido).
     * Soporte técnico: el pedido se armó en la sesión con copias de los ítems y de la PoliCuenta.
     * Antes de confirmarPago() se reemplazan por su versión actual, gestionada por la transacción,
     * para validar stock y saldo reales y que los cambios se guarden con bloqueo optimista (@Version).
     */
    public void adjuntar(Pedido pedido, Transaccion tx) {
        EntityManager em = tx.em();
        for (DetallePedido detalle : pedido.getDetalles()) {
            ItemMenu actual = em.find(ItemMenu.class, detalle.getItem().getId());
            if (actual == null) {
                throw new IllegalStateException("\"" + detalle.getItem().getNombre() + "\" ya no existe en el menú");
            }
            detalle.reemplazarItem(actual);
        }
        pedido.reemplazarPolicuenta(em.find(Policuenta.class, pedido.getPolicuenta().getNumero()));
    }

    /**
     * Trazabilidad: Secuencia "Realizar Pedido y Pagar" - guarda el Pedido y el Comprobante (CodigoEntrega)
     * que creó confirmarPago(). Primero el pedido, para que la base de datos le asigne el id; luego el comprobante,
     * cuyo @PrePersist arma el código PD-0000 con ese id. Un solo flush al final.
     */
    public void guardarConComprobante(Pedido pedido, Transaccion tx) {
        EntityManager em = tx.em();
        em.persist(pedido);
        em.persist(pedido.getComprobante());
        em.flush();
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU06 - mensajes 3-4: buscarPorCliente(cedula) / lista de pedidos.
     * Devuelve los pedidos del cliente, del más reciente al más antiguo, para la vista "Mis pedidos".
     */
    public List<Pedido> buscarPorCliente(String cedula) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Pedido p WHERE p.cliente.cedula = :cedula ORDER BY p.fecha DESC",
                            Pedido.class)
                    .setParameter("cedula", cedula)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Trazabilidad: Diagrama de robustez CU05 - consulta de los pedidos del día para el reporte de ventas
     * (ReporteVentas(fecha, pedidos) del diagrama de clases Inc. 3).
     * Pedido.fecha guarda fecha y hora, por eso se busca en el rango [00:00 del día, 00:00 del día siguiente).
     * Orden ascendente por id, que es el orden en que se pagaron.
     */
    public List<Pedido> buscarPorFecha(Date dia) {
        Calendar inicio = Calendar.getInstance();
        inicio.setTime(dia);
        inicio.set(Calendar.HOUR_OF_DAY, 0);
        inicio.set(Calendar.MINUTE, 0);
        inicio.set(Calendar.SECOND, 0);
        inicio.set(Calendar.MILLISECOND, 0);
        Calendar fin = (Calendar) inicio.clone();
        fin.add(Calendar.DAY_OF_MONTH, 1);

        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Pedido p WHERE p.fecha >= :inicio AND p.fecha < :fin ORDER BY p.id",
                            Pedido.class)
                    .setParameter("inicio", inicio.getTime(), TemporalType.TIMESTAMP)
                    .setParameter("fin", fin.getTime(), TemporalType.TIMESTAMP)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
