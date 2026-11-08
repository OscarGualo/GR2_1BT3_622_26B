package ec.edu.epn.polidinner.servicio;

import java.time.LocalDateTime;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import ec.edu.epn.polidinner.modelo.DescarteRechazadoException;
import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.modelo.ValidadorDescarte;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

/**
 * Soporte técnico NO modelado - HU20 Registrar pérdidas por caducidad (reglas 9 a 11).
 * aplicarDescarte: origen TDD ServicioPerdidasMockTest (PM02).
 * registrar: guarda la pérdida y el nuevo stock en UNA transacción (Transaccion). La entidad Producto se
 * carga y se modifica dentro de ella, así su @Version detecta un cambio simultáneo del mismo registro.
 */
public class ServicioPerdidas {

    static final String SIN_CONFIRMACION = "La confirmación no es válida. Vuelva a abrir el formulario de pérdidas.";
    static final String NO_ENCONTRADO = "No se encontró esa existencia de producto.";
    static final String CAMBIO_CONCURRENTE = "La existencia cambió mientras se registraba la pérdida. No se guardó"
            + " nada; revise el stock e inténtelo de nuevo.";
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
        if (tokenOperacion == null || tokenOperacion.trim().isEmpty()) {
            throw new DescarteRechazadoException(SIN_CONFIRMACION);
        }
        try (Transaccion tx = Transaccion.iniciar()) {
            if (perdidaDAO.existeToken(tokenOperacion, tx)) {
                return false;
            }
            Producto producto = productoDAO.buscarProducto(idProducto, tx);
            if (producto == null) {
                throw new DescarteRechazadoException(NO_ENCONTRADO);
            }
            PerdidaCaducidad perdida = aplicarDescarte(producto, unidades, responsable, ahora, tokenOperacion);
            perdidaDAO.guardar(perdida, tx);
            tx.confirmar();
            return true;
        } catch (OptimisticLockException e) {
            throw new DescarteRechazadoException(CAMBIO_CONCURRENTE);
        } catch (PersistenceException e) {
            if (perdidaDAO.existeToken(tokenOperacion)) {
                return false; // la misma confirmación llegó dos veces a la vez: solo una se guardó
            }
            throw e;
        }
    }

    private void descontarUnidades(Producto producto, int unidades) {
        producto.setStockActual(producto.getStockActual() - unidades);
    }
}