package ec.edu.epn.polidinner.modelo;

/** Soporte técnico NO modelado - HU20: el descarte no cumple las reglas; el mensaje se muestra al personal. */
public class DescarteRechazadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DescarteRechazadoException(String mensaje) {
        super(mensaje);
    }
}