package ec.edu.epn.polidinner.modelo;

import java.util.Date;

/**
 * Soporte técnico NO modelado - HU18 (regla 6): una fila de la tabla «Ventas de productos».
 * Una fila por referencia (registro de Producto) con sus unidades vendidas en el periodo.
 */
public final class FilaVentaProducto {

    private final int referencia;
    private final String nombre;
    private final Date fechaCaducidad;
    private final int unidades;

    public FilaVentaProducto(int referencia, String nombre, Date fechaCaducidad, int unidades) {
        this.referencia = referencia;
        this.nombre = nombre;
        this.fechaCaducidad = fechaCaducidad;
        this.unidades = unidades;
    }

    public int getReferencia() {
        return referencia;
    }

    public String getNombre() {
        return nombre;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public int getUnidades() {
        return unidades;
    }

    @Override
    public String toString() {
        return "FilaVentaProducto[" + referencia + " " + nombre + ": " + unidades + "]";
    }
}