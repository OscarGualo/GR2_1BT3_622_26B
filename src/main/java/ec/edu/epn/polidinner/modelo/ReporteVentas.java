package ec.edu.epn.polidinner.modelo;

import java.util.Date;
import java.util.List;

public class ReporteVentas {

    private final Date fecha;
    private final List<Pedido> pedidos;

    public ReporteVentas(Date fecha, List<Pedido> pedidos) {
        this.fecha = fecha;
        this.pedidos = pedidos;
    }

    public double calcularTotalVendido() {
        double suma = 0;
        for (Pedido pedido : pedidos) {
            if ("Pagado".equals(pedido.getEstado()) || "Entregado".equals(pedido.getEstado())) {
                suma += pedido.getTotal();
            }
        }
        return suma;
    }
}