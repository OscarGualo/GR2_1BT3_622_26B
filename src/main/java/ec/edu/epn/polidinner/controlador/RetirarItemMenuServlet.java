package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.OptimisticLockException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.StaleStateException;

import ec.edu.epn.polidinner.modelo.ItemMenu;
import ec.edu.epn.polidinner.modelo.Menu;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Plato;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.persistencia.ItemMenuDAO;
import ec.edu.epn.polidinner.persistencia.MenuDAO;

/**
 * Trazabilidad: Diagrama de robustez - controlador "Retirar plato y/o producto del menú".
 * Diagrama de secuencia CU02 - participante "Controlador (Retirar plato y/o producto)".
 * doGet corresponde al mensaje 2 iniciarRetiro() y doPost (accion=retirar) al mensaje 9 retirarItem(idItem).
 */
@WebServlet("/personal/retirar")
public class RetirarItemMenuServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/retirarItem.jsp";
    private static final String DISPONIBLE = "Disponible";
    /** Texto del mensaje 18 del diagrama de secuencia CU02. */
    private static final String MENSAJE_CONCURRENCIA = "El ítem está siendo comprado, intente de nuevo.";

    private final MenuDAO menuDAO = new MenuDAO();
    private final ItemMenuDAO itemMenuDAO = new ItemMenuDAO();

    /** Trazabilidad: Diagrama de secuencia CU02 - mensajes 2 iniciarRetiro() y 7 (muestra ítems). */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        iniciarRetiro(request);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU02 - mensaje 2 iniciarRetiro().
     * Mensajes 3-4: MenuDAO.buscarMenuDelDia() / menuHoy. Mensajes 5-6: menuHoy.obtenerItems() / lista de ítems.
     * Mensaje 7: deja para la vista solo los ítems con estado "Disponible" (un ítem retirado ya no aparece).
     */
    private void iniciarRetiro(HttpServletRequest request) {
        Menu menuHoy = menuDAO.buscarMenuDelDia();
        List<ItemMenu> disponibles = new ArrayList<>();
        int retirados = 0;
        if (menuHoy != null) {
            for (ItemMenu item : menuHoy.obtenerItems()) {
                if (DISPONIBLE.equals(item.getEstado())) {
                    disponibles.add(item);
                } else {
                    retirados++;
                }
            }
        }
        request.setAttribute("menuHoy", menuHoy);
        request.setAttribute("items", disponibles);
        request.setAttribute("retirados", retirados);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"retirar".equals(request.getParameter("accion"))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no reconocida");
            return;
        }
        HttpSession sesion = request.getSession();
        PersonalComedor personal = (PersonalComedor) sesion.getAttribute("personal");
        Integer idItem = leerEntero(request.getParameter("idItem"));
        Integer versionVista = leerEntero(request.getParameter("version"));

        if (idItem == null) {
            sesion.setAttribute("flashError", "Seleccione el ítem que desea retirar.");
        } else {
            retirarItem(sesion, personal, idItem, versionVista);
        }
        // Mensaje 19: la interfaz muestra el mensaje y la lista actualizada (patrón POST-redirect-GET)
        response.sendRedirect(request.getContextPath() + "/personal/retirar");
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU02 - mensaje 9 retirarItem(idItem).
     * Mensajes 10-11: ItemMenuDAO.buscarPorId(idItem) / item.
     * Fragmento alt: [Plato] mensajes 12-13 eliminarPlato(plato) → cambiarEstado("Agotado");
     * [Producto] mensajes 14-15 eliminarProducto(producto) → cambiarEstado("Agotado").
     * Mensaje 16: ItemMenuDAO.actualizar(item). Fragmento alt: mensaje 17 (éxito) o
     * 18 [versión desactualizada u OptimisticLockException].
     * Validación técnica no modelada: antes del mensaje 10 se comprueba que el ítem pertenezca al menú
     * de hoy (perteneceAlMenu), para rechazar ids manipulados en el formulario.
     */
    private void retirarItem(HttpSession sesion, PersonalComedor personal, int idItem, Integer versionVista) {
        Menu menuHoy = menuDAO.buscarMenuDelDia();
        if (menuHoy == null || !perteneceAlMenu(menuHoy, idItem)) {
            sesion.setAttribute("flashError", "El ítem no pertenece al menú de hoy.");
            return;
        }

        ItemMenu item = itemMenuDAO.buscarPorId(idItem);
        if (item == null) {
            sesion.setAttribute("flashError", "El ítem ya no existe.");
            return;
        }
        if (!DISPONIBLE.equals(item.getEstado())) {
            sesion.setAttribute("flashError", "\"" + item.getNombre() + "\" ya fue retirado del menú.");
            return;
        }
        // Soporte técnico del @Version: si el ítem cambió desde que se mostró la lista, otra
        // transacción (por ejemplo un pago) lo está modificando.
        if (versionVista != null && versionVista != item.getVersion()) {
            sesion.setAttribute("flashError", MENSAJE_CONCURRENCIA);
            return;
        }

        if (item instanceof Plato) {
            personal.eliminarPlato((Plato) item);
        } else if (item instanceof Producto) {
            personal.eliminarProducto((Producto) item);
        }

        try {
            itemMenuDAO.actualizar(item);
            // Mensaje 17
            sesion.setAttribute("flashExito", "Ítem \"" + item.getNombre() + "\" retirado del menú.");
        } catch (RuntimeException e) {
            if (!esConflictoDeVersion(e)) {
                throw e;
            }
            // Mensaje 18
            sesion.setAttribute("flashError", MENSAJE_CONCURRENCIA);
        }
    }

    /** Validación técnica no modelada: el ítem debe estar en menuHoy.obtenerItems(). */
    private static boolean perteneceAlMenu(Menu menu, int idItem) {
        for (ItemMenu item : menu.obtenerItems()) {
            if (item.getId() == idItem) {
                return true;
            }
        }
        return false;
    }

    /** Hibernate puede envolver el conflicto de versión en RollbackException o PersistenceException. */
    private static boolean esConflictoDeVersion(Throwable e) {
        for (Throwable causa = e; causa != null; causa = causa.getCause()) {
            if (causa instanceof OptimisticLockException || causa instanceof StaleStateException) {
                return true;
            }
        }
        return false;
    }

    private static Integer leerEntero(String valor) {
        try {
            return valor == null ? null : Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
