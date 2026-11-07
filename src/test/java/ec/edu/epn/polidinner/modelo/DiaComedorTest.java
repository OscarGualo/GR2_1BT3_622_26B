package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Corrección de productos persistentes, Paquete 1a (V37 de la guía): un solo día civil del comedor.
 * Caso límite: el servidor está en UTC y ya es 17/11 a la 01:30, pero en el comedor (America/Guayaquil)
 * todavía son las 20:30 del 16/11. Menú, cierre y comprobante deben usar el 16/11.
 */
class DiaComedorTest {

    private static final ZoneId GUAYAQUIL = ZoneId.of("America/Guayaquil");
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 16);
    /** 2026-11-17 01:30 UTC = 2026-11-16 20:30 en Guayaquil. */
    private static final Instant NOCHE_DEL_LUNES = Instant.parse("2026-11-17T01:30:00Z");

    private TimeZone zonaOriginal;

    @BeforeEach
    void servidorEnUtc() {
        zonaOriginal = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterEach
    void restaurarZona() {
        TimeZone.setDefault(zonaOriginal);
    }

    @Test
    void elDiaDelComedorNoAvanzaConElDiaDelServidor() {
        DiaComedor dia = DiaComedor.de(NOCHE_DEL_LUNES, GUAYAQUIL);

        assertEquals(LUNES, dia.getFecha());
        assertEquals(GUAYAQUIL, dia.getZona());
    }

    /** El día del comedor va de 00:00 a 00:00 en Guayaquil (05:00 a 05:00 en UTC). */
    @Test
    void contieneUsaLosLimitesDelDiaEnLaZonaDelComedor() {
        DiaComedor lunes = new DiaComedor(LUNES, GUAYAQUIL);

        assertFalse(lunes.contiene(Date.from(Instant.parse("2026-11-16T04:59:59Z"))));
        assertTrue(lunes.contiene(Date.from(Instant.parse("2026-11-16T05:00:00Z"))));
        assertTrue(lunes.contiene(Date.from(NOCHE_DEL_LUNES)));
        assertFalse(lunes.contiene(Date.from(Instant.parse("2026-11-17T05:00:00Z"))));
        assertFalse(lunes.contiene(null));
    }

    /** La columna DATE guarda la fecha civil: no se desplaza aunque la JVM esté en UTC. */
    @Test
    void laFechaCivilNoDependeDeLaZonaDeLaJvm() {
        Date civil = new DiaComedor(LUNES, GUAYAQUIL).comoFechaCivil();

        assertEquals(LUNES, ((java.sql.Date) civil).toLocalDate());
    }

    @Test
    void elMenuDiarioSeCreaConLaFechaDelComedor() {
        PersonalComedor maria = new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "clave");

        Menu menu = maria.crearMenuDiario(DiaComedor.de(NOCHE_DEL_LUNES, GUAYAQUIL));

        assertEquals(LUNES, ((java.sql.Date) menu.getFecha()).toLocalDate());
        assertEquals(Menu.BORRADOR, menu.getEstado());
        assertEquals(maria, menu.getPersonal());
    }

    /** Un comprobante generado a las 20:30 del lunes (hora del comedor) es del lunes, no del martes. */
    @Test
    void elComprobanteDeLaNocheEsDelDiaDelComedor() {
        Comprobante comprobante = comprobantePagado(Date.from(NOCHE_DEL_LUNES));

        assertTrue(comprobante.esDeHoy(new DiaComedor(LUNES, GUAYAQUIL)));
        assertTrue(comprobante.validarCodigoEntrega(new DiaComedor(LUNES, GUAYAQUIL)));
        assertFalse(comprobante.esDeHoy(new DiaComedor(LUNES.plusDays(1), GUAYAQUIL)));
        assertFalse(comprobante.validarCodigoEntrega(new DiaComedor(LUNES.plusDays(1), GUAYAQUIL)));
    }

    @Test
    void validarCodigoConElDiaDelComedor() {
        PersonalComedor maria = new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "clave");
        Comprobante comprobante = comprobantePagado(Date.from(NOCHE_DEL_LUNES));

        assertTrue(maria.validarCodigo("pd-0457", comprobante, new DiaComedor(LUNES, GUAYAQUIL)));
        assertFalse(maria.validarCodigo("PD-0457", comprobante, new DiaComedor(LUNES.plusDays(1), GUAYAQUIL)));
        assertFalse(maria.validarCodigo("PD-0458", comprobante, new DiaComedor(LUNES, GUAYAQUIL)));
    }

    private static Comprobante comprobantePagado(Date generadoEn) {
        ClienteUniversitario andrea = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(new Plato("Arroz con pollo", 3.50, "Disponible", "", 20), 1);
        pedido.confirmarPago();
        return new Comprobante("PD-0457", generadoEn, pedido);
    }
}
