package ec.edu.epn.polidinner.controlador;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ec.edu.epn.polidinner.modelo.Menu;
import ec.edu.epn.polidinner.persistencia.MenuDAO;

/**
 * Trazabilidad: Diagrama de robustez CU05 - controlador "Cerrar menú".
 * Diagrama de secuencia CU05 - participante "CerrarMenuServlet".
 * doGet corresponde al mensaje 2 iniciarCierre() y doPost (accion=cerrar) al mensaje 7 cerrar().
 */
@WebServlet("/personal/cerrar-menu")
public class CerrarMenuServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/cerrarMenu.jsp";

    private final MenuDAO menuDAO = new MenuDAO();

    /** Trazabilidad: Diagrama de secuencia CU05 - mensajes 2 iniciarCierre() y 5 (muestra el menú y su estado). */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        iniciarCierre(request);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /** Trazabilidad: Diagrama de secuencia CU05 - mensajes 3-4 MenuDAO.buscarMenuDelDia() / menuHoy. */
    private void iniciarCierre(HttpServletRequest request) {
        request.setAttribute("menuHoy", menuDAO.buscarMenuDelDia());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"cerrar".equals(request.getParameter("accion"))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no reconocida");
            return;
        }
        // Mensajes 10, 13, 16 y 17: la interfaz muestra el resultado (patrón POST-redirect-GET)
        response.sendRedirect(request.getContextPath() + cerrar(request.getSession()));
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU05 - mensaje 7 cerrar().
     * Mensajes 8-9: MenuDAO.buscarMenuDelDia() / menuHoy; alt [sin menú] mensaje 10.
     * Mensajes 11-12: menuHoy.cerrarParaVenta() / true|false; alt [false] mensaje 13.
     * Mensajes 14-15: MenuDAO.actualizar(menuHoy); mensaje 16 (venta cerrada) y 17 (redirección al reporte).
     *
     * @return ruta a la que se redirige: el reporte si se cerró, o de vuelta a esta pantalla si no
     */
    private String cerrar(HttpSession sesion) {
        Menu menuHoy = menuDAO.buscarMenuDelDia();
        if (menuHoy == null) {
            sesion.setAttribute("flashError", "No hay menú abierto hoy.");
            return "/personal/cerrar-menu";
        }
        if (!menuHoy.cerrarParaVenta()) {
            sesion.setAttribute("flashError", "Solo se cierra un menú publicado.");
            return "/personal/cerrar-menu";
        }
        menuDAO.actualizar(menuHoy);
        sesion.setAttribute("flashExito", "Venta del día cerrada. Los estudiantes ya no pueden pagar pedidos.");
        return "/personal/reporte";
    }
}
