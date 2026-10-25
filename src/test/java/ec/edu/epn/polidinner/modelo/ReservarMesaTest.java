package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * HU-01 Reservar una mesa - pruebas PU-01, PU-02 y PU-03 (tarea T1.4).
 */
class ReservarMesaTest {

    private LocalDate hoy;
    private ClienteUniversitario andrea;
    private ClienteUniversitario luis;
    private ClienteUniversitario carla;
    private Mesa mesa1;
    private Mesa mesa2;
    private Mesa mesa3;
    private AgendaReservas agenda;

    @BeforeEach
    void preparar() {
        hoy = LocalDate.of(2026, 10, 26);
        andrea = new ClienteUniversitario("1712345678", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));
        luis = new ClienteUniversitario("1798765432", "Luis", "Torres", "clave",
                new Policuenta("2024-0201", 8.00));
        carla = new ClienteUniversitario("1755555555", "Carla", "Ruiz", "clave",
                new Policuenta("2024-0318", 5.00));
        mesa1 = new Mesa(1);
        mesa2 = new Mesa(2);
        mesa3 = new Mesa(3);
        agenda = new AgendaReservas(hoy, new ArrayList<>());
    }

    /** PU-01 (HU-01 / Escenario 1.1): reservar una mesa libre crea una reserva Activa con sus datos. */
    @Test
    void reservarUnaMesaLibreCreaUnaReservaActiva() {
        Reserva reserva = agenda.reservar(andrea, mesa1, Franja.F12_30);

        assertNotNull(reserva);
        assertEquals(Reserva.ACTIVA, reserva.getEstado());
        assertSame(andrea, reserva.getCliente());
        assertSame(mesa1, reserva.getMesa());
        assertEquals(hoy, reserva.getFecha());
        assertEquals(Franja.F12_30, reserva.getFranja());
        assertTrue(agenda.getReservas().contains(reserva));
    }
}