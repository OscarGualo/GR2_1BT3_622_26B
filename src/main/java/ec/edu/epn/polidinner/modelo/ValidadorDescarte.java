package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/**
 * HU20 (criterios 20.1 y 20.2): reglas para descartar unidades caducadas de un registro de Producto.
 * Origen TDD: ValidadorDescarteTest (PU07, PU08 y PP02).
 */
public class ValidadorDescarte {

    public boolean admiteDescarte(Producto producto, LocalDate hoy) {
        return producto.admiteDescarte(hoy);
    }

    public void validarCantidad(Producto producto, int cantidad) {
        validarCantidadPositiva(cantidad);
        if (cantidad > producto.getStockActual()) {
            throw new DescarteRechazadoException(
                    "La cantidad supera las " + producto.getStockActual() + " unidades disponibles");
        }
    }

    private void validarCantidadPositiva(int cantidad) {
        if (cantidad <= 0) {
            throw new DescarteRechazadoException("La cantidad debe ser mayor que cero");
        }
    }
}