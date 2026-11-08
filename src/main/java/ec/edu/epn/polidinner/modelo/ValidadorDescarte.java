package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/** HU20: valida el descarte. PU08 delega la elegibilidad en Producto; PP02 implementará la cantidad. */
public class ValidadorDescarte {
    public boolean admiteDescarte(Producto producto, LocalDate hoy) {
        return producto.admiteDescarte(hoy);
    }

    public void validarCantidad(Producto producto, int cantidad) {
    }
}