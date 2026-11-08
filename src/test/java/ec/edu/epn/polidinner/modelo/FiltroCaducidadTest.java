package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/** HU19 Consultar productos por franja de caducidad - TDD de FiltroCaducidad (PU05 y PU06). */
class FiltroCaducidadTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);

    /** Doble de prueba escrito a mano: hasta hoy+3 es «Próximos 3 días» y después «Más adelante». */
    private final ClasificadorFranja clasificadorDePrueba = (caducidad, hoy) ->
            caducidad.isAfter(hoy.plusDays(3)) ? FranjaCaducidad.MAS_ADELANTE : FranjaCaducidad.PROXIMOS_3_DIAS;

    private final FiltroCaducidad filtro = new FiltroCaducidad(clasificadorDePrueba);

    private static List<Integer> referencias(List<ExistenciaProducto> existencias) {
        return existencias.stream().map(ExistenciaProducto::getReferencia).toList();
    }

    /** PU05 - Escenarios 19.1.2 y 19.2.2: solo la franja elegida, por caducidad y luego por referencia. */
    @Test
    void muestraSoloLaFranjaElegidaOrdenadaPorCaducidadYReferencia() {
        List<ExistenciaProducto> existencias = List.of(
                new ExistenciaProducto(201, "Yogur 150 g", 4, HOY.plusDays(2), true),
                new ExistenciaProducto(105, "Agua 500 ml", 4, HOY.plusDays(2), true),
                new ExistenciaProducto(102, "Agua 500 ml", 7, HOY.plusDays(4), true),
                new ExistenciaProducto(101, "Agua 500 ml", 3, HOY.plusDays(1), true));

        List<ExistenciaProducto> resultado = filtro.filtrar(existencias, FranjaCaducidad.PROXIMOS_3_DIAS, HOY);

        assertEquals(List.of(101, 105, 201), referencias(resultado));
    }
}