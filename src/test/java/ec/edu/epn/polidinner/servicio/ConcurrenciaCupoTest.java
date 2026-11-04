package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.ClienteUniversitario;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.persistencia.FranjaRetiroDAO;
import ec.edu.epn.polidinner.persistencia.PedidoDAO;

/**
 * Trazabilidad: Examen 1B, HU16 CA3 «Asignar el último cupo a un solo cliente» (T16.3 Probar la
 * concurrencia del último cupo). Los dos clientes usan cuentas y productos distintos, así que la única
 * fila compartida es la de la franja: si el cupo se controlara con «leer y luego escribir», los dos pasarían.
 */
class ConcurrenciaCupoTest extends BaseDatosPrueba {

    @BeforeEach
    void preparar() {
        prepararBase();
    }

    @AfterEach
    void limpiar() {
        cerrarBase();
    }

    @Test
    void hu16_esc_3_1_dosClientesPidenElUltimoLugarAlMismoTiempoSoloUnoLoObtiene() throws Exception {
        asignar(franja(12, 30), 14);
        Pedido deAndrea = pedidoDe(andrea, arroz, 1);
        Pedido deLuis = pedidoDe(luis, seco, 1);

        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        List<Future<Boolean>> resultados = new ArrayList<>();
        try {
            for (Pedido pedido : List.of(deAndrea, deLuis)) {
                Callable<Boolean> pagar = () -> {
                    salida.await();
                    try {
                        servicio.programarYPagar(pedido, LUNES, franja(12, 30), a(10, 0));
                        return true;
                    } catch (ReglaPedidoException e) {
                        return false;
                    }
                };
                resultados.add(hilos.submit(pagar));
            }
            salida.countDown();

            int pagados = 0;
            for (Future<Boolean> resultado : resultados) {
                pagados += resultado.get() ? 1 : 0;
            }
            assertEquals(1, pagados, "Solo uno de los dos pedidos pasa a Pagado");
        } finally {
            hilos.shutdownNow();
        }

        assertEquals(15, asignados(franja(12, 30)));
        // quien perdió conserva su saldo y sus existencias
        boolean andreaPago = saldo("2024-0153") < 10.00;
        boolean luisPago = saldo("2024-0201") < 8.00;
        assertEquals(1, (andreaPago ? 1 : 0) + (luisPago ? 1 : 0));
        assertEquals(andreaPago ? 19 : 20, stock(arroz));
        assertEquals(luisPago ? 19 : 20, stock(seco));
    }

    @Test
    void hu16_esc_3_1_conVariosClientesNuncaSeSuperaElCupo() throws Exception {
        asignar(franja(12, 30), 12);
        List<Pedido> pedidos = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            ClienteUniversitario cliente = crearCliente("17600000" + i, "Cliente" + i, "Prueba", "2024-70" + i, 20.00);
            pedidos.add(pedidoDe(cliente, jugo, 1));
        }

        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(6);
        List<Future<Boolean>> resultados = new ArrayList<>();
        try {
            for (Pedido pedido : pedidos) {
                resultados.add(hilos.submit(() -> {
                    salida.await();
                    try {
                        servicio.programarYPagar(pedido, LUNES, franja(12, 30), a(10, 0));
                        return true;
                    } catch (ReglaPedidoException e) {
                        return false;
                    }
                }));
            }
            salida.countDown();
            int pagados = 0;
            for (Future<Boolean> resultado : resultados) {
                pagados += resultado.get() ? 1 : 0;
            }
            assertEquals(3, pagados, "Quedaban 3 lugares de 15");
        } finally {
            hilos.shutdownNow();
        }
        assertEquals(15, asignados(franja(12, 30)));
    }

    @Test
    void hu16_esc_3_2_siElUltimoLugarSeOcupaDuranteLaConfirmacionSeRechazaSinCobrar() {
        asignar(franja(12, 30), 14);
        programar(andrea, a(10, 0), franja(12, 30), arroz, 1);
        // La verificación previa de Luis todavía veía un lugar; su reserva atómica llega tarde
        FranjaRetiroDAO franjasQueSeLlenan = spy(new FranjaRetiroDAO());
        doReturn(false).when(franjasQueSeLlenan).reservar(anyInt(), anyInt(), any());
        asignar(franja(12, 30), 14);
        ServicioPedidos servicioDeLuis = new ServicioPedidos(new PedidoDAO(), franjasQueSeLlenan);
        Pedido deLuis = pedidoDe(luis, seco, 1);

        String mensaje = assertThrows(ReglaPedidoException.class,
                () -> servicioDeLuis.programarYPagar(deLuis, LUNES, franja(12, 30), a(10, 1))).getMessage();

        assertEquals("La franja se llenó mientras confirmabas", mensaje);
        assertEquals(8.00, saldo("2024-0201"), 0.001);
        assertEquals(20, stock(seco));
        assertEquals("Creado", deLuis.getEstado());
        // Luis puede elegir otra franja
        servicio.programarYPagar(deLuis, LUNES, franja(12, 45), a(10, 2));
        assertEquals("Pagado", pedido(deLuis.getId()).getEstado());
    }
}
