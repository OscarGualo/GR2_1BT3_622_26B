package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ec.edu.epn.polidinner.modelo.ClienteUniversitario;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.persistencia.PedidoDAO;
import ec.edu.epn.polidinner.servicio.ReglaPedidoException;
import ec.edu.epn.polidinner.servicio.ReglasFranja;
import ec.edu.epn.polidinner.servicio.ServicioFranjas;
import ec.edu.epn.polidinner.servicio.ServicioPedidos;

/**
 * Trazabilidad: Examen 1B, HU14 «Consultar pedidos programados» (T14.1-T14.3) y HU17 «Reprogramar la franja
 * de retiro» (T17.3 opción «Cambiar franja»). La consulta se limita al estudiante de la sesión: la ruta
 * /cliente/* la protege FiltroCliente (Esc. 3.2) y el DAO filtra por su cédula (Esc. 3.1).
 */
@WebServlet("/cliente/programados")
public class MisProgramadosServlet extends HttpServlet {

    private static final String VISTA = "/vistas/cliente/misProgramados.jsp";

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ServicioFranjas servicioFranjas = new ServicioFranjas();
    private final ServicioPedidos servicioPedidos = new ServicioPedidos();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ClienteUniversitario cliente = (ClienteUniversitario) request.getSession().getAttribute("cliente");
        RelojAplicacion.exponer(request);
        request.setAttribute("ahora", RelojAplicacion.horaActual());
        request.setAttribute("fechaHoy", RelojAplicacion.fechaActual());
        request.setAttribute("pedidos", pedidoDAO.buscarProgramadosPendientes(cliente.getCedula()));
        request.setAttribute("franjas", servicioFranjas.franjasOfrecidas(RelojAplicacion.ahora()));
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /** HU17: acción «reprogramar» con idPedido y la nueva franja (HH:mm) de hoy. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"reprogramar".equals(request.getParameter("accion"))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no reconocida");
            return;
        }
        HttpSession sesion = request.getSession();
        ClienteUniversitario cliente = (ClienteUniversitario) sesion.getAttribute("cliente");
        try {
            int idPedido = leerEntero(request.getParameter("idPedido"));
            LocalTime franja = leerFranja(request.getParameter("franja"));
            LocalDate fecha = leerFecha(request.getParameter("fecha"));
            Pedido pedido = servicioPedidos.reprogramar(idPedido, cliente.getCedula(), fecha, franja,
                    RelojAplicacion.ahora());
            sesion.setAttribute("flashExito", String.format(Locale.US,
                    "Pedido reprogramado para las %s. Conserva su código y no se cobró nada más.",
                    ReglasFranja.etiqueta(pedido.getFranja().getHoraInicio())));
        } catch (ReglaPedidoException e) {
            sesion.setAttribute("flashError", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/cliente/programados");
    }

    private static int leerEntero(String valor) {
        try {
            return Integer.parseInt(valor == null ? "" : valor.trim());
        } catch (NumberFormatException e) {
            throw new ReglaPedidoException("Pedido no válido.");
        }
    }

    private static LocalTime leerFranja(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new ReglaPedidoException("La hora de retiro no es válida");
        }
    }

    private static LocalDate leerFecha(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return RelojAplicacion.fechaActual();
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new ReglaPedidoException("Solo puedes programar pedidos para hoy");
        }
    }
}
