package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Corrección de productos persistentes, Paquete 2 (reglas en memoria): el menú de cada día ofrece ítems
 * mediante OfertaMenu. El producto conserva identidad y existencias entre jornadas; el plato es de una
 * sola jornada; el retiro es de la oferta del día y no del producto.
 */
class OfertaMenuTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 11, 16);
    private static final LocalDate MARTES = LUNES.plusDays(1);

    private Menu menuLunes;
    private Menu menuMartes;
    private Plato arroz;
    private Producto agua;

    @BeforeEach
    void preparar() {
        menuLunes = new Menu(java.sql.Date.valueOf(LUNES), Menu.BORRADOR);
        menuMartes = new Menu(java.sql.Date.valueOf(MARTES), Menu.BORRADOR);
        arroz = new Plato("Arroz con pollo", 3.50, "Disponible", "Arroz y ensalada", 20);
        agua = Producto.registrar("Agua sin gas", 0.60, "Botella 500 ml", 20, java.sql.Date.valueOf(LUNES.plusDays(7)));
        asignarId(arroz, 1);
        asignarId(agua, 2);
    }

    @Test
    void agregarItemCreaUnaOfertaPorItemYObtenerItemsSeDerivaDeEllas() {
        assertTrue(menuLunes.agregarItem(arroz));
        assertTrue(menuLunes.agregarItem(agua));

        assertEquals(2, menuLunes.obtenerOfertas().size());
        assertEquals(Arrays.asList(arroz, agua), menuLunes.obtenerItems());
        assertSame(agua, menuLunes.buscarOferta(2).getItem());
        assertSame(menuLunes, menuLunes.buscarOferta(2).getMenu());
        assertNull(menuLunes.buscarOferta(99));
        assertTrue(menuLunes.perteneceAlMenu(2));
    }

    /** V07: seleccionar dos veces el mismo producto no crea otra oferta. */
    @Test
    void elMismoProductoNoSeAgregaDosVecesAlMismoMenu() {
        menuLunes.agregarItem(agua);

        assertFalse(menuLunes.agregarItem(agua));
        assertEquals(1, menuLunes.obtenerOfertas().size());
        assertEquals(20, agua.getStockActual());
    }

    /** V15 (en memoria): el mismo registro de producto se ofrece en otra jornada, con las mismas existencias. */
    @Test
    void elMismoProductoSeOfreceEnVariosMenus() {
        menuLunes.agregarItem(agua);

        assertTrue(menuMartes.agregarItem(agua));
        assertSame(menuLunes.buscarOferta(2).getItem(), menuMartes.buscarOferta(2).getItem());
    }

    /** V10: un plato es de su jornada; no se reutiliza en el menú de otro día. */
    @Test
    void unPlatoDeOtraJornadaNoSePuedeReutilizar() {
        assertTrue(menuLunes.agregarItem(arroz));

        assertEquals(LUNES, arroz.getFechaPreparacion());
        assertFalse(menuMartes.agregarItem(arroz));
        assertEquals(LUNES, arroz.getFechaPreparacion());
    }

    /** V19: retirar el producto de la oferta de hoy no cambia su estado ni sus existencias. */
    @Test
    void retirarLaOfertaNoCambiaElProducto() {
        menuLunes.agregarItem(agua);
        OfertaMenu oferta = menuLunes.buscarOferta(2);

        oferta.retirar();

        assertFalse(oferta.estaOfrecida());
        assertFalse(oferta.esComprable(1, LUNES));
        assertEquals("Disponible", agua.getEstado());
        assertEquals(20, agua.getStockActual());
        assertEquals(OfertaMenu.RETIRADO, oferta.estadoEn(LUNES));
    }

    /** V20: una cancelación devuelve las unidades, pero no deshace el retiro del día. */
    @Test
    void cancelarDespuesDelRetiroNoReactivaLaOferta() {
        menuLunes.agregarItem(agua);
        Pedido pedido = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00)).crearPedido();
        pedido.agregarItem(agua, 2);
        pedido.confirmarPago();
        OfertaMenu oferta = menuLunes.buscarOferta(2);
        oferta.retirar();

        pedido.cancelar();

        assertEquals(20, agua.getStockActual());
        assertFalse(oferta.estaOfrecida());
    }

    /** V21: el retiro de hoy no impide ofrecer el producto en otra jornada. */
    @Test
    void elRetiroDeHoyNoImpideOfrecerloOtroDia() {
        menuLunes.agregarItem(agua);
        menuLunes.buscarOferta(2).retirar();

        assertTrue(menuMartes.agregarItem(agua));
        assertTrue(menuMartes.buscarOferta(2).esComprable(1, MARTES));
        assertFalse(menuLunes.buscarOferta(2).estaOfrecida());
    }

    /** V04 y V23 (convención propuesta): la caducidad D se puede vender durante D; desde D+1, no. */
    @Test
    void unProductoSeVendeHastaSuFechaDeCaducidadInclusive() {
        Producto pastel = Producto.registrar("Pastel de chocolate", 1.25, "Porción", 5, java.sql.Date.valueOf(LUNES));
        asignarId(pastel, 3);
        menuLunes.agregarItem(pastel);
        OfertaMenu oferta = menuLunes.buscarOferta(3);

        assertTrue(pastel.estaVigente(LUNES));
        assertFalse(pastel.estaVigente(MARTES));
        assertTrue(oferta.esComprable(1, LUNES));
        assertFalse(oferta.esComprable(1, MARTES));
        assertEquals(OfertaMenu.CADUCADO, oferta.estadoEn(MARTES));
    }

    @Test
    void esComprableExigeUnidadesSuficientes() {
        menuLunes.agregarItem(agua);
        OfertaMenu oferta = menuLunes.buscarOferta(2);

        assertTrue(oferta.esComprable(20, LUNES));
        assertFalse(oferta.esComprable(21, LUNES));
        assertEquals(OfertaMenu.DISPONIBLE, oferta.estadoEn(LUNES));
    }

    @Test
    void unaOfertaSinUnidadesSeMuestraComoSinStock() {
        Producto ultima = Producto.registrar("Empanada", 0.90, "Unidad", 1, java.sql.Date.valueOf(LUNES));
        asignarId(ultima, 4);
        menuLunes.agregarItem(ultima);
        ultima.reducirStock(1);

        assertEquals(OfertaMenu.SIN_STOCK, menuLunes.buscarOferta(4).estadoEn(LUNES));
    }

    /** Registrar un producto lo deja habilitado para seleccionarlo; los creados de otra forma no lo están. */
    @Test
    void registrarDejaElProductoHabilitadoYDisponible() {
        assertTrue(agua.isHabilitado());
        assertEquals("Disponible", agua.getEstado());
        assertNotNull(agua.getFechaCaducidad());
        assertFalse(new Producto("Jugo", 0.75, "Disponible", "", 3, null).isHabilitado());
    }

    @Test
    void unMenuCerradoNoRecibeOfertas() {
        menuLunes.agregarItem(arroz);
        menuLunes.publicarParaVenta();
        menuLunes.cerrarParaVenta();

        assertFalse(menuLunes.agregarItem(agua));
    }

    /** Soporte de prueba: simula el id que JPA asigna al guardar el ítem. */
    private static void asignarId(ItemMenu item, int id) {
        try {
            java.lang.reflect.Field campo = ItemMenu.class.getDeclaredField("id");
            campo.setAccessible(true);
            campo.setInt(item, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
