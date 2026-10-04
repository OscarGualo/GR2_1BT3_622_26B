package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;

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
     * Atributo técnico NO modelado (bloqueo optimista de JPA).
     * Justificación: caso de prueba del CU 02, no retirar un ítem mientras otra
     * transacción lo modifica; Hibernate lanza OptimisticLockException.
     */
    @Version
    private int version;

    protected ItemMenu() {
    }

    protected ItemMenu(String nombre, double precio, String estado, String descripcion) {
        this.nombre = nombre;
        this.precio = precio;
        this.estado = estado;
        this.descripcion = descripcion;
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

    public int getVersion() {
        return version;
    }

    /** Nombre del tipo concreto (Plato o Producto), usado por las vistas. */
    public String getTipo() {
        return getClass().getSimpleName();
    }
}
