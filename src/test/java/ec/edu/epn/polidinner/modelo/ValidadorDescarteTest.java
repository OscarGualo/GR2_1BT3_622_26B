package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
        Producto yogurCaducado = crearYogurCaducado();
        Producto aguaVigente = crearAguaVigente();

        assertTrue(validador.admiteDescarte(yogurCaducado, HOY));
        assertFalse(validador.admiteDescarte(aguaVigente, HOY));
    }

    /** PU08 - Regla 9: solo existencias habilitadas y con stock positivo. */
    @Test
    void noAdmiteDescartarUnaExistenciaCaducadaSinStockONoHabilitada() {
        Producto sinStock = Producto.registrar("Galletas 100 g", 0.75, "Paquete", 0, fecha(HOY.minusDays(1)));
        Producto historico = new Producto("Jugo 250 ml", 0.80, "Disponible", "Caja", 5, fecha(HOY.minusDays(1)));

        assertFalse(validador.admiteDescarte(sinStock, HOY));
        assertFalse(validador.admiteDescarte(historico, HOY));
    }

    /** PP02 - Escenario 20.2.2 y regla 10: cantidades inválidas para un registro con 6 unidades. */
    @ParameterizedTest(name = "{0} unidades -> {1}")
    @CsvSource({
            "8,  La cantidad supera las 6 unidades disponibles",
            "7,  La cantidad supera las 6 unidades disponibles",
            "0,  La cantidad debe ser mayor que cero",
            "-1, La cantidad debe ser mayor que cero"
    })
    void rechazaCantidadesFueraDelStockDisponible(int cantidad, String mensajeEsperado) {
        Producto yogurCaducado = Producto.registrar("Yogur 150 g", 0.90, "Vaso", 6, fecha(HOY.minusDays(1)));

        DescarteRechazadoException error = assertThrows(DescarteRechazadoException.class,
                () -> validador.validarCantidad(yogurCaducado, cantidad));

        assertEquals(mensajeEsperado, error.getMessage());
    }

    private static Producto crearYogurCaducado() {
        return Producto.registrar("Yogur 150 g", 0.90, "Vaso", 6, fecha(HOY.minusDays(1)));
    }

    private static Producto crearAguaVigente() {
        return Producto.registrar("Agua 500 ml", 0.50, "Botella", 3, fecha(HOY.plusDays(1)));
    }
}