package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Trazabilidad: Examen 1B, "Control de cupos (Iteración 2)" - T15.1 «Modelar la franja persistida».
 * Cada franja de 15 minutos es UNA sola fila por fecha y hora de inicio, con su contador de pedidos
 * asignados. El cupo específico (HU15 CA2) es opcional: si es null rige el cupo general de
 * {@link ConfiguracionCupos}. Reservar, liberar y reducir el cupo se hacen con actualizaciones
 * condicionadas desde FranjaRetiroDAO (no modificando esta entidad), por eso no tiene setters.
 */
@Entity
@Table(name = "franja_retiro",
        uniqueConstraints = @UniqueConstraint(name = "uk_franja_fecha_hora", columnNames = {"fecha", "hora_inicio"}))
public class FranjaRetiro implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    /** Cupo propio de la franja (hora pico); null significa "usar el cupo general". */
    @Column(name = "cupo_especifico")
    private Integer cupoEspecifico;

    @Column(nullable = false)
    private boolean cerrada;

    /** Pedidos que ocupan un lugar: Pagado o Entregado; se libera solo al cancelar o reprogramar. */
    @Column(nullable = false)
    private int asignados;

    protected FranjaRetiro() {
    }

    public FranjaRetiro(LocalDate fecha, LocalTime horaInicio) {
        this.fecha = fecha;
        this.horaInicio = horaInicio;
    }

    /** HU15 CA1-CA2: cupo propio si lo tiene; si no, el general. */
    public int cupoEfectivo(int cupoGeneral) {
        return cupoEspecifico != null ? cupoEspecifico : cupoGeneral;
    }

    /** HU16 CA1: lugares que quedan en la franja (nunca negativo). */
    public int lugaresDisponibles(int cupoGeneral) {
        return Math.max(0, cupoEfectivo(cupoGeneral) - asignados);
    }

    public boolean estaLlena(int cupoGeneral) {
        return asignados >= cupoEfectivo(cupoGeneral);
    }

    public int getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public Integer getCupoEspecifico() {
        return cupoEspecifico;
    }

    public boolean isCerrada() {
        return cerrada;
    }

    public int getAsignados() {
        return asignados;
    }
}
