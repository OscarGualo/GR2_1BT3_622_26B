package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.persistence.PersistenceException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.persistencia.JPAUtil;
import ec.edu.epn.polidinner.persistencia.PedidoDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

/**
 * Trazabilidad: Examen 1B, HU17 «Reprogramar la franja de retiro» (CA1, CA2, CA3).
 * Pedido base: PD-0101 de Andrea, Pagado para las 12:30–12:45.
 */
class ReprogramarPedidoTest extends BaseDatosPrueba {

    private Pedido pd0101;

    @BeforeEach
    void preparar() {
        prepararBase();
        pd0101 = programar(andrea, a(10, 0), franja(12, 30), arroz, 1, jugo, 2);
    }

    @AfterEach
    void limpiar() {
        cerrarBase();
    }

    private void reprogramar(Pedido pedido, String cedula, LocalTime destino, int hora, int minuto) {
        servicio.reprogramar(pedido.getId(), cedula, LUNES, destino, a(hora, minuto));
    }

    private String rechazo(Pedido pedido, String cedula, LocalTime destino, int hora, int minuto) {
        return assertThrows(ReglaPedidoException.class,
                () -> reprogramar(pedido, cedula, destino, hora, minuto)).getMessage();
    }

    @Test
    void hu17_esc_1_1_muevePedidoAOtraFranjaConElMismoCodigoYSinCambiarElSaldo() {
        asignar(franja(12, 30), 15);
        asignar(franja(13, 15), 5);
        String codigo = pedido(pd0101.getId()).getComprobante().getCodigo();

        reprogramar(pd0101, andrea.getCedula(), franja(13, 15), 11, 30);

        Pedido guardado = pedido(pd0101.getId());
        assertEquals(LocalTime.of(13, 15), guardado.getFranja().getHoraInicio());
        assertEquals(codigo, guardado.getComprobante().getCodigo());
        assertEquals("Pagado", guardado.getEstado());
        assertEquals(5.00, saldo("2024-0153"), 0.001);
        assertEquals(14, asignados(franja(12, 30)));
        assertEquals(6, asignados(franja(13, 15)));
    }

    @Test
    void hu17_esc_1_2_unaFranjaDestinoLlenaRechazaYNoCambiaNingunCupo() {
        asignar(franja(12, 30), 15);
        asignar(franja(13, 0), 15);

        assertEquals("La franja 13:00–13:15 ya no tiene cupo",
                rechazo(pd0101, andrea.getCedula(), franja(13, 0), 11, 30));

        assertEquals(LocalTime.of(12, 30), pedido(pd0101.getId()).getFranja().getHoraInicio());
        assertEquals(5.00, saldo("2024-0153"), 0.001);
        assertEquals(15, asignados(franja(12, 30)));
        assertEquals(15, asignados(franja(13, 0)));
    }

    @Test
    void hu17_esc_1_3_cancelarYReprogramarAlMismoTiempoSoloCompletaUna() throws Exception {
        asignar(franja(12, 30), 15);
        asignar(franja(13, 15), 5);
        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> cancelacion = hilos.submit(() -> {
                salida.await();
                try {
                    servicio.cancelar(pd0101.getId(), andrea.getCedula(), a(11, 30));
                    return true;
                } catch (ReglaPedidoException e) {
                    return false;
                }
            });
            Future<Boolean> reprogramacion = hilos.submit(() -> {
                salida.await();
                try {
                    reprogramar(pd0101, andrea.getCedula(), franja(13, 15), 11, 30);
                    return true;
                } catch (ReglaPedidoException e) {
                    return false;
                }
            });
            salida.countDown();

            // Cualquiera que gane, el resultado es consistente: ningún cupo queda duplicado o perdido
            assertTrue(cancelacion.get() || reprogramacion.get(), "Al menos una de las dos se completa");
            Pedido guardado = pedido(pd0101.getId());
            if (guardado.getEstado().equals("Cancelado")) {
                assertEquals(14, asignados(franja(12, 30)));
                assertEquals(5, asignados(franja(13, 15)));
                assertEquals(10.00, saldo("2024-0153"), 0.001);
            } else {
                assertEquals("Pagado", guardado.getEstado());
                assertEquals(14, asignados(franja(12, 30)));
                assertEquals(6, asignados(franja(13, 15)));
                assertEquals(5.00, saldo("2024-0153"), 0.001);
            }
        } finally {
            hilos.shutdownNow();
        }
    }

    @Test
    void hu17_esc_2_1_rechazaReprogramarConMenosDe30MinutosAntesDeLaFranjaOriginal() {
        assertEquals("Solo puedes reprogramar hasta 30 minutos antes del inicio de tu franja original",
                rechazo(pd0101, andrea.getCedula(), franja(13, 15), 12, 10));

        assertEquals(LocalTime.of(12, 30), pedido(pd0101.getId()).getFranja().getHoraInicio());
    }

    @Test
    void hu17_esc_2_2_rechazaUnDestinoQueYaComenzo() {
        Pedido pedido1330 = programar(luis, a(10, 0), franja(13, 30), seco, 1);

        assertEquals("La hora de retiro debe ser posterior a la hora actual",
                rechazo(pedido1330, luis.getCedula(), franja(11, 30), 11, 40));

        assertEquals(LocalTime.of(13, 30), pedido(pedido1330.getId()).getFranja().getHoraInicio());
    }

    @Test
    void hu17_esc_2_3_aceptaReprogramarJustoEnElLimite() {
        asignar(franja(13, 15), 5);

        reprogramar(pd0101, andrea.getCedula(), franja(13, 15), 12, 0);

        assertEquals(LocalTime.of(13, 15), pedido(pd0101.getId()).getFranja().getHoraInicio());
        assertEquals(6, asignados(franja(13, 15)));
    }

    @Test
    void hu17_esc_3_1_noReprogramaPedidosCanceladosNiEntregados() {
        Pedido pd0105 = programar(andrea, a(10, 0), franja(14, 45), jugo, 1);
        servicio.cancelar(pd0101.getId(), andrea.getCedula(), a(11, 0));
        var em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("UPDATE Pedido p SET p.estado = 'Entregado' WHERE p.id = :id")
                    .setParameter("id", pd0105.getId()).executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        int antes1330 = asignados(franja(13, 30));

        String esperado = "Solo se pueden reprogramar pedidos pagados pendientes de retiro";
        assertEquals(esperado, rechazo(pd0101, andrea.getCedula(), franja(13, 30), 11, 30));
        assertEquals(esperado, rechazo(pd0105, andrea.getCedula(), franja(13, 30), 11, 30));

        assertEquals(antes1330, asignados(franja(13, 30)));
    }

    @Test
    void hu17_esc_3_2_noReprogramaElPedidoDeOtroEstudiante() {
        Pedido pd0102 = programar(luis, a(10, 0), franja(13, 0), seco, 1);

        assertEquals("No se encontró ese pedido entre los suyos.",
                rechazo(pd0102, andrea.getCedula(), franja(13, 30), 11, 30));

        assertEquals(LocalTime.of(13, 0), pedido(pd0102.getId()).getFranja().getHoraInicio());
    }

    @Test
    void hu17_noPermiteElegirLaMismaFranja() {
        assertEquals("Elija una franja distinta de la que ya tiene el pedido",
                rechazo(pd0101, andrea.getCedula(), franja(12, 30), 11, 30));
        assertEquals(1, asignados(franja(12, 30)));
    }

    @Test
    void hu17_laFechaDeLaFranjaNuevaDebeSerHoy() {
        LocalDateTime ahora = a(11, 30);
        String mensaje = assertThrows(ReglaPedidoException.class, () -> servicio.reprogramar(
                pd0101.getId(), andrea.getCedula(), LUNES.plusDays(1), franja(13, 15), ahora)).getMessage();

        assertEquals("Solo puedes programar pedidos para hoy", mensaje);
    }

    @Test
    void hu17_esc_1_3_elControlDeVersionDelPedidoImpideQueLasDosOperacionesConfirmen() {
        asignar(franja(12, 30), 15);
        asignar(franja(13, 15), 5);
        PedidoDAO pedidoDAO = new PedidoDAO();
        try (Transaccion cancelacion = Transaccion.iniciar(); Transaccion traslado = Transaccion.iniciar()) {
            Pedido paraCancelar = pedidoDAO.buscarPorId(pd0101.getId(), cancelacion);
            Pedido paraMover = pedidoDAO.buscarPorId(pd0101.getId(), traslado);

            assertTrue(paraCancelar.cancelar());
            cancelacion.confirmar();

            paraMover.asignarFranja(franjas.buscar(LUNES, franja(13, 15), traslado));
            assertThrows(PersistenceException.class, traslado::confirmar);
        }
        // gana la cancelación: la reprogramación no deja rastro
        assertEquals("Cancelado", pedido(pd0101.getId()).getEstado());
        assertEquals(LocalTime.of(12, 30), pedido(pd0101.getId()).getFranja().getHoraInicio());
        assertEquals(5, asignados(franja(13, 15)));
    }
}
