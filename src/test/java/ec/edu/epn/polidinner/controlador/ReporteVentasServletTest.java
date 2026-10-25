package ec.edu.epn.polidinner.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * Pruebas del parámetro "fecha" del reporte de ventas (CU05, ReporteVentasServlet).
 */
class ReporteVentasServletTest {

    @Test
    void sinFechaDevuelveElInicioDeHoy() {
        Calendar hoy = Calendar.getInstance();
        for (String vacio : new String[] {null, "", "  "}) {
            Calendar leida = aCalendario(ReporteVentasServlet.leerFecha(vacio));
            assertEquals(hoy.get(Calendar.YEAR), leida.get(Calendar.YEAR));
            assertEquals(hoy.get(Calendar.DAY_OF_YEAR), leida.get(Calendar.DAY_OF_YEAR));
            assertEquals(0, leida.get(Calendar.HOUR_OF_DAY));
            assertEquals(0, leida.get(Calendar.MINUTE));
        }
    }

    @Test
    void leeUnaFechaValida() {
        Calendar leida = aCalendario(ReporteVentasServlet.leerFecha("2026-10-24"));
        assertEquals(2026, leida.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, leida.get(Calendar.MONTH));
        assertEquals(24, leida.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    void rechazaFechasInvalidas() {
        assertNull(ReporteVentasServlet.leerFecha("abc"));
        assertNull(ReporteVentasServlet.leerFecha("2026-13-40"));
        assertNull(ReporteVentasServlet.leerFecha("2026-02-30"));
        assertNull(ReporteVentasServlet.leerFecha("24/10/2026"));
    }

    private static Calendar aCalendario(Date fecha) {
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(fecha);
        return calendario;
    }
}
