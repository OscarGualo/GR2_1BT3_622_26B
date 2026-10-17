package ec.edu.epn.polidinner.modelo;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PedidoCancelarTest {

    private ClienteUniversitario andrea = null;
    private Plato arroz = null;
    private Producto jugo = null;

    @BeforeEach
    public void setUp() {
        andrea = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));
        arroz = new Plato("Arroz con pollo", 3.50, "Disponible", "Arroz, pollo y ensalada", 20);
        jugo = new Producto("Jugo de naranja", 0.75, "Disponible", "Vaso 300 ml", 30, new Date());
        System.out.println("setUp()");
    }

    /** T4 (TDD): un pedido pagado se cancela, su código de entrega queda inutilizable. */
    @Test
    public void cancelarUnPedidoPagadoLoDejaCanceladoConElCodigoAnulado() {
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(arroz, 1);
        pedido.agregarItem(jugo, 1);
        assertTrue(pedido.confirmarPago());

        assertTrue(pedido.cancelar());

        assertEquals("Cancelado", pedido.getEstado());
        assertTrue(pedido.getComprobante().isUsado());
        assertFalse(pedido.getComprobante().validarCodigoEntrega());
        System.out.println("Test 1");
    }
}