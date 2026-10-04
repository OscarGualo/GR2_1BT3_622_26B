package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de Comprobante (CU 04 Habilitar entrega de comida).
 * Trazabilidad: Diagrama de clases (Fig. 15) y Diagrama de secuencia CU04 - mensajes 6-7 y 10.
 * Caso de prueba del informe: el código es correcto, del día y no usado.
 */
class ComprobanteTest {

    private Pedido pedidoPagado;

    @BeforeEach
    void prepararPedidoPagado() {
        ClienteUniversitario andrea = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));
        pedidoPagado = andrea.crearPedido();
        pedidoPagado.agregarItem(new Plato("Arroz con pollo", 3.50, "Disponible", "", 20), 1);
        pedidoPagado.confirmarPago();
    }

    /** Mensajes 6-7: un código del día, sin usar y de un pedido "Pagado" es válido. */
    @Test
    void codigoDelDiaSinUsarDeUnPedidoPagadoEsValido() {
        Comprobante comprobante = new Comprobante("PD-0457", new Date(), pedidoPagado);

        assertTrue(comprobante.validarCodigoEntrega());
    }

    /** Mensaje 10 y caso de prueba "PD-0457 por segunda vez": un código usado ya no es válido. */
    @Test
    void marcarCodigoUsadoInvalidaElCodigo() {
        Comprobante comprobante = new Comprobante("PD-0457", new Date(), pedidoPagado);

        comprobante.marcarCodigoUsado();

        assertTrue(comprobante.isUsado());
        assertFalse(comprobante.validarCodigoEntrega());
    }

    @Test
    void codigoDeAyerNoEsValido() {
        Calendar ayer = Calendar.getInstance();
        ayer.add(Calendar.DAY_OF_MONTH, -1);
        Comprobante comprobante = new Comprobante("PD-0457", ayer.getTime(), pedidoPagado);

        assertFalse(comprobante.esDeHoy());
        assertFalse(comprobante.validarCodigoEntrega());
    }

    @Test
    void codigoDeUnPedidoNoPagadoNoEsValido() {
        Pedido sinPagar = new ClienteUniversitario("1750000002", "Luis", "Mora", "clave",
                new Policuenta("2024-0999", 1.00)).crearPedido();
        Comprobante comprobante = new Comprobante("PD-0458", new Date(), sinPagar);

        assertFalse(comprobante.validarCodigoEntrega());
    }
}
