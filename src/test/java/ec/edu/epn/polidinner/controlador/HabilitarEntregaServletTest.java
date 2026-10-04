package ec.edu.epn.polidinner.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Pruebas del formato del código ingresado por el personal (mensaje 1 del Diagrama de secuencia CU04).
 */
class HabilitarEntregaServletTest {

    @Test
    void normalizaElCodigoAlFormatoPD() {
        assertEquals("PD-0457", HabilitarEntregaServlet.normalizarCodigo("PD-0457"));
        assertEquals("PD-0457", HabilitarEntregaServlet.normalizarCodigo(" pd-0457 "));
        assertEquals("PD-0457", HabilitarEntregaServlet.normalizarCodigo("457"));
        assertEquals("PD-0001", HabilitarEntregaServlet.normalizarCodigo("PD-1"));
    }

    @Test
    void rechazaCodigosConOtroFormato() {
        assertNull(HabilitarEntregaServlet.normalizarCodigo(null));
        assertNull(HabilitarEntregaServlet.normalizarCodigo(""));
        assertNull(HabilitarEntregaServlet.normalizarCodigo("ABC-12"));
        assertNull(HabilitarEntregaServlet.normalizarCodigo("PD-12A"));
    }
}
