package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del CU 01 Crear Menú.
 * Trazabilidad: Diagrama de clases (Fig. 7) y Diagrama de secuencia CU01.
 */
class CrearMenuTest {

    private PersonalComedor personal;

    @BeforeEach
    void prepararPersonal() {
        personal = new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "clave");
    }

    /** Mensajes 3-6: crearMenuDiario() crea un Menu con fecha de hoy, estado "Borrador" y el personal. */
    @Test
    void crearMenuDiarioCreaMenuEnBorradorConFechaDeHoy() {
        Menu menuHoy = personal.crearMenuDiario();

        assertNotNull(menuHoy);
        assertEquals("Borrador", menuHoy.getEstado());
        SimpleDateFormat dia = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(dia.format(new Date()), dia.format(menuHoy.getFecha()));
        assertSame(personal, menuHoy.getPersonal());
        assertTrue(menuHoy.obtenerItems().isEmpty());
    }

    /** Mensaje 11: <<create>> Plato(nombre, precio, "Disponible", descripcion) + porciones (stockActual, Fig. 15). */
    @Test
    void constructorPlatoSigueLaSecuencia() {
        Plato plato = new Plato("Almuerzo Ejecutivo", 2.50, "Disponible", "Sopa, segundo y jugo", 10);

        assertEquals("Almuerzo Ejecutivo", plato.getNombre());
        assertEquals(2.50, plato.getPrecio(), 0.001);
        assertEquals("Disponible", plato.getEstado());
        assertEquals("Sopa, segundo y jugo", plato.getDescripcion());
        assertEquals(10, plato.getStockActual());
    }

    /** Mensaje 13: <<create>> Producto(nombre, precio, "Disponible", descripcion, stock, fechaCaducidad). */
    @Test
    void constructorProductoSigueLaSecuencia() {
        Date caducidad = new Date();
        Producto producto = new Producto("Jugo de naranja", 0.75, "Disponible", "Botella 300 ml", 20, caducidad);

        assertEquals("Disponible", producto.getEstado());
        assertEquals(20, producto.getStock());
        assertEquals(caducidad, producto.getFechaCaducidad());
    }

    /** Mensajes 15-16: agregarItem(nuevoItem) devuelve true y la lista de ítems crece. */
    @Test
    void agregarItemDevuelveTrueYLaListaCrece() {
        Menu menuHoy = personal.crearMenuDiario();
        Plato plato = new Plato("Almuerzo Ejecutivo", 2.50, "Disponible", "", 10);
        Producto producto = new Producto("Agua", 0.50, "Disponible", "", 10, new Date());

        assertTrue(menuHoy.agregarItem(plato));
        assertTrue(menuHoy.agregarItem(producto));

        assertEquals(2, menuHoy.obtenerItems().size());
        assertSame(plato, menuHoy.obtenerItems().get(0));
    }

    @Test
    void agregarItemRechazaNuloRepetidoYMenuCerrado() {
        Menu menuHoy = personal.crearMenuDiario();
        Plato plato = new Plato("Seco de Pollo", 3.00, "Disponible", "", 10);

        assertFalse(menuHoy.agregarItem(null));
        assertTrue(menuHoy.agregarItem(plato));
        assertFalse(menuHoy.agregarItem(plato));

        menuHoy.cerrarParaVenta();
        assertFalse(menuHoy.agregarItem(new Plato("Menestra", 2.00, "Disponible", "", 10)));
        assertEquals(1, menuHoy.obtenerItems().size());
    }

    @Test
    void obtenerItemNoPermiteModificarLaListaDesdeFuera() {
        Menu menuHoy = personal.crearMenuDiario();
        assertThrows(UnsupportedOperationException.class,
                () -> menuHoy.obtenerItems().add(new Plato("X", 1, "Disponible", "", 10)));
    }

    /** Mensajes 21-22: publicarParaVenta() devuelve true y el estado cambia a "Publicado". */
    @Test
    void publicarParaVentaCambiaEstadoAPublicado() {
        Menu menuHoy = personal.crearMenuDiario();
        menuHoy.agregarItem(new Plato("Almuerzo Ejecutivo", 2.50, "Disponible", "", 10));

        assertTrue(menuHoy.publicarParaVenta());
        assertEquals("Publicado", menuHoy.getEstado());
    }

    @Test
    void publicarParaVentaFallaSiNoHayItems() {
        Menu menuHoy = personal.crearMenuDiario();

        assertFalse(menuHoy.publicarParaVenta());
        assertEquals("Borrador", menuHoy.getEstado());
    }

    @Test
    void cerrarParaVentaCambiaEstadoACerradoYYaNoSePublica() {
        Menu menuHoy = personal.crearMenuDiario();
        menuHoy.agregarItem(new Plato("Almuerzo Ejecutivo", 2.50, "Disponible", "", 10));

        menuHoy.cerrarParaVenta();

        assertEquals("Cerrado", menuHoy.getEstado());
        assertFalse(menuHoy.publicarParaVenta());
    }


    /** Mensaje 6: <<create>> Plato(nombre, precio, estado, descripcion); luego se asignan las porciones. */
    @Test
    void constructorPlatoDelMensaje6YAsignacionDeStock() {
        Plato nuevoPlato = new Plato("Almuerzo Ejecutivo", 2.50, "Disponible", "Sopa, segundo y jugo");

        assertEquals("Almuerzo Ejecutivo", nuevoPlato.getNombre());
        assertEquals(2.50, nuevoPlato.getPrecio(), 0.001);
        assertEquals("Disponible", nuevoPlato.getEstado());
        assertEquals(0, nuevoPlato.getStockActual());

        nuevoPlato.setStockActual(15);
        assertEquals(15, nuevoPlato.getStockActual());
        assertTrue(nuevoPlato.hayStock(15));
    }

    @Test
    void setStockActualRechazaValoresNegativos() {
        Plato nuevoPlato = new Plato("Seco de Pollo", 3.00, "Disponible", "");
        assertThrows(IllegalArgumentException.class, () -> nuevoPlato.setStockActual(-1));
    }
}
