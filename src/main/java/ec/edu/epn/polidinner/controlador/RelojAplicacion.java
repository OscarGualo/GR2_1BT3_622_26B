package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;

/**
 * Soporte técnico (no modelado): hora y fecha que usan los controladores de reservas (HU-01, HU-02 y HU-03).
 * Las reglas de negocio (Franja, AgendaReservas, Reserva) reciben esta hora como parámetro y no cambian.
 * La hora se toma, en este orden:
 * <ol>
 *   <li>la hora simulada fijada desde el panel "Modo demostración" (horaDemostracion.jsp);</li>
 *   <li>el archivo polidinner-hora.txt de la carpeta del usuario, o el que indique POLIDINNER_ARCHIVO_HORA;</li>
 *   <li>la variable de entorno POLIDINNER_HORA (por ejemplo 11:30, útil en docker-compose);</li>
 *   <li>la hora real en la zona POLIDINNER_ZONA (por defecto America/Guayaquil, aunque el contenedor esté en UTC).</li>
 * </ol>
 * Un valor vacío o con formato incorrecto se ignora y se pasa al siguiente.
 */
final class RelojAplicacion {

    static final ZoneId ZONA_POR_DEFECTO = ZoneId.of("America/Guayaquil");

    private static final AtomicReference<LocalTime> horaDelPanel = new AtomicReference<>();

    private RelojAplicacion() {
    }

    static LocalTime horaActual() {
        return resolver(horaDelPanel.get(), leerArchivoDeHora(), System.getenv("POLIDINNER_HORA"), zona());
    }

    /** Fecha de hoy en la zona del comedor (en UTC, después de las 19:00 de Ecuador ya sería "mañana"). */
    static LocalDate fechaActual() {
        return LocalDate.now(zona());
    }

    /** Panel "Modo demostración": fija la hora simulada para toda la aplicación hasta que se quite. */
    static void fijarHoraSimulada(LocalTime hora) {
        horaDelPanel.set(hora.truncatedTo(ChronoUnit.MINUTES));
    }

    /** Panel "Modo demostración": vuelve a la hora real. */
    static void quitarHoraSimulada() {
        horaDelPanel.set(null);
    }

    static boolean esHoraSimulada() {
        return horaDelPanel.get() != null || leerHora(leerArchivoDeHora()) != null
                || leerHora(System.getenv("POLIDINNER_HORA")) != null;
    }

    /** El panel se muestra salvo que se apague con POLIDINNER_DEMO=false (por ejemplo, en producción). */
    static boolean modoDemostracionActivo() {
        return !"false".equalsIgnoreCase(String.valueOf(System.getenv("POLIDINNER_DEMO")).trim());
    }

    /** Deja en la petición los datos que muestra el panel: horaComedor, horaSimulada y modoDemo. */
    static void exponer(HttpServletRequest request) {
        request.setAttribute("horaComedor", horaActual().truncatedTo(ChronoUnit.MINUTES));
        request.setAttribute("horaSimulada", esHoraSimulada());
        request.setAttribute("modoDemo", modoDemostracionActivo());
    }

    /** Orden de prioridad de la hora, separado de System.getenv y del disco para poder probarlo. */
    static LocalTime resolver(LocalTime panel, String textoArchivo, String textoEntorno, ZoneId zona) {
        if (panel != null) {
            return panel;
        }
        LocalTime delArchivo = leerHora(textoArchivo);
        if (delArchivo != null) {
            return delArchivo;
        }
        LocalTime delEntorno = leerHora(textoEntorno);
        if (delEntorno != null) {
            return delEntorno;
        }
        return LocalTime.now(zona);
    }

    static ZoneId zona(String texto) {
        try {
            return texto == null || texto.trim().isEmpty() ? ZONA_POR_DEFECTO : ZoneId.of(texto.trim());
        } catch (DateTimeException e) {
            return ZONA_POR_DEFECTO;
        }
    }

    private static ZoneId zona() {
        return zona(System.getenv("POLIDINNER_ZONA"));
    }

    private static LocalTime leerHora(String texto) {
        try {
            return texto == null || texto.trim().isEmpty() ? null : LocalTime.parse(texto.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String leerArchivoDeHora() {
        Path archivo = archivoDeHora();
        try {
            return Files.exists(archivo) ? new String(Files.readAllBytes(archivo), StandardCharsets.UTF_8) : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static Path archivoDeHora() {
        String ruta = System.getenv("POLIDINNER_ARCHIVO_HORA");
        if (ruta != null && !ruta.trim().isEmpty()) {
            return Paths.get(ruta.trim());
        }
        return Paths.get(System.getProperty("user.home"), "polidinner-hora.txt");
    }
}
