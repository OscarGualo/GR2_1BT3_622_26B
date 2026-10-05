package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Trazabilidad: Diagrama de clases (Fig. 15) - clase Pedido.
 * Atributos del diagrama: id, fecha, estado, total.
 * Métodos: agregarItem(item, cantidad), calcularTotal(): double, confirmarPago(): boolean,
 * generarCodigoEntrega(): Comprobante (la Fig. 15 dice CodigoEntrega; la clase modelada es Comprobante).
 * Asociaciones: "realiza" (0..* Pedido - 1 ClienteUniversitario), "contiene" (1 Pedido ◆ 1..* DetallePedido),
 * "utiliza para pagar" (0..* Pedido - 1 Policuenta), "genera" (1 Pedido - 1 Comprobante) y
 * "entrega" (0..* Pedido - 1 PersonalComedor).
 * Estados: "Creado" → "Pagado" → "Entregado".
 */
@Entity
public class Pedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    private String estado;

    private double total;

    /** Extremo de la asociación "realiza" (1 ClienteUniversitario realiza 0..* Pedido). */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_cedula")
    private ClienteUniversitario cliente;

    /** Extremo de la asociación "utiliza para pagar" (0..* Pedido - 1 Policuenta). */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "policuenta_numero")
    private Policuenta policuenta;

    /** Extremo de la asociación "contiene" (composición 1 Pedido ◆ 1..* DetallePedido). */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id", nullable = false)
    @OrderBy("id")
    private List<DetallePedido> detalles = new ArrayList<>();

    /** Extremo de la asociación "genera" (1 Pedido - 1 Comprobante); la columna vive en Comprobante. */
    @OneToOne(mappedBy = "pedido", fetch = FetchType.EAGER)
    private Comprobante comprobante;

    /** Extremo de la asociación "entrega" (1 PersonalComedor entrega 0..* Pedido). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "personal_entrega_cedula")
    private PersonalComedor personalEntrega;

    protected Pedido() {
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU03 - mensaje 4:
     * &lt;&lt;create&gt;&gt; Pedido(fecha=hoy, estado="Creado", cliente).
     * El pedido utiliza para pagar la PoliCuenta que posee el cliente.
     */
    public Pedido(Date fecha, String estado, ClienteUniversitario cliente) {
        this.fecha = fecha;
        this.estado = estado;
        this.cliente = cliente;
        this.policuenta = cliente.getPolicuenta();
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - Pedido.agregarItem(item: ItemMenu, cantidad: int).
     * Diagrama de secuencia CU03 - loop [Por cada ítem seleccionado]: mensajes 14 (agregarItem),
     * 15-16 (item.hayStock(cantidad)), 17-18 (&lt;&lt;create&gt;&gt; DetallePedido(cantidad, precioUnitario))
     * y 19 (confirmación de ítem agregado).
     * Si el ítem ya estaba en el pedido, suma la cantidad. Sin stock lanza IllegalStateException
     * (actividad "Preguntar si desea elegir otro plato o producto").
     */
    public DetallePedido agregarItem(ItemMenu item, int cantidad) {
        if (!"Creado".equals(estado)) {
            throw new IllegalStateException("El pedido ya fue pagado");
        }
        if (cantidad < 1) {
            throw new IllegalArgumentException("La cantidad debe ser al menos 1");
        }
        DetallePedido existente = buscarDetalle(item);
        int cantidadTotal = cantidad + (existente == null ? 0 : existente.getCantidad());
        if (!item.hayStock(cantidadTotal)) {
            throw new IllegalStateException("No hay stock suficiente de \"" + item.getNombre() + "\"");
        }
        if (existente != null) {
            existente.sumarCantidad(cantidad);
            return existente;
        }
        DetallePedido detalle = new DetallePedido(cantidad, item.getPrecio());
        detalle.asignarItem(item);
        detalles.add(detalle);
        return detalle;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - Pedido.calcularTotal(): double.
     * Diagrama de secuencia CU03 - mensajes 23 (calcularTotal()), loop 24-25
     * (calcularSubtotal() / subtotal) y 26 (total del pedido); también mensaje 36 dentro de confirmarPago().
     */
    public double calcularTotal() {
        double suma = 0;
        for (DetallePedido detalle : detalles) {
            suma += detalle.calcularSubtotal();
        }
        total = Math.round(suma * 100) / 100.0;
        return total;
    }

    /**
     * Trazabilidad: Secuencia "Realizar Pedido y Pagar" - confirmarPago() → debitar(monto) → true (saldo descontado)
     * → <<create>> CodigoEntrega (Comprobante). Verifica stock con hayStock() y lo descuenta con reducirStock().
     */
    public boolean confirmarPago() {
        if (!"Creado".equals(estado) || detalles.isEmpty()) {
            return false;
        }
        calcularTotal();
        for (DetallePedido detalle : detalles) {
            if (!detalle.getItem().hayStock(detalle.getCantidad())) {
                return false;
            }
        }
        if (!policuenta.debitar(total)) {
            return false;
        }
        for (DetallePedido detalle : detalles) {
            detalle.getItem().reducirStock(detalle.getCantidad());
        }
        estado = "Pagado";
        generarCodigoEntrega();
        return true;
    }

    /**
     * Trazabilidad: Diagrama de clases - +generarCodigoEntrega(): Comprobante.
     * Lo invoca confirmarPago(). Si el comprobante ya existe devuelve el mismo; solo completa el código si falta.
     */
    public Comprobante generarCodigoEntrega() {
        if (!"Pagado".equals(estado)) {
            throw new IllegalStateException("Solo un pedido pagado genera código de entrega");
        }
        if (comprobante == null) {
            comprobante = new Comprobante(new Date(), this);
        }
        comprobante.asignarCodigo();
        return comprobante;
    }

    /**
     * Soporte técnico NO modelado (no está en el diagrama de clases): quita un ítem del pedido en armado.
     * Necesario en la práctica para corregir el carrito (acción "quitar" del controlador).
     */
    public boolean quitarItem(int idItem) {
        if (!"Creado".equals(estado)) {
            return false;
        }
        return detalles.removeIf(detalle -> detalle.getItem().getId() == idItem);
    }

    /**
     * Soporte técnico NO modelado: PedidoDAO.adjuntar() reemplaza la PoliCuenta guardada en la sesión
     * por su versión actual de la base antes de confirmar el pago (saldo al día).
     */
    public void reemplazarPolicuenta(Policuenta policuentaActual) {
        if (policuentaActual == null || !policuentaActual.getNumero().equals(policuenta.getNumero())) {
            throw new IllegalArgumentException("La PoliCuenta no corresponde al pedido");
        }
        this.policuenta = policuentaActual;
    }

    /**
     * Lo usa PersonalComedor.entregarPedido() (Fig. 15, asociación "entrega"): el pedido pasa a
     * "Entregado" y queda enlazado al personal que lo despachó.
     */
    void registrarEntrega(PersonalComedor personal) {
        this.estado = "Entregado";
        this.personalEntrega = personal;
    }

    private DetallePedido buscarDetalle(ItemMenu item) {
        for (DetallePedido detalle : detalles) {
            ItemMenu actual = detalle.getItem();
            if (actual == item || (item.getId() != 0 && actual.getId() == item.getId())) {
                return detalle;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public double getTotal() {
        return total;
    }

    public ClienteUniversitario getCliente() {
        return cliente;
    }

    public Policuenta getPolicuenta() {
        return policuenta;
    }

    public List<DetallePedido> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public PersonalComedor getPersonalEntrega() {
        return personalEntrega;
    }
}
