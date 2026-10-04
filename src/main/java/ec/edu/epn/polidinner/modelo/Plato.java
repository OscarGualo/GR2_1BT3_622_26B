package ec.edu.epn.polidinner.modelo;

import javax.persistence.Entity;

/**
 * Trazabilidad: Diagrama de clases (Fig. 7) - clase Plato, "es un" ItemMenu.
 * Método del diagrama: getId().
 */
@Entity
public class Plato extends ItemMenu {

    protected Plato() {
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU01 - mensaje 11:
     * &lt;&lt;create&gt;&gt; Plato(nombre, precio, "Disponible", descripcion).
     */
    public Plato(String nombre, double precio, String estado, String descripcion) {
        super(nombre, precio, estado, descripcion);
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
