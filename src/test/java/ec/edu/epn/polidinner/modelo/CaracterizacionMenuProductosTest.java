package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Corrección de productos persistentes, Paquete 1c: pruebas de caracterización. Fijan comportamientos
 * actuales que deben conservarse cuando los productos se separen del menú (Paquete 2).
 */
class CaracterizacionMenuProductosTest {

    private Plato arroz;
    private Producto jugo;
    private Policuenta cuenta;
    private ClienteUniversitario andrea;

    @BeforeEach
    void preparar() {
        arroz = new Plato("Arroz con pollo", 3.50, "Disponible", "Arroz y ensalada", 20);
        jugo = new Producto("Jugo de naranja", 0.75, "Disponible", "Vaso de 300 ml", 30, new Date());
        cuenta = new Policuenta("2024-0153", 10.00);
        andrea = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave", cuenta);
    }

    /** V11: un menú no vacío compuesto solo por productos se puede publicar. */
    @Test
    void unMenuSoloConProductosSePuedePublicar() {
        Menu menu = new Menu(new Date(), Menu.BORRADOR);
        menu.agregarItem(jugo);

        assertTrue(menu.publicarParaVenta());
        assertEquals(Menu.PUBLICADO, menu.getEstado());
    }

    /** Un menú ya publicado (venta abierta) sigue recibiendo ítems. */
    @Test
    void unMenuPublicadoSigueAceptandoItems() {
        Menu menu = new Menu(new Date(), Menu.BORRADOR);
        menu.agregarItem(arroz);
        menu.publicarParaVenta();

        assertTrue(menu.agregarItem(jugo));
        assertEquals(2, menu.obtenerItems().size());
    }

    /** V13 y V17: un pago mixto descuenta porciones y unidades; cancelarlo devuelve saldo y existencias. */
    @Test
    void pagoMixtoDescuentaYCancelarloRestituye() {
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(arroz, 2);
        pedido.agregarItem(jugo, 3);

        assertTrue(pedido.confirmarPago());
        assertEquals(18, arroz.getStockActual());
        assertEquals(27, jugo.getStockActual());
        assertEquals(10.00 - 9.25, cuenta.consultarSaldo(), 0.001);

        assertTrue(pedido.cancelar());
        assertEquals(20, arroz.getStockActual());
        assertEquals(30, jugo.getStockActual());
        assertEquals(10.00, cuenta.consultarSaldo(), 0.001);
    }

    /** V20 (hoy): un producto retirado con stock sigue retirado aunque una cancelación le reponga unidades. */
    @Test
    void cancelarNoReactivaUnProductoRetiradoConStock() {
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(jugo, 2);
        pedido.confirmarPago();
        new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "clave").eliminarProducto(jugo);

        pedido.cancelar();

        assertEquals(30, jugo.getStockActual());
        assertEquals("Agotado", jugo.getEstado());
    }

    /** V22: un producto que se agotó por ventas vuelve a estar disponible cuando una cancelación lo repone. */
    @Test
    void unProductoAgotadoPorVentasVuelveConLaCancelacion() {
        Producto pastel = new Producto("Pastel", 1.25, "Disponible", "Porción", 1, new Date());
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(pastel, 1);
        pedido.confirmarPago();
        assertEquals("Agotado", pastel.getEstado());

        pedido.cancelar();

        assertEquals(1, pastel.getStockActual());
        assertEquals("Disponible", pastel.getEstado());
    }
}
