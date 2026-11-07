package ec.edu.epn.polidinner.persistencia;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import ec.edu.epn.polidinner.modelo.Menu;
import ec.edu.epn.polidinner.modelo.Producto;

/**
 * Corrección de productos persistentes (Paquete 2) - existencias de productos, independientes del menú.
 */
public class ProductoDAO extends GenericDAO<Producto, Integer> {

    /** Filtros de la pantalla Productos; VIGENTES es el inicial. */
    public enum Filtro { VIGENTES, AGOTADOS, CADUCADOS, HISTORICOS }

    public ProductoDAO() {
        super(Producto.class);
    }

    /** Productos del filtro indicado, los más recientes primero. */
    public List<Producto> listar(Filtro filtro, LocalDate hoy) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto producto : listarTodosRecientes()) {
            if (cumple(producto, filtro, hoy)) {
                resultado.add(producto);
            }
        }
        return resultado;
    }

    /**
     * Existencias que el personal puede agregar al menú: habilitadas, vigentes, con stock y que ese menú
     * todavía no ofrece.
     */
    public List<Producto> listarSeleccionables(LocalDate hoy, Menu menu) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto producto : listar(Filtro.VIGENTES, hoy)) {
            if (menu == null || !menu.perteneceAlMenu(producto.getId())) {
                resultado.add(producto);
            }
        }
        return resultado;
    }

    /**
     * Regla de cada filtro (separada de la consulta para probarla sin base de datos).
     * Vigentes: habilitadas, sin caducar y con stock. Agotados: habilitadas sin unidades.
     * Caducados: habilitadas con la fecha vencida. Históricos: de menús anteriores a esta corrección.
     */
    static boolean cumple(Producto producto, Filtro filtro, LocalDate hoy) {
        if (filtro == Filtro.HISTORICOS) {
            return !producto.isHabilitado();
        }
        if (!producto.isHabilitado()) {
            return false;
        }
        boolean vigente = producto.estaVigente(hoy);
        switch (filtro) {
            case AGOTADOS:
                return vigente && producto.getStockActual() == 0;
            case CADUCADOS:
                return !vigente;
            default:
                return vigente && producto.hayStock(1);
        }
    }

    /** Producto por id dentro de una transacción (null si el id no es de un Producto). */
    public Producto buscarProducto(int id, Transaccion tx) {
        return tx.em().find(Producto.class, id);
    }

    private List<Producto> listarTodosRecientes() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Producto p ORDER BY p.id DESC", Producto.class).getResultList();
        } finally {
            em.close();
        }
    }
}
