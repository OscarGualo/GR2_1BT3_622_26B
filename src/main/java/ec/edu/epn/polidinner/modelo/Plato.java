package ec.edu.epn.polidinner.modelo;

import javax.persistence.Entity;

/**
 * Trazabilidad: Diagrama de clases (Fig. 7 y Fig. 15) - clase Plato, "es un" ItemMenu.
 * Método del diagrama: getId().
 */
@Entity
public class Plato extends ItemMenu {

    private static final long serialVersionUID = 1L;

    protected Plato() {
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU01 - mensaje 11:
     * &lt;&lt;create&gt;&gt; Plato(nombre, precio, "Disponible", descripcion).
     * Evolución del incremento 2 (Fig. 15): recibe además las porciones disponibles (stockActual de ItemMenu).
     */
    public Plato(String nombre, double precio, String estado, String descripcion, int porciones) {
        super(nombre, precio, estado, descripcion, porciones);
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - Plato.getId().
     * El id técnico está declarado en ItemMenu; Plato lo hereda.
     */
    @Override
    public int getId() {
        return super.getId();
    }
}
