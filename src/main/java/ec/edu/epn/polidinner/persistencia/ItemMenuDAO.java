package ec.edu.epn.polidinner.persistencia;

import ec.edu.epn.polidinner.modelo.ItemMenu;

/**
 * Soporte técnico de acceso a datos para ItemMenu (Plato y Producto).
 * Trazabilidad: Diagrama de secuencia CU02 - participante ItemMenuDAO
 * (mensajes 10 buscarPorId(idItem) y 16 actualizar(item), heredados de GenericDAO).
 */
public class ItemMenuDAO extends GenericDAO<ItemMenu, Integer> {

    public ItemMenuDAO() {
        super(ItemMenu.class);
    }
}
