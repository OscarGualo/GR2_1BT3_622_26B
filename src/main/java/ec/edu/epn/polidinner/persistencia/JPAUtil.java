package ec.edu.epn.polidinner.persistencia;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Soporte técnico de acceso a datos (no pertenece al modelo de dominio).
 * Mantiene un único EntityManagerFactory de la unidad PoliDinnerPU para toda la aplicación.
 * Usuario y clave de la base se toman de las variables de entorno
 * POLIDINNER_DB_USER y POLIDINNER_DB_PASSWORD, para no subir credenciales al repositorio.
 */
public final class JPAUtil {

    private static final String UNIDAD_PERSISTENCIA = "PoliDinnerPU";
    private static EntityManagerFactory emf;

    private JPAUtil() {
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            Map<String, String> propiedades = new HashMap<>();
            String usuario = System.getenv("POLIDINNER_DB_USER");
            String clave = System.getenv("POLIDINNER_DB_PASSWORD");
            if (usuario != null && !usuario.isEmpty()) {
                propiedades.put("javax.persistence.jdbc.user", usuario);
            }
            if (clave != null && !clave.isEmpty()) {
                propiedades.put("javax.persistence.jdbc.password", clave);
            }
            emf = Persistence.createEntityManagerFactory(UNIDAD_PERSISTENCIA, propiedades);
        }
        return emf;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static synchronized void cerrar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}
