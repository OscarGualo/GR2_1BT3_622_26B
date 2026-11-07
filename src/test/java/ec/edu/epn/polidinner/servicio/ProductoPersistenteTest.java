package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.Comprobante;
import ec.edu.epn.polidinner.modelo.ItemMenu;
import ec.edu.epn.polidinner.modelo.Menu;
import ec.edu.epn.polidinner.modelo.OfertaMenu;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.persistencia.JPAUtil;
import ec.edu.epn.polidinner.persistencia.OfertaMenuDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

/**
 * Corrección de productos persistentes, Paquete 2 (H2 con Hibernate real): un producto conserva su identidad
 * y sus existencias entre jornadas; la oferta del día se revalida al pagar (inmediato y programado).
 * Las jornadas se controlan con fechas fijas (LUNES y MARTES), sin cambiar el reloj del equipo.
 */
class ProductoPersistenteTest extends BaseDatosPrueba {

    private static final LocalDate MARTES = LUNES.plusDays(1);

    private final OfertaMenuDAO ofertas = new OfertaMenuDAO();
    private final ProductoDAO productos = new ProductoDAO();

    @BeforeEach
    void preparar() {
        prepararBase();
    }

    @AfterEach
    void cerrar() {
        cerrarBase();
    }

    /**
     * Ejemplo verificable de la guía (V12, V14, V15, V16, V17): 20 aguas en el menú A; se venden 6 (quedan 14);
     * se cierra A; el menú B de otra jornada ofrece la MISMA existencia con 14; se venden 2 (12) y la
     * cancelación las devuelve (14).
     */
    @Test
    void lasExistenciasDeUnProductoSeConservanEntreJornadas() {
        Producto agua = registrar("Agua sin gas", 0.60, "Botella 500 ml", 20, LUNES.plusDays(7));
        ofrecer(menuDelLunes, agua);

        servicio.pagarAhora(pedidoDe(andrea, agua, 6), LUNES);
        assertEquals(14, stock(agua));
        assertEquals(10.00 - 3.60, saldo("2024-0153"), 0.001);

        cerrarVenta(menuDelLunes);
        assertEquals(14, stock(agua));
        assertEquals(ServicioPedidos.VENTA_CERRADA,
                assertThrows(ReglaPedidoException.class,
                        () -> servicio.pagarAhora(pedidoDe(luis, agua, 1), LUNES)).getMessage());

        Menu menuDelMartes = publicarMenu(MARTES, agua);
        assertEquals(agua.getId(), menuDelMartes.buscarOferta(agua.getId()).getItem().getId());
        assertEquals(14, stock(agua));

        Comprobante comprobante = servicio.pagarAhora(pedidoDe(luis, agua, 2), MARTES);
        assertEquals(12, stock(agua));

        servicio.cancelar(comprobante.getPedido().getId(), luis.getCedula(), LocalDateTime.of(MARTES, LocalTime.NOON));
        assertEquals(14, stock(agua));
        assertEquals(8.00, saldo("2024-0201"), 0.001);
        assertEquals(1, contar("SELECT COUNT(*) FROM ItemMenu WHERE nombre = 'Agua sin gas'"));
    }

    /** V13: un pedido mixto descuenta porciones y unidades en la misma transacción. */
    @Test
    void unPedidoMixtoDescuentaPlatoYProducto() {
        servicio.pagarAhora(pedidoDe(andrea, arroz, 1, jugo, 2), LUNES);

        assertEquals(19, stock(arroz));
        assertEquals(28, stock(jugo));
        assertEquals(10.00 - 5.00, saldo("2024-0153"), 0.001);
    }

    /** V08: un producto registrado pero no ofrecido no se cobra aunque se envíe su id. */
    @Test
    void unProductoRegistradoQueNoSeOfrecioNoSeCobra() {
        Producto papitas = registrar("Papitas", 0.50, "Funda 45 g", 10, LUNES.plusDays(30));

        String mensaje = assertThrows(ReglaPedidoException.class,
                () -> servicio.pagarAhora(pedidoDe(andrea, papitas, 1), LUNES)).getMessage();

        assertEquals("\"Papitas\" no está en el menú de hoy. Quítelo de su pedido para continuar.", mensaje);
        assertEquals(10, stock(papitas));
        assertEquals(10.00, saldo("2024-0153"), 0.001);
    }

    /** V19 y V21: retirar la oferta de hoy impide comprar hoy, conserva las unidades y no impide ofrecerlo mañana. */
    @Test
    void elRetiroEsSoloDeLaOfertaDelDia() {
        retirar(menuDelLunes, jugo);

        String mensaje = assertThrows(ReglaPedidoException.class,
                () -> servicio.pagarAhora(pedidoDe(andrea, jugo, 1), LUNES)).getMessage();
        assertEquals("\"Jugo de naranja\" fue retirado del menú. Quítelo de su pedido para continuar.", mensaje);
        assertEquals(30, stock(jugo));

        publicarMenu(MARTES, jugo);
        servicio.pagarAhora(pedidoDe(andrea, jugo, 1), MARTES);
        assertEquals(29, stock(jugo));
        assertFalse(ofertaGuardada(menuDelLunes, jugo).estaOfrecida());
    }

    /** V20: una cancelación posterior al retiro devuelve las unidades sin reactivar la oferta retirada. */
    @Test
    void cancelarDespuesDelRetiroNoReactivaLaOferta() {
        Comprobante comprobante = servicio.pagarAhora(pedidoDe(andrea, jugo, 2), LUNES);
        retirar(menuDelLunes, jugo);

        servicio.cancelar(comprobante.getPedido().getId(), andrea.getCedula(), a(12, 0));

        assertEquals(30, stock(jugo));
        assertFalse(ofertaGuardada(menuDelLunes, jugo).estaOfrecida());
        assertThrows(ReglaPedidoException.class, () -> servicio.pagarAhora(pedidoDe(luis, jugo, 1), LUNES));
    }

    /** V24 (inmediato): un producto que caducó mientras estaba en el carrito no se cobra. */
    @Test
    void unProductoCaducadoEnElCarritoNoSeCobra() {
        Producto pastel = registrar("Pastel de chocolate", 1.25, "Porción", 5, LUNES);
        publicarMenu(MARTES, pastel);

        String mensaje = assertThrows(ReglaPedidoException.class,
                () -> servicio.pagarAhora(pedidoDe(andrea, pastel, 1), MARTES)).getMessage();

        assertEquals("\"Pastel de chocolate\" caducó. Quítelo de su pedido para continuar.", mensaje);
        assertEquals(5, stock(pastel));
        assertEquals(10.00, saldo("2024-0153"), 0.001);
    }

    /** V24 y V27 (programado): el rechazo por caducidad tampoco consume cupo de la franja. */
    @Test
    void unPedidoProgramadoConUnProductoCaducadoNoConsumeCupo() {
        Producto pastel = registrar("Pastel de chocolate", 1.25, "Porción", 5, LUNES);
        publicarMenu(MARTES, pastel);
        LocalDateTime martesDiezAm = LocalDateTime.of(MARTES, LocalTime.of(10, 0));

        assertThrows(ReglaPedidoException.class,
                () -> servicio.programarYPagar(pedidoDe(andrea, pastel, 1), MARTES, franja(12, 30), martesDiezAm));

        assertEquals(5, stock(pastel));
        assertEquals(10.00, saldo("2024-0153"), 0.001);
        assertEquals(0, contar("SELECT COALESCE(SUM(asignados), 0) FROM franja_retiro WHERE fecha = DATE '"
                + MARTES + "'"));
    }

    /** V27: un pedido programado de un producto descuenta del mismo stock y ocupa su lugar en la franja. */
    @Test
    void unPedidoProgramadoUsaLasMismasExistencias() {
        programar(andrea, a(10, 0), franja(12, 30), jugo, 2);

        assertEquals(28, stock(jugo));
        assertEquals(1, asignados(franja(12, 30)));
    }

    /** V25: dos clientes compiten por la última unidad; solo uno paga y no queda stock ni saldo incoherente. */
    @Test
    void dosPagosPorLaUltimaUnidadSoloConfirmaUno() throws Exception {
        Producto ultima = registrar("Empanada", 0.90, "Unidad", 1, LUNES.plusDays(2));
        ofrecer(menuDelLunes, ultima);
        Pedido deAndrea = pedidoDe(andrea, ultima, 1);
        Pedido deLuis = pedidoDe(luis, ultima, 1);
        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> resultados = new ArrayList<>();
            for (Pedido pedido : new Pedido[]{deAndrea, deLuis}) {
                resultados.add(hilos.submit((Callable<Boolean>) () -> {
                    salida.await();
                    try {
                        servicio.pagarAhora(pedido, LUNES);
                        return true;
                    } catch (RuntimeException e) {
                        return false;
                    }
                }));
            }
            salida.countDown();
            int pagados = 0;
            for (Future<Boolean> resultado : resultados) {
                pagados += resultado.get() ? 1 : 0;
            }
            assertEquals(1, pagados);
        } finally {
            hilos.shutdownNow();
        }
        assertEquals(0, stock(ultima));
        assertEquals(18.00 - 0.90, saldo("2024-0153") + saldo("2024-0201"), 0.001);
    }

    /** V35: dos existencias con el mismo nombre y distinta caducidad son independientes. */
    @Test
    void dosExistenciasConElMismoNombreSonIndependientes() {
        Producto pastelHoy = registrar("Pastel", 1.25, "Porción", 4, LUNES);
        Producto pastelNuevo = registrar("Pastel", 1.25, "Porción", 6, LUNES.plusDays(3));
        ofrecer(menuDelLunes, pastelHoy);
        ofrecer(menuDelLunes, pastelNuevo);

        Comprobante comprobante = servicio.pagarAhora(pedidoDe(andrea, pastelNuevo, 2), LUNES);
        assertEquals(4, stock(pastelHoy));
        assertEquals(4, stock(pastelNuevo));

        servicio.cancelar(comprobante.getPedido().getId(), andrea.getCedula(), a(12, 0));
        assertEquals(4, stock(pastelHoy));
        assertEquals(6, stock(pastelNuevo));
    }

    /** V36 y V40: un producto histórico (anterior a la corrección) no se puede seleccionar para nuevos menús. */
    @Test
    void unProductoHistoricoNoEsSeleccionable() {
        Producto historico = guardar(new Producto("Galletas", 0.40, "Disponible", "Paquete", 12,
                java.sql.Date.valueOf(LUNES.plusDays(60))));
        Producto vigente = registrar("Agua sin gas", 0.60, "Botella 500 ml", 20, LUNES.plusDays(7));

        List<Integer> seleccionables = ids(productos.listarSeleccionables(LUNES, menuDelLunes));

        assertFalse(seleccionables.contains(historico.getId()));
        assertTrue(seleccionables.contains(vigente.getId()));
        assertFalse(seleccionables.contains(jugo.getId()), "el jugo ya está en el menú del lunes");
        assertTrue(ids(productos.listar(ProductoDAO.Filtro.HISTORICOS, LUNES)).contains(historico.getId()));
        assertFalse(ids(productos.listar(ProductoDAO.Filtro.VIGENTES, LUNES)).contains(historico.getId()));
    }

    /** V41: la cancelación de un pedido con un producto histórico repone sobre esa misma existencia. */
    @Test
    void cancelarUnPedidoDeUnProductoHistoricoReponeSobreElOriginal() {
        Producto historico = guardar(new Producto("Galletas", 0.40, "Disponible", "Paquete", 12,
                java.sql.Date.valueOf(LUNES.plusDays(60))));
        ofrecer(menuDelLunes, historico);
        Comprobante comprobante = servicio.pagarAhora(pedidoDe(andrea, historico, 3), LUNES);
        assertEquals(9, stock(historico));

        servicio.cancelar(comprobante.getPedido().getId(), andrea.getCedula(), a(12, 0));

        assertEquals(12, stock(historico));
        assertEquals(10.00, saldo("2024-0153"), 0.001);
        assertEquals(1, contar("SELECT COUNT(*) FROM ItemMenu WHERE nombre = 'Galletas'"));
    }

    /** V07 (persistido): el índice único menú + ítem impide ofrecer dos veces el mismo producto. */
    @Test
    void laBaseImpideOfrecerDosVecesElMismoProductoEnUnMenu() {
        Producto agua = registrar("Agua sin gas", 0.60, "Botella 500 ml", 20, LUNES.plusDays(7));
        ofrecer(menuDelLunes, agua);

        assertThrows(RuntimeException.class, () -> {
            EntityManager em = JPAUtil.getEntityManager();
            try {
                em.getTransaction().begin();
                em.createNativeQuery("INSERT INTO oferta_menu (menu_id, item_id, retirada, version) VALUES (?, ?, FALSE, 0)")
                        .setParameter(1, menuDelLunes.getId()).setParameter(2, agua.getId()).executeUpdate();
                em.getTransaction().commit();
            } finally {
                em.close();
            }
        });
        assertEquals(1, contar("SELECT COUNT(*) FROM oferta_menu WHERE item_id = " + agua.getId()));
    }

    // ---------------------------------------------------------------- soporte

    private Producto registrar(String nombre, double precio, String presentacion, int unidades, LocalDate caducidad) {
        return guardar(Producto.registrar(nombre, precio, presentacion, unidades, java.sql.Date.valueOf(caducidad)));
    }

    private Producto guardar(Producto producto) {
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

    /** Ofrece una existencia ya guardada en un menú existente (como «Agregar producto del inventario»). */
    private void ofrecer(Menu menu, ItemMenu item) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Menu gestionado = em.find(Menu.class, menu.getId());
            assertTrue(gestionado.agregarItem(em.find(ItemMenu.class, item.getId())));
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    private void retirar(Menu menu, ItemMenu item) {
        try (Transaccion tx = Transaccion.iniciar()) {
            ofertas.buscar(menu.getId(), item.getId(), tx).retirar();
            tx.confirmar();
        }
    }

    private void cerrarVenta(Menu menu) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            assertTrue(em.find(Menu.class, menu.getId()).cerrarParaVenta());
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    private OfertaMenu ofertaGuardada(Menu menu, ItemMenu item) {
        try (Transaccion tx = Transaccion.iniciar()) {
            OfertaMenu oferta = ofertas.buscar(menu.getId(), item.getId(), tx);
            assertNotNull(oferta);
            return oferta;
        }
    }

    private int contar(String sql) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return ((Number) em.createNativeQuery(sql).getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    private static List<Integer> ids(List<Producto> lista) {
        List<Integer> ids = new ArrayList<>();
        for (Producto producto : lista) {
            ids.add(producto.getId());
        }
        return ids;
    }
}
