package ec.edu.epn.polidinner.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemMenuReponerStockTest {

    private Plato seco = null;


    @BeforeEach
    public void setUp() {
        seco = new Plato("Seco de pollo", 3.00, "Disponible", "Arroz, seco y maduro", 2);
        System.out.println("setUp(): " );
    }

    /** T3 (TDD): reponer porciones suma al stock y reactiva un ítem que se agotó por falta de stock. */
    @Test
    public void reponerStockDevuelveLasPorcionesYReactivaElItemAgotado() {
        seco.reducirStock(2);                         // stock 0 -> "Agotado" (regla del Inc. 2)

        seco.reponerStock(2);

        assertEquals(2, seco.getStockActual());
        assertEquals("Disponible", seco.getEstado());
    }
}
