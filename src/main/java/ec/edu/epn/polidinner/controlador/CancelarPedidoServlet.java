package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.util.Locale;

import javax.persistence.PersistenceException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ec.edu.epn.polidinner.modelo.ClienteUniversitario;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.persistencia.ClienteUniversitarioDAO;
import ec.edu.epn.polidinner.persistencia.PedidoDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;


/**
 * Trazabilidad: Diagrama de robustez CU 06 - controlador "Cancelar pedido".
 * Diagrama de secuencia CU06 - participante CancelarPedidoServlet.
 * Recibe de la frontera "Mis pedidos" el mensaje listar() (msj 2, doGet).
 */
@WebServlet("/cliente/cancelar")
public class CancelarPedidoServlet extends HttpServlet {

    private static final String VISTA_MIS_PEDIDOS = "/vistas/cliente/misPedidos.jsp";

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ClienteUniversitarioDAO clienteDAO = new ClienteUniversitarioDAO();

    /**
     * Trazabilidad: Diagrama de secuencia CU06 - mensajes 2 (listar()), 3-4 (PedidoDAO.buscarPorCliente(cedula)
     * / lista de pedidos) y 5 (muestra fecha, código, total y estado de cada pedido).
     * El saldo se lee de la base para mostrar el valor actual de la PoliCuenta.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ClienteUniversitario enSesion = (ClienteUniversitario) request.getSession().getAttribute("cliente");
        request.setAttribute("pedidos", pedidoDAO.buscarPorCliente(enSesion.getCedula()));
        request.setAttribute("saldo", clienteDAO.buscarPorCedula(enSesion.getCedula()).consultarSaldo());
        request.getRequestDispatcher(VISTA_MIS_PEDIDOS).forward(request, response);
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU06 - mensaje 7 cancelar(idPedido) desde el botón "Cancelar".
     * Patrón POST-redirect-GET: deja el mensaje (flashExito / flashError) y vuelve a "Mis pedidos".
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"cancelar".equals(request.getParameter("accion"))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no reconocida");
            return;
        }
        HttpSession sesion = request.getSession();
        Integer idPedido = leerEntero(request.getParameter("idPedido"));
        if (idPedido == null) {
            sesion.setAttribute("flashError", "Pedido no válido.");
        } else {
            cancelar(sesion, (ClienteUniversitario) sesion.getAttribute("cliente"), idPedido);
        }
        response.sendRedirect(request.getContextPath() + "/cliente/cancelar");
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU06 - mensajes 8 (Transaccion.iniciar()), 9-10
     * (PedidoDAO.buscarPorId(idPedido, tx) / pedido), 11-15 (pedido.cancelar(): acreditar, loop reponerStock,
     * marcarCodigoUsado), 16 (confirmar()) o rollback, y 17 (mensaje al cliente).
     * El pedido queda gestionado por el EntityManager junto con su PoliCuenta, ítems y comprobante:
     * el commit guarda todos los cambios juntos. Si el personal entrega el mismo pedido al mismo tiempo,
     * el @Version de Comprobante hace fallar una de las dos transacciones (OptimisticLockException).
     */
    private void cancelar(HttpSession sesion, ClienteUniversitario cliente, int idPedido) {
        try (Transaccion tx = Transaccion.iniciar()) {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido, tx);
            if (pedido == null || !pedido.getCliente().getCedula().equals(cliente.getCedula())) {
                // Sin confirmar(): close() hace rollback
                sesion.setAttribute("flashError", "No se encontró ese pedido entre los suyos.");
                return;
            }
            if (!pedido.cancelar()) {
                sesion.setAttribute("flashError", "Solo se puede cancelar un pedido pagado que aún no se entregó.");
                return;
            }
            tx.confirmar();
            sesion.setAttribute("flashExito", String.format(Locale.US,
                    "Pedido cancelado. Se devolvieron $%.2f a su PoliCuenta.", pedido.getTotal()));
        } catch (PersistenceException e) {
            // OptimisticLockException / RollbackException: el pedido cambió (p. ej. se entregó) mientras se cancelaba
            log("Cancelación no completada; se hizo rollback", e);
            sesion.setAttribute("flashError", "El pedido acaba de ser entregado o modificado; no se pudo cancelar."
                    + " No se movió dinero de su PoliCuenta.");
        }
    }

    private static Integer leerEntero(String valor) {
        try {
            return valor == null ? null : Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
