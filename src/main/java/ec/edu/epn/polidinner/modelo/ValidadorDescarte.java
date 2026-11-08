package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/**
 * HU20 (criterios 20.1 y 20.2): reglas para descartar unidades caducadas de un registro de Producto.
 * Contrato de la iteración: pendiente de PU07, PU08 y PP02 (Jean).
 */
public class ValidadorDescarte {

    public boolean admiteDescarte(Producto producto, LocalDate hoy) {
        return false;
    }

    public void validarCantidad(Producto producto, int cantidad) {
    }
}