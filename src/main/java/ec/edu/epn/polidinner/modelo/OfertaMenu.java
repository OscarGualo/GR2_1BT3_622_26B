package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

/**
 * Corrección de productos persistentes (Paquete 2) - la oferta de un ítem en el menú de un día.
 * Concreta la asociación "contiene" (1 Menu - * ItemMenu) del diagrama de clases: un Producto puede
 * ofrecerse en varios menús conservando su identidad y sus existencias, y el retiro es de la oferta del
 * día, no del producto. Es un concepto del dominio (tiene identidad, habilitación y ciclo de vida), por lo
 * que debe reflejarse en el diagrama de clases al actualizarlo.
 * <ul>
 *   <li>Unicidad menú + ítem: el mismo ítem no se ofrece dos veces en un menú, ni con envíos repetidos.</li>
 *   <li>{@code @Version}: el retiro actualiza esta fila; el pago la bloquea con LockModeType.OPTIMISTIC, así
 *       un retiro simultáneo hace fallar el pago al confirmarlo (rollback completo).</li>
 * </ul>
 */
@Entity
@Table(name = "oferta_menu",
        uniqueConstraints = @UniqueConstraint(name = "uk_oferta_menu_item", columnNames = {"menu_id", "item_id"}))
public class OfertaMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Situación de la oferta para el personal (CU02) y para filtrar lo que ve el estudiante (CU03). */
    public static final String DISPONIBLE = "Disponible";
    public static final String RETIRADO = "Retirado";
    public static final String SIN_STOCK = "Sin stock";
    public static final String CADUCADO = "Caducado";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    /**
     * Solo se propaga PERSIST (guardar la oferta de un plato recién creado lo guarda). Nunca MERGE ni REMOVE:
     * la oferta no sobrescribe ni borra las existencias del ítem.
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "item_id")
    private ItemMenu item;

    /** Retiro manual del ítem para este día (CU02); distinto de quedarse sin stock o de caducar. */
    private boolean retirada;

    @Version
    private int version;

    protected OfertaMenu() {
    }

    /** La crea Menu.agregarItem(item). */
    OfertaMenu(Menu menu, ItemMenu item) {
        this.menu = menu;
        this.item = item;
    }

    /** CU02 corregido: retira el ítem de la oferta de este día; su registro y sus existencias no cambian. */
    public void retirar() {
        this.retirada = true;
    }

    public boolean estaOfrecida() {
        return !retirada;
    }

    /**
     * ¿Se puede comprar esta cantidad hoy? Ofrecida (no retirada), con stock disponible y, si es un producto,
     * sin caducar. La caducidad y el stock se vuelven a comprobar al confirmar el pago.
     */
    public boolean esComprable(int cantidad, LocalDate hoy) {
        return !retirada && item.hayStock(cantidad) && vigente(hoy);
    }

    /** Situación que se muestra al personal: Retirado, Caducado, Sin stock o Disponible (en ese orden). */
    public String estadoEn(LocalDate hoy) {
        if (retirada) {
            return RETIRADO;
        }
        if (!vigente(hoy)) {
            return CADUCADO;
        }
        return item.hayStock(1) ? DISPONIBLE : SIN_STOCK;
    }

    private boolean vigente(LocalDate hoy) {
        return !(item instanceof Producto) || ((Producto) item).estaVigente(hoy);
    }

    public int getId() {
        return id;
    }

    public Menu getMenu() {
        return menu;
    }

    public ItemMenu getItem() {
        return item;
    }

    public boolean isRetirada() {
        return retirada;
    }

    public int getVersion() {
        return version;
    }
}
