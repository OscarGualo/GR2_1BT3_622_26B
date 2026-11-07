package ec.edu.epn.polidinner.persistencia;

import java.util.List;

import javax.persistence.LockModeType;

import ec.edu.epn.polidinner.modelo.OfertaMenu;

/**
 * Corrección de productos persistentes (Paquete 2) - acceso a las ofertas del día (OfertaMenu).
 */
public class OfertaMenuDAO extends GenericDAO<OfertaMenu, Integer> {

    public OfertaMenuDAO() {
        super(OfertaMenu.class);
    }

    /** Oferta del ítem en el menú indicado, leída dentro de la transacción, o null si no existe. */
    public OfertaMenu buscar(int idMenu, int idItem, Transaccion tx) {
        List<OfertaMenu> ofertas = tx.em().createQuery(
                        "SELECT o FROM OfertaMenu o WHERE o.menu.id = :menu AND o.item.id = :item", OfertaMenu.class)
                .setParameter("menu", idMenu)
                .setParameter("item", idItem)
                .getResultList();
        return ofertas.isEmpty() ? null : ofertas.get(0);
    }

    /**
     * Para el pago: vuelve a obtener la oferta en el mismo contexto de persistencia y transacción del pago y
     * pide LockModeType.OPTIMISTIC. Hibernate comprueba su versión antes del commit; si el personal la retiró
     * mientras tanto (el retiro incrementa esa versión), el commit falla y se deshace todo el pago.
     */
    public OfertaMenu buscarParaPago(int idMenu, int idItem, Transaccion tx) {
        OfertaMenu oferta = buscar(idMenu, idItem, tx);
        if (oferta != null) {
            tx.em().lock(oferta, LockModeType.OPTIMISTIC);
        }
        return oferta;
    }
}
