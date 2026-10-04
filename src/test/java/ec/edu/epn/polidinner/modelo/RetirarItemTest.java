package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del CU 02 Retirar plato y/o producto del menú.
 * Trazabilidad: Diagrama de clases (Fig. 7) y Diagrama de secuencia CU02.
 */
class RetirarItemTest {

    private PersonalComedor personal;

    @BeforeEach
    void prepararPersonal() {
        personal = new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "clave");
    }

    /** Diagrama de clases: ItemMenu.cambiarEstado() cambia el estado del ítem. */
    @Test
    void cambiarEstadoCambiaElEstado() {
        Plato plato = new Plato("Seco de Pollo", 3.00, "Disponible", "", 10);

        plato.cambiarEstado("Agotado");

        assertEquals("Agotado", plato.getEstado());
    }

    @Test
    void cambiarEstadoRechazaEstadoVacioONulo() {
        Plato plato = new Plato("Seco de Pollo", 3.00, "Disponible", "", 10);

        assertThrows(IllegalArgumentException.class, () -> plato.cambiarEstado(null));
        assertThrows(IllegalArgumentException.class, () -> plato.cambiarEstado("  "));
        assertEquals("Disponible", plato.getEstado());
    }

    /** Mensajes 12-13: eliminarPlato(plato) → cambiarEstado("Agotado"). */
    @Test
    void eliminarPlatoDejaElPlatoAgotado() {
        Plato plato = new Plato("Seco de Pollo", 3.00, "Disponible", "Arroz, pollo y menestra", 10);

        personal.eliminarPlato(plato);

        assertEquals("Agotado", plato.getEstado());
    }

    /** Mensajes 14-15: eliminarProducto(producto) → cambiarEstado("Agotado"); no altera el stock. */
    @Test
    void eliminarProductoDejaElProductoAgotadoYConservaElStock() {
        Producto producto = new Producto("Jugo de naranja", 0.75, "Disponible", "Botella 300 ml", 20, new Date());

        personal.eliminarProducto(producto);

        assertEquals("Agotado", producto.getEstado());
        assertEquals(20, producto.getStock());
    }

    /**
     * Caso de prueba del informe: "Seco de Pollo" disponible se retira; el registro no se borra
     * (sigue en obtenerItems(), mensajes 5-6) pero ya no aparece entre los ítems disponibles (mensaje 7).
     */
    @Test
    void secoDePolloRetiradoNoApareceEntreLosDisponibles() {
        Menu menuHoy = personal.crearMenuDiario();
        Plato seco = new Plato("Seco de Pollo", 3.00, "Disponible", "", 10);
        Plato almuerzo = new Plato("Almuerzo Ejecutivo", 2.50, "Disponible", "", 10);
        menuHoy.agregarItem(seco);
        menuHoy.agregarItem(almuerzo);
        menuHoy.publicarParaVenta();

        personal.eliminarPlato(seco);

        assertEquals(2, menuHoy.obtenerItems().size());
        List<ItemMenu> disponibles = menuHoy.obtenerItems().stream()
                .filter(i -> "Disponible".equals(i.getEstado()))
                .collect(Collectors.toList());
        assertFalse(disponibles.contains(seco));
        assertTrue(disponibles.contains(almuerzo));
        assertEquals("Publicado", menuHoy.getEstado());
    }
}
