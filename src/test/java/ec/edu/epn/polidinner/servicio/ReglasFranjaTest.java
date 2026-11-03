package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Trazabilidad: Examen 1B, reglas comunes del release y HU11 (escenarios 1.2, 3.1, 3.2, 3.3, 3.5),
 * HU12 (1.2, 2.1) y HU17 (2.1, 2.2, 2.3). Reglas puras de la franja de retiro, sin base de datos.
 */
class ReglasFranjaTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 11, 16);

    private static LocalDateTime a(int hora, int minuto) {
        return LocalDateTime.of(LUNES, LocalTime.of(hora, minuto));
    }

    private static String mensaje(Runnable accion) {
        return assertThrows(ReglaPedidoException.class, accion::run).getMessage();
    }

    @Test
    void hu11_esc_1_2_ofreceFranjasConsecutivasDesdeLaSiguienteHastaElCierre() {
        List<LocalTime> franjas = ReglasFranja.iniciosPosteriores(LocalTime.of(12, 5));

        assertEquals(LocalTime.of(12, 15), franjas.get(0));
        assertEquals(LocalTime.of(14, 45), franjas.get(franjas.size() - 1));
        assertEquals(11, franjas.size());
        assertEquals("12:15–12:30", ReglasFranja.etiqueta(franjas.get(0)));
        assertEquals("14:45–15:00", ReglasFranja.etiqueta(franjas.get(franjas.size() - 1)));
    }

    @Test
    void hu11_esc_1_1_aceptaUnaFranjaFuturaAlineadaDeHoy() {
        assertDoesNotThrow(() -> ReglasFranja.validarFranja(LUNES, LocalTime.of(12, 30), a(10, 0)));
    }

    @Test
    void hu11_esc_3_1_rechazaUnaFranjaQueYaComenzo() {
        assertEquals("La hora de retiro debe ser posterior a la hora actual",
                mensaje(() -> ReglasFranja.validarFranja(LUNES, LocalTime.of(12, 30), a(12, 35))));
    }

    @Test
    void hu11_rechazaLaFranjaQueEmpiezaJustoAhora() {
        assertEquals("La hora de retiro debe ser posterior a la hora actual",
                mensaje(() -> ReglasFranja.validarFranja(LUNES, LocalTime.of(12, 30), a(12, 30))));
    }

    @Test
    void hu11_esc_3_2_rechazaUnaHoraDesalineada() {
        assertEquals("La hora de retiro debe ser el inicio de una franja de 15 minutos",
                mensaje(() -> ReglasFranja.validarFranja(LUNES, LocalTime.of(12, 37), a(10, 0))));
    }

    @Test
    void hu11_esc_3_3_rechazaUnaFranjaFueraDelHorarioDelComedor() {
        assertEquals("La hora de retiro está fuera del horario del comedor",
                mensaje(() -> ReglasFranja.validarFranja(LUNES, LocalTime.of(15, 0), a(10, 0))));
        assertEquals("La hora de retiro está fuera del horario del comedor",
                mensaje(() -> ReglasFranja.validarFranja(LUNES, LocalTime.of(10, 45), a(10, 0))));
    }

    @Test
    void hu11_aceptaLaPrimeraYLaUltimaFranjaDelHorario() {
        assertDoesNotThrow(() -> ReglasFranja.validarFranja(LUNES, LocalTime.of(11, 0), a(10, 0)));
        assertDoesNotThrow(() -> ReglasFranja.validarFranja(LUNES, LocalTime.of(14, 45), a(10, 0)));
    }

    @Test
    void hu11_esc_3_5_rechazaUnaFranjaDeOtroDia() {
        assertEquals("Solo puedes programar pedidos para hoy",
                mensaje(() -> ReglasFranja.validarFranja(LUNES.plusDays(1), LocalTime.of(12, 30), a(10, 0))));
    }

    @Test
    void hu12_esc_1_2_aceptaCancelarJustoEnElLimiteDe30Minutos() {
        assertDoesNotThrow(() -> ReglasFranja.validarPlazoDeCancelacion(LUNES, LocalTime.of(12, 30), a(12, 0)));
    }

    @Test
    void hu12_esc_2_1_rechazaCancelarConMenosDe30MinutosDeAnticipacion() {
        assertEquals("Solo puedes cancelar hasta 30 minutos antes de la hora de retiro",
                mensaje(() -> ReglasFranja.validarPlazoDeCancelacion(LUNES, LocalTime.of(12, 30), a(12, 10))));
    }

    @Test
    void hu17_esc_2_3_aceptaReprogramarJustoEnElLimiteDe30Minutos() {
        assertDoesNotThrow(() -> ReglasFranja.validarPlazoDeReprogramacion(LUNES, LocalTime.of(12, 30), a(12, 0)));
    }

    @Test
    void hu17_esc_2_1_rechazaReprogramarConMenosDe30MinutosDeAnticipacion() {
        assertEquals("Solo puedes reprogramar hasta 30 minutos antes del inicio de tu franja original",
                mensaje(() -> ReglasFranja.validarPlazoDeReprogramacion(LUNES, LocalTime.of(12, 30), a(12, 10))));
    }
}
