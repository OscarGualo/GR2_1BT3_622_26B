package ec.edu.epn.polidinner.persistencia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Session;

/**
 * Soporte técnico (no pertenece al modelo): carga los datos iniciales al desplegar la aplicación,
 * para que el contenedor Docker quede listo para usarse sin ejecutar scripts a mano.
 * Se activa con la variable de entorno POLIDINNER_DATOS_INICIALES, que indica el script del classpath
 * (por ejemplo datos_iniciales_h2.sql). El script debe poder repetirse sin duplicar datos.
 */
public final class CargaDatosIniciales {

    static final String VARIABLE_SCRIPT = "POLIDINNER_DATOS_INICIALES";

    private CargaDatosIniciales() {
    }

    /** Ejecuta el script indicado en POLIDINNER_DATOS_INICIALES; sin la variable no hace nada. */
    public static int cargarSiCorresponde() throws IOException {
        String script = System.getenv(VARIABLE_SCRIPT);
        if (script == null || script.isEmpty()) {
            return 0;
        }
        List<String> sentencias = separarSentencias(leerRecurso(script));
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.unwrap(Session.class).doWork(conexion -> {
                try (Statement st = conexion.createStatement()) {
                    for (String sentencia : sentencias) {
                        st.execute(sentencia);
                    }
                }
            });
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
        return sentencias.size();
    }

    /** Divide el script en sentencias terminadas en ';' e ignora las líneas de comentario (--). */
    static List<String> separarSentencias(String script) {
        StringBuilder sinComentarios = new StringBuilder();
        for (String linea : script.split("\\R")) {
            if (!linea.trim().startsWith("--")) {
                sinComentarios.append(linea).append('\n');
            }
        }
        List<String> sentencias = new ArrayList<>();
        for (String parte : sinComentarios.toString().split(";")) {
            if (!parte.trim().isEmpty()) {
                sentencias.add(parte.trim());
            }
        }
        return sentencias;
    }

    private static String leerRecurso(String nombre) throws IOException {
        InputStream entrada = CargaDatosIniciales.class.getClassLoader().getResourceAsStream(nombre);
        if (entrada == null) {
            throw new IOException("No se encontró el script de datos iniciales: " + nombre);
        }
        StringBuilder texto = new StringBuilder();
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                texto.append(linea).append('\n');
            }
        }
        return texto.toString();
    }
}
