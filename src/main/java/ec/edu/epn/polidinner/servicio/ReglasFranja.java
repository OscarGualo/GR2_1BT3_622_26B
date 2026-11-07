package ec.edu.epn.polidinner.servicio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Soporte técnico NO modelado - Examen 1B, "Reglas comunes del release".
 * Reglas puras (sin base de datos) de la franja de retiro: 15 minutos alineados al reloj, solo del día
 * en curso, dentro del horario de retiro (11:00-15:00, valor de ejemplo) y plazo de 30 minutos
 * (incluido) para cancelar o reprogramar. Trazabilidad: HU11 CA3, HU12 CA1-CA2, HU17 CA2.
 */
public final class ReglasFranja {

    public static final LocalTime APERTURA = LocalTime.of(11, 0);
    public static final LocalTime CIERRE = LocalTime.of(15, 0);
    public static final int MINUTOS_FRANJA = 15;
    public static final int MINUTOS_PLAZO = 30;

    private ReglasFranja() {
    }

    /** Texto de una franja, por ejemplo «12:30–12:45». */
    public static String etiqueta(LocalTime inicio) {
        return inicio + "–" + inicio.plusMinutes(MINUTOS_FRANJA);
    }

    /** Inicios de todas las franjas del día: 11:00, 11:15 ... 14:45. */
    public static List<LocalTime> iniciosDelDia() {
        List<LocalTime> inicios = new ArrayList<>();
        for (LocalTime inicio = APERTURA; !inicio.plusMinutes(MINUTOS_FRANJA).isAfter(CIERRE);
             inicio = inicio.plusMinutes(MINUTOS_FRANJA)) {
            inicios.add(inicio);
        }
        return inicios;
    }

    /** HU11 Esc. 1.2: franjas del día que empiezan después de la hora indicada. */
    public static List<LocalTime> iniciosPosteriores(LocalTime ahora) {
        List<LocalTime> posteriores = new ArrayList<>();
        for (LocalTime inicio : iniciosDelDia()) {
            if (inicio.isAfter(ahora)) {
                posteriores.add(inicio);
            }
        }
        return posteriores;
    }

    /**
     * HU11 CA3 (escenarios 3.1, 3.2, 3.3 y 3.5), validada en el servicio para que ninguna ruta la omita.
     * El orden de las reglas sigue la especificidad del mensaje: día, alineación, horario y hora actual.
     */
    public static void validarFranja(LocalDate fecha, LocalTime inicio, LocalDateTime ahora) {
        if (!ahora.toLocalDate().equals(fecha)) {
            throw new ReglaPedidoException("Solo puedes programar pedidos para hoy");
        }
        if (inicio.getSecond() != 0 || inicio.getNano() != 0 || inicio.getMinute() % MINUTOS_FRANJA != 0) {
            throw new ReglaPedidoException("La hora de retiro debe ser el inicio de una franja de 15 minutos");
        }
        if (inicio.isBefore(APERTURA) || inicio.plusMinutes(MINUTOS_FRANJA).isAfter(CIERRE)) {
            throw new ReglaPedidoException("La hora de retiro está fuera del horario del comedor");
        }
        if (!inicio.isAfter(ahora.toLocalTime())) {
            throw new ReglaPedidoException("La hora de retiro debe ser posterior a la hora actual");
        }
    }

    /** HU12 CA1-CA2: se puede cancelar hasta 30 minutos antes del inicio de la franja, límite incluido. */
    public static void validarPlazoDeCancelacion(LocalDate fecha, LocalTime inicio, LocalDateTime ahora) {
        if (!dentroDelPlazo(fecha, inicio, ahora)) {
            throw new ReglaPedidoException("Solo puedes cancelar hasta 30 minutos antes de la hora de retiro");
        }
    }

    /** HU17 CA2: el plazo de reprogramación se mide contra el inicio de la franja ORIGINAL. */
    public static void validarPlazoDeReprogramacion(LocalDate fecha, LocalTime inicio, LocalDateTime ahora) {
        if (!dentroDelPlazo(fecha, inicio, ahora)) {
            throw new ReglaPedidoException(
                    "Solo puedes reprogramar hasta 30 minutos antes del inicio de tu franja original");
        }
    }

    private static boolean dentroDelPlazo(LocalDate fecha, LocalTime inicio, LocalDateTime ahora) {
        LocalDateTime limite = LocalDateTime.of(fecha, inicio).minusMinutes(MINUTOS_PLAZO);
        return !ahora.isAfter(limite);
    }
}
