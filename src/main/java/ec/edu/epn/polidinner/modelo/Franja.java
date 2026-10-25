package ec.edu.epn.polidinner.modelo;

import java.time.LocalTime;

/**
 * Trazabilidad: HU-01, HU-02 y HU-03 - franjas horarias en las que se reserva una mesa (T1.1).
 * No es entidad: Reserva la guarda como texto con @Enumerated.
 */
public enum Franja {

    F12_00(LocalTime.of(12, 0), LocalTime.of(12, 30)),
    F12_30(LocalTime.of(12, 30), LocalTime.of(13, 0)),
    F13_00(LocalTime.of(13, 0), LocalTime.of(13, 30));

    private final LocalTime inicio;
    private final LocalTime fin;

    Franja(LocalTime inicio, LocalTime fin) {
        this.inicio = inicio;
        this.fin = fin;
    }

    public LocalTime getInicio() {
        return inicio;
    }

    public LocalTime getFin() {
        return fin;
    }

    /** Texto para las vistas, por ejemplo "12:30–13:00". */
    public String getEtiqueta() {
        return inicio + "–" + fin;
    }
}