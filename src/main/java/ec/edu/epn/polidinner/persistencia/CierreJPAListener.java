package ec.edu.epn.polidinner.persistencia;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Soporte técnico (no pertenece al modelo): crea el EntityManagerFactory al desplegar la aplicación
 * y lo cierra cuando Tomcat la detiene o recarga.
 */
@WebListener
public class CierreJPAListener implements ServletContextListener {

    /**
     * Inicializa JPA al arrancar para que Hibernate cree las tablas de inmediato y un error de conexión
     * con SQL Server aparezca en el log de Tomcat, no como un error 500 en el primer inicio de sesión.
     * Si falla, la aplicación se despliega igual y JPAUtil reintenta en la siguiente petición.
     */
    @Override
    public void contextInitialized(ServletContextEvent evento) {
        try {
            JPAUtil.getEntityManagerFactory();
            evento.getServletContext().log("PoliDinner: conexión JPA (PoliDinnerPU) inicializada.");
        } catch (RuntimeException e) {
            evento.getServletContext().log("PoliDinner: NO se pudo conectar a SQL Server. Revise que el servicio"
                    + " esté activo en localhost:1433, que exista la base PoliDinner y las variables de entorno"
                    + " POLIDINNER_DB_USER / POLIDINNER_DB_PASSWORD (reinicie IntelliJ tras crearlas).", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        JPAUtil.cerrar();
    }
}
