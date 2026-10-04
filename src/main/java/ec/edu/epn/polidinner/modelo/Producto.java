package ec.edu.epn.polidinner.modelo;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Trazabilidad: Diagrama de clases (Fig. 7) - clase Producto, "es un" ItemMenu.
 * Atributo del diagrama: fechaCaducidad. Método: getStock().
 * Evolución del incremento 2 (Fig. 15): el atributo stock pasa a ItemMenu como stockActual.
 */
@Entity
public class Producto extends ItemMenu {

    private static final long serialVersionUID = 1L;

    @Temporal(TemporalType.DATE)
    private Date fechaCaducidad;

    protected Producto() {
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU01 - mensaje 13:
     * &lt;&lt;create&gt;&gt; Producto(nombre, precio, "Disponible", descripcion, stock, fechaCaducidad).
     * El stock se guarda en ItemMenu.stockActual (Fig. 15).
     */
    public Producto(String nombre, double precio, String estado, String descripcion,
                    int stock, Date fechaCaducidad) {
        super(nombre, precio, estado, descripcion, stock);
        this.fechaCaducidad = fechaCaducidad;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - Producto.getStock().
     * Desde el incremento 2 devuelve ItemMenu.stockActual (Fig. 15).
     */
    public int getStock() {
        return getStockActual();
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }
}
