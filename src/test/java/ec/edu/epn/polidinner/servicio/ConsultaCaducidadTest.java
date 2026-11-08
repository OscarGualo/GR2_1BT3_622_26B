package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.ExistenciaProducto;
import ec.edu.epn.polidinner.modelo.FranjaCaducidad;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.persistencia.JPAUtil;

/**
 * Comprobación de soporte de HU19 (no es una de las 12 pruebas TDD), con H2 y Hibernate reales: los
 * registros de Producto guardados se leen, se clasifican con ClasificadorCaducidad y se filtran con
 * FiltroCaducidad. Usa el código de Jorge (PP01) y de Jean (PU05 y PU06).
 */
class ConsultaCaducidadTest extends BaseDatosPrueba {

    private final ServicioCaducidad consulta = new ServicioCaducidad();

    @BeforeEach
    void preparar() {
        prepararBase();
    }

    @AfterEach
    void cerrar() {
        cerrarBase();
    }

    @Test
    void consultaCadaFranjaConLosRegistrosGuardados() {
        Producto pastel = registrar("Pastel porción", 2, LUNES, true);
        Producto agua = registrar("Agua 500 ml", 3, LUNES.plusDays(1), true);
        Producto empanada = registrar("Empanada unidad", 2, LUNES.plusDays(3), true);
        registrar("Agua 500 ml", 7, LUNES.plusDays(4), true);
        registrar("Galletas 100 g", 0, LUNES.plusDays(1), true);
        registrar("Jugo 250 ml", 5, LUNES.plusDays(1), false);
        Producto yogur = registrar("Yogur 150 g", 6, LUNES.minusDays(1), true);

        assertEquals(List.of(pastel.getId()), referencias(FranjaCaducidad.CADUCAN_HOY));
        assertEquals(List.of(agua.getId(), empanada.getId()), referencias(FranjaCaducidad.PROXIMOS_3_DIAS));
        assertEquals(List.of(yogur.getId()), referencias(FranjaCaducidad.CADUCADOS));
    }

    private List<Integer> referencias(FranjaCaducidad franja) {
        return consulta.consultar(franja, LUNES).stream().map(ExistenciaProducto::getReferencia).toList();
    }

    private Producto registrar(String nombre, int stock, LocalDate caducidad, boolean habilitado) {
        java.sql.Date fecha = java.sql.Date.valueOf(caducidad);
        Producto producto = habilitado
                ? Producto.registrar(nombre, 1.00, nombre, stock, fecha)
                : new Producto(nombre, 1.00, "Disponible", nombre, stock, fecha);
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(producto);
            em.getTransaction().commit();
            return producto;
        } finally {
            em.close();
        }
    }
}