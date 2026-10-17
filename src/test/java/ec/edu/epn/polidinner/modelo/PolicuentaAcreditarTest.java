package ec.edu.epn.polidinner.modelo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PolicuentaAcreditarTest {

    /** T5 (TDD, parametrizado): acreditar(monto) suma el monto al saldo de la PoliCuenta. */
    @ParameterizedTest(name = "saldo {0} + acreditar {1} = {2}")
    @CsvSource({
            "5.75, 4.25, 10.00",   // reembolso del caso de prueba de Andrea
            "0.00, 3.50, 3.50",    // cuenta en cero
            "10.00, 0.75, 10.75",
            "2.10, 0.20, 2.30"     // decimales que en double dan 2.3000000000000003
    })
    void acreditarSumaElMontoAlSaldo(double saldoInicial, double monto, double saldoEsperado) {
        Policuenta cuenta = new Policuenta("2024-0153", saldoInicial);

        cuenta.acreditar(monto);

        assertEquals(saldoEsperado, cuenta.consultarSaldo(), 0.001);
    }
}