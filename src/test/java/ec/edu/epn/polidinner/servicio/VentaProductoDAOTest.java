package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.DiaComedor;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.modelo.VentaProducto;
import ec.edu.epn.polidinner.persistencia.JPAUtil;
import ec.edu.epn.polidinner.persistencia.VentaProductoDAO;

/**
 * Comprobación de soporte de HU18 (no es una de las 12 pruebas TDD): la consulta usa las entidades reales
 * en H2, toma la fecha del comprobante y deja fuera los platos.
 */
class VentaProductoDAOTest extends BaseDatosPrueba {

    private final VentaProductoDAO dao = new VentaProductoDAO();

    @BeforeEach
    void preparar() {
        prepararBase();
    }

    @AfterEach
    void cerrar() {
        cerrarBase();
    }

    @Test
    void devuelveLosProductosPagadosEnElPeriodoSegunLaFechaDelComprobante() {
        Pedido pedido = pedidoDe(andrea, jugo, 2, arroz, 1);
        servicio.pagarAhora(pedido, LUNES);
        DiaComedor hoy = DiaComedor.delSistema();

        List<VentaProducto> ventas = dao.buscarPorFechaDePago(hoy.inicio(), hoy.fin());

        assertEquals(1, ventas.size());
        assertEquals(jugo.getId(), ventas.get(0).getReferencia());
        assertEquals(2, ventas.get(0).getCantidad());
        assertEquals("Pagado", ventas.get(0).getEstadoPedido());

        moverPago(pedido, hoy.getFecha().minusDays(8));
        assertTrue(dao.buscarPorFechaDePago(hoy.inicio(), hoy.fin()).isEmpty());
    }

    private void moverPago(Pedido pedido, LocalDate fecha) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("UPDATE Comprobante c SET c.fechaGeneracion = :fecha WHERE c.pedido.id = :id")
                    .setParameter("fecha", java.sql.Timestamp.valueOf(fecha.atTime(10, 0)))
                    .setParameter("id", pedido.getId())
                    .executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
