package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Trazabilidad: Examen 1B, HU15 CA1 - «cupo general» de las franjas (T15.1).
 * Fila única (id = 1) con el cupo que rige en toda franja sin cupo específico.
 * Se modifica desde FranjaRetiroDAO con una actualización condicionada.
 */
@Entity
@Table(name = "configuracion_cupos")
public class ConfiguracionCupos implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int ID_UNICO = 1;
    public static final int CUPO_GENERAL_INICIAL = 15;

    @Id
    private int id = ID_UNICO;

    @Column(name = "cupo_general", nullable = false)
    private int cupoGeneral = CUPO_GENERAL_INICIAL;

    public ConfiguracionCupos() {
    }

    public int getId() {
        return id;
    }

    public int getCupoGeneral() {
        return cupoGeneral;
    }
}
