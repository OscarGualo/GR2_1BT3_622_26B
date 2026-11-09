package ec.edu.epn.polidinner.modelo;

import java.util.Date;

/**
 * Soporte técnico NO modelado - HU18 Consultar ventas de productos (no es entidad JPA).
 * Una línea de pedido de un Producto con el estado actual del pedido y la fecha de pago, que es la
 * fecha de generación de su Comprobante (regla 4). La arma VentaProductoDAO.
 */
public final class VentaProducto {

    private final int referencia;
    private final String nombre;
    private final Date fechaCaducidad;
    private final int cantidad;
    private final String estadoPedido;
    private final Date fechaPago;

    public VentaProducto(int referencia, String nombre, Date fechaCaducidad, int cantidad,
                         String estadoPedido, Date fechaPago) {
        this.referencia = referencia;
        this.nombre = nombre;
        this.fechaCaducidad = fechaCaducidad;
        this.cantidad = cantidad;
        this.estadoPedido = estadoPedido;
        this.fechaPago = fechaPago;
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

    public int getCantidad() {
        return cantidad;
    }

    public String getEstadoPedido() {
        return estadoPedido;
    }

    public Date getFechaPago() {
        return fechaPago;
    }
}