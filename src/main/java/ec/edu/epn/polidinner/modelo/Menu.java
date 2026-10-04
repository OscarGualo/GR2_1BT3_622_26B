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
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Trazabilidad: Diagrama de clases (Fig. 7) - clase Menu.
 * Atributos del diagrama: Fecha, id, Estado.
 * Métodos: publicarParaVenta(), cerrarParaVenta(), agregarItem(), obtenerItem().
 * Asociaciones: "contiene" (1 Menu - * ItemMenu) y "administra" (* Menu - 1 PersonalComedor).
 */
@Entity
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Temporal(TemporalType.DATE)
    private Date fecha;

    private String estado;

    /** Extremo de la asociación "administra" (1 PersonalComedor administra * Menu). */
    @ManyToOne
    @JoinColumn(name = "personal_cedula")
    private PersonalComedor personal;

    /**
     * Extremo de la asociación "contiene" (1 Menu contiene * ItemMenu).
     * Unidireccional: la columna menu_id vive en la tabla ItemMenu, sin añadir atributos a ItemMenu.
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_id")
    @OrderBy("id")
    private List<ItemMenu> items = new ArrayList<>();

    protected Menu() {
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU01 - mensaje 4:
     * &lt;&lt;create&gt;&gt; Menu(fecha=hoy, estado="Borrador", personal).
     */
    public Menu(Date fecha, String estado, PersonalComedor personal) {
        this.fecha = fecha;
        this.estado = estado;
        this.personal = personal;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - Menu.publicarParaVenta().
     * Diagrama de secuencia CU01 - mensajes 21 (publicarParaVenta()) y 22 (true, confirma publicación).
     * El estado cambia a "Publicado". No se publica un menú cerrado ni un menú sin ítems.
     */
    public boolean publicarParaVenta() {
        if ("Cerrado".equals(estado) || items.isEmpty()) {
            return false;
        }
        this.estado = "Publicado";
        return true;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - Menu.cerrarParaVenta().
     * El estado cambia a "Cerrado".
     */
    public void cerrarParaVenta() {
        this.estado = "Cerrado";
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - Menu.agregarItem().
     * Diagrama de secuencia CU01 - mensajes 15 (agregarItem(nuevoItem)) y 16 (true, confirma adición).
     */
    public boolean agregarItem(ItemMenu item) {
        if (item == null || "Cerrado".equals(estado) || items.contains(item)) {
            return false;
        }
        return items.add(item);
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - Menu.obtenerItem().
     * Diagrama de secuencia CU02 - mensajes 5 y 6: obtenerItem() / lista de ítems.
     */
    public List<ItemMenu> obtenerItem() {
        return Collections.unmodifiableList(items);
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

    public PersonalComedor getPersonal() {
        return personal;
    }
}
