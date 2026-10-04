package ec.edu.epn.polidinner.persistencia;

import javax.persistence.EntityManager;

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
}
