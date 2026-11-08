package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/**
 * HU19 (tabla de franjas): ubica cada fecha de caducidad en su franja. Se conserva la convención de
 * Producto.estaVigente(): el producto sigue vigente durante su fecha de caducidad.
 * Origen TDD: ClasificadorCaducidadTest (PP01).
 */
public class ClasificadorCaducidad implements ClasificadorFranja {

    @Override
    public FranjaCaducidad clasificar(LocalDate caducidad, LocalDate hoy) {
        if (caducidad.isBefore(hoy)) {
            return FranjaCaducidad.CADUCADOS;
        }
        if (caducidad.isEqual(hoy)) {
            return FranjaCaducidad.CADUCAN_HOY;
        }
        if (!caducidad.isAfter(hoy.plusDays(3))) {
            return FranjaCaducidad.PROXIMOS_3_DIAS;
        }
        return FranjaCaducidad.MAS_ADELANTE;
    }
}