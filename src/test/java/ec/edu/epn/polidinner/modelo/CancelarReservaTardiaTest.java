package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

/**
 * HU-02 Cancelar mi reserva.
 * Prueba convencional PU-04.
 * Tareas T2.2, T2.3 y T2.4.
 */

class CancelarReservaTardiaTest {

    /**
     * HU-02, escenarios 1.3 y 2.1:
     * una cancelación tardía libera la mesa para otro cliente
     * e impide al cliente que canceló volver a reservar ese día.
     */

    @Test
    void cancelarTardeLiberaLaMesaPeroImpideAlClienteReservarDeNuevoHoy() {
        ClienteUniversitario andrea = new ClienteUniversitario("1712345678", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));
        ClienteUniversitario luis = new ClienteUniversitario("1798765432", "Luis", "Torres", "clave",
                new Policuenta("2024-0201", 8.00));
        Mesa mesa1 = new Mesa(1);
        Mesa mesa3 = new Mesa(3);
        AgendaReservas agenda = new AgendaReservas(LocalDate.of(2026, 10, 26), new ArrayList<>());
        Reserva reservaAndrea = agenda.reservar(andrea, mesa1, Franja.F12_30);

        assertTrue(reservaAndrea.cancelar(LocalTime.of(12, 20)));   // faltan 10 minutos: fuera de plazo

        // Escenario 1.3: Andrea queda restringida el resto del día
        ReservaRechazadaException error = assertThrows(ReservaRechazadaException.class,
                () -> agenda.reservar(andrea, mesa3, Franja.F13_00));
        assertEquals("Canceló una reserva fuera de plazo; no puede reservar otra mesa hoy", error.getMessage());

        // Escenario 2.1: la Mesa 1 quedó libre y Luis puede reservarla en la misma franja
        Reserva reservaLuis = agenda.reservar(luis, mesa1, Franja.F12_30);
        assertTrue(reservaLuis.estaActiva());
        assertEquals(2, agenda.getReservas().size());
    }
}