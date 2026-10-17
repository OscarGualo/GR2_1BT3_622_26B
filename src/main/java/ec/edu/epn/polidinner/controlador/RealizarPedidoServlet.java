package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ec.edu.epn.polidinner.modelo.ClienteUniversitario;
import ec.edu.epn.polidinner.modelo.Comprobante;
import ec.edu.epn.polidinner.modelo.DetallePedido;
import ec.edu.epn.polidinner.modelo.ItemMenu;
import ec.edu.epn.polidinner.modelo.Menu;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.persistencia.ClienteUniversitarioDAO;
import ec.edu.epn.polidinner.persistencia.ComprobanteDAO;
import ec.edu.epn.polidinner.persistencia.MenuDAO;
import ec.edu.epn.polidinner.persistencia.PedidoDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

/**
 * Trazabilidad: Diagrama de robustez (incremento 2) - controlador "Realizar pedido de comida".
 * Diagrama de secuencia CU03 (partes 1 y 2) - participante "Controlador (Realizar pedido de comida)".
 * Mensajes que recibe de la "Interfaz de pedidos de comida": iniciar() (msj 2), agregarItem(idItem, cantidad)
 * (msj 13), calcular() (msj 22) y confirmar() (msj 33). Envía el resultado a la frontera "Salida" (msj 49-50).
 * El Pedido vive en la sesión mientras se arma y se guarda en la base recién al confirmar el pago.
 */
@WebServlet("/cliente/pedido")
public class RealizarPedidoServlet extends HttpServlet {

    private static final String VISTA_MENU = "/vistas/cliente/menuDia.jsp";
    private static final String VISTA_CARRITO = "/vistas/cliente/carrito.jsp";
    private static final String VISTA_SALIDA = "/vistas/comun/salida.jsp";
    /** Atributo de sesión con el pedido en armado (objeto pedido de la secuencia). */
    private static final String PEDIDO = "pedido";
    /** Atributo de sesión con el código del último comprobante emitido al cliente. */
    private static final String ULTIMO_CODIGO = "ultimoCodigo";
    private static final int MAXIMO_POR_ITEM = 10;

    private final MenuDAO menuDAO = new MenuDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
    private final ClienteUniversitarioDAO clienteDAO = new ClienteUniversitarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String vista = request.getParameter("vista");
        if ("carrito".equals(vista)) {
            calcular(request);
            request.getRequestDispatcher(VISTA_CARRITO).forward(request, response);
        } else if ("comprobante".equals(vista)) {
            mostrarComprobante(request, response);
        } else {
            Pedido pedido = obtenerPedido(request.getSession());
            request.setAttribute(PEDIDO, pedido);
            request.setAttribute("menuHoy", mostrarItemsDisponibles(request));
            request.getRequestDispatcher(VISTA_MENU).forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        String accion = request.getParameter("accion");
        String volverA = "";

        if ("agregar".equals(accion)) {
            agregarItem(sesion, leerEntero(request.getParameter("idItem")), leerEntero(request.getParameter("cantidad")));
            volverA = "carrito".equals(request.getParameter("origen")) ? "?vista=carrito" : "";
        } else if ("quitar".equals(accion)) {
            quitarItem(sesion, leerEntero(request.getParameter("idItem")));
            volverA = "?vista=carrito";
        } else if ("confirmar".equals(accion)) {
            volverA = confirmar(sesion);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no reconocida");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/cliente/pedido" + volverA);
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU03 - mensaje 2 iniciar().
     * Mensajes 3-6: cliente.crearPedido() crea el pedido (fecha=hoy, estado="Creado", cliente).
     */
    private Pedido iniciar(HttpSession sesion) {
        ClienteUniversitario cliente = (ClienteUniversitario) sesion.getAttribute("cliente");
        Pedido pedido = cliente.crearPedido();
        sesion.setAttribute(PEDIDO, pedido);
        return pedido;
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU03 - mensajes 7-8 MenuDAO.buscarMenuDelDia() / menuHoy,
     * 9-10 menuHoy.obtenerItems() / lista de ItemMenu y 11 (muestra ítems "Disponible" con stock).
     * Solo se ofrece un menú "Publicado"; un ítem retirado en el CU 02 ya no aparece.
     */
    private Menu mostrarItemsDisponibles(HttpServletRequest request) {
        Menu menuHoy = menuDAO.buscarMenuDelDia();
        List<ItemMenu> disponibles = new ArrayList<>();
        if (menuHoy != null && "Publicado".equals(menuHoy.getEstado())) {
            for (ItemMenu item : menuHoy.obtenerItems()) {
                if (item.hayStock(1)) {
                    disponibles.add(item);
                }
            }
        }
        request.setAttribute("items", disponibles);
        return menuHoy;
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU03 - loop [Por cada ítem seleccionado], mensaje 13
     * agregarItem(idItem, cantidad). Mensajes 14-19: pedido.agregarItem(item, cantidad), que verifica
     * hayStock(cantidad) y crea el DetallePedido. Mensaje 20: "Ítem agregado" y carrito actualizado.
     */
    private void agregarItem(HttpSession sesion, Integer idItem, Integer cantidad) {
        if (idItem == null || cantidad == null || cantidad < 1 || cantidad > MAXIMO_POR_ITEM) {
            sesion.setAttribute("flashError", "Elija una cantidad entre 1 y " + MAXIMO_POR_ITEM + ".");
            return;
        }
        ItemMenu item = buscarItemOfrecido(idItem);
        if (item == null) {
            sesion.setAttribute("flashError", "Ese ítem ya no está disponible en el menú de hoy.");
            return;
        }
        Pedido pedido = obtenerPedido(sesion);
        try {
            pedido.agregarItem(item, cantidad);
            sesion.setAttribute(PEDIDO, pedido);
            sesion.setAttribute("flashExito", "\"" + item.getNombre() + "\" agregado a su pedido.");
        } catch (IllegalStateException e) {
            // Actividad "Preguntar si desea elegir otro plato o producto"
            sesion.setAttribute("flashError", "No hay porciones suficientes de \"" + item.getNombre()
                    + "\". Elija una cantidad menor u otro plato o producto.");
        }
    }

    /**
     * Soporte técnico NO modelado (acción "quitar"): permite corregir el carrito antes de pagar.
     */
    private void quitarItem(HttpSession sesion, Integer idItem) {
        Pedido pedido = (Pedido) sesion.getAttribute(PEDIDO);
        if (pedido != null && idItem != null && pedido.quitarItem(idItem)) {
            sesion.setAttribute(PEDIDO, pedido);
            sesion.setAttribute("flashExito", "Ítem quitado de su pedido.");
        }
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU03 - mensaje 22 calcular().
     * Mensajes 23-26: pedido.calcularTotal() (loop calcularSubtotal() / subtotal) / total del pedido.
     * Mensajes 27-30: cliente.consultarSaldo() (delega en su Policuenta). Mensaje 31: muestra total y saldo.
     * El saldo se lee de la base para mostrar el valor actual de la PoliCuenta.
     */
    private void calcular(HttpServletRequest request) {
        HttpSession sesion = request.getSession();
        Pedido pedido = obtenerPedido(sesion);
        double total = pedido.calcularTotal();
        ClienteUniversitario enSesion = (ClienteUniversitario) sesion.getAttribute("cliente");
        ClienteUniversitario cliente = clienteDAO.buscarPorCedula(enSesion.getCedula());
        double saldo = cliente.consultarSaldo();

        request.setAttribute(PEDIDO, pedido);
        request.setAttribute("total", total);
        request.setAttribute("saldo", saldo);
        request.setAttribute("saldoSuficiente", saldo + 0.005 >= total);
    }

    /**
     * Trazabilidad: Secuencia "Realizar Pedido y Pagar" - confirmarPago() → debitar(monto) → <<create>> CodigoEntrega
     * (Comprobante) → instancia de CodigoEntrega al cliente. Todo en una transacción: si el pago falla, no se guarda nada.
     */
    private String confirmar(HttpSession sesion) {
        Pedido pedido = (Pedido) sesion.getAttribute(PEDIDO);
        if (pedido == null || pedido.getDetalles().isEmpty()) {
            sesion.setAttribute("flashError", "Su pedido está vacío. Agregue al menos un plato o producto.");
            return "";
        }
        try (Transaccion tx = Transaccion.iniciar()) {
            pedidoDAO.adjuntar(pedido, tx);
            if (!pedido.confirmarPago()) {
                // Mensaje 50: rollback (se cierra la transacción sin confirmar) y motivo del rechazo
                sesion.setAttribute("flashError", motivoRechazo(pedido));
                return "?vista=carrito";
            }
            pedidoDAO.guardarConComprobante(pedido, tx);
            Comprobante comprobante = pedido.getComprobante();
            tx.confirmar();

            sesion.removeAttribute(PEDIDO);
            sesion.setAttribute(ULTIMO_CODIGO, comprobante.getCodigo());
            return "?vista=comprobante";
        } catch (RuntimeException e) {
            // Conflicto de versión (otro pago o un retiro simultáneo) u otro error: rollback completo
            log("Pago no completado; se hizo rollback", e);
            sesion.removeAttribute(PEDIDO);
            sesion.setAttribute("flashError", "No se pudo completar el pago porque el menú cambió mientras"
                    + " confirmaba. No se descontó nada de su PoliCuenta; vuelva a armar su pedido.");
            return "";
        }
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU03 - mensajes 49 (Salida con el código de retiro) y
     * 51 (la Salida muestra el comprobante al cliente: actividad "Recibir comprobante").
     */
    private void mostrarComprobante(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        ClienteUniversitario cliente = (ClienteUniversitario) sesion.getAttribute("cliente");
        Comprobante comprobante = comprobanteDAO.buscarPorCodigo((String) sesion.getAttribute(ULTIMO_CODIGO));
        if (comprobante == null || !comprobante.getPedido().getCliente().getCedula().equals(cliente.getCedula())) {
            response.sendRedirect(request.getContextPath() + "/cliente/pedido");
            return;
        }
        request.setAttribute("tipoSalida", "comprobante");
        request.setAttribute("exitoSalida", true);
        request.setAttribute("mensajeSalida", "Presente este código en el despacho del comedor para retirar su pedido.");
        request.setAttribute("comprobante", comprobante);
        request.setAttribute(PEDIDO, comprobante.getPedido());
        request.setAttribute("saldo", comprobante.getPedido().getPolicuenta().consultarSaldo());
        request.getRequestDispatcher(VISTA_SALIDA).forward(request, response);
    }

    /** Explica por qué confirmarPago() devolvió false: falta de stock de algún ítem o saldo insuficiente. */
    private static String motivoRechazo(Pedido pedido) {
        for (DetallePedido detalle : pedido.getDetalles()) {
            ItemMenu item = detalle.getItem();
            if (!"Disponible".equals(item.getEstado())) {
                return "\"" + item.getNombre() + "\" fue retirado del menú. Quítelo de su pedido para continuar.";
            }
            if (!item.hayStock(detalle.getCantidad())) {
                return "\"" + item.getNombre() + "\" ya no tiene porciones suficientes."
                        + " Quítelo o reduzca la cantidad.";
            }
        }
        return "Saldo insuficiente en PoliCuenta.";
    }

    /** Pedido en armado de la sesión; si no existe, se inicia (mensaje 2 iniciar()). */
    private Pedido obtenerPedido(HttpSession sesion) {
        Pedido pedido = (Pedido) sesion.getAttribute(PEDIDO);
        return pedido != null ? pedido : iniciar(sesion);
    }

    /**
     * Busca el ítem entre los "Disponible" del menú publicado de hoy: evita pedir ítems de otro menú o
     * retirados en el CU 02 manipulando el formulario.
     */
    private ItemMenu buscarItemOfrecido(int idItem) {
        Menu menuHoy = menuDAO.buscarMenuDelDia();
        if (menuHoy == null || !"Publicado".equals(menuHoy.getEstado())) {
            return null;
        }
        for (ItemMenu item : menuHoy.obtenerItems()) {
            if (item.getId() == idItem && "Disponible".equals(item.getEstado())) {
                return item;
            }
        }
        return null;
    }

    private static Integer leerEntero(String valor) {
        try {
            return valor == null ? null : Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
