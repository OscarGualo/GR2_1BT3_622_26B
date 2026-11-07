package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.Comprobante;
import ec.edu.epn.polidinner.modelo.ItemMenu;
import ec.edu.epn.polidinner.modelo.OfertaMenu;
import ec.edu.epn.polidinner.persistencia.JPAUtil;
import ec.edu.epn.polidinner.persistencia.OfertaMenuDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

/**
 * Corrección de productos persistentes, Paquete 2 (V26) con Hibernate y H2 reales: política transaccional
 * entre retirar la oferta del día y pagar. El pago vuelve a obtener la oferta en su transacción y pide
 * LockModeType.OPTIMISTIC; el retiro actualiza esa misma fila versionada.
 * <ul>
 *   <li>Si el retiro confirma mientras el pago está en curso (después de su validación), el pago falla al
 *       confirmar y no deja efectos parciales.</li>
 *   <li>Si el retiro confirma antes de la validación, el pago se rechaza con el motivo.</li>
 *   <li>Si el pago confirma primero, la venta queda válida y el retiro impide las ventas siguientes.</li>
 * </ul>
 */
class RetiroConcurrentePagoTest extends BaseDatosPrueba {

    private final OfertaMenuDAO ofertas = new OfertaMenuDAO();

    @BeforeEach
    void preparar() {
        prepararBase();
    }

    @AfterEach
    void cerrar() {
        cerrarBase();
    }

    /** El retiro confirma entre la validación protegida del pago y su commit: el pago se deshace completo. */
    @Test
    void unRetiroQueConfirmaDuranteElPagoLoHaceFallarSinEfectos() {
        try (Transaccion pago = Transaccion.iniciar()) {
            OfertaMenu oferta = ofertas.buscarParaPago(menuDelLunes.getId(), jugo.getId(), pago);
            assertTrue(oferta.estaOfrecida());
            ItemMenu itemDelPago = oferta.getItem();
            itemDelPago.reducirStock(1);

            retirarEnOtraTransaccion();

            assertThrows(RuntimeException.class, pago::confirmar);
        }
        assertEquals(30, stock(jugo));
        assertFalse(ofertaGuardada().estaOfrecida());
    }

    /** Sin el bloqueo optimista (solo leer la oferta) el mismo intercalado SÍ confirmaría: el bloqueo es necesario. */
    @Test
    void leerLaOfertaSinBloquearlaNoDetectaElRetiro() {
        try (Transaccion pago = Transaccion.iniciar()) {
            OfertaMenu oferta = ofertas.buscar(menuDelLunes.getId(), jugo.getId(), pago);
            oferta.getItem().reducirStock(1);

            retirarEnOtraTransaccion();

            pago.confirmar();
        }
        assertEquals(29, stock(jugo));
    }

    /** El retiro confirmó antes de que el pago validara: el servicio rechaza el cobro sin débito ni stock. */
    @Test
    void unRetiroAnteriorALaValidacionRechazaElPago() {
        retirarEnOtraTransaccion();

        assertThrows(ReglaPedidoException.class, () -> servicio.pagarAhora(pedidoDe(andrea, jugo, 1), LUNES));
        assertEquals(30, stock(jugo));
        assertEquals(10.00, saldo("2024-0153"), 0.001);
    }

    /** El pago confirmó primero: la venta ya hecha es válida y el retiro posterior impide las siguientes. */
    @Test
    void unPagoQueConfirmaAntesDelRetiroQuedaValido() {
        Comprobante comprobante = servicio.pagarAhora(pedidoDe(andrea, jugo, 2), LUNES);

        retirarEnOtraTransaccion();

        assertEquals("Pagado", pedido(comprobante.getPedido().getId()).getEstado());
        assertEquals(28, stock(jugo));
        assertThrows(ReglaPedidoException.class, () -> servicio.pagarAhora(pedidoDe(luis, jugo, 1), LUNES));
        assertEquals(28, stock(jugo));
    }

    private void retirarEnOtraTransaccion() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("SELECT o FROM OfertaMenu o WHERE o.menu.id = :m AND o.item.id = :i", OfertaMenu.class)
                    .setParameter("m", menuDelLunes.getId()).setParameter("i", jugo.getId())
                    .getSingleResult().retirar();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    private OfertaMenu ofertaGuardada() {
        try (Transaccion tx = Transaccion.iniciar()) {
            return ofertas.buscar(menuDelLunes.getId(), jugo.getId(), tx);
        }
    }
}
