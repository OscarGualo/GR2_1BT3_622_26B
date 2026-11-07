package ec.edu.epn.polidinner.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Trazabilidad: Examen 1B, HU16 Esc. 3.1 - el script de datos iniciales de la base H2 (Docker) incluye
 * «Seco de pollo» en el menú de hoy y se puede ejecutar dos veces sin duplicar nada.
 */
class DatosInicialesH2Test {

    @AfterEach
    void limpiar() {
        JPAUtil.cerrar();
        System.clearProperty("polidinner.db.url");
    }

    private void ejecutar(List<String> sentencias) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.unwrap(Session.class).doWork(conexion -> {
                try (java.sql.Statement st = conexion.createStatement()) {
                    for (String s : sentencias) {
                        st.execute(s);
                    }
                }
            });
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Test
    void elScriptCreaSecoDePolloUnaSolaVezAunqueSeEjecuteDosVeces() throws Exception {
        JPAUtil.cerrar();
        System.setProperty("polidinner.db.url", "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        String script = new String(Files.readAllBytes(Paths.get("src/main/resources/datos_iniciales_h2.sql")),
                StandardCharsets.UTF_8);
        List<String> sentencias = CargaDatosIniciales.separarSentencias(script);

        ejecutar(sentencias);
        ejecutar(sentencias);

        EntityManager em = JPAUtil.getEntityManager();
        try {
            Number seco = (Number) em.createNativeQuery("SELECT COUNT(*) FROM ItemMenu WHERE nombre = 'Seco de pollo'")
                    .getSingleResult();
            Number platos = (Number) em.createNativeQuery("SELECT COUNT(*) FROM Plato p JOIN ItemMenu i ON i.id = p.id"
                    + " WHERE i.nombre = 'Seco de pollo'").getSingleResult();
            assertEquals(1, seco.intValue());
            assertEquals(1, platos.intValue());
        } finally {
            em.close();
        }
    }

    /**
     * Corrección de productos persistentes, Paquete 1b (V38): si la venta de hoy ya se cerró, reiniciar el
     * contenedor (volver a ejecutar el script) no crea otro menú publicado ni le agrega ítems nuevos.
     */
    @Test
    void reiniciarConLaVentaDeHoyCerradaNoReabreLaVenta() throws Exception {
        JPAUtil.cerrar();
        System.setProperty("polidinner.db.url", "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        List<String> sentencias = CargaDatosIniciales.separarSentencias(new String(
                Files.readAllBytes(Paths.get("src/main/resources/datos_iniciales_h2.sql")), StandardCharsets.UTF_8));
        ejecutar(sentencias);
        int itemsAntes = contar("SELECT COUNT(*) FROM ItemMenu");
        ejecutar(java.util.Collections.singletonList(
                "UPDATE Menu SET estado = 'Cerrado' WHERE fecha = CURRENT_DATE"));

        ejecutar(sentencias);

        assertEquals(1, contar("SELECT COUNT(*) FROM Menu WHERE fecha = CURRENT_DATE"));
        assertEquals(0, contar("SELECT COUNT(*) FROM Menu WHERE fecha = CURRENT_DATE AND estado <> 'Cerrado'"));
        assertEquals(itemsAntes, contar("SELECT COUNT(*) FROM ItemMenu"));
    }

    /** Base H2 nueva con el esquema de Hibernate; devuelve las sentencias del script de datos iniciales. */
    private List<String> baseNuevaYScript() throws Exception {
        JPAUtil.cerrar();
        System.setProperty("polidinner.db.url", "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        return CargaDatosIniciales.separarSentencias(new String(
                Files.readAllBytes(Paths.get("src/main/resources/datos_iniciales_h2.sql")), StandardCharsets.UTF_8));
    }

    private void sql(String... sentencias) {
        ejecutar(java.util.Arrays.asList(sentencias));
    }

    /** La jornada de hoy pasa a ser "ayer" (menú cerrado y sus platos), para simular el arranque de otro día. */
    private void pasarAlDiaSiguiente() {
        sql("UPDATE Menu SET estado = 'Cerrado' WHERE fecha = CURRENT_DATE AND estado = 'Publicado'",
            "UPDATE Menu SET fecha = DATEADD('DAY', -1, fecha) WHERE fecha = CURRENT_DATE",
            "UPDATE Plato SET fecha_preparacion = DATEADD('DAY', -1, fecha_preparacion) WHERE fecha_preparacion = CURRENT_DATE");
    }

    /**
     * Corrección de productos persistentes, Paquete 3 (V33 y V39): la existencia de demostración se crea una
     * sola vez. Reiniciar el mismo día o arrancar otro día no le devuelve sus 30 unidades ni le renueva la
     * caducidad; el menú del día siguiente ofrece ESA misma existencia y platos nuevos de ese día.
     */
    @Test
    void laExistenciaDeDemostracionNoSeRecargaNiSeRenuevaEntreArranques() throws Exception {
        List<String> sentencias = baseNuevaYScript();
        ejecutar(sentencias);
        int jugo = contar("SELECT id FROM Producto WHERE clave_semilla = 'demo-jugo-naranja'");
        String caducidad = texto("SELECT CAST(fechaCaducidad AS VARCHAR) FROM Producto WHERE id = " + jugo);
        sql("UPDATE ItemMenu SET stockActual = 12 WHERE id = " + jugo);

        ejecutar(sentencias);
        assertEquals(12, contar("SELECT stockActual FROM ItemMenu WHERE id = " + jugo));

        pasarAlDiaSiguiente();
        ejecutar(sentencias);

        assertEquals(1, contar("SELECT COUNT(*) FROM ItemMenu WHERE nombre = 'Jugo de naranja'"));
        assertEquals(12, contar("SELECT stockActual FROM ItemMenu WHERE id = " + jugo));
        assertEquals(caducidad, texto("SELECT CAST(fechaCaducidad AS VARCHAR) FROM Producto WHERE id = " + jugo));
        assertEquals(2, contar("SELECT COUNT(*) FROM oferta_menu WHERE item_id = " + jugo));
        assertEquals(1, contar("SELECT COUNT(*) FROM oferta_menu o JOIN Menu m ON m.id = o.menu_id"
                + " WHERE m.fecha = CURRENT_DATE AND o.item_id = " + jugo));
        assertEquals(2, contar("SELECT COUNT(*) FROM ItemMenu i JOIN Plato p ON p.id = i.id"
                + " WHERE i.nombre = 'Arroz con pollo'"));
        assertEquals(1, contar("SELECT COUNT(*) FROM Menu WHERE fecha = CURRENT_DATE"));
    }

    /**
     * Corrección de productos persistentes, Paquete 3 (V32 y V36): una base anterior (ítems con menu_id) se
     * migra sin duplicar nada aunque el script se ejecute varias veces; los saldos, unidades y precios no
     * cambian; el producto del menú abierto de hoy sigue habilitado y el de un menú cerrado queda histórico;
     * un retiro antiguo (Agotado con unidades) pasa a la oferta y el producto conserva sus unidades.
     */
    @Test
    void laMigracionDeUnaBaseAnteriorEsConservadoraYRepetible() throws Exception {
        List<String> sentencias = baseNuevaYScript();
        ejecutar(sentencias);
        int menuHoy = contar("SELECT id FROM Menu WHERE fecha = CURRENT_DATE");
        sql("INSERT INTO Menu (fecha, estado, personal_cedula) VALUES (DATEADD('DAY', -1, CURRENT_DATE), 'Cerrado', '1700000001')");
        int menuAyer = contar("SELECT id FROM Menu WHERE fecha = DATEADD('DAY', -1, CURRENT_DATE)");
        sql("INSERT INTO ItemMenu (nombre, precio, estado, descripcion, stockActual, version, menu_id)"
                + " VALUES ('Pastel', 1.25, 'Agotado', 'Porción', 3, 0, " + menuAyer + ")",
            "INSERT INTO Producto (id, fechaCaducidad, habilitado) SELECT id, DATEADD('DAY', 5, CURRENT_DATE), FALSE"
                + " FROM ItemMenu WHERE nombre = 'Pastel'",
            "INSERT INTO ItemMenu (nombre, precio, estado, descripcion, stockActual, version, menu_id)"
                + " VALUES ('Galletas', 0.40, 'Disponible', 'Paquete', 7, 0, " + menuHoy + ")",
            "INSERT INTO Producto (id, fechaCaducidad, habilitado) SELECT id, DATEADD('DAY', 30, CURRENT_DATE), FALSE"
                + " FROM ItemMenu WHERE nombre = 'Galletas'",
            "INSERT INTO ItemMenu (nombre, precio, estado, descripcion, stockActual, version, menu_id)"
                + " VALUES ('Locro', 2.50, 'Disponible', 'Sopa', 9, 0, " + menuAyer + ")",
            "INSERT INTO Plato (id) SELECT id FROM ItemMenu WHERE nombre = 'Locro'");
        String antes = foto();

        ejecutar(sentencias);
        int ofertas = contar("SELECT COUNT(*) FROM oferta_menu");
        ejecutar(sentencias);

        assertEquals(ofertas, contar("SELECT COUNT(*) FROM oferta_menu"));
        assertEquals(antes, foto());
        assertEquals(1, contar("SELECT COUNT(*) FROM oferta_menu o JOIN ItemMenu i ON i.id = o.item_id"
                + " WHERE i.nombre = 'Pastel' AND o.retirada = TRUE AND o.menu_id = " + menuAyer));
        assertEquals("Disponible", texto("SELECT estado FROM ItemMenu WHERE nombre = 'Pastel'"));
        assertEquals(3, contar("SELECT stockActual FROM ItemMenu WHERE nombre = 'Pastel'"));
        assertEquals(0, contar("SELECT CASE WHEN habilitado THEN 1 ELSE 0 END FROM Producto p JOIN ItemMenu i"
                + " ON i.id = p.id WHERE i.nombre = 'Pastel'"));
        assertEquals(1, contar("SELECT CASE WHEN habilitado THEN 1 ELSE 0 END FROM Producto p JOIN ItemMenu i"
                + " ON i.id = p.id WHERE i.nombre = 'Galletas'"));
        assertEquals(1, contar("SELECT COUNT(*) FROM Plato p JOIN ItemMenu i ON i.id = p.id"
                + " WHERE i.nombre = 'Locro' AND p.fecha_preparacion = DATEADD('DAY', -1, CURRENT_DATE)"));
    }

    /** Totales que la migración no debe alterar: ítems, unidades, precios, saldos y pedidos. */
    private String foto() {
        return contar("SELECT COUNT(*) FROM ItemMenu") + "/" + contar("SELECT COALESCE(SUM(stockActual), 0) FROM ItemMenu")
                + "/" + texto("SELECT CAST(COALESCE(SUM(precio), 0) AS VARCHAR) FROM ItemMenu")
                + "/" + texto("SELECT CAST(COALESCE(SUM(saldo), 0) AS VARCHAR) FROM Policuenta")
                + "/" + contar("SELECT COUNT(*) FROM Pedido");
    }

    private String texto(String sql) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return String.valueOf(em.createNativeQuery(sql).getSingleResult());
        } finally {
            em.close();
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
}
