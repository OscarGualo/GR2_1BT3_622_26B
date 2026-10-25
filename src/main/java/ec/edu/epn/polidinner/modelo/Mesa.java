package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Trazabilidad: Diagramas de robustez HU-01, HU-02 y HU-03 - entidad Mesa (T1.1).
 * Las mesas se cargan con datos_iniciales.sql; ninguna historia las crea desde la aplicación.
 */
@Entity
public class Mesa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private int numero;

    protected Mesa() {
    }

    public Mesa(int numero) {
        this.numero = numero;
    }

    public int getNumero() {
        return numero;
    }
}