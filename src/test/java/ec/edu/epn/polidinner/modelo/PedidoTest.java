package ec.edu.epn.polidinner.modelo;

import java.lang.reflect.Field;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del CU 03 Realizar pedido de comida (carrito y pago).
 * Trazabilidad: Diagrama de clases (Fig. 15) y Diagrama de secuencia CU03.
 * Caso de prueba del informe: Andrea Gómez, PoliCuenta 2024-0153 con $10.00, compra
 * "Arroz con pollo" ($3.50) y "Jugo de naranja" ($0.75): total $4.25 y saldo final $5.75.
 */
class PedidoTest {

    private ClienteUniversitario andrea;
    private Plato arroz;
    private Producto jugo;

    @BeforeEach
    void prepararCasoDePrueba() {
        andrea = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));
        arroz = new Plato("Arroz con pollo", 3.50, "Disponible", "Arroz, pollo y ensalada", 20);
        jugo = new Producto("Jugo de naranja", 0.75, "Disponible", "Vaso 300 ml", 30, new Date());
    }

    /** Mensajes 14-19: agregarItem(item, cantidad) crea un DetallePedido(cantidad, precioUnitario). */
    @Test
    void agregarItemConStockCreaElDetalle() {
        Pedido pedido = andrea.crearPedido();

        DetallePedido detalle = pedido.agregarItem(arroz, 2);

        assertEquals(1, pedido.getDetalles().size());
        assertEquals(2, detalle.getCantidad());
        assertEquals(3.50, detalle.getPrecioUnitario(), 0.001);
        assertSame(arroz, detalle.getItem());
    }

    @Test
    void agregarElMismoItemSumaLaCantidad() {
        Pedido pedido = andrea.crearPedido();

        pedido.agregarItem(jugo, 1);
        pedido.agregarItem(jugo, 2);

        assertEquals(1, pedido.getDetalles().size());
        assertEquals(3, pedido.getDetalles().get(0).getCantidad());
    }

    /** Mensajes 15-16: sin stock (hayStock false) no se agrega nada. */
    @Test
    void agregarItemSinStockLanzaExcepcion() {
        Pedido pedido = andrea.crearPedido();
        Plato ultimo = new Plato("Menestra", 2.00, "Disponible", "", 1);

        assertThrows(IllegalStateException.class, () -> pedido.agregarItem(ultimo, 2));
        assertTrue(pedido.getDetalles().isEmpty());
    }

    /** Conexión con el CU 02: un plato retirado no se puede pedir. */
    @Test
    void unPlatoRetiradoNoSePuedeAgregar() {
        Pedido pedido = andrea.crearPedido();
        new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "clave").eliminarPlato(arroz);

        assertThrows(IllegalStateException.class, () -> pedido.agregarItem(arroz, 1));
    }

    /** Mensajes 24-25: calcularSubtotal() = cantidad × precioUnitario. */
    @Test
    void calcularSubtotalMultiplicaCantidadPorPrecio() {
        Pedido pedido = andrea.crearPedido();

        DetallePedido detalle = pedido.agregarItem(jugo, 3);

        assertEquals(2.25, detalle.calcularSubtotal(), 0.001);
    }

    /** Mensajes 23-26 del caso de prueba: arroz con pollo $3.50 + jugo $0.75 = $4.25. */
    @Test
    void calcularTotalDelCasoDePruebaEs425() {
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(arroz, 1);
        pedido.agregarItem(jugo, 1);

        assertEquals(4.25, pedido.calcularTotal(), 0.001);
        assertEquals(4.25, pedido.getTotal(), 0.001);
    }

    /** Mensajes 35-42: confirmarPago() debita $4.25, deja $5.75, reduce el stock y el pedido pasa a "Pagado". */
    @Test
    void confirmarPagoDebitaReduceStockYCambiaEstado() {
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(arroz, 1);
        pedido.agregarItem(jugo, 1);

        assertTrue(pedido.confirmarPago());

        assertEquals("Pagado", pedido.getEstado());
        assertEquals(5.75, andrea.consultarSaldo(), 0.001);
        assertEquals(19, arroz.getStockActual());
        assertEquals(29, jugo.getStockActual());
    }

    @Test
    void confirmarPagoConSaldoInsuficienteNoCambiaNada() {
        ClienteUniversitario sinSaldo = new ClienteUniversitario("1750000002", "Luis", "Mora", "clave",
                new Policuenta("2024-0999", 2.00));
        Pedido pedido = sinSaldo.crearPedido();
        pedido.agregarItem(arroz, 1);

        assertFalse(pedido.confirmarPago());

        assertEquals("Creado", pedido.getEstado());
        assertEquals(2.00, sinSaldo.consultarSaldo(), 0.001);
        assertEquals(20, arroz.getStockActual());
    }

    @Test
    void confirmarPagoSinStockAlConfirmarNoDebita() {
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(arroz, 1);
        // Mientras el cliente revisaba el carrito, el personal retiró el plato (CU 02)
        new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "clave").eliminarPlato(arroz);

        assertFalse(pedido.confirmarPago());
        assertEquals(10.00, andrea.consultarSaldo(), 0.001);
    }

    /** Mensajes 44-47: generarCodigoEntrega() crea el Comprobante PD-0457 para el pedido 457, sin usar. */
    @Test
    void generarCodigoEntregaUsaElFormatoPD() throws Exception {
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(arroz, 1);
        pedido.agregarItem(jugo, 1);
        pedido.confirmarPago();
        asignarId(pedido, 457);

        Comprobante comprobante = pedido.generarCodigoEntrega();

        assertNotNull(comprobante);
        assertEquals("PD-0457", comprobante.getCodigo());
        assertFalse(comprobante.isUsado());
        assertSame(pedido, comprobante.getPedido());
        assertSame(comprobante, pedido.getComprobante());
    }

    @Test
    void generarCodigoEntregaExigeUnPedidoPagado() throws Exception {
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(arroz, 1);
        asignarId(pedido, 1);

        assertThrows(IllegalStateException.class, pedido::generarCodigoEntrega);
    }

    @Test
    void quitarItemEliminaElDetalle() {
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(arroz, 1);

        assertTrue(pedido.quitarItem(arroz.getId()));
        assertTrue(pedido.getDetalles().isEmpty());
    }

    /** El id lo asigna la base de datos (IDENTITY); en la prueba se fija por reflexión. */
    private static void asignarId(Pedido pedido, int id) throws Exception {
        Field campo = Pedido.class.getDeclaredField("id");
        campo.setAccessible(true);
        campo.setInt(pedido, id);
    }

    /** Secuencia: confirmarPago() → debitar(monto) → <<create>> CodigoEntrega (Comprobante). */
    @Test
    void confirmarPagoCreaElComprobante() {
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(arroz, 1);

        assertTrue(pedido.confirmarPago());

        assertNotNull(pedido.getComprobante());
        assertSame(pedido, pedido.getComprobante().getPedido());
        assertFalse(pedido.getComprobante().isUsado());
    }

    @Test
    void confirmarPagoFallidoNoCreaComprobante() {
        ClienteUniversitario sinSaldo = new ClienteUniversitario("1750000002", "Luis", "Mora", "clave",
                new Policuenta("2024-0999", 2.00));
        Pedido pedido = sinSaldo.crearPedido();
        pedido.agregarItem(arroz, 1);

        assertFalse(pedido.confirmarPago());
        assertNull(pedido.getComprobante());
    }

    /** El código PD-0000 se completa cuando el pedido ya tiene id (al guardarse). */
    @Test
    void elCodigoSeCompletaCuandoElPedidoTieneId() throws Exception {
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(arroz, 1);
        pedido.confirmarPago();
        assertNull(pedido.getComprobante().getCodigo());

        asignarId(pedido, 12);

        assertSame(pedido.getComprobante(), pedido.generarCodigoEntrega());
        assertEquals("PD-0012", pedido.getComprobante().getCodigo());
    }
}
