package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.ClienteUniversitario;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.persistencia.JPAUtil;
import ec.edu.epn.polidinner.persistencia.PedidoDAO;

/**
 * Trazabilidad: Examen 1B, HU13 «Ver pedidos programados por franja» (CA1, CA2) y
 * HU14 «Consultar pedidos programados» (CA1, CA2, CA3). Las restricciones de acceso (HU13 CA3 y
 * HU14 Esc. 3.2) las resuelven FiltroPersonal y FiltroCliente y se prueban en FiltrosAccesoTest.
 */
class PedidosProgramadosTest extends BaseDatosPrueba {

    private final ServicioFranjas franjasDelServicio = new ServicioFranjas();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private ClienteUniversitario carla;

    @BeforeEach
    void preparar() {
        prepararBase();
        carla = crearCliente("1750000003", "Carla", "Ruiz", "2024-0318", 100.00);
    }

    @AfterEach
    void limpiar() {
        cerrarBase();
    }

    @Test
    void hu13_esc_1_1_agrupaPorFranjaEnOrdenDeHoraConSusCodigos() {
        programar(carla, a(10, 0), franja(12, 30), arroz, 1);
        programar(carla, a(10, 0), franja(12, 30), arroz, 1);
        programar(carla, a(10, 0), franja(12, 30), arroz, 1);
        Pedido ultimo = programar(carla, a(10, 0), franja(12, 45), seco, 1);

        var resumen = franjasDelServicio.pedidosProgramadosDelDia(LUNES);

        assertEquals(2, resumen.size());
        assertEquals("12:30–12:45", resumen.get(0).getEtiqueta());
        assertEquals(3, resumen.get(0).getCantidadPedidos());
        assertEquals("12:45–13:00", resumen.get(1).getEtiqueta());
        assertEquals(1, resumen.get(1).getCantidadPedidos());
        assertEquals(String.format("PD-%04d", ultimo.getId()),
                resumen.get(1).getPedidos().get(0).getComprobante().getCodigo());
    }

    @Test
    void hu13_esc_1_2_unDiaSinPedidosProgramadosDevuelveListaVacia() {
        assertTrue(franjasDelServicio.pedidosProgramadosDelDia(LUNES).isEmpty());
    }

    @Test
    void hu13_esc_2_1_sumaLosProductosDeCadaFranja() {
        programar(carla, a(10, 0), franja(12, 30), arroz, 1, jugo, 2);
        programar(carla, a(10, 0), franja(12, 30), arroz, 1, jugo, 1);

        var franja = franjasDelServicio.pedidosProgramadosDelDia(LUNES).get(0);

        assertEquals(2, franja.getTotales().get("Arroz con pollo"));
        assertEquals(3, franja.getTotales().get("Jugo de naranja"));
    }

    @Test
    void hu13_esc_2_2_excluyeUnPedidoCanceladoYRecalculaLosTotales() {
        Pedido deAndrea = programar(andrea, a(10, 0), franja(12, 30), arroz, 1, jugo, 2);
        programar(carla, a(10, 0), franja(12, 30), arroz, 1, jugo, 1);

        servicio.cancelar(deAndrea.getId(), andrea.getCedula(), a(11, 30));

        var franja = franjasDelServicio.pedidosProgramadosDelDia(LUNES).get(0);
        assertEquals(1, franja.getCantidadPedidos());
        assertEquals(1, franja.getTotales().get("Arroz con pollo"));
        assertEquals(1, franja.getTotales().get("Jugo de naranja"));
    }

    @Test
    void hu14_esc_1_1_listaLosPendientesOrdenadosPorHoraDeRetiro() {
        Pedido pd0105 = programar(andrea, a(10, 0), franja(14, 45), jugo, 1);
        Pedido pd0101 = programar(andrea, a(10, 0), franja(12, 30), arroz, 1);

        List<Pedido> lista = pedidoDAO.buscarProgramadosPendientes(andrea.getCedula());

        assertEquals(2, lista.size());
        assertEquals(pd0101.getId(), lista.get(0).getId());
        assertEquals(pd0105.getId(), lista.get(1).getId());
        assertEquals("12:30", lista.get(0).getFranja().getHoraInicio().toString());
        assertEquals(1, lista.get(0).getDetalles().size());
    }

    @Test
    void hu14_esc_1_2_sinPedidosProgramadosLaListaEstaVacia() {
        assertTrue(pedidoDAO.buscarProgramadosPendientes(andrea.getCedula()).isEmpty());
    }

    @Test
    void hu14_esc_2_1_ocultaUnPedidoCancelado() {
        Pedido pd0105 = programar(andrea, a(10, 0), franja(14, 45), jugo, 1);
        Pedido pd0101 = programar(andrea, a(10, 0), franja(12, 30), arroz, 1);

        servicio.cancelar(pd0105.getId(), andrea.getCedula(), a(11, 0));

        List<Pedido> lista = pedidoDAO.buscarProgramadosPendientes(andrea.getCedula());
        assertEquals(1, lista.size());
        assertEquals(pd0101.getId(), lista.get(0).getId());
    }

    @Test
    void hu14_esc_2_2_ocultaUnPedidoEntregado() {
        Pedido pd0101 = programar(andrea, a(10, 0), franja(12, 30), arroz, 1);
        var em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("UPDATE Pedido p SET p.estado = 'Entregado' WHERE p.id = :id")
                    .setParameter("id", pd0101.getId()).executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        assertTrue(pedidoDAO.buscarProgramadosPendientes(andrea.getCedula()).isEmpty());
    }

    @Test
    void hu14_esc_3_1_noMuestraLosPedidosDeOtroEstudiante() {
        programar(luis, a(10, 0), franja(13, 0), seco, 1);
        Pedido propio = programar(andrea, a(10, 0), franja(12, 30), arroz, 1);

        List<Pedido> lista = pedidoDAO.buscarProgramadosPendientes(andrea.getCedula());

        assertEquals(1, lista.size());
        assertEquals(propio.getId(), lista.get(0).getId());
    }

    @Test
    void hu14_losPedidosInmediatosDelRelease1NoSeListanComoProgramados() {
        // un pedido sin franja (compra inmediata) no tiene hora de retiro que consultar
        assertTrue(pedidoDAO.buscarProgramadosPendientes(carla.getCedula()).isEmpty());
    }
}
