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
 * POLIDINNER_DB_URL permite cambiar la base sin tocar persistence.xml: en el contenedor Docker
 * apunta a una base H2 embebida (jdbc:h2:...) y en desarrollo, si no existe, se usa SQL Server.
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
            propiedades.putAll(propiedadesDeUrl(System.getenv("POLIDINNER_DB_URL")));
            emf = Persistence.createEntityManagerFactory(UNIDAD_PERSISTENCIA, propiedades);
        }
        return emf;
    }

    /**
     * Propiedades JPA para la URL indicada: una URL jdbc:h2 cambia también el driver y el dialecto.
     * Sin URL devuelve un mapa vacío y se mantiene la configuración de persistence.xml (SQL Server).
     */
    static Map<String, String> propiedadesDeUrl(String url) {
        Map<String, String> propiedades = new HashMap<>();
        if (url == null || url.isEmpty()) {
            return propiedades;
        }
        propiedades.put("javax.persistence.jdbc.url", url);
        if (url.startsWith("jdbc:h2:")) {
            propiedades.put("javax.persistence.jdbc.driver", "org.h2.Driver");
            propiedades.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            propiedades.put("hibernate.use_nationalized_character_data", "false");
        }
        return propiedades;
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
