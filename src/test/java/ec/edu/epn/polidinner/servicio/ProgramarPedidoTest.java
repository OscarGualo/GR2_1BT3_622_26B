package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.Pedido;

/**
 * Trazabilidad: Examen 1B, HU11 «Programar pedido para llevar con hora de retiro» (CA1, CA2, CA3)
 * y HU16 CA2 (programar solo en franjas con cupo). Usa H2 en memoria.
 */
class ProgramarPedidoTest extends BaseDatosPrueba {

    @BeforeEach
    void preparar() {
        prepararBase();
    }

    @AfterEach
    void limpiar() {
        cerrarBase();
    }

    private String rechazo(Pedido pedido, int hora, int minuto, LocalTime franja) {
        return assertThrows(ReglaPedidoException.class,
                () -> servicio.programarYPagar(pedido, LUNES, franja, a(hora, minuto))).getMessage();
    }

    @Test
    void hu11_esc_1_1_programaUnPedidoParaUnaFranjaFutura() {
        // Andrea: $10.00, pedido de $5.00 (1 Arroz + 2 Jugo = 3.50 + 1.50), son las 10:00
        Pedido pedido = pedidoDe(andrea, arroz, 1, jugo, 2);

        var comprobante = servicio.programarYPagar(pedido, LUNES, franja(12, 30), a(10, 0));

        Pedido guardado = pedido(pedido.getId());
        assertEquals("Pagado", guardado.getEstado());
        assertEquals(LocalTime.of(12, 30), guardado.getFranja().getHoraInicio());
        assertEquals(5.00, saldo("2024-0153"), 0.001);
        assertEquals(19, stock(arroz));
        assertEquals(28, stock(jugo));
        assertEquals(String.format("PD-%04d", pedido.getId()), comprobante.getCodigo());
        assertEquals(1, asignados(franja(12, 30)));
    }

    @Test
    void hu11_esc_2_1_conSaldoInsuficienteNoProgramaNiCobraNiOcupaCupo() {
        andrea = crearCliente("1750000009", "Ana", "Prueba", "2024-0999", 4.00);
        Pedido pedido = pedidoDe(andrea, arroz, 1, jugo, 2);

        assertEquals("Saldo insuficiente en PoliCuenta", rechazo(pedido, 10, 0, franja(12, 30)));

        assertEquals(4.00, saldo("2024-0999"), 0.001);
        assertEquals("Creado", pedido.getEstado());
        assertNull(pedido.getFranja());
        assertEquals(20, stock(arroz));
        assertEquals(0, asignados(franja(12, 30)));
    }

    @Test
    void hu11_esc_2_2_conSaldoExactoPaga() {
        andrea = crearCliente("1750000010", "Ana", "Exacta", "2024-0998", 5.00);
        Pedido pedido = pedidoDe(andrea, arroz, 1, jugo, 2);

        servicio.programarYPagar(pedido, LUNES, franja(13, 0), a(10, 0));

        assertEquals("Pagado", pedido(pedido.getId()).getEstado());
        assertEquals(0.00, saldo("2024-0998"), 0.001);
    }

    @Test
    void hu11_esc_3_1_rechazaUnaFranjaQueYaComenzoSinDescontarSaldo() {
        Pedido pedido = pedidoDe(andrea, arroz, 1, jugo, 2);

        assertEquals("La hora de retiro debe ser posterior a la hora actual",
                rechazo(pedido, 12, 35, franja(12, 30)));

        assertEquals(10.00, saldo("2024-0153"), 0.001);
    }

    @Test
    void hu11_esc_3_2_rechazaUnaHoraDesalineada() {
        Pedido pedido = pedidoDe(andrea, arroz, 1);

        assertEquals("La hora de retiro debe ser el inicio de una franja de 15 minutos",
                rechazo(pedido, 10, 0, LocalTime.of(12, 37)));

        assertEquals(10.00, saldo("2024-0153"), 0.001);
    }

    @Test
    void hu11_esc_3_3_rechazaUnaFranjaFueraDelHorario() {
        Pedido pedido = pedidoDe(andrea, arroz, 1);

        assertEquals("La hora de retiro está fuera del horario del comedor",
                rechazo(pedido, 10, 0, LocalTime.of(15, 0)));

        assertEquals(10.00, saldo("2024-0153"), 0.001);
    }

    @Test
    void hu11_esc_3_4_sinElegirFranjaPideElegirUnaYNoCobra() {
        Pedido pedido = pedidoDe(andrea, arroz, 1);

        assertEquals("Elija una franja de retiro para programar su pedido", rechazo(pedido, 10, 0, null));

        assertEquals("Creado", pedido.getEstado());
        assertEquals(10.00, saldo("2024-0153"), 0.001);
    }

    @Test
    void hu11_esc_3_5_rechazaUnaFranjaDeOtroDia() {
        Pedido pedido = pedidoDe(andrea, arroz, 1);

        String mensaje = assertThrows(ReglaPedidoException.class,
                () -> servicio.programarYPagar(pedido, LUNES.plusDays(1), franja(12, 30), a(10, 0))).getMessage();

        assertEquals("Solo puedes programar pedidos para hoy", mensaje);
        assertEquals(10.00, saldo("2024-0153"), 0.001);
    }

    @Test
    void hu16_esc_2_1_programaEnUnaFranjaConUnLugarLibreYQuedaLlena() {
        asignar(franja(12, 30), 14);
        Pedido pedido = pedidoDe(andrea, arroz, 1, jugo, 2);

        servicio.programarYPagar(pedido, LUNES, franja(12, 30), a(10, 0));

        assertEquals("Pagado", pedido(pedido.getId()).getEstado());
        assertEquals(15, asignados(franja(12, 30)));
    }

    @Test
    void hu16_esc_2_2_unaFranjaLlenaRechazaSinDescontarSaldoNiExistencias() {
        asignar(franja(12, 30), 15);
        Pedido pedido = pedidoDe(andrea, arroz, 1, jugo, 2);

        assertEquals("La franja 12:30–12:45 ya no tiene cupo", rechazo(pedido, 10, 0, franja(12, 30)));

        assertEquals("Creado", pedido.getEstado());
        assertEquals(10.00, saldo("2024-0153"), 0.001);
        assertEquals(20, stock(arroz));
        assertEquals(15, asignados(franja(12, 30)));
    }

    @Test
    void hu15_esc_2_2_unaFranjaCerradaNoAdmitePedidosNuevos() {
        new ServicioFranjas().cerrarFranja(LUNES, franja(13, 0));
        Pedido pedido = pedidoDe(andrea, arroz, 1);

        assertEquals("La franja 13:00–13:15 está cerrada", rechazo(pedido, 10, 0, franja(13, 0)));
        assertNotNull(pedido);
        assertEquals(10.00, saldo("2024-0153"), 0.001);
    }
}
