package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ec.edu.epn.polidinner.modelo.CalculadoraVentasProducto;
import ec.edu.epn.polidinner.modelo.DiaComedor;
import ec.edu.epn.polidinner.modelo.FilaVentaProducto;
import ec.edu.epn.polidinner.modelo.FiltroVentasValidas;
import ec.edu.epn.polidinner.modelo.PeriodoVentas;
import ec.edu.epn.polidinner.modelo.VentaProducto;
import ec.edu.epn.polidinner.persistencia.VentaProductoDAO;

/**
 * HU18 Consultar ventas de productos - TDD con mocks de ServicioVentasProducto (PM01).
 * El DAO, el filtro y la calculadora son objetos simulados con Mockito.
 */
class ServicioVentasProductoMockTest {

    private static final ZoneId ZONA = ZoneId.of("America/Guayaquil");

    /** PM01 - Criterio 18.2: consulta por fecha de pago del periodo y resume solo las ventas válidas. */
    @Test
    void consultaPorFechaDePagoDelPeriodoYResumeSoloLasVentasValidas() {
        VentaProductoDAO dao = Mockito.mock(VentaProductoDAO.class);
        FiltroVentasValidas filtro = Mockito.mock(FiltroVentasValidas.class);
        CalculadoraVentasProducto calculadora = Mockito.mock(CalculadoraVentasProducto.class);
        ServicioVentasProducto servicio = new ServicioVentasProducto(dao, filtro, calculadora);

        PeriodoVentas periodo = new PeriodoVentas(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 8));
        java.util.Date desde = new DiaComedor(LocalDate.of(2026, 11, 2), ZONA).inicio();
        java.util.Date hasta = new DiaComedor(LocalDate.of(2026, 11, 8), ZONA).fin();
        Date caducidad = Date.valueOf(LocalDate.of(2026, 11, 10));
        VentaProducto entregada = new VentaProducto(201, "Yogur 150 g", caducidad, 2, "Entregado", desde);
        VentaProducto cancelada = new VentaProducto(201, "Yogur 150 g", caducidad, 3, "Cancelado", desde);
        List<VentaProducto> encontradas = List.of(entregada, cancelada);
        List<VentaProducto> validas = List.of(entregada);
        List<FilaVentaProducto> resumen = List.of(new FilaVentaProducto(201, "Yogur 150 g", caducidad, 2));

        Mockito.when(dao.buscarPorFechaDePago(desde, hasta)).thenReturn(encontradas);
        Mockito.when(filtro.filtrar(encontradas)).thenReturn(validas);
        Mockito.when(calculadora.resumir(validas)).thenReturn(resumen);

        List<FilaVentaProducto> resultado = servicio.consultar(periodo, ZONA);

        assertEquals(resumen, resultado);
        Mockito.verify(dao).buscarPorFechaDePago(desde, hasta);
        Mockito.verify(calculadora).resumir(validas);
    }
}