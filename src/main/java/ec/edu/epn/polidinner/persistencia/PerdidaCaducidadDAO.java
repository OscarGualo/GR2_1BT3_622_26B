package ec.edu.epn.polidinner.persistencia;

import java.util.Collections;
import java.util.List;

import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;

/**
 * Soporte técnico NO modelado - HU20: pérdidas por caducidad y su historial.
 * Contrato de la iteración: pendiente (Jorge, HU20).
 */
public class PerdidaCaducidadDAO extends GenericDAO<PerdidaCaducidad, Integer> {

    public PerdidaCaducidadDAO() {
        super(PerdidaCaducidad.class);
    }

    /** ¿Ya se registró una pérdida con este token de confirmación? (dentro de la transacción) */
    public boolean existeToken(String tokenOperacion, Transaccion tx) {
        return false;
    }

    /** ¿Ya se registró una pérdida con este token de confirmación? (fuera de una transacción) */
    public boolean existeToken(String tokenOperacion) {
        return false;
    }

    /** Historial de pérdidas, de la más reciente a la más antigua (regla 11). */
    public List<PerdidaCaducidad> listarRecientes() {
        return Collections.emptyList();
    }
}