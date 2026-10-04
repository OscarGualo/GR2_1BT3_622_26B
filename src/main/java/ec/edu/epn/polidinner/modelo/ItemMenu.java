package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Version;

/**
 * Trazabilidad: Diagrama de clases (Fig. 7) - clase abstracta ItemMenu.
 * Atributos del diagrama: nombre, precio, id, estado, descripcion. Método: cambiarEstado().
 * Evolución en el Diagrama de clases del incremento 2 (Fig. 15): atributo stockActual y métodos
 * hayStock(cantidad) y reducirStock(cantidad).
 * Generalización "es un": Plato y Producto (herencia JOINED: una tabla por clase).
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ItemMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;

    private double precio;

    private String estado;

    private String descripcion;

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - ItemMenu.stockActual (porciones de un plato o
     * unidades de un producto). El valor por defecto 0 permite agregar la columna a tablas con datos.
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int stockActual;

    /**
     * Atributo técnico NO modelado (bloqueo optimista de JPA).
     * Justificación: caso de prueba del CU 02, no retirar un ítem mientras otra
     * transacción lo modifica; Hibernate lanza OptimisticLockException.
     */
    @Version
    private int version;

    protected ItemMenu() {
    }

    protected ItemMenu(String nombre, double precio, String estado, String descripcion, int stockActual) {
        this.nombre = nombre;
        this.precio = precio;
        this.estado = estado;
        this.descripcion = descripcion;
        this.stockActual = stockActual;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - ItemMenu.cambiarEstado().
     * Diagrama de secuencia CU02 - mensajes 13 (plato) y 15 (producto): cambiarEstado("Agotado").
     */
    public void cambiarEstado(String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            throw new IllegalArgumentException("El nuevo estado no puede estar vacío");
        }
        this.estado = nuevoEstado.trim();
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - ItemMenu.hayStock(cantidad: int): boolean.
     * Diagrama de secuencia CU03 - mensajes 15-16 (al agregar al pedido) y 37-38 (al confirmar el pago).
     * Un ítem retirado en el CU 02 (estado "Agotado") no tiene stock disponible para pedir.
     */
    public boolean hayStock(int cantidad) {
        return cantidad > 0 && "Disponible".equals(estado) && stockActual >= cantidad;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - ItemMenu.reducirStock(cantidad: int).
     * Diagrama de secuencia CU03 - mensaje 41: reducirStock(cantidad) tras el débito en la PoliCuenta.
     * Extensión documentada: al llegar a 0 el ítem pasa a "Agotado" (mismo estado del CU 02).
     */
    public void reducirStock(int cantidad) {
        if (!hayStock(cantidad)) {
            throw new IllegalStateException("No hay stock suficiente de \"" + nombre + "\"");
        }
        stockActual -= cantidad;
        if (stockActual == 0) {
            cambiarEstado("Agotado");
        }
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getEstado() {
        return estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getStockActual() {
        return stockActual;
    }

    /**
     * Trazabilidad: Diagrama de clases - atributo stockActual de ItemMenu.
     * Secuencia "Crear Menú": después del mensaje 6 (<<create>> Plato) el controlador asigna las porciones.
     */
    public void setStockActual(int stockActual) {
        if (stockActual < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        this.stockActual = stockActual;
    }
    public int getVersion() {
        return version;
    }

    /** Nombre del tipo concreto (Plato o Producto), usado por las vistas. */
    public String getTipo() {
        return getClass().getSimpleName();
    }
}
