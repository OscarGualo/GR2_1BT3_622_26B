package ec.edu.epn.polidinner.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReporteVentasTest {

    private ClienteUniversitario andrea;
    private Plato arroz;
    private Producto jugo;
    private PersonalComedor personal;

    @BeforeEach
    void preparar() {
        andrea = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));
        arroz = new Plato("Arroz con pollo", 3.50, "Disponible", "Arroz, pollo y ensalada", 20);
        jugo = new Producto("Jugo de naranja", 0.75, "Disponible", "Vaso 300 ml", 30, new Date());
        personal = new PersonalComedor("1700000001", "Carlos", "Pérez", "Cocinero", "clave");
    }

    @Test
    void calcularTotalVendidoSumaSoloPedidosPagadosYEntregados() {
        Pedido pagado = andrea.crearPedido();
        pagado.agregarItem(arroz, 1);
        pagado.agregarItem(jugo, 1);
        pagado.confirmarPago();                       // 4.25, "Pagado"

        Pedido entregado = andrea.crearPedido();
        entregado.agregarItem(jugo, 2);
        entregado.confirmarPago();                    // 1.50
        personal.entregarPedido(entregado);           // "Entregado"

        Pedido creado = andrea.crearPedido();
        creado.agregarItem(arroz, 1);
        creado.calcularTotal();                       // 3.50 sin pagar: no es venta

        ReporteVentas reporte = new ReporteVentas(new Date(), Arrays.asList(pagado, entregado, creado));

        assertEquals(5.75, reporte.calcularTotalVendido(), 0.001);
    }
}