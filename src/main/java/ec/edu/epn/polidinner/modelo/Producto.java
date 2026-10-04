package ec.edu.epn.polidinner.modelo;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Trazabilidad: Diagrama de clases (Fig. 7) - clase Producto, "es un" ItemMenu.
 * Atributos del diagrama: stock, fechaCaducidad. Método: getStock().
 */
@Entity
public class Producto extends ItemMenu {

    private int stock;

    @Temporal(TemporalType.DATE)
    private Date fechaCaducidad;

    protected Producto() {
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU01 - mensaje 13:
     * &lt;&lt;create&gt;&gt; Producto(nombre, precio, "Disponible", descripcion, stock, fechaCaducidad).
     */
    public Producto(String nombre, double precio, String estado, String descripcion,
                    int stock, Date fechaCaducidad) {
        super(nombre, precio, estado, descripcion);
        this.stock = stock;
        this.fechaCaducidad = fechaCaducidad;
    }

    /** Trazabilidad: Diagrama de clases (Fig. 7) - Producto.getStock(). */
    public int getStock() {
        return stock;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }
}
