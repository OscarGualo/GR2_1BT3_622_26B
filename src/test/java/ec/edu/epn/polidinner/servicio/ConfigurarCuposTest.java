package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.ClienteUniversitario;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.servicio.ServicioFranjas.DisponibilidadFranja;
import ec.edu.epn.polidinner.servicio.ServicioFranjas.Estado;

/**
 * Trazabilidad: Examen 1B, HU15 «Configurar el cupo de las franjas» (CA1, CA2, CA3) y
 * HU16 CA1 (mostrar la disponibilidad de cada franja). El inicio de sesión del personal lo cubre FiltroPersonal.
 */
class ConfigurarCuposTest extends BaseDatosPrueba {

    private final ServicioFranjas cupos = new ServicioFranjas();

    @BeforeEach
    void preparar() {
        prepararBase();
    }

    @AfterEach
    void limpiar() {
        cerrarBase();
    }

    private DisponibilidadFranja franjaDel(int hora, int minuto) {
        return cupos.franjasDelDia(LUNES).stream()
                .filter(f -> f.getHoraInicio().equals(LocalTime.of(hora, minuto)))
                .findFirst().orElseThrow();
    }

    @Test
    void hu15_elDiaTiene16FranjasDe15MinutosEntre11Y15() {
        var franjas = cupos.franjasDelDia(LUNES);

        assertEquals(16, franjas.size());
        assertEquals("11:00–11:15", franjas.get(0).getEtiqueta());
        assertEquals("14:45–15:00", franjas.get(15).getEtiqueta());
    }

    @Test
    void hu15_esc_1_1_elCupoGeneralRigeEnTodasLasFranjasSinCupoEspecifico() {
        cupos.configurarCupoGeneral(12);
        cupos.configurarCupoGeneral(15);

        assertEquals(15, cupos.cupoGeneral());
        assertTrue(cupos.franjasDelDia(LUNES).stream().allMatch(f -> f.getCupoEfectivo() == 15));
    }

    @Test
    void hu15_esc_1_2_rechazaUnCupoNoValidoYConservaElAnterior() {
        cupos.configurarCupoGeneral(15);

        assertEquals("El cupo debe ser un número entero mayor que cero",
                assertThrows(ReglaPedidoException.class, () -> ServicioFranjas.leerCupo("0")).getMessage());
        assertEquals("El cupo debe ser un número entero mayor que cero",
                assertThrows(ReglaPedidoException.class, () -> ServicioFranjas.leerCupo("abc")).getMessage());
        assertEquals("El cupo debe ser un número entero mayor que cero",
                assertThrows(ReglaPedidoException.class, () -> ServicioFranjas.leerCupo("-3")).getMessage());
        assertEquals("El cupo debe ser un número entero mayor que cero",
                assertThrows(ReglaPedidoException.class, () -> cupos.configurarCupoGeneral(0)).getMessage());
        assertEquals(15, cupos.cupoGeneral());
    }

    @Test
    void hu15_esc_2_1_unaFranjaDeHoraPicoAdmiteMasQueLasDemas() {
        cupos.configurarCupoGeneral(15);

        cupos.fijarCupoFranja(LUNES, franja(12, 30), 25);

        assertEquals(25, franjaDel(12, 30).getCupoEfectivo());
        assertEquals(15, franjaDel(12, 45).getCupoEfectivo());
        assertEquals(15, franjaDel(13, 0).getCupoEfectivo());
    }

    @Test
    void hu15_esc_2_2_cerrarUnaFranjaLaMuestraCerradaYNoAdmitePedidos() {
        cupos.cerrarFranja(LUNES, franja(13, 0));

        assertEquals(Estado.CERRADA, franjaDel(13, 0).getEstado());
        assertFalse(franjaDel(13, 0).isElegible());
    }

    @Test
    void hu15_unaFranjaCerradaSePuedeReabrir() {
        cupos.cerrarFranja(LUNES, franja(13, 0));
        cupos.abrirFranja(LUNES, franja(13, 0));

        assertEquals(Estado.DISPONIBLE, franjaDel(13, 0).getEstado());
    }

    @Test
    void hu15_esc_3_1_rechazaUnCupoMenorQueLosPedidosAsignados() {
        cupos.configurarCupoGeneral(15);
        asignar(franja(12, 30), 12);

        String mensaje = assertThrows(ReglaPedidoException.class,
                () -> cupos.fijarCupoFranja(LUNES, franja(12, 30), 10)).getMessage();

        assertEquals("La franja 12:30–12:45 ya tiene 12 pedidos asignados; el cupo no puede ser menor que 12",
                mensaje);
        assertEquals(15, franjaDel(12, 30).getCupoEfectivo());
    }

    @Test
    void hu15_elCupoEspecificoIgualALosAsignadosSeAcepta() {
        asignar(franja(12, 30), 12);

        cupos.fijarCupoFranja(LUNES, franja(12, 30), 12);

        assertEquals(12, franjaDel(12, 30).getCupoEfectivo());
        assertEquals(Estado.LLENA, franjaDel(12, 30).getEstado());
    }

    @Test
    void hu15_elCupoGeneralNoPuedeBajarPorDebajoDeLosPedidosDeUnaFranjaQueLoUsa() {
        cupos.configurarCupoGeneral(15);
        asignar(franja(12, 30), 12);

        String mensaje = assertThrows(ReglaPedidoException.class, () -> cupos.configurarCupoGeneral(10))
                .getMessage();

        assertEquals("La franja 12:30–12:45 ya tiene 12 pedidos asignados; el cupo no puede ser menor que 12",
                mensaje);
        assertEquals(15, cupos.cupoGeneral());
    }

    @Test
    void hu15_esc_3_2_cerrarUnaFranjaConPedidosLosConservaPagados() {
        ClienteUniversitario rico = crearCliente("1750000004", "Rico", "Prueba", "2024-0500", 100.00);
        for (int i = 0; i < 4; i++) {
            programar(rico, a(10, 0), franja(13, 0), arroz, 1);
        }

        cupos.cerrarFranja(LUNES, franja(13, 0));

        var resumen = cupos.pedidosProgramadosDelDia(LUNES);
        assertEquals(1, resumen.size());
        assertEquals(4, resumen.get(0).getCantidadPedidos());
        for (Pedido pedido : resumen.get(0).getPedidos()) {
            assertEquals("Pagado", pedido.getEstado());
        }
        assertThrows(ReglaPedidoException.class,
                () -> servicio.programarYPagar(pedidoDe(andrea, jugo, 1), LUNES, franja(13, 0), a(10, 0)));
        assertEquals(4, asignados(franja(13, 0)));
    }

    @Test
    void hu16_esc_1_1_muestraLosLugaresDisponibles() {
        cupos.configurarCupoGeneral(15);
        asignar(franja(12, 30), 9);

        var ofrecida = cupos.franjasOfrecidas(a(10, 0)).stream()
                .filter(f -> f.getHoraInicio().equals(franja(12, 30))).findFirst().orElseThrow();

        assertEquals("Quedan 6 lugares", ofrecida.getTextoLugares());
        assertTrue(ofrecida.isElegible());
    }

    @Test
    void hu16_esc_1_2_marcaComoLlenaYCerradaLasFranjasNoElegibles() {
        cupos.configurarCupoGeneral(15);
        asignar(franja(12, 45), 15);
        cupos.cerrarFranja(LUNES, franja(13, 0));

        var ofrecidas = cupos.franjasOfrecidas(a(10, 0));
        var llena = ofrecidas.stream().filter(f -> f.getHoraInicio().equals(franja(12, 45))).findFirst().orElseThrow();
        var cerrada = ofrecidas.stream().filter(f -> f.getHoraInicio().equals(franja(13, 0))).findFirst().orElseThrow();

        assertEquals("Llena", llena.getTextoLugares());
        assertFalse(llena.isElegible());
        assertEquals("Cerrada", cerrada.getTextoLugares());
        assertFalse(cerrada.isElegible());
    }

    @Test
    void hu16_elSelectorSoloOfreceLasFranjasQueTodaviaNoEmpezaron() {
        var ofrecidas = cupos.franjasOfrecidas(a(12, 5));

        assertEquals(LocalTime.of(12, 15), ofrecidas.get(0).getHoraInicio());
        assertEquals(LocalTime.of(14, 45), ofrecidas.get(ofrecidas.size() - 1).getHoraInicio());
    }
}
