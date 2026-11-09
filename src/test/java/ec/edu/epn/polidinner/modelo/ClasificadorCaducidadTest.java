package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** HU19 Consultar productos por franja de caducidad - TDD de ClasificadorCaducidad (PP01). */
class ClasificadorCaducidadTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);

    private final ClasificadorCaducidad clasificador = new ClasificadorCaducidad();

    /** PP01 - Tabla de franjas (escenarios 19.1.1, 19.1.2 y 19.2.1): límites de cada franja. */
    @ParameterizedTest(name = "caduca a {0} días de hoy -> {1}")
    @CsvSource({
            "-1, CADUCADOS",
            "0,  CADUCAN_HOY",
            "1,  PROXIMOS_3_DIAS",
            "3,  PROXIMOS_3_DIAS",
            "4,  MAS_ADELANTE"
    })
    void ubicaCadaFechaDeCaducidadEnSuFranja(int diasDesdeHoy, FranjaCaducidad franjaEsperada) {
        assertEquals(franjaEsperada, clasificador.clasificar(HOY.plusDays(diasDesdeHoy), HOY));
    }
}