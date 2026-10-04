package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Trazabilidad: Diagrama de clases (Fig. 7) - clase PersonalComedor.
 * Atributos del diagrama: nombres, apellidos, rol, cedula (la cédula es la clave primaria).
 * Métodos: crearMenuDiario(), eliminarProducto(), eliminarPlato().
 * Incremento 2 (Fig. 15): validarCodigo(codigo, comprobante) y entregarPedido(pedido).
 * Asociaciones: "administra" (1 - * Menu, navegable desde Menu), "entrega" (1 - 0..* Pedido,
 * navegable desde Pedido) y "valida" (1 - 0..* Comprobante, navegable desde Comprobante).
 */
@Entity
public class PersonalComedor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String cedula;

    private String nombres;

    private String apellidos;

    private String rol;

    /**
     * Atributo técnico NO modelado en el diagrama de clases.
     * Justificación: el diagrama de secuencia CU01 tiene la precondición "login" y el caso
     * de prueba exige que el personal se identifique exitosamente.
     */
    private String clave;

    protected PersonalComedor() {
    }

    public PersonalComedor(String cedula, String nombres, String apellidos, String rol, String clave) {
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.rol = rol;
        this.clave = clave;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - PersonalComedor.crearMenuDiario().
     * Diagrama de secuencia CU01 - mensajes 3 (crearMenuDiario()),
     * 4 (&lt;&lt;create&gt;&gt; Menu(fecha=hoy, estado="Borrador", personal)) y 5-6 (menuHoy).
     */
    public Menu crearMenuDiario() {
        return new Menu(new Date(), "Borrador", this);
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - PersonalComedor.eliminarPlato().
     * Diagrama de secuencia CU02 - mensajes 12 (eliminarPlato(plato)) y 13 (cambiarEstado("Agotado")).
     * No borra el registro: el plato pasa a estado "Agotado".
     */
    public void eliminarPlato(Plato plato) {
        plato.cambiarEstado("Agotado");
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - PersonalComedor.eliminarProducto().
     * Diagrama de secuencia CU02 - mensajes 14 (eliminarProducto(producto)) y 15 (cambiarEstado("Agotado")).
     * No borra el registro: el producto pasa a estado "Agotado".
     */
    public void eliminarProducto(Producto producto) {
        producto.cambiarEstado("Agotado");
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - PersonalComedor.validarCodigo(codigo: String): boolean,
     * ajustado a validarCodigo(codigo, comprobante): la entidad no consulta la base; el controlador
     * busca el Comprobante con ComprobanteDAO y se lo entrega (decisión documentada en la trazabilidad).
     * Diagrama de secuencia CU04 - mensajes 5 (validarCodigo), 6-7 (comprobante.validarCodigoEntrega())
     * y 8 (resultado). Si es válido, queda registrado quién lo validó (asociación "valida").
     */
    public boolean validarCodigo(String codigo, Comprobante comprobante) {
        if (codigo == null || comprobante == null
                || !comprobante.getCodigo().equalsIgnoreCase(codigo.trim())) {
            return false;
        }
        boolean valido = comprobante.validarCodigoEntrega();
        if (valido) {
            comprobante.registrarValidacion(this);
        }
        return valido;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 15) - PersonalComedor.entregarPedido(pedido: Pedido).
     * Diagrama de secuencia CU04 - mensaje 9: entregarPedido(pedido); el pedido pasa a "Entregado"
     * y queda asociado al personal (asociación "entrega").
     */
    public void entregarPedido(Pedido pedido) {
        if (!"Pagado".equals(pedido.getEstado())) {
            throw new IllegalStateException("Solo se entrega un pedido pagado");
        }
        pedido.registrarEntrega(this);
    }

    /** Comprueba la clave ingresada en el inicio de sesión (soporte del login, no modelado). */
    public boolean verificarClave(String claveIngresada) {
        return clave != null && clave.equals(claveIngresada);
    }

    public String getCedula() {
        return cedula;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getRol() {
        return rol;
    }
}
