package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * HU20 Registrar pérdidas por caducidad (reglas 9 a 12): unidades descartadas de un registro de Producto.
 * Conserva el producto de origen, su nombre y caducidad, las unidades, la fecha del registro y el personal
 * responsable. El token identifica la confirmación del formulario: es único, así que una misma
 * confirmación enviada dos veces no genera dos pérdidas (escenario 20.3.2).
 */
@Entity
@Table(name = "perdida_caducidad")
public class PerdidaCaducidad implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_caducidad")
    private Date fechaCaducidad;

    @Column(nullable = false)
    private int unidades;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "responsable_cedula")
    private PersonalComedor responsable;

    @Column(name = "token_operacion", nullable = false, unique = true, length = 40)
    private String tokenOperacion;

    protected PerdidaCaducidad() {
    }

    public PerdidaCaducidad(Producto producto, int unidades, PersonalComedor responsable,
                            LocalDateTime fechaRegistro, String tokenOperacion) {
        this.producto = producto;
        this.nombreProducto = producto.getNombre();
        this.fechaCaducidad = producto.getFechaCaducidad();
        this.unidades = unidades;
        this.responsable = responsable;
        this.fechaRegistro = fechaRegistro;
        this.tokenOperacion = tokenOperacion;
    }

    public int getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    /** Referencia del registro de Producto de origen (su id). */
    public int getReferencia() {
        return producto.getId();
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    /** Caducidad en formato dd/MM/yyyy para el historial. */
    public String getCaducidadTexto() {
        LocalDate fecha = DiaComedor.fechaCivilDe(fechaCaducidad);
        return fecha == null ? "Sin fecha" : fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public int getUnidades() {
        return unidades;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    /** Fecha y hora del registro en formato dd/MM/yyyy HH:mm para el historial. */
    public String getFechaRegistroTexto() {
        return fechaRegistro.format(FECHA_HORA);
    }

    public PersonalComedor getResponsable() {
        return responsable;
    }

    public String getTokenOperacion() {
        return tokenOperacion;
    }
}