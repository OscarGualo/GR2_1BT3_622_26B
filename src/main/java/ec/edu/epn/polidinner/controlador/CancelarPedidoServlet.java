package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.util.Locale;

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
import ec.edu.epn.polidinner.servicio.ReglaPedidoException;
import ec.edu.epn.polidinner.servicio.ServicioPedidos;


/**
 * Trazabilidad: Diagrama de robustez CU 06 - controlador "Cancelar pedido".
 * Diagrama de secuencia CU06 - participante CancelarPedidoServlet.
 * Recibe de la frontera "Mis pedidos" el mensaje listar() (msj 2, doGet).
 */
@WebServlet("/cliente/cancelar")
public class CancelarPedidoServlet extends HttpServlet {

    private static final String VISTA_MIS_PEDIDOS = "/vistas/cliente/misPedidos.jsp";

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ServicioPedidos servicioPedidos = new ServicioPedidos();
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
        response.sendRedirect(request.getContextPath() + destino(request.getParameter("origen")));
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU06 - mensajes 8 (Transaccion.iniciar()), 9-10
     * (PedidoDAO.buscarPorId(idPedido, tx) / pedido), 11-15 (pedido.cancelar(): acreditar, loop reponerStock,
     * marcarCodigoUsado), 16 (confirmar()) o rollback, y 17 (mensaje al cliente).
     * Examen 1B (HU12, T12.1-T12.2): la cancelación vive ahora en ServicioPedidos, que aplica el plazo de
     * 30 minutos a los pedidos con hora de retiro, libera el cupo de la franja (HU16 CA4) y garantiza un solo
     * reembolso (Esc. 3.1 y 3.2). Esta ruta del Release 1 no puede omitir esas reglas porque no tiene otra.
     */
    private void cancelar(HttpSession sesion, ClienteUniversitario cliente, int idPedido) {
        try {
            Pedido cancelado = servicioPedidos.cancelar(idPedido, cliente.getCedula(), RelojAplicacion.ahora());
            sesion.setAttribute("flashExito", String.format(Locale.US,
                    "Pedido cancelado. Se devolvieron $%.2f a su PoliCuenta.", cancelado.getTotal()));
        } catch (ReglaPedidoException e) {
            sesion.setAttribute("flashError", e.getMessage());
        }
    }

    /** Vuelve a «Mis pedidos programados» si la cancelación se pidió desde esa lista; si no, a «Mis pedidos». */
    static String destino(String origen) {
        return "programados".equals(origen) ? "/cliente/programados" : "/cliente/cancelar";
    }

    private static Integer leerEntero(String valor) {
        try {
            return valor == null ? null : Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
