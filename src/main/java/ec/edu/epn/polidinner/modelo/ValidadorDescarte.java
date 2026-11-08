package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/**
 * HU20 (criterios 20.1 y 20.2): reglas para descartar unidades caducadas de un registro de Producto.
 * Origen TDD: ValidadorDescarteTest (PU07, PU08 y PP02).
 */
public class ValidadorDescarte {

    public boolean admiteDescarte(Producto producto, LocalDate hoy) {
        return producto.isHabilitado() && producto.getStockActual() > 0 && !producto.estaVigente(hoy);
    }

    public void validarCantidad(Producto producto, int cantidad) {
    }
}