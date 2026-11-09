package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Soporte técnico NO modelado - HU19: vista de un registro de Producto para la consulta por franjas.
 * La referencia es el id existente del registro (regla 2); no es un código comercial nuevo.
 */
public final class ExistenciaProducto {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int referencia;
    private final String nombre;
    private final int stock;
    private final LocalDate caducidad;
    private final boolean habilitado;

    public ExistenciaProducto(int referencia, String nombre, int stock, LocalDate caducidad, boolean habilitado) {
        this.referencia = referencia;
        this.nombre = nombre;
        this.stock = stock;
        this.caducidad = caducidad;
        this.habilitado = habilitado;
    }

    /** Copia los datos que necesita la consulta desde el registro de Producto. */
    public static ExistenciaProducto desde(Producto producto) {
        return new ExistenciaProducto(producto.getId(), producto.getNombre(), producto.getStockActual(),
                DiaComedor.fechaCivilDe(producto.getFechaCaducidad()), producto.isHabilitado());
    }

    public int getReferencia() {
        return referencia;
    }

    public String getNombre() {
        return nombre;
    }

    public int getStock() {
        return stock;
    }

    public LocalDate getCaducidad() {
        return caducidad;
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    /** Caducidad en formato dd/MM/yyyy para las vistas («Sin fecha» si no tiene). */
    public String getCaducidadTexto() {
        return caducidad == null ? "Sin fecha" : caducidad.format(FORMATO);
    }
}