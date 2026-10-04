package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

/**
 * Trazabilidad: Diagrama de clases (Fig. 15) - clase Comprobante
 * (la Fig. 16 la llama :CodigoEntrega; se usa el nombre del diagrama de clases).
 * Atributos del diagrama: idComprobante, codigo, fechaGeneracion, usado.
 * Métodos: validarCodigoEntrega(): boolean, marcarCodigoUsado().
 * Asociaciones: "genera" (1 Pedido - 1 Comprobante) y "valida" (1 PersonalComedor - 0..* Comprobante).
 */
@Entity
public class Comprobante implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Atributo del diagrama idComprobante: String (se genera como UUID). */
    @Id
    @Column(length = 36)
    private String idComprobante;

    @Column(unique = true, nullable = false, length = 20)
    private String codigo;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaGeneracion;

    private boolean usado;

    /** Extremo de la asociación "genera" (1 Pedido genera 1 Comprobante). */
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "pedido_id", unique = true)
    private Pedido pedido;

    /** Extremo de la asociación "valida" (1 PersonalComedor valida 0..* Comprobante). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "personal_valida_cedula")
    private PersonalComedor personalValida;

    /**
     * Atributo técnico NO modelado (bloqueo optimista de JPA): si dos personas validan el mismo código
     * a la vez, solo una entrega el pedido; la otra recibe OptimisticLockException.
     */
    @Version
    private int version;

    protected Comprobante() {
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU03 - mensaje 45:
     * &lt;&lt;create&gt;&gt; Comprobante(codigo, fechaGeneracion, pedido); nace sin usar.
     */
    public Comprobante(String codigo, Date fechaGeneracion, Pedido pedido) {
        this.idComprobante = UUID.randomUUID().toString();
        this.codigo = codigo;
        this.fechaGeneracion = fechaGeneracion;
        this.pedido = pedido;
        this.usado = false;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - Comprobante.validarCodigoEntrega(): boolean.
     * Diagrama de secuencia CU04 - mensajes 6-7: validarCodigoEntrega() / resultado.
     * Válido solo si no se usó, se generó hoy y su pedido está "Pagado"
     * (caso de prueba: "el código es correcto, del día y no usado").
     */
    public boolean validarCodigoEntrega() {
        return !usado && esDeHoy() && pedido != null && "Pagado".equals(pedido.getEstado());
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - Comprobante.marcarCodigoUsado().
     * Diagrama de secuencia CU04 - mensaje 10: marcarCodigoUsado().
     */
    public void marcarCodigoUsado() {
        this.usado = true;
    }

    /** Registra quién validó el comprobante (asociación "valida"); lo usa PersonalComedor.validarCodigo(). */
    void registrarValidacion(PersonalComedor personal) {
        this.personalValida = personal;
    }

    /** Soporte de validarCodigoEntrega() y de los mensajes de rechazo: ¿se generó en la fecha actual? */
    public boolean esDeHoy() {
        SimpleDateFormat dia = new SimpleDateFormat("yyyyMMdd");
        return fechaGeneracion != null && dia.format(fechaGeneracion).equals(dia.format(new Date()));
    }

    public String getIdComprobante() {
        return idComprobante;
    }

    public String getCodigo() {
        return codigo;
    }

    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }

    public boolean isUsado() {
        return usado;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public PersonalComedor getPersonalValida() {
        return personalValida;
    }
}
