package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de ClienteUniversitario.
 * Trazabilidad: Diagrama de clases (Fig. 15) y Diagrama de secuencia CU03 - mensajes 3-6 y 27-30.
 */
class ClienteUniversitarioTest {

    /** Mensajes 3-6: crearPedido() crea un Pedido de hoy, "Creado", del cliente y con su PoliCuenta. */
    @Test
    void crearPedidoCreaUnPedidoVacioDelCliente() {
        Policuenta cuenta = new Policuenta("2024-0153", 10.00);
        ClienteUniversitario andrea = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave", cuenta);

        Pedido pedido = andrea.crearPedido();

        assertEquals("Creado", pedido.getEstado());
        SimpleDateFormat dia = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(dia.format(new Date()), dia.format(pedido.getFecha()));
        assertSame(andrea, pedido.getCliente());
        assertSame(cuenta, pedido.getPolicuenta());
        assertTrue(pedido.getDetalles().isEmpty());
    }

    /** Mensajes 27-30: consultarSaldo() delega en la PoliCuenta que posee el cliente. */
    @Test
    void consultarSaldoDelegaEnLaPolicuenta() {
        ClienteUniversitario andrea = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));

        assertEquals(10.00, andrea.consultarSaldo(), 0.001);
    }
}
