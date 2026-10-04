package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * Trazabilidad: Diagrama de clases (Fig. 15) - clase Policuenta.
 * Atributos del diagrama: numero, saldo. Métodos: consultarSaldo(), debitar(monto).
 * Asociaciones: "posee" (1 ClienteUniversitario - 1 Policuenta) y
 * "utiliza para pagar" (0..* Pedido - 1 Policuenta), navegables desde ClienteUniversitario y Pedido.
 * PoliCuenta es un sistema externo de la EPN; en este proyecto se simula con esta tabla.
 */
@Entity
public class Policuenta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String numero;

    private double saldo;

    /**
     * Atributo técnico NO modelado (bloqueo optimista de JPA): dos pagos simultáneos con la misma
     * PoliCuenta no pueden debitar sobre el mismo saldo; el segundo recibe OptimisticLockException.
     */
    @Version
    private int version;

    protected Policuenta() {
    }

    public Policuenta(String numero, double saldo) {
        this.numero = numero;
        this.saldo = saldo;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - Policuenta.consultarSaldo(): double.
     * Diagrama de secuencia CU03 - mensajes 28-29: consultarSaldo() / saldo.
     */
    public double consultarSaldo() {
        return saldo;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - Policuenta.debitar(monto: double): boolean.
     * Diagrama de secuencia CU03 - mensajes 39 (debitar(monto)) y 40 (true, saldo descontado).
     * Si el saldo alcanza, descuenta el monto y devuelve true; si no, no cambia nada y devuelve false.
     */
    public boolean debitar(double monto) {
        if (!(monto > 0)) {
            throw new IllegalArgumentException("El monto a debitar debe ser mayor que 0");
        }
        // Tolerancia de medio centavo por la representación de double
        if (saldo + 0.005 < monto) {
            return false;
        }
        saldo = Math.round((saldo - monto) * 100) / 100.0;
        return true;
    }

    public String getNumero() {
        return numero;
    }

    public int getVersion() {
        return version;
    }
}
