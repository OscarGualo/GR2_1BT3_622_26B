package ec.edu.epn.polidinner.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

/**
 * Pruebas del orden de prioridad de la hora del comedor (RelojAplicacion, soporte técnico de HU-01 y HU-02):
 * panel "Modo demostración" > archivo polidinner-hora.txt > POLIDINNER_HORA > hora real en POLIDINNER_ZONA.
 */
class RelojAplicacionTest {

    private static final ZoneId ZONA = RelojAplicacion.ZONA_POR_DEFECTO;

    @Test
    void elPanelGanaAlArchivoYALaVariableDeEntorno() {
        assertEquals(LocalTime.of(12, 50),
                RelojAplicacion.resolver(LocalTime.of(12, 50), "11:00", "13:10", ZONA));
    }

    @Test
    void elArchivoGanaALaVariableDeEntorno() {
        assertEquals(LocalTime.of(11, 0), RelojAplicacion.resolver(null, " 11:00\n", "13:10", ZONA));
    }

    @Test
    void laVariableDeEntornoGanaALaHoraReal() {
        assertEquals(LocalTime.of(13, 10), RelojAplicacion.resolver(null, null, "13:10", ZONA));
    }

    @Test
    void ignoraTextosVaciosOInvalidosYUsaLaHoraRealDeLaZona() {
        LocalTime hora = RelojAplicacion.resolver(null, "   ", "doce", ZONA);
        long diferencia = Math.abs(Duration.between(LocalTime.now(ZONA), hora).getSeconds());
        assertTrue(diferencia < 5 || diferencia > 86_395, "Se esperaba la hora real de " + ZONA);
    }

    @Test
    void laZonaPorDefectoEsEcuadorAunqueElServidorEsteEnUtc() {
        assertEquals(ZoneId.of("America/Guayaquil"), RelojAplicacion.zona(null));
        assertEquals(ZoneId.of("America/Guayaquil"), RelojAplicacion.zona(" "));
        assertEquals(ZoneId.of("America/Guayaquil"), RelojAplicacion.zona("Zona/Inexistente"));
        assertEquals(ZoneId.of("UTC"), RelojAplicacion.zona("UTC"));
    }

    @Test
    void elPanelSoloRegresaAPantallasDeReservas() {
        assertEquals("/cliente/mi-reserva", HoraDemostracionServlet.destino("/cliente/mi-reserva"));
        assertEquals("/cliente/reservar", HoraDemostracionServlet.destino("/cliente/reservar"));
        assertEquals("/cliente/reservar", HoraDemostracionServlet.destino("https://otro-sitio.com"));
        assertEquals("/cliente/reservar", HoraDemostracionServlet.destino(null));
    }
}
