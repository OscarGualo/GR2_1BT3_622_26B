package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
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

    /**
     * Corrección Paquete 2: existencia habilitada para ofrecerse en nuevos menús (ver isHabilitado()).
     * El valor por defecto 0 permite agregar la columna a una tabla con datos (los antiguos quedan históricos).
     */
    @Column(nullable = false, columnDefinition = "bit default 0")
    private boolean habilitado;

    /**
     * Atributo técnico NO modelado (Paquete 3): identidad estable de la existencia de demostración que crean
     * los datos iniciales («demo-jugo-naranja»), para no fabricarla de nuevo en cada arranque ni identificarla
     * por su nombre. Es null en los productos registrados por el personal. La unicidad la asegura la migración
     * con un índice filtrado (SQL Server admite un solo NULL en una restricción UNIQUE simple).
     */
    @Column(name = "clave_semilla", length = 40)
    private String claveSemilla;

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

    /**
     * Corrección Paquete 2 (pantalla Productos): registra una existencia independiente del menú, con su
     * cantidad inicial y su caducidad, habilitada para ofrecerla en los menús de una o varias jornadas.
     * Registrarla no crea ninguna oferta.
     */
    public static Producto registrar(String nombre, double precio, String descripcion, int stock, Date fechaCaducidad) {
        Producto producto = new Producto(nombre, precio, "Disponible", descripcion, stock, fechaCaducidad);
        producto.habilitado = true;
        return producto;
    }

    /**
     * ¿Se puede seleccionar para nuevos menús? Solo las existencias registradas en Productos y las que la
     * migración conservó del menú abierto. Los productos de menús antiguos quedan como históricos (false):
     * siguen sirviendo a sus pedidos y cancelaciones, pero no se vuelven a ofrecer.
     */
    public boolean isHabilitado() {
        return habilitado;
    }

    /**
     * Convención propuesta (no es un acuerdo con el comedor): un producto con caducidad D se vende durante
     * D; desde D+1 ya no. Sin fecha registrada se considera vigente.
     */
    public boolean estaVigente(LocalDate hoy) {
        return fechaCaducidad == null || !hoy.isAfter(DiaComedor.fechaCivilDe(fechaCaducidad));
    }

    /** HU20: solo se descartan existencias habilitadas, con stock y ya caducadas. */
    public boolean admiteDescarte(LocalDate hoy) {
        return isHabilitado() && getStockActual() > 0 && !estaVigente(hoy);
    }
}
