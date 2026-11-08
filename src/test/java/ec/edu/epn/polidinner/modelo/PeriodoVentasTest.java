package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** HU18 Consultar ventas de productos - TDD de PeriodoVentas (PU04). */
class PeriodoVentasTest {

    /** PU04 - Escenario 18.2.2 y regla 5: «Últimos 7 días» comprende hoy y los seis días anteriores. */
    @Test
    void ultimosSieteDiasComprendeHoyYLosSeisDiasAnteriores() {
        LocalDate hoy = LocalDate.of(2026, 11, 8);

        PeriodoVentas periodo = PeriodoVentas.ultimos7Dias(hoy);

        assertEquals(new PeriodoVentas(LocalDate.of(2026, 11, 2), hoy), periodo);
    }
}