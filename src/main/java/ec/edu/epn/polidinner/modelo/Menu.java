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
 * Métodos: publicarParaVenta(), cerrarParaVenta(), agregarItem(), obtenerItems()
 * (obtenerItem() en la Fig. 7; renombrado obtenerItems() en el Diagrama de clases del incremento 2, Fig. 15).
 * Asociaciones: "contiene" (1 Menu - * ItemMenu) y "administra" (* Menu - 1 PersonalComedor).
 */
@Entity
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Estados del menú: Borrador → Publicado → Cerrado. */
    public static final String BORRADOR = "Borrador";
    public static final String PUBLICADO = "Publicado";
    public static final String CERRADO = "Cerrado";

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
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
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
    /** Trazabilidad: Secuencia "Crear Menú" - mensaje 3 <<create>> Menu(Fecha=hoy, Estado="Borrador"). */
    public Menu(Date fecha, String estado) {
        this.fecha = fecha;
        this.estado = estado;
    }

    /** Trazabilidad: Diagrama de clases - relación "administra" (PersonalComedor 1 — 0..* Menu). */
    void asignarPersonal(PersonalComedor personal) {
        this.personal = personal;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - Menu.publicarParaVenta().
     * Diagrama de secuencia CU01 - mensajes 21 (publicarParaVenta()) y 22 (true, confirma publicación).
     * El estado cambia a "Publicado". No se publica un menú cerrado ni un menú sin ítems.
     */
    /** Trazabilidad: Secuencia "Crear Menú" - mensajes 10-11 publicarParaVenta(); el estado cambia a "Publicado". */

    public boolean publicarParaVenta() {
        if (CERRADO.equals(estado) || items.isEmpty()) {
            return false;
        }
        this.estado = PUBLICADO;
        return true;
    }

    /**
     * Trazabilidad: Diagrama de clases Inc. 3 - Menu.cerrarParaVenta(): boolean
     * (en la Fig. 7 era void). Diagrama de secuencia CU05 - cerrarParaVenta().
     * Solo se cierra un menú "Publicado"; un "Borrador" nunca estuvo a la venta y no cambia.
     * Origen TDD: CerrarMenuTest (T1).
     *
     * @return true si el menú pasó a "Cerrado"; false si no estaba publicado
     */
    public boolean cerrarParaVenta() {
        if (!PUBLICADO.equals(estado)) {
            return false;
        }
        this.estado = CERRADO;
        return true;
    }

    /** Trazabilidad: Secuencia "Crear Menú" - mensajes 8-9 agregarItem(nuevoPlato) / confirma adición. */
    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - Menu.agregarItem().
     * Diagrama de secuencia CU01 - mensajes 15 (agregarItem(nuevoItem)) y 16 (true, confirma adición).
     */
    public boolean agregarItem(ItemMenu item) {
        if (item == null || CERRADO.equals(estado) || items.contains(item)) {
            return false;
        }
        return items.add(item);
    }

    /**
     * Soporte técnico NO modelado por refactorización Move Method.
     * Verifica si un ítem pertenece a este menú.
     */
    public boolean perteneceAlMenu(int idItem) {
        for (ItemMenu item : items) {
            if (item.getId() == idItem) {
                return true;
            }
        }
        return false;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - Menu.obtenerItems() (obtenerItem() en la Fig. 7).
     * Diagrama de secuencia CU02 - mensajes 5 y 6: obtenerItems() / lista de ítems.
     * Diagrama de secuencia CU03 - mensajes 9 y 10: obtenerItems() / lista de ItemMenu.
     * Devuelve todos los ítems; cada controlador filtra los que necesita (CU02 y CU03: "Disponible").
     */
    public List<ItemMenu> obtenerItems() {
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
