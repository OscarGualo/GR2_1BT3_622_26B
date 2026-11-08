package ec.edu.epn.polidinner.modelo;

/** Soporte técnico NO modelado - HU19: franjas de caducidad (no son franjas de retiro). */
public enum FranjaCaducidad {
    CADUCADOS("Caducados"),
    CADUCAN_HOY("Caducan hoy"),
    PROXIMOS_3_DIAS("Próximos 3 días"),
    MAS_ADELANTE("Más adelante");

    private final String etiqueta;

    FranjaCaducidad(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}