package ec.edu.epn.polidinner.modelo;

import ec.edu.epn.polidinner.persistencia.ComprobanteDAO;

import java.io.Serializable;
import java.time.LocalDateTime;

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
     * Trazabilidad: Secuencia "Crear Menú" - mensaje 2 crearMenuDiario() y
     * mensajes 3-4 <<create>> Menu(Fecha=hoy, Estado="Borrador") / retorna menuHoy.
     */
    public Menu crearMenuDiario() {
        return crearMenuDiario(DiaComedor.delSistema());
    }

    /** Corrección Paquete 1a: crearMenuDiario() con la fecha civil del comedor. */
    public Menu crearMenuDiario(DiaComedor hoy) {
        Menu menuHoy = new Menu(hoy.comoFechaCivil(), Menu.BORRADOR);
        menuHoy.asignarPersonal(this);
        return menuHoy;
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
     * Corrección de productos persistentes (Paquete 2) - CU02 sobre la oferta del día: el personal retira el
     * plato o producto del menú de hoy. A diferencia de eliminarProducto(), no cambia el estado global del
     * producto: sus existencias se conservan y puede volver a ofrecerse en otra jornada.
     */
    public void retirarDeOferta(OfertaMenu oferta) {
        oferta.retirar();
    }


    /**
     * Trazabilidad: Diagrama de clases - +validarCodigo(codigo: String): boolean.
     * Relación "valida" (PersonalComedor 1 — 0..* Comprobante): busca el comprobante del código
     * y comprueba que se pueda entregar (no usado, del día y con pedido pagado).
     */
    public boolean  validarCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }
        return validarCodigoEnElDia(codigo, DiaComedor.delSistema());
    }

    /**
     * Soporte técnico (no es público): la regla de validación separada de la búsqueda,
     * para poder probarla sin base de datos.
     */
    boolean validarCodigo(String codigo, Comprobante comprobante) {
        return validarCodigo(codigo, comprobante, DiaComedor.delSistema());
    }

    /**
     * Corrección Paquete 1a: validarCodigo(codigo) con el día del comedor. Otro nombre, y no una
     * sobrecarga, para que validarCodigo(codigo, null) siga sin ambigüedad.
     */
    public boolean validarCodigoEnElDia(String codigo, DiaComedor hoy) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }
        Comprobante comprobante = new ComprobanteDAO().buscarPorCodigo(codigo.trim());
        return validarCodigo(codigo, comprobante, hoy);
    }

    /** Soporte técnico: regla de validarCodigoEnElDia() sin base de datos, para las pruebas. */
    boolean validarCodigo(String codigo, Comprobante comprobante, DiaComedor hoy) {
        if (codigo == null || comprobante == null
                || !comprobante.getCodigo().equalsIgnoreCase(codigo.trim())) {
            return false;
        }
        return comprobante.validarCodigoEntrega(hoy);
    }

    /**
     * Trazabilidad: Diagrama de clases - +entregarPedido(pedido: Pedido).
     * Relación "entrega" (PersonalComedor 1 — 0..* Pedido) y "valida" (PersonalComedor 1 — 0..* Comprobante).
     */
    public void entregarPedido(Pedido pedido) {
        entregarPedido(pedido, LocalDateTime.now());
    }

    /**
     * Soporte técnico NO modelado - Examen 1B, medición del objetivo 5.1: misma operación que
     * entregarPedido(pedido), recibiendo la hora del comedor en que se valida el código
     * (HabilitarEntregaServlet le pasa RelojAplicacion.ahora(), la misma referencia de las franjas).
     */
    public void entregarPedido(Pedido pedido, LocalDateTime momento) {
        if (!"Pagado".equals(pedido.getEstado())) {
            throw new IllegalStateException("Solo se entrega un pedido pagado");
        }
        pedido.registrarEntrega(this);
        if (pedido.getComprobante() != null) {
            pedido.getComprobante().registrarValidacion(this, momento);
        }
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
