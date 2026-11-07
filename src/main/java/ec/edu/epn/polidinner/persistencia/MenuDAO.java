package ec.edu.epn.polidinner.persistencia;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;

import ec.edu.epn.polidinner.modelo.DiaComedor;
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
        return buscarMenuDelDia(DiaComedor.delSistema());
    }

    /** Corrección Paquete 1a: menú no cerrado del día del comedor indicado. */
    public Menu buscarMenuDelDia(DiaComedor hoy) {
        return buscarUno("SELECT m FROM Menu m WHERE m.fecha = :dia AND m.estado <> 'Cerrado' ORDER BY m.id DESC",
                hoy.comoFechaCivil());
    }

    /**
     * Corrección Paquete 2: menú no cerrado de la fecha, leído dentro de la transacción del pago, para
     * revalidar que la venta sigue abierta y que cada ítem pertenece a su oferta.
     */
    public Menu buscarMenuDelDia(LocalDate fecha, Transaccion tx) {
        List<Menu> menus = tx.em().createQuery(
                        "SELECT m FROM Menu m WHERE m.fecha = :dia AND m.estado <> 'Cerrado' ORDER BY m.id DESC",
                        Menu.class)
                .setParameter("dia", java.sql.Date.valueOf(fecha), TemporalType.DATE)
                .setMaxResults(1)
                .getResultList();
        return menus.isEmpty() ? null : menus.get(0);
    }

    /** Corrección Paquete 1a: menú "Cerrado" del día del comedor indicado. */
    public Menu buscarMenuCerrado(DiaComedor dia) {
        return buscarMenuCerrado(dia.comoFechaCivil());
    }

    /** El menú más reciente que cumple la consulta para una fecha civil (columna DATE), o null. */
    private Menu buscarUno(String jpql, Date dia) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Menu> menus = em.createQuery(jpql, Menu.class)
                    .setParameter("dia", dia, TemporalType.DATE)
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
        return buscarUno("SELECT m FROM Menu m WHERE m.fecha = :dia AND m.estado = 'Cerrado' ORDER BY m.id DESC", dia);
    }
}
