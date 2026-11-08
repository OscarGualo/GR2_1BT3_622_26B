package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** HU20 Registrar pérdidas por caducidad - TDD de ValidadorDescarte (PU07, PU08 y PP02). */
class ValidadorDescarteTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);

    private final ValidadorDescarte validador = new ValidadorDescarte();

    private static Date fecha(LocalDate dia) {
        return Date.valueOf(dia);
    }

    /** PU07 - Escenarios 20.1.1 y 20.1.2: se ofrece la existencia caducada y no la vigente. */
    @Test
    void admiteDescartarUnaExistenciaCaducadaYNoUnaVigente() {
        Producto yogurCaducado = Producto.registrar("Yogur 150 g", 0.90, "Vaso", 6, fecha(HOY.minusDays(1)));
        Producto aguaVigente = Producto.registrar("Agua 500 ml", 0.50, "Botella", 3, fecha(HOY.plusDays(1)));

        assertTrue(validador.admiteDescarte(yogurCaducado, HOY));
        assertFalse(validador.admiteDescarte(aguaVigente, HOY));
    }
}