package ec.edu.epn.polidinner.servicio;

/**
 * Soporte técnico NO modelado: rechazo de una regla de negocio de los pedidos programados.
 * Su mensaje es el texto exacto de los escenarios del Examen 1B y se muestra tal cual al usuario.
 * Mismo patrón que ReservaRechazadaException de las reservas de mesa.
 */
public class ReglaPedidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaPedidoException(String mensaje) {
        super(mensaje);
    }
}
