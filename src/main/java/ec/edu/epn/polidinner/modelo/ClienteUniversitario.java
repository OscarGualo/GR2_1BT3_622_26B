package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Trazabilidad: Diagrama de clases (Fig. 15) - clase ClienteUniversitario.
 * Atributos del diagrama: cedula, nombres, apellidos (la cédula es la clave primaria).
 * Métodos: crearPedido(): Pedido, consultarSaldo(): double.
 * Asociaciones: "posee" (1 - 1 Policuenta) y "realiza" (1 - 0..* Pedido, navegable desde Pedido).
 */
@Entity
public class ClienteUniversitario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String cedula;

    private String nombres;

    private String apellidos;

    /**
     * Atributo técnico NO modelado en el diagrama de clases.
     * Justificación: el cliente debe identificarse para realizar un pedido (precondición del CU 03).
     */
    private String clave;

    /** Extremo de la asociación "posee" (1 ClienteUniversitario - 1 Policuenta). */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policuenta_numero", unique = true)
    private Policuenta policuenta;

    protected ClienteUniversitario() {
    }

    public ClienteUniversitario(String cedula, String nombres, String apellidos, String clave,
                                Policuenta policuenta) {
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.clave = clave;
        this.policuenta = policuenta;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - ClienteUniversitario.crearPedido(): Pedido.
     * Diagrama de secuencia CU03 - mensajes 3 (crearPedido()),
     * 4 (&lt;&lt;create&gt;&gt; Pedido(fecha=hoy, estado="Creado", cliente)) y 5-6 (pedido).
     */
    public Pedido crearPedido() {
        return new Pedido(new Date(), "Creado", this);
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - ClienteUniversitario.consultarSaldo(): double.
     * Diagrama de secuencia CU03 - mensajes 27 (consultarSaldo()), 28-29 (delega en su Policuenta) y 30 (saldo).
     */
    public double consultarSaldo() {
        return policuenta.consultarSaldo();
    }

    /** Comprueba la clave ingresada en el inicio de sesión (soporte del login, no modelado). */
    public boolean verificarClave(String claveIngresada) {
        return clave != null && clave.equals(claveIngresada);
    }

    public String getCedula() {
        return cedula;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public Policuenta getPolicuenta() {
        return policuenta;
    }
}
