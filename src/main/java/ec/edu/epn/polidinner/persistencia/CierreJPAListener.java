package ec.edu.epn.polidinner.persistencia;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Soporte técnico (no pertenece al modelo): cierra el EntityManagerFactory
 * cuando Tomcat detiene o recarga la aplicación.
 */
@WebListener
public class CierreJPAListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        JPAUtil.cerrar();
    }
}
