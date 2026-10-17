package ec.edu.epn.polidinner.controlador;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ec.edu.epn.polidinner.modelo.ClienteUniversitario;
import ec.edu.epn.polidinner.persistencia.ClienteUniversitarioDAO;
import ec.edu.epn.polidinner.persistencia.PedidoDAO;

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
}
