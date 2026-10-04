package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de Policuenta (simulación del sistema externo PoliCuenta).
 * Trazabilidad: Diagrama de clases (Fig. 15) y Diagrama de secuencia CU03 - mensajes 28-29 y 39-40.
 */
class PolicuentaTest {

    /** Mensajes 39-40: debitar(monto) con saldo suficiente descuenta y devuelve true (saldo descontado). */
    @Test
    void debitarConSaldoSuficienteDescuentaElMonto() {
        Policuenta cuenta = new Policuenta("2024-0153", 10.00);

        assertTrue(cuenta.debitar(4.25));
        assertEquals(5.75, cuenta.consultarSaldo(), 0.001);
    }

    @Test
    void debitarElSaldoExactoDejaLaCuentaEnCero() {
        Policuenta cuenta = new Policuenta("2024-0153", 4.25);

        assertTrue(cuenta.debitar(4.25));
        assertEquals(0.00, cuenta.consultarSaldo(), 0.001);
    }

    @Test
    void debitarConSaldoInsuficienteDevuelveFalseYNoCambiaElSaldo() {
        Policuenta cuenta = new Policuenta("2024-0153", 3.00);

        assertFalse(cuenta.debitar(4.25));
        assertEquals(3.00, cuenta.consultarSaldo(), 0.001);
    }

    @Test
    void debitarRechazaMontoCeroONegativo() {
        Policuenta cuenta = new Policuenta("2024-0153", 10.00);

        assertThrows(IllegalArgumentException.class, () -> cuenta.debitar(0));
        assertThrows(IllegalArgumentException.class, () -> cuenta.debitar(-1));
        assertEquals(10.00, cuenta.consultarSaldo(), 0.001);
    }
}
