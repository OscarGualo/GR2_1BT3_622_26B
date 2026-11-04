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
}
