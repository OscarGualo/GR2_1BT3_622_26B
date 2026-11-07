package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;
import java.time.LocalDate;
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
 * Corrección de productos persistentes (Paquete 2): "contiene" se representa con OfertaMenu, la oferta de un
 * ítem en el menú de un día. Así un mismo Producto se ofrece en varios menús sin copiar sus existencias.
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
     * Asociación "contiene" (1 Menu - * ItemMenu) a través de la oferta del día.
     * Única fuente de verdad de los ítems del menú (la antigua columna ItemMenu.menu_id ya no se usa).
     * Solo se propaga PERSIST: guardar un menú nuevo guarda sus ofertas nuevas. No hay MERGE (un merge del
     * menú no sobrescribe ofertas ni existencias que otra transacción acaba de vender) ni REMOVE/orphanRemoval
     * (quitar una oferta nunca borra el producto ni su historial de pedidos).
     */
    @OneToMany(mappedBy = "menu", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @OrderBy("id")
    private List<OfertaMenu> ofertas = new ArrayList<>();

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
     * Diagrama de secuencia CU01 - mensajes 10-11 (21-22) publicarParaVenta() / confirma publicación.
     * El estado cambia a "Publicado". No se publica un menú cerrado ni un menú sin ítems.
     */
    public boolean publicarParaVenta() {
        if (CERRADO.equals(estado) || ofertas.isEmpty()) {
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

    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - Menu.agregarItem().
     * Diagrama de secuencia CU01 - mensajes 8-9 (15-16) agregarItem(nuevoItem) / confirma adición.
     * Corrección Paquete 2: crea la oferta del ítem en este menú. Rechaza un ítem nulo, un menú cerrado,
     * un ítem ya ofrecido en este menú (V07) y un plato preparado para otra jornada (V10).
     */
    public boolean agregarItem(ItemMenu item) {
        if (item == null || CERRADO.equals(estado) || buscarOferta(item) != null) {
            return false;
        }
        if (item instanceof Plato && !((Plato) item).prepararPara(getFechaCivil())) {
            return false;
        }
        return ofertas.add(new OfertaMenu(this, item));
    }

    /**
     * Soporte técnico NO modelado por refactorización Move Method.
     * Verifica si un ítem pertenece a este menú.
     */
    public boolean perteneceAlMenu(int idItem) {
        return buscarOferta(idItem) != null;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - Menu.obtenerItems() (obtenerItem() en la Fig. 7).
     * Diagrama de secuencia CU02 - mensajes 5 y 6 y CU03 - mensajes 9 y 10: obtenerItems() / lista de ítems.
     * Se deriva de las ofertas, en el orden en que se agregaron; incluye los ítems retirados hoy.
     */
    public List<ItemMenu> obtenerItems() {
        List<ItemMenu> items = new ArrayList<>();
        for (OfertaMenu oferta : ofertas) {
            items.add(oferta.getItem());
        }
        return Collections.unmodifiableList(items);
    }

    /** Corrección Paquete 2: ofertas del día (cada una sabe si está retirada, sin stock o caducada). */
    public List<OfertaMenu> obtenerOfertas() {
        return Collections.unmodifiableList(ofertas);
    }

    /** Corrección Paquete 2: oferta del ítem indicado en este menú, o null si el menú no lo ofrece. */
    public OfertaMenu buscarOferta(int idItem) {
        for (OfertaMenu oferta : ofertas) {
            if (oferta.getItem().getId() == idItem) {
                return oferta;
            }
        }
        return null;
    }

    /** Busca por identidad (ítems nuevos aún sin id) o por id (ítems ya guardados). */
    private OfertaMenu buscarOferta(ItemMenu item) {
        for (OfertaMenu oferta : ofertas) {
            ItemMenu ofrecido = oferta.getItem();
            if (ofrecido == item || (item.getId() != 0 && ofrecido.getId() == item.getId())) {
                return oferta;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public Date getFecha() {
        return fecha;
    }

    /** Fecha del menú como fecha civil (columna DATE), sin depender de la zona de la JVM. */
    public LocalDate getFechaCivil() {
        return DiaComedor.fechaCivilDe(fecha);
    }

    public String getEstado() {
        return estado;
    }

    public PersonalComedor getPersonal() {
        return personal;
    }
}
