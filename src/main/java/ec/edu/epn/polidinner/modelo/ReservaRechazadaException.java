package ec.edu.epn.polidinner.modelo;

/**
 * Se lanza cuando una reserva no cumple una regla de HU-01 o HU-02.
 * El mensaje es el que se muestra al cliente en la vista de salida.
 */
public class ReservaRechazadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReservaRechazadaException(String mensaje) {
        super(mensaje);
    }
}