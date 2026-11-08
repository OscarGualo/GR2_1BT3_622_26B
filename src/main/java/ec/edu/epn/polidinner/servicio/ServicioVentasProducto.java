package ec.edu.epn.polidinner.servicio;

import java.time.ZoneId;
import java.util.List;

import ec.edu.epn.polidinner.modelo.CalculadoraVentasProducto;
import ec.edu.epn.polidinner.modelo.FilaVentaProducto;
import ec.edu.epn.polidinner.modelo.FiltroVentasValidas;
import ec.edu.epn.polidinner.modelo.PeriodoVentas;
import ec.edu.epn.polidinner.modelo.VentaProducto;
import ec.edu.epn.polidinner.persistencia.VentaProductoDAO;

/**
 * Soporte técnico NO modelado - HU18 (criterio 18.2): consulta las ventas por fecha de pago y entrega
 * solo las válidas al resumen. Origen TDD: ServicioVentasProductoMockTest (PM01).
 */
public class ServicioVentasProducto {

    private final VentaProductoDAO ventaDAO;
    private final FiltroVentasValidas filtro;
    private final CalculadoraVentasProducto calculadora;

    public ServicioVentasProducto() {
        this(new VentaProductoDAO(), new FiltroVentasValidas(), new CalculadoraVentasProducto());
    }

    ServicioVentasProducto(VentaProductoDAO ventaDAO, FiltroVentasValidas filtro,
                           CalculadoraVentasProducto calculadora) {
        this.ventaDAO = ventaDAO;
        this.filtro = filtro;
        this.calculadora = calculadora;
    }

    public List<FilaVentaProducto> consultar(PeriodoVentas periodo, ZoneId zona) {
        List<VentaProducto> ventas = ventaDAO.buscarPorFechaDePago(periodo.desde(zona), periodo.hastaExclusivo(zona));
        return calculadora.resumir(filtro.filtrar(ventas));
    }
}