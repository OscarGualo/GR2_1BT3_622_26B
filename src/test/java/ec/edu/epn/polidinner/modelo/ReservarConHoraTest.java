package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * HU-01 Reservar una mesa - reservar con la hora actual (tareas T1.2 y T1.4). Pruebas adicionales
 * del incremento, fuera de las seis pruebas TDD (PU-01 a PU-06).
 */
class ReservarConHoraTest {

    private ClienteUniversitario andrea;
    private AgendaReservas agenda;

    @BeforeEach
    void preparar() {
        andrea = new ClienteUniversitario("1712345678", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));
        agenda = new AgendaReservas(LocalDate.of(2026, 10, 26), new ArrayList<>());
    }

    /** HU-01 / Escenario 2.1: a las 11:00 se puede reservar la franja de 12:30. */
    @Test
    void reservarUnaFranjaFuturaCreaLaReserva() {
        Reserva reserva = agenda.reservar(andrea, new Mesa(2), Franja.F12_30, LocalTime.of(11, 0));

        assertTrue(reserva.estaActiva());
        assertEquals(1, agenda.getReservas().size());
    }

    /** HU-01 / Escenario 2.2: a las 12:40 ya no se puede reservar la franja de 12:30. */
    @Test
    void reservarUnaFranjaQueYaComenzoSeRechaza() {
        ReservaRechazadaException error = assertThrows(ReservaRechazadaException.class,
                () -> agenda.reservar(andrea, new Mesa(2), Franja.F12_30, LocalTime.of(12, 40)));

        assertEquals("No puede reservar una franja que ya comenzó", error.getMessage());
        assertTrue(agenda.getReservas().isEmpty());
    }
}
