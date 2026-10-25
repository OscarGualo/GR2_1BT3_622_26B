package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * HU-01 Reservar una mesa - reglas de franja (tarea T1.2). Pruebas adicionales del incremento,
 * fuera de las seis pruebas TDD (PU-01 a PU-06).
 */
class FranjaTest {

    /** HU-01 / Escenario 2.2: una franja ya comenzó desde su hora exacta de inicio. */
    @ParameterizedTest(name = "a las {0} la franja de 12:30 ya comenzó: {1}")
    @CsvSource({
            "12:29, false",
            "12:30, true",     // límite exacto
            "12:40, true"
    })
    void yaComenzoDesdeSuHoraExactaDeInicio(LocalTime hora, boolean comenzo) {
        assertEquals(comenzo, Franja.F12_30.yaComenzo(hora));
    }

    /** HU-01 / Escenarios 2.1 y 2.3: solo se ofrecen las franjas que todavía no comienzan. */
    @Test
    void noComenzadasDevuelveSoloLasFranjasFuturas() {
        assertEquals(Arrays.asList(Franja.F12_00, Franja.F12_30, Franja.F13_00), Franja.noComenzadas(LocalTime.of(11, 0)));
        assertEquals(Collections.singletonList(Franja.F13_00), Franja.noComenzadas(LocalTime.of(12, 40)));
        assertTrue(Franja.noComenzadas(LocalTime.of(13, 40)).isEmpty());
    }
}