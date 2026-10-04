package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de la evolución de ItemMenu en el incremento 2: stockActual, hayStock() y reducirStock().
 * Trazabilidad: Diagrama de clases (Fig. 15) y Diagrama de secuencia CU03 - mensajes 15-16, 37-38 y 41.
 */
class ItemMenuStockTest {

    @Test
    void hayStockSoloSiAlcanzaLaCantidad() {
        Plato arroz = new Plato("Arroz con pollo", 3.50, "Disponible", "", 2);

        assertTrue(arroz.hayStock(1));
        assertTrue(arroz.hayStock(2));
        assertFalse(arroz.hayStock(3));
        assertFalse(arroz.hayStock(0));
    }

    /** Conexión con el CU 02: un ítem retirado ("Agotado") no tiene stock para pedir aunque tenga unidades. */
    @Test
    void unItemRetiradoNoTieneStockDisponible() {
        Plato seco = new Plato("Seco de Pollo", 3.00, "Disponible", "", 10);
        new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "clave").eliminarPlato(seco);

        assertFalse(seco.hayStock(1));
    }

    /** Mensaje 41: reducirStock(cantidad) descuenta las unidades vendidas. */
    @Test
    void reducirStockDescuentaLasUnidades() {
        Producto jugo = new Producto("Jugo de naranja", 0.75, "Disponible", "", 20, new Date());

        jugo.reducirStock(1);

        assertEquals(19, jugo.getStockActual());
        assertEquals(19, jugo.getStock());
        assertEquals("Disponible", jugo.getEstado());
    }

    @Test
    void reducirStockHastaCeroDejaElItemAgotado() {
        Plato arroz = new Plato("Arroz con pollo", 3.50, "Disponible", "", 1);

        arroz.reducirStock(1);

        assertEquals(0, arroz.getStockActual());
        assertEquals("Agotado", arroz.getEstado());
    }

    @Test
    void reducirStockSinStockSuficienteLanzaExcepcionYNoCambiaNada() {
        Plato arroz = new Plato("Arroz con pollo", 3.50, "Disponible", "", 1);

        assertThrows(IllegalStateException.class, () -> arroz.reducirStock(2));
        assertEquals(1, arroz.getStockActual());
    }
}
