package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PedidoPagoTest {
    @ParameterizedTest
    @CsvSource({"4.00, false, 4.00, 20", "5.00, true, 0.00, 18", "10.00, true, 5.00, 18"})
    void hu07_validaSaldoAntesDeDescontarExistencias(double saldo, boolean permitido,
                                                    double saldoFinal, int stockFinal) {
        Policuenta cuenta = new Policuenta("2024-0153", saldo);
        ClienteUniversitario cliente = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave", cuenta);
        Plato plato = new Plato("Almuerzo de prueba", 2.50, "Disponible", "Prueba de pago", 20);
        Pedido pedido = cliente.crearPedido();
        pedido.agregarItem(plato, 2);

        assertEquals(permitido, pedido.confirmarPago());
        assertEquals(saldoFinal, cuenta.consultarSaldo(), 0.001);
        assertEquals(stockFinal, plato.getStockActual());
        assertEquals(permitido ? "Pagado" : "Creado", pedido.getEstado());
        if (permitido) assertNotNull(pedido.getComprobante());
        else assertNull(pedido.getComprobante());
    }

    @Test
    void hu07_confirmarDosVecesNoRepiteElCobro() {
        Policuenta cuenta = new Policuenta("2024-0153", 10.00);
        ClienteUniversitario cliente = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave", cuenta);
        Plato plato = new Plato("Almuerzo de prueba", 5.00, "Disponible", "Prueba de pago", 20);
        Pedido pedido = new Pedido(new Date(), "Creado", cliente);
        pedido.agregarItem(plato, 1);
        assertTrue(pedido.confirmarPago());
        Comprobante comprobante = pedido.getComprobante();

        assertFalse(pedido.confirmarPago());
        assertEquals(5.00, cuenta.consultarSaldo(), 0.001);
        assertEquals(19, plato.getStockActual());
        assertSame(comprobante, pedido.getComprobante());
    }
}
