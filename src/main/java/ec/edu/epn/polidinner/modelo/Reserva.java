package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;
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