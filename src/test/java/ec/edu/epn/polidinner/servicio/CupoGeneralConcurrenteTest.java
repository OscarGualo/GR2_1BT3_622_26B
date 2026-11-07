package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.persistencia.FranjaRetiroDAO;
import ec.edu.epn.polidinner.persistencia.PedidoDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

/** HU15/HU16: cambiar el cupo general y reservar deben respetar el mismo límite. */
class CupoGeneralConcurrenteTest extends BaseDatosPrueba {
    @BeforeEach
    void preparar() {
        prepararBase();
        asignar(franja(12, 30), 14);
    }

    @AfterEach
    void limpiar() {
        cerrarBase();
    }

    @Test
    void reservaPrimeroImpideReducirElCupoPorDebajoDeLosAsignados() throws Exception {
        CountDownLatch cupoLeido = new CountDownLatch(1);
        CountDownLatch continuarReserva = new CountDownLatch(1);
        CountDownLatch cambioIniciado = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        FranjaRetiroDAO dao = spy(new FranjaRetiroDAO());
        doAnswer(invocacion -> {
            int cupo = (Integer) invocacion.callRealMethod();
            cupoLeido.countDown();
            assertTrue(continuarReserva.await(5, TimeUnit.SECONDS));
            return cupo;
        }).when(dao).cupoGeneral(any(Transaccion.class));
        Pedido pedido = pedidoDe(andrea, arroz, 1);
        try {
            Future<?> reserva = hilos.submit(() -> new ServicioPedidos(new PedidoDAO(), dao)
                    .programarYPagar(pedido, LUNES, franja(12, 30), a(10, 0)));
            assertTrue(cupoLeido.await(5, TimeUnit.SECONDS));
            Future<?> cambio = hilos.submit(() -> {
                cambioIniciado.countDown();
                new ServicioFranjas().configurarCupoGeneral(14);
            });
            assertTrue(cambioIniciado.await(5, TimeUnit.SECONDS));
            // El cambio debe esperar a que la reserva confirme o revierta su transacción.
            assertThrows(TimeoutException.class, () -> cambio.get(500, TimeUnit.MILLISECONDS));
            continuarReserva.countDown();
            reserva.get(5, TimeUnit.SECONDS);
            ExecutionException rechazo = assertThrows(ExecutionException.class,
                    () -> cambio.get(5, TimeUnit.SECONDS));
            assertInstanceOf(ReglaPedidoException.class, rechazo.getCause());
            assertEquals(15, franjas.cupoGeneral());
            assertEquals(15, asignados(franja(12, 30)));
            assertEquals("Pagado", pedido(pedido.getId()).getEstado());
            assertEquals(6.50, saldo("2024-0153"), 0.001);
            assertEquals(19, stock(arroz));
        } finally {
            continuarReserva.countDown();
            hilos.shutdownNow();
            assertTrue(hilos.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void reduccionPrimeroRechazaLaReservaSinCobrarNiDescontarStock() throws Exception {
        ExecutorService hilo = Executors.newSingleThreadExecutor();
        CountDownLatch reservaIniciada = new CountDownLatch(1);
        Pedido pedido = pedidoDe(andrea, arroz, 1);
        try {
            Future<?> reserva;
            try (Transaccion tx = Transaccion.iniciar()) {
                assertTrue(franjas.fijarCupoGeneral(14, tx));
                reserva = hilo.submit(() -> {
                    reservaIniciada.countDown();
                    servicio.programarYPagar(pedido, LUNES, franja(12, 30), a(10, 0));
                });
                assertTrue(reservaIniciada.await(5, TimeUnit.SECONDS));
                assertThrows(TimeoutException.class, () -> reserva.get(500, TimeUnit.MILLISECONDS));
                tx.confirmar();
            }
            ExecutionException rechazo = assertThrows(ExecutionException.class,
                    () -> reserva.get(5, TimeUnit.SECONDS));
            assertInstanceOf(ReglaPedidoException.class, rechazo.getCause());
            assertEquals(14, franjas.cupoGeneral());
            assertEquals(14, asignados(franja(12, 30)));
            assertEquals("Creado", pedido.getEstado());
            assertEquals(10.00, saldo("2024-0153"), 0.001);
            assertEquals(20, stock(arroz));
        } finally {
            hilo.shutdownNow();
            assertTrue(hilo.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
