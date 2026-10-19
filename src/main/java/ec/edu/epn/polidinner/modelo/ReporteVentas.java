package ec.edu.epn.polidinner.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ReporteVentas {

    private static final String PAGADO = "Pagado";
    private static final String ENTREGADO = "Entregado";

    private final Date fecha;
    private final List<Pedido> pedidos;

    public ReporteVentas(Date fecha, List<Pedido> pedidos) {
        this.fecha = fecha;
        this.pedidos = new ArrayList<>(pedidos);
    }

    public double calcularTotalVendido() {
        double suma = 0;
        for (Pedido pedido : pedidos) {
            if (esVenta(pedido)) {
                suma += pedido.getTotal();
            }
        }
        return Math.round(suma * 100) / 100.0;
    }

    private boolean esVenta(Pedido pedido) {
        return PAGADO.equals(pedido.getEstado()) || ENTREGADO.equals(pedido.getEstado());
    }

    public Date getFecha() {
        return fecha;
    }

    public List<Pedido> getPedidos() {
        return Collections.unmodifiableList(pedidos);
    }
}