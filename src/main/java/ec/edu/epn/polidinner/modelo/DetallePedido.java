package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Trazabilidad: Diagrama de clases (Fig. 15) - clase DetallePedido.
 * Atributos del diagrama: cantidad, precioUnitario. Método: calcularSubtotal(): double.
 * Asociaciones: "contiene" (1 Pedido ◆ 1..* DetallePedido, navegable desde Pedido) y
 * "corresponde a" (* DetallePedido - 1 ItemMenu).
 */
@Entity
public class DetallePedido implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador técnico NO modelado (clave primaria de la tabla). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int cantidad;

    private double precioUnitario;

    /** Extremo de la asociación "corresponde a" (* DetallePedido - 1 ItemMenu). */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "item_id")
    private ItemMenu item;

    protected DetallePedido() {
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU03 - mensaje 17:
     * &lt;&lt;create&gt;&gt; DetallePedido(cantidad, precioUnitario).
     */
    public DetallePedido(int cantidad, double precioUnitario) {
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - DetallePedido.calcularSubtotal(): double.
     * Diagrama de secuencia CU03 - mensajes 24 (calcularSubtotal()) y 25 (subtotal).
     */
    public double calcularSubtotal() {
        return Math.round(cantidad * precioUnitario * 100) / 100.0;
    }

    /** Enlaza el detalle con su ItemMenu ("corresponde a"); lo usa Pedido.agregarItem(). */
    void asignarItem(ItemMenu item) {
        this.item = item;
    }

    /** Suma unidades cuando el mismo ítem se agrega otra vez al pedido (lo usa Pedido.agregarItem()). */
    void sumarCantidad(int unidades) {
        this.cantidad += unidades;
    }

    /**
     * Soporte técnico NO modelado: PedidoDAO.adjuntar() reemplaza el ítem guardado en la sesión
     * por su versión actual de la base antes de confirmar el pago (stock y estado al día).
     */
    public void reemplazarItem(ItemMenu itemActual) {
        if (itemActual == null || itemActual.getId() != item.getId()) {
            throw new IllegalArgumentException("El ítem no corresponde a este detalle");
        }
        this.item = itemActual;
    }

    public int getId() {
        return id;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public ItemMenu getItem() {
        return item;
    }
}
