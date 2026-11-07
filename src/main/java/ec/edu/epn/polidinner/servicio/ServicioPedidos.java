package ec.edu.epn.polidinner.servicio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.PersistenceException;

import ec.edu.epn.polidinner.modelo.Comprobante;
import ec.edu.epn.polidinner.modelo.DetallePedido;
import ec.edu.epn.polidinner.modelo.FranjaRetiro;
import ec.edu.epn.polidinner.modelo.ItemMenu;
import ec.edu.epn.polidinner.modelo.Menu;
import ec.edu.epn.polidinner.modelo.OfertaMenu;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.persistencia.FranjaRetiroDAO;
import ec.edu.epn.polidinner.persistencia.MenuDAO;
import ec.edu.epn.polidinner.persistencia.OfertaMenuDAO;
import ec.edu.epn.polidinner.persistencia.PedidoDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

/**
 * Soporte técnico NO modelado - Examen 1B, "Reglas comunes del release": las reglas de plazo y cupo se
 * validan en la capa de servicio para que ninguna ruta, incluida la cancelación existente
 * (/cliente/cancelar), pueda omitirlas. Cada operación corre en UNA transacción: si algo falla se
 * deshace todo (saldo, existencias y cupo), igual que Pedido.cancelar() del Release 1.
 * Trazabilidad: HU11 (T11.3), HU12 (T12.1, T12.2), HU16 (T16.2, T16.5) y HU17 (T17.1, T17.2).
 */
public class ServicioPedidos {

    static final String PEDIDO_NO_ENCONTRADO = "No se encontró ese pedido entre los suyos.";
    static final String CAMBIO_CONCURRENTE = "El pedido cambió mientras se procesaba su solicitud. No se movió"
            + " dinero, existencias ni cupo; revise su lista de pedidos e inténtelo de nuevo.";

    static final String VENTA_CERRADA = "El comedor ya cerró la venta de hoy.";

    private final PedidoDAO pedidoDAO;
    private final FranjaRetiroDAO franjaDAO;
    private final MenuDAO menuDAO = new MenuDAO();
    private final OfertaMenuDAO ofertaDAO = new OfertaMenuDAO();

    public ServicioPedidos() {
        this(new PedidoDAO(), new FranjaRetiroDAO());
    }

    ServicioPedidos(PedidoDAO pedidoDAO, FranjaRetiroDAO franjaDAO) {
        this.pedidoDAO = pedidoDAO;
        this.franjaDAO = franjaDAO;
    }

    /**
     * HU11 (Esc. 1.1, 2.1, 2.2, 3.1-3.5) y HU16 (Esc. 2.1, 2.2, 3.1, 3.2): paga el pedido y le reserva un
     * lugar en la franja, todo o nada. Primero se valida la franja; luego, dentro de la transacción, se
     * comprueba la disponibilidad y se reserva con una actualización atómica antes de cobrar.
     *
     * @return el comprobante con el código de retiro PD-xxxx
     * @throws ReglaPedidoException con el mensaje exacto del escenario si se rechaza
     */
    public Comprobante programarYPagar(Pedido pedido, LocalDate fecha, LocalTime inicio, LocalDateTime ahora) {
        if (pedido == null || pedido.getDetalles().isEmpty()) {
            throw new ReglaPedidoException("Su pedido está vacío. Agregue al menos un plato o producto.");
        }
        if (inicio == null) {
            throw new ReglaPedidoException("Elija una franja de retiro para programar su pedido");
        }
        ReglasFranja.validarFranja(fecha, inicio, ahora);
        franjaDAO.asegurarFranjasDelDia(fecha);
        try (Transaccion tx = Transaccion.iniciar()) {
            FranjaRetiro franja = franjaDAO.buscar(fecha, inicio, tx);
            int cupoGeneral = franjaDAO.cupoGeneral(tx);
            exigirFranjaConCupo(franja, cupoGeneral);
            pedidoDAO.adjuntar(pedido, tx);
            // Corrección Paquete 2: la venta sigue abierta y cada ítem sigue en la oferta, antes de cobrar o reservar
            revisarOfertas(pedido, menuAbierto(fecha, tx), fecha, tx);
            if (!franjaDAO.reservar(franja.getId(), cupoGeneral, tx)) {
                throw new ReglaPedidoException("La franja se llenó mientras confirmabas");
            }
            if (!pedido.confirmarPago()) {
                throw new ReglaPedidoException(motivoRechazoDePago(pedido));
            }
            pedido.asignarFranja(franja);
            pedidoDAO.guardarConComprobante(pedido, tx);
            Comprobante comprobante = pedido.getComprobante();
            tx.confirmar();
            return comprobante;
        } catch (PersistenceException e) {
            throw new ReglaPedidoException("No se pudo completar el pago porque el menú o la franja cambiaron"
                    + " mientras confirmaba. No se descontó nada de su PoliCuenta; vuelva a intentarlo.");
        }
    }

    /**
     * CU03 «Pagar ahora» (Release 1), movido desde RealizarPedidoServlet para compartir la revalidación con el
     * pedido programado. En UNA transacción: vuelve a leer el menú (por si se cerró la venta), adjunta los
     * ítems y la PoliCuenta actuales, revalida la oferta de cada ítem con bloqueo optimista (Paquete 2),
     * cobra, descuenta stock y guarda el pedido con su comprobante. Cualquier rechazo deshace todo.
     *
     * @return el comprobante con el código PD-xxxx
     * @throws ReglaPedidoException con el motivo del rechazo (venta cerrada, retirado, caducado, stock o saldo)
     * @throws PersistenceException si otra transacción cambió el menú, el stock o la oferta al mismo tiempo
     */
    public Comprobante pagarAhora(Pedido pedido, LocalDate hoy) {
        try (Transaccion tx = Transaccion.iniciar()) {
            Menu menu = menuAbierto(hoy, tx);
            pedidoDAO.adjuntar(pedido, tx);
            revisarOfertas(pedido, menu, hoy, tx);
            if (!pedido.confirmarPago()) {
                throw new ReglaPedidoException(motivoRechazoDePagoInmediato(pedido));
            }
            pedidoDAO.guardarConComprobante(pedido, tx);
            Comprobante comprobante = pedido.getComprobante();
            tx.confirmar();
            return comprobante;
        }
    }

    /** Menú publicado de la fecha, leído en la transacción; si no lo hay, la venta está cerrada (CU05). */
    private Menu menuAbierto(LocalDate fecha, Transaccion tx) {
        Menu menu = menuDAO.buscarMenuDelDia(fecha, tx);
        if (menu == null || !Menu.PUBLICADO.equals(menu.getEstado())) {
            throw new ReglaPedidoException(VENTA_CERRADA);
        }
        return menu;
    }

    /**
     * Corrección Paquete 2: cada ítem del pedido debe seguir ofrecido en el menú de hoy y, si es un producto,
     * sin caducar. La oferta se lee con LockModeType.OPTIMISTIC: si el personal la retira antes del commit,
     * el pago falla. No confía en el id enviado por el navegador ni en la copia del pedido en la sesión.
     */
    private void revisarOfertas(Pedido pedido, Menu menu, LocalDate hoy, Transaccion tx) {
        for (DetallePedido detalle : pedido.getDetalles()) {
            ItemMenu item = detalle.getItem();
            OfertaMenu oferta = ofertaDAO.buscarParaPago(menu.getId(), item.getId(), tx);
            if (oferta == null) {
                throw new ReglaPedidoException("\"" + item.getNombre()
                        + "\" no está en el menú de hoy. Quítelo de su pedido para continuar.");
            }
            if (!oferta.estaOfrecida()) {
                throw new ReglaPedidoException("\"" + item.getNombre()
                        + "\" fue retirado del menú. Quítelo de su pedido para continuar.");
            }
            if (item instanceof Producto && !((Producto) item).estaVigente(hoy)) {
                throw new ReglaPedidoException("\"" + item.getNombre()
                        + "\" caducó. Quítelo de su pedido para continuar.");
            }
        }
    }

    /**
     * HU12 (CA1-CA3) y HU16 CA4: cancela un pedido propio con reembolso, reposición de existencias y, si es
     * programado, liberación de su lugar en la franja. Para los pedidos inmediatos del Release 1 no
     * aplica el plazo. Un segundo envío encuentra el pedido ya Cancelado (Esc. 3.2) y, si es simultáneo,
     * el control de versión deja pasar un solo reembolso (Esc. 3.1).
     *
     * @return el pedido cancelado (para informar el monto reembolsado)
     */
    public Pedido cancelar(int idPedido, String cedulaCliente, LocalDateTime ahora) {
        try (Transaccion tx = Transaccion.iniciar()) {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido, tx);
            exigirPedidoPropio(pedido, cedulaCliente);
            if ("Cancelado".equals(pedido.getEstado())) {
                throw new ReglaPedidoException("El pedido ya fue cancelado");
            }
            if (!"Pagado".equals(pedido.getEstado())) {
                throw new ReglaPedidoException("Solo se puede cancelar un pedido pagado que aún no se entregó.");
            }
            FranjaRetiro franja = pedido.getFranja();
            if (franja != null) {
                ReglasFranja.validarPlazoDeCancelacion(franja.getFecha(), franja.getHoraInicio(), ahora);
            }
            if (!pedido.cancelar()) {
                throw new ReglaPedidoException("Solo se puede cancelar un pedido pagado que aún no se entregó.");
            }
            if (franja != null) {
                franjaDAO.liberar(franja.getId(), tx);
            }
            tx.sincronizar();
            tx.confirmar();
            return pedido;
        } catch (PersistenceException e) {
            throw new ReglaPedidoException(CAMBIO_CONCURRENTE);
        }
    }

    /**
     * HU17 (CA1-CA3): mueve un pedido programado propio y Pagado a otra franja con cupo, sin perder lo
     * pagado. El traslado ocupa el destino y libera el origen en la misma transacción; si cualquiera
     * falla no cambia ningún cupo (Esc. 1.2). Una cancelación simultánea del mismo pedido se resuelve con
     * el control de versión del pedido (Esc. 1.3).
     */
    public Pedido reprogramar(int idPedido, String cedulaCliente, LocalDate fechaNueva, LocalTime inicioNuevo,
                              LocalDateTime ahora) {
        franjaDAO.asegurarFranjasDelDia(ahora.toLocalDate());
        try (Transaccion tx = Transaccion.iniciar()) {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido, tx);
            exigirPedidoPropio(pedido, cedulaCliente);
            if (!"Pagado".equals(pedido.getEstado()) || !pedido.esProgramado()) {
                throw new ReglaPedidoException("Solo se pueden reprogramar pedidos pagados pendientes de retiro");
            }
            FranjaRetiro origen = pedido.getFranja();
            ReglasFranja.validarPlazoDeReprogramacion(origen.getFecha(), origen.getHoraInicio(), ahora);
            if (inicioNuevo == null) {
                throw new ReglaPedidoException("Elija la nueva franja de retiro");
            }
            ReglasFranja.validarFranja(fechaNueva, inicioNuevo, ahora);
            FranjaRetiro destino = franjaDAO.buscar(fechaNueva, inicioNuevo, tx);
            if (destino.getId() == origen.getId()) {
                throw new ReglaPedidoException("Elija una franja distinta de la que ya tiene el pedido");
            }
            int cupoGeneral = franjaDAO.cupoGeneral(tx);
            exigirFranjaConCupo(destino, cupoGeneral);
            moverCupo(origen, destino, cupoGeneral, tx);
            pedido.asignarFranja(destino);
            tx.sincronizar();
            tx.confirmar();
            return pedido;
        } catch (PersistenceException e) {
            throw new ReglaPedidoException(CAMBIO_CONCURRENTE);
        }
    }

    /** Ocupa el destino y libera el origen; siempre en orden de id para no provocar bloqueos cruzados. */
    private void moverCupo(FranjaRetiro origen, FranjaRetiro destino, int cupoGeneral, Transaccion tx) {
        if (origen.getId() < destino.getId()) {
            franjaDAO.liberar(origen.getId(), tx);
            reservarDestino(destino, cupoGeneral, tx);
        } else {
            reservarDestino(destino, cupoGeneral, tx);
            franjaDAO.liberar(origen.getId(), tx);
        }
    }

    private void reservarDestino(FranjaRetiro destino, int cupoGeneral, Transaccion tx) {
        if (!franjaDAO.reservar(destino.getId(), cupoGeneral, tx)) {
            throw new ReglaPedidoException("La franja " + ReglasFranja.etiqueta(destino.getHoraInicio())
                    + " ya no tiene cupo");
        }
    }

    private static void exigirFranjaConCupo(FranjaRetiro franja, int cupoGeneral) {
        String etiqueta = ReglasFranja.etiqueta(franja.getHoraInicio());
        if (franja.isCerrada()) {
            throw new ReglaPedidoException("La franja " + etiqueta + " está cerrada");
        }
        if (franja.estaLlena(cupoGeneral)) {
            throw new ReglaPedidoException("La franja " + etiqueta + " ya no tiene cupo");
        }
    }

    private static void exigirPedidoPropio(Pedido pedido, String cedulaCliente) {
        if (pedido == null || !pedido.getCliente().getCedula().equals(cedulaCliente)) {
            throw new ReglaPedidoException(PEDIDO_NO_ENCONTRADO);
        }
    }

    /**
     * Explica por qué confirmarPago() devolvió false: falta de stock de algún ítem o saldo insuficiente.
     * HU07 Esc. 2.3: primero se revisa el stock; "retirado del menú" solo aplica si el personal lo retiró con stock.
     */
    public static String motivoRechazoDePago(Pedido pedido) {
        for (DetallePedido detalle : pedido.getDetalles()) {
            ItemMenu item = detalle.getItem();
            if (item.getStockActual() < detalle.getCantidad()) {
                return "\"" + item.getNombre() + "\" ya no tiene stock suficiente."
                        + " Quítelo o reduzca la cantidad.";
            }
            if (!"Disponible".equals(item.getEstado())) {
                return "\"" + item.getNombre() + "\" fue retirado del menú. Quítelo de su pedido para continuar.";
            }
        }
        return "Saldo insuficiente en PoliCuenta";
    }

    /** Mismo motivo con el texto que ya mostraba «Pagar ahora» (termina en punto). */
    static String motivoRechazoDePagoInmediato(Pedido pedido) {
        String motivo = motivoRechazoDePago(pedido);
        return motivo.endsWith(".") ? motivo : motivo + ".";
    }
}
