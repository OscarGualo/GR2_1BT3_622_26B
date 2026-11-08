package ec.edu.epn.polidinner.servicio;

import java.time.LocalDateTime;

import ec.edu.epn.polidinner.modelo.DescarteRechazadoException;
import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.modelo.ValidadorDescarte;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;

/**
 * Soporte técnico NO modelado - HU20 Registrar pérdidas por caducidad (reglas 9 a 11).
 * aplicarDescarte: origen TDD ServicioPerdidasMockTest (PM02). registrar: pendiente (Christian, HU20).
 */
public class ServicioPerdidas {

    static final String NO_ADMITE_DESCARTE = "Solo se registran pérdidas de existencias caducadas, habilitadas y con stock.";

    private final PerdidaCaducidadDAO perdidaDAO;
    private final ProductoDAO productoDAO;
    private final ValidadorDescarte validador;

    public ServicioPerdidas() {
        this(new PerdidaCaducidadDAO(), new ProductoDAO(), new ValidadorDescarte());
    }

    ServicioPerdidas(PerdidaCaducidadDAO perdidaDAO, ProductoDAO productoDAO, ValidadorDescarte validador) {
        this.perdidaDAO = perdidaDAO;
        this.productoDAO = productoDAO;
        this.validador = validador;
    }

    /** Valida el descarte, descuenta las unidades del registro y arma la pérdida (sin guardarla). */
    PerdidaCaducidad aplicarDescarte(Producto producto, int unidades, PersonalComedor responsable,
                                     LocalDateTime momento, String tokenOperacion) {
        if (!validador.admiteDescarte(producto, momento.toLocalDate())) {
            throw new DescarteRechazadoException(NO_ADMITE_DESCARTE);
        }
        validador.validarCantidad(producto, unidades);
        descontarUnidades(producto, unidades);
        return new PerdidaCaducidad(producto, unidades, responsable, momento, tokenOperacion);
    }

    /**
     * Registra la pérdida y descuenta el stock en una sola transacción.
     *
     * @return true si se registró; false si esa misma confirmación ya estaba registrada
     */
    public boolean registrar(int idProducto, int unidades, PersonalComedor responsable, String tokenOperacion,
                             LocalDateTime ahora) {
        return false;
    }

    private void descontarUnidades(Producto producto, int unidades) {
        producto.setStockActual(producto.getStockActual() - unidades);
    }
}