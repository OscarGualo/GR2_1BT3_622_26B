package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regresión de HU-02, escenario 2.3.
 * Una reserva cancelada no se cancela nuevamente
 * ni cambia su estado o su condición de restricción.
 */
class CancelarReservaRepetidaTest {

    private Reserva crearReservaActiva() {
        ClienteUniversitario andrea = new ClienteUniversitario(
                "1712345678",
                "Andrea",
                "Gómez",
                "clave",
                new Policuenta("2024-0153", 10.00)
        );

        return new Reserva(
                andrea,
                new Mesa(1),
                LocalDate.of(2026, 10, 26),
                Franja.F12_30
        );
    }

    /**
     * Cancelar a tiempo y repetir fuera de plazo
     * no debe generar una restricción nueva.
     */
    @Test
    void repetirUnaCancelacionATiempoNoGeneraRestriccion() {
        Reserva reserva = crearReservaActiva();

        assertTrue(reserva.cancelar(LocalTime.of(12, 0)));

        boolean resultado = reserva.cancelar(LocalTime.of(12, 20));

        assertAll(
                () -> assertFalse(resultado),
                () -> assertEquals(
                        Reserva.CANCELADA,
                        reserva.getEstado()
                ),
                () -> assertFalse(reserva.restringeReservasDelDia())
        );
    }

    /**
     * Repetir una cancelación tardía no debe volver a cancelarla
     * ni eliminar la restricción existente.
     */
    @Test
    void repetirUnaCancelacionTardiaConservaSuEstadoYRestriccion() {
        Reserva reserva = crearReservaActiva();

        assertTrue(reserva.cancelar(LocalTime.of(12, 20)));

        boolean resultado = reserva.cancelar(LocalTime.of(12, 21));

        assertAll(
                () -> assertFalse(resultado),
                () -> assertEquals(
                        Reserva.CANCELADA_FUERA_DE_PLAZO,
                        reserva.getEstado()
                ),
                () -> assertTrue(reserva.restringeReservasDelDia())
        );
    }
}