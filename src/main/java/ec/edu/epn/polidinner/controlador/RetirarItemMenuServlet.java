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

import ec.edu.epn.polidinner.modelo.DiaComedor;
import ec.edu.epn.polidinner.modelo.Menu;
import ec.edu.epn.polidinner.modelo.OfertaMenu;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.persistencia.MenuDAO;
import ec.edu.epn.polidinner.persistencia.OfertaMenuDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

/**
 * Trazabilidad: Diagrama de robustez - controlador "Retirar plato y/o producto del menú".
 * Diagrama de secuencia CU02 - participante "Controlador (Retirar plato y/o producto)".
 * doGet corresponde al mensaje 2 iniciarRetiro() y doPost (accion=retirar) al mensaje 9 retirarItem(idItem).
 * Corrección de productos persistentes (Paquete 2): se retira la OFERTA del ítem en el menú de hoy. El producto
 * conserva su estado y sus existencias, y se puede volver a ofrecer en otra jornada; el plato deja de
 * ofrecerse igual que antes.
 */
@WebServlet("/personal/retirar")
public class RetirarItemMenuServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/retirarItem.jsp";
    /** Texto del mensaje 18 del diagrama de secuencia CU02. */
    private static final String MENSAJE_CONCURRENCIA = "El ítem está siendo comprado, intente de nuevo.";

    private final MenuDAO menuDAO = new MenuDAO();
    private final OfertaMenuDAO ofertaDAO = new OfertaMenuDAO();

    /** Trazabilidad: Diagrama de secuencia CU02 - mensajes 2 iniciarRetiro() y 7 (muestra ítems). */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        iniciarRetiro(request);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU02 - mensaje 2 iniciarRetiro().
     * Mensajes 3-4: MenuDAO.buscarMenuDelDia() / menuHoy. Mensajes 5-6: ofertas del menú de hoy.
     * Mensaje 7: solo las ofertas "Disponible" se pueden retirar; las demás se listan aparte con su situación
     * (retirado hoy, sin stock o caducado).
     */
    private void iniciarRetiro(HttpServletRequest request) {
        DiaComedor hoy = RelojAplicacion.diaComedor();
        Menu menuHoy = menuDAO.buscarMenuDelDia(hoy);
        List<OfertaMenu> disponibles = new ArrayList<>();
        List<OfertaMenu> noDisponibles = new ArrayList<>();
        int retirados = 0;
        if (menuHoy != null) {
            for (OfertaMenu oferta : menuHoy.obtenerOfertas()) {
                if (OfertaMenu.DISPONIBLE.equals(oferta.estadoEn(hoy.getFecha()))) {
                    disponibles.add(oferta);
                } else {
                    noDisponibles.add(oferta);
                }
                if (!oferta.estaOfrecida()) {
                    retirados++;
                }
            }
        }
        request.setAttribute("menuHoy", menuHoy);
        request.setAttribute("ofertas", disponibles);
        request.setAttribute("noDisponibles", noDisponibles);
        request.setAttribute("retirados", retirados);
        request.setAttribute("hoy", hoy.getFecha());
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
     * Corrección Paquete 2: en una transacción se lee la oferta del ítem en el menú de hoy (rechaza ids que no
     * pertenecen al menú), el personal la retira (retirarDeOferta) y se guarda esa misma fila versionada.
     * Fragmento alt: mensaje 17 (éxito) o 18 [versión desactualizada u OptimisticLockException].
     */
    private void retirarItem(HttpSession sesion, PersonalComedor personal, int idItem, Integer versionVista) {
        Menu menuHoy = menuDAO.buscarMenuDelDia(RelojAplicacion.diaComedor());
        if (menuHoy == null || !menuHoy.perteneceAlMenu(idItem)) {
            sesion.setAttribute("flashError", "El ítem no pertenece al menú de hoy.");
            return;
        }
        try (Transaccion tx = Transaccion.iniciar()) {
            OfertaMenu oferta = ofertaDAO.buscar(menuHoy.getId(), idItem, tx);
            if (oferta == null) {
                sesion.setAttribute("flashError", "El ítem no pertenece al menú de hoy.");
                return;
            }
            String nombre = oferta.getItem().getNombre();
            if (!oferta.estaOfrecida()) {
                sesion.setAttribute("flashError", "\"" + nombre + "\" ya fue retirado del menú.");
                return;
            }
            // Soporte técnico del @Version: si la oferta cambió desde que se mostró la lista, otra solicitud
            // la modificó mientras tanto.
            if (versionVista != null && versionVista != oferta.getVersion()) {
                sesion.setAttribute("flashError", MENSAJE_CONCURRENCIA);
                return;
            }
            personal.retirarDeOferta(oferta);
            tx.confirmar();
            // Mensaje 17
            sesion.setAttribute("flashExito", "Ítem \"" + nombre + "\" retirado del menú.");
        } catch (RuntimeException e) {
            if (!esConflictoDeVersion(e)) {
                throw e;
            }
            // Mensaje 18
            sesion.setAttribute("flashError", MENSAJE_CONCURRENCIA);
        }
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
