package ec.edu.epn.polidinner.persistencia;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;

import ec.edu.epn.polidinner.modelo.Menu;

/**
 * Trazabilidad: Diagrama de secuencia CU01 - participante MenuDAO
 * (mensajes 7 guardar(menuHoy), 17 y 23 actualizar(menuHoy), heredados de GenericDAO).
 * Diagrama de secuencia CU02 - mensajes 3 y 4: buscarMenuDelDia() / menuHoy.
 */
public class MenuDAO extends GenericDAO<Menu, Integer> {

    public MenuDAO() {
        super(Menu.class);
    }

    /**
     * Menú de la fecha actual que no esté "Cerrado" (el más reciente si hubiera varios),
     * o null si todavía no se ha creado.
     */
    public Menu buscarMenuDelDia() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Menu> menus = em.createQuery(
                            "SELECT m FROM Menu m WHERE m.fecha = :hoy AND m.estado <> 'Cerrado' ORDER BY m.id DESC",
                            Menu.class)
                    .setParameter("hoy", new Date(), TemporalType.DATE)
                    .setMaxResults(1)
                    .getResultList();
            return menus.isEmpty() ? null : menus.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU05 - soporte técnico del cierre de la venta.
     * Menú "Cerrado" de la fecha indicada, o null si ese día la venta no se cerró.
     * Lo usan el cierre (para no cerrar dos veces), el reporte (venta cerrada o abierta)
     * y la creación del menú (para no reabrir la venta de un día ya cerrado).
     */
    public Menu buscarMenuCerrado(Date dia) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Menu> menus = em.createQuery(
                            "SELECT m FROM Menu m WHERE m.fecha = :dia AND m.estado = 'Cerrado' ORDER BY m.id DESC",
                            Menu.class)
                    .setParameter("dia", dia, TemporalType.DATE)
                    .setMaxResults(1)
                    .getResultList();
            return menus.isEmpty() ? null : menus.get(0);
        } finally {
            em.close();
        }
    }
}
