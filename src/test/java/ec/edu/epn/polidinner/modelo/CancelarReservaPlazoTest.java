package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * HU-02 Cancelar mi reserva - prueba parametrizada PU-06 (tarea T2.3).
 */
class CancelarReservaPlazoTest {

    /**
     * PU-06 (HU-02 / Escenarios 1.1, 1.2 y 1.3): para una reserva de 12:30 las tres horas cancelan,
     * pero solo a las 12:20 (menos de 15 minutos antes) la cancelación queda fuera de plazo.
     */
    @ParameterizedTest(name = "cancelar a las {0} deja la reserva \"{1}\" (restringe: {2})")
    @CsvSource({
            "12:00, Cancelada, false",                  // Escenario 1.1: con anticipación
            "12:15, Cancelada, false",                  // Escenario 1.2: límite exacto de 15 minutos
            "12:20, Cancelada fuera de plazo, true"     // Escenario 1.3: fuera de plazo
    })
    void cancelarAplicaElPlazoDe15Minutos(LocalTime horaActual, String estadoEsperado, boolean restringe) {
        ClienteUniversitario andrea = new ClienteUniversitario("1712345678", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));
        Reserva reserva = new Reserva(andrea, new Mesa(1), LocalDate.of(2026, 10, 26), Franja.F12_30);

        assertTrue(reserva.cancelar(horaActual));

        assertEquals(estadoEsperado, reserva.getEstado());
        assertEquals(restringe, reserva.restringeReservasDelDia());
    }
}