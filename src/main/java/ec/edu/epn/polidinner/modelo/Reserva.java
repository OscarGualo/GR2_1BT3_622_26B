package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.Duration;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


/**
 * Trazabilidad: Diagramas de robustez HU-01, HU-02 y HU-03 - entidad Reserva (T1.1).
 * Una reserva une a un ClienteUniversitario con una Mesa en una fecha y una franja.
 * Estados: "Activa", "Cancelada" y "Cancelada fuera de plazo" (esta última restringe al cliente ese día).
 */
@Entity
public class Reserva implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ACTIVA = "Activa";
    public static final String CANCELADA = "Cancelada";
    public static final String CANCELADA_FUERA_DE_PLAZO = "Cancelada fuera de plazo";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_cedula")
    private ClienteUniversitario cliente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "mesa_numero")
    private Mesa mesa;

    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Franja franja;

    @Column(length = 30)
    private String estado;

    protected Reserva() {
    }

    public Reserva(ClienteUniversitario cliente, Mesa mesa, LocalDate fecha, Franja franja) {
        this.cliente = cliente;
        this.mesa = mesa;
        this.fecha = fecha;
        this.franja = franja;
        this.estado = ACTIVA;
    }

    /**
     * Trazabilidad: Diagrama de actividades HU-02 - "Revisar hora actual y franja de la reserva",
     * decisión "¿Faltan ≥ 15 min?", "Marcar reserva como cancelada" y "Anotar restricción de reservas por el día".
     * HU-02 / Escenarios 1.1, 1.2 y 1.3 (prueba PU-06). La hora llega como parámetro para poder probarla.
     */
    public boolean cancelar(LocalTime horaActual) {
        if (minutosAntesDelInicio(horaActual) >= 15) {
            estado = CANCELADA;
        } else {
            estado = CANCELADA_FUERA_DE_PLAZO;
        }
        return true;
    }

    /** Minutos que faltan, desde horaActual, para que empiece la franja reservada. */
    private long minutosAntesDelInicio(LocalTime horaActual) {
        return Duration.between(horaActual, franja.getInicio()).toMinutes();
    }

    /** HU-02 / Escenario 1.3: una cancelación fuera de plazo impide al cliente reservar ese día. */
    public boolean restringeReservasDelDia() {
        return CANCELADA_FUERA_DE_PLAZO.equals(estado);
    }

    public boolean estaActiva() {
        return ACTIVA.equals(estado);
    }

    public Integer getId() {
        return id;
    }

    public ClienteUniversitario getCliente() {
        return cliente;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Franja getFranja() {
        return franja;
    }

    public String getEstado() {
        return estado;
    }
}