package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.persistencia.JPAUtil;
import ec.edu.epn.polidinner.persistencia.PedidoDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

/**
 * Trazabilidad: Examen 1B, HU12 «Cancelar pedido programado con reembolso» (CA1, CA2, CA3) y
 * HU16 CA4 (liberar el cupo al cancelar, adaptación de HU12 en la Iteración 2).
 * Pedido base de los escenarios: 1 «Arroz con pollo» y 2 «Jugo de naranja» = $5.00, franja 12:30–12:45.
 */
class CancelarProgramadoTest extends BaseDatosPrueba {

    private Pedido pedido0101;

    @BeforeEach
    void preparar() {
        prepararBase();
        pedido0101 = programar(andrea, a(10, 0), franja(12, 30), arroz, 1, jugo, 2);
    }

    @AfterEach
    void limpiar() {
        cerrarBase();
    }

    private String rechazo(int idPedido, String cedula, int hora, int minuto) {
        return assertThrows(ReglaPedidoException.class,
                () -> servicio.cancelar(idPedido, cedula, a(hora, minuto))).getMessage();
    }

    @Test
    void hu12_esc_1_1_cancelaConAnticipacionReembolsaYRestituyeExistencias() {
        assertEquals(5.00, saldo("2024-0153"), 0.001);

        servicio.cancelar(pedido0101.getId(), andrea.getCedula(), a(11, 30));

        Pedido guardado = pedido(pedido0101.getId());
        assertEquals("Cancelado", guardado.getEstado());
        assertEquals(10.00, saldo("2024-0153"), 0.001);
        assertEquals(20, stock(arroz));
        assertEquals(30, stock(jugo));
        assertTrue(guardado.getComprobante().isUsado(), "El código PD-0101 queda anulado");
    }

    @Test
    void hu12_esc_1_2_aceptaCancelarJustoEnElLimite() {
        servicio.cancelar(pedido0101.getId(), andrea.getCedula(), a(12, 0));

        assertEquals("Cancelado", pedido(pedido0101.getId()).getEstado());
        assertEquals(10.00, saldo("2024-0153"), 0.001);
    }

    @Test
    void hu12_esc_2_1_rechazaCancelarConMenosDe30Minutos() {
        assertEquals("Solo puedes cancelar hasta 30 minutos antes de la hora de retiro",
                rechazo(pedido0101.getId(), andrea.getCedula(), 12, 10));

        assertEquals("Pagado", pedido(pedido0101.getId()).getEstado());
        assertEquals(5.00, saldo("2024-0153"), 0.001);
    }

    @Test
    void hu12_esc_2_2_rechazaCancelarUnPedidoYaEntregado() {
        marcarEntregado(pedido0101.getId());

        assertEquals("Solo se puede cancelar un pedido pagado que aún no se entregó.",
                rechazo(pedido0101.getId(), andrea.getCedula(), 11, 30));

        assertEquals(5.00, saldo("2024-0153"), 0.001);
    }

    @Test
    void hu12_esc_2_3_laRutaDeCancelacionDelRelease1TambienAplicaElPlazo() {
        // /cliente/cancelar llama a este mismo servicio: no existe otra ruta que omita el plazo
        assertEquals("Solo puedes cancelar hasta 30 minutos antes de la hora de retiro",
                rechazo(pedido0101.getId(), andrea.getCedula(), 12, 10));
        assertEquals("Pagado", pedido(pedido0101.getId()).getEstado());
    }

    @Test
    void hu12_esc_2_4_noPermiteCancelarElPedidoDeOtroEstudiante() {
        Pedido pd0102 = programar(luis, a(10, 0), franja(13, 0), seco, 1);
        double saldoLuis = saldo("2024-0201");

        assertEquals("No se encontró ese pedido entre los suyos.",
                rechazo(pd0102.getId(), andrea.getCedula(), 11, 30));

        assertEquals("Pagado", pedido(pd0102.getId()).getEstado());
        assertEquals(saldoLuis, saldo("2024-0201"), 0.001);
        assertEquals(19, stock(seco));
    }

    @Test
    void hu12_esc_3_1_confirmarDosVecesSeguidasReembolsaUnaSolaVez() {
        servicio.cancelar(pedido0101.getId(), andrea.getCedula(), a(11, 30));

        assertEquals("El pedido ya fue cancelado", rechazo(pedido0101.getId(), andrea.getCedula(), 11, 30));

        assertEquals(10.00, saldo("2024-0153"), 0.001);
        assertEquals(20, stock(arroz));
        assertEquals(30, stock(jugo));
    }

    @Test
    void hu12_esc_3_1_dosConfirmacionesSimultaneasReembolsanUnaSolaVez() throws Exception {
        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> cancelacion = () -> {
                salida.await();
                try {
                    servicio.cancelar(pedido0101.getId(), andrea.getCedula(), a(11, 30));
                    return true;
                } catch (ReglaPedidoException e) {
                    return false;
                }
            };
            Future<Boolean> primera = hilos.submit(cancelacion);
            Future<Boolean> segunda = hilos.submit(cancelacion);
            salida.countDown();

            int exitosas = (primera.get() ? 1 : 0) + (segunda.get() ? 1 : 0);
            assertEquals(1, exitosas, "Solo una de las dos confirmaciones puede cancelar");
        } finally {
            hilos.shutdownNow();
        }
        assertEquals(10.00, saldo("2024-0153"), 0.001);
        assertEquals(20, stock(arroz));
        assertEquals(0, asignados(franja(12, 30)));
    }

    @Test
    void hu12_esc_3_2_cancelarUnPedidoYaCanceladoInformaQueYaFueCancelado() {
        servicio.cancelar(pedido0101.getId(), andrea.getCedula(), a(11, 30));

        assertEquals("El pedido ya fue cancelado", rechazo(pedido0101.getId(), andrea.getCedula(), 11, 31));
        assertEquals(10.00, saldo("2024-0153"), 0.001);
    }

    @Test
    void hu16_esc_4_1_cancelarEnUnaFranjaLlenaDejaUnLugarParaOtroCliente() {
        asignar(franja(12, 30), 15);

        servicio.cancelar(pedido0101.getId(), andrea.getCedula(), a(11, 30));

        assertEquals(14, asignados(franja(12, 30)));
        servicio.programarYPagar(pedidoDe(luis, seco, 1), LUNES, franja(12, 30), a(11, 31));
        assertEquals(15, asignados(franja(12, 30)));
    }

    @Test
    void hu16_esc_4_2_unaCancelacionRechazadaNoLiberaCupo() {
        asignar(franja(12, 30), 15);

        rechazo(pedido0101.getId(), andrea.getCedula(), 12, 10);

        assertEquals(15, asignados(franja(12, 30)));
    }

    @Test
    void release1_unPedidoInmediatoSeCancelaSinPlazoNiFranja() {
        Pedido inmediato = pedidoDe(andrea, jugo, 1);
        guardarPagadoSinFranja(inmediato);

        servicio.cancelar(inmediato.getId(), andrea.getCedula(), a(12, 59));

        assertEquals("Cancelado", pedido(inmediato.getId()).getEstado());
    }

    /** Compra inmediata del Release 1: confirmarPago() sin franja, guardada con su comprobante. */
    private void guardarPagadoSinFranja(Pedido pedido) {
        PedidoDAO dao = new PedidoDAO();
        try (Transaccion tx = Transaccion.iniciar()) {
            dao.adjuntar(pedido, tx);
            assertTrue(pedido.confirmarPago());
            dao.guardarConComprobante(pedido, tx);
            tx.confirmar();
        }
    }

    private void marcarEntregado(int idPedido) {
        var em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("UPDATE Pedido p SET p.estado = 'Entregado' WHERE p.id = :id")
                    .setParameter("id", idPedido).executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
