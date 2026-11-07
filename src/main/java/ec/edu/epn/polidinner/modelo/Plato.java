package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Trazabilidad: Diagrama de clases (Fig. 7 y Fig. 15) - clase Plato, "es un" ItemMenu.
 * Método del diagrama: getId().
 */
@Entity
public class Plato extends ItemMenu {

    private static final long serialVersionUID = 1L;

    /** Corrección Paquete 2: jornada para la que se preparó el plato (null hasta agregarlo a un menú). */
    @Column(name = "fecha_preparacion")
    private LocalDate fechaPreparacion;

    protected Plato() {
    }

    /**
     * Trazabilidad: Secuencia "Crear Menú" - mensaje 6 <<create>> Plato(nombre, precio, estado, descripcion).
     * El stock queda en 0; el controlador lo asigna con setStockActual().
     */
    public Plato(String nombre, double precio, String estado, String descripcion) {
        super(nombre, precio, estado, descripcion, 0);
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
     * Trazabilidad: Diagrama de clases - atributo -id: int y método +getId(): int de Plato.
     * El @Id se declara en ItemMenu porque JPA con herencia JOINED exige el id en la clase raíz.
     */
    @Override
    public int getId() {
        return super.getId();
    }

    /**
     * Corrección Paquete 2: el plato se prepara para una sola jornada. Se fija al agregarlo a su primer menú
     * y después no cambia; un menú de otra fecha lo rechaza (no se reutilizan las porciones de ayer).
     *
     * @return true si el plato es (o queda) de esa fecha; false si ya es de otra jornada
     */
    boolean prepararPara(LocalDate fecha) {
        if (fechaPreparacion == null) {
            fechaPreparacion = fecha;
            return true;
        }
        return fechaPreparacion.equals(fecha);
    }

    public LocalDate getFechaPreparacion() {
        return fechaPreparacion;
    }
}
