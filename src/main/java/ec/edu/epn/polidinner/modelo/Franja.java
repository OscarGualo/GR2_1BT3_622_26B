package ec.edu.epn.polidinner.modelo;

import java.time.LocalTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Trazabilidad: Diagrama de actividades HU-02 - "Revisar hora actual y franja de la reserva".
     * Minutos que faltan, desde horaActual, para que empiece esta franja.
     * Trasladado desde Reserva (refactorización Move Method).
     */
    public long minutosHastaInicio(LocalTime horaActual) {
        return Duration.between(horaActual, inicio).toMinutes();
    }
    /**
     * T1.2 - HU-01 / Escenario 2.2: una franja ya comenzó desde su hora exacta de inicio
     * (a las 12:30 la franja 12:30–13:00 ya no se puede reservar).
     */
    public boolean yaComenzo(LocalTime hora) {
        return !hora.isBefore(inicio);
    }

    /**
     * T1.2 - HU-01 / Escenarios 2.1 y 2.3: franjas del día que todavía se pueden reservar.
     * Lista vacía = ya pasaron todas ("No hay franjas disponibles para hoy").
     */
    public static List<Franja> noComenzadas(LocalTime hora) {
        List<Franja> disponibles = new ArrayList<>();
        for (Franja franja : values()) {
            if (!franja.yaComenzo(hora)) {
                disponibles.add(franja);
            }
        }
        return disponibles;
    }
    /** Texto para las vistas, por ejemplo "12:30–13:00". */
    public String getEtiqueta() {
        return inicio + "–" + fin;
    }
}