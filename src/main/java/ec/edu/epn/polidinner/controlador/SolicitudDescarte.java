package ec.edu.epn.polidinner.controlador;

import javax.servlet.http.HttpServletRequest;

import ec.edu.epn.polidinner.modelo.DescarteRechazadoException;

/**
 * Soporte técnico NO modelado - Refactorización: Extract Class (desde PerdidasCaducidadServlet.doPost()).
 * Lee y convierte los datos del formulario de pérdidas; si faltan o no son números, lanza
 * DescarteRechazadoException con el mismo mensaje que mostraba el servlet.
 */
final class SolicitudDescarte {

    private final int referencia;
    private final int unidades;
    private final String token;

    private SolicitudDescarte(int referencia, int unidades, String token) {
        this.referencia = referencia;
        this.unidades = unidades;
        this.token = token;
    }

    static SolicitudDescarte desde(HttpServletRequest request) {
        String referenciaTexto = request.getParameter("referencia");
        String unidadesTexto = request.getParameter("unidades");
        if (referenciaTexto == null || referenciaTexto.trim().isEmpty()
                || unidadesTexto == null || unidadesTexto.trim().isEmpty()) {
            throw new DescarteRechazadoException("Seleccione la existencia e indique las unidades descartadas.");
        }
        try {
            return new SolicitudDescarte(Integer.parseInt(referenciaTexto.trim()),
                    Integer.parseInt(unidadesTexto.trim()), request.getParameter("token"));
        } catch (NumberFormatException e) {
            throw new DescarteRechazadoException("Las unidades descartadas deben ser un número entero.");
        }
    }

    int getReferencia() {
        return referencia;
    }

    int getUnidades() {
        return unidades;
    }

    String getToken() {
        return token;
    }
}