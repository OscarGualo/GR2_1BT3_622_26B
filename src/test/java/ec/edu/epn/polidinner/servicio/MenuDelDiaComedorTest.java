package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.ZoneId;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.DiaComedor;
import ec.edu.epn.polidinner.modelo.Menu;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Plato;
import ec.edu.epn.polidinner.persistencia.JPAUtil;
import ec.edu.epn.polidinner.persistencia.MenuDAO;

/**
 * Corrección de productos persistentes, Paquete 1a (H2): el menú se guarda y se busca con la fecha civil
 * del comedor, y el cierre de un día no afecta al siguiente. Usa miércoles y jueves: la base de
 * pruebas ya tiene el menú publicado del LUNES.
 */
class MenuDelDiaComedorTest extends BaseDatosPrueba {

    private static final ZoneId GUAYAQUIL = ZoneId.of("America/Guayaquil");

    private final MenuDAO menuDAO = new MenuDAO();
    private final DiaComedor miercoles = new DiaComedor(LUNES.plusDays(2), GUAYAQUIL);
    private final DiaComedor jueves = new DiaComedor(LUNES.plusDays(3), GUAYAQUIL);
    private PersonalComedor maria;

    @BeforeEach
    void preparar() {
        prepararBase();
        maria = new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "clave");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(maria);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @AfterEach
    void cerrar() {
        cerrarBase();
    }

    @Test
    void elMenuCreadoParaElDiaDelComedorSeEncuentraEseDiaYNoOtro() {
        Menu menu = maria.crearMenuDiario(miercoles);
        menuDAO.guardar(menu);

        Menu encontrado = menuDAO.buscarMenuDelDia(miercoles);

        assertNotNull(encontrado);
        assertEquals(menu.getId(), encontrado.getId());
        assertNull(menuDAO.buscarMenuDelDia(jueves));
    }

    @Test
    void elCierreSeBuscaPorElDiaDelComedor() {
        Menu menu = maria.crearMenuDiario(miercoles);
        menu.agregarItem(new Plato("Locro de papa", 2.50, "Disponible", "Sopa", 10));
        menu.publicarParaVenta();
        menu.cerrarParaVenta();
        menuDAO.guardar(menu);

        assertNull(menuDAO.buscarMenuDelDia(miercoles));
        assertNotNull(menuDAO.buscarMenuCerrado(miercoles));
        assertNull(menuDAO.buscarMenuCerrado(jueves));
    }
}
