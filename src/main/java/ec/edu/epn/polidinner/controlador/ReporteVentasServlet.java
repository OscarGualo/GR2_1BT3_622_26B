package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.modelo.ReporteVentas;
import ec.edu.epn.polidinner.persistencia.MenuDAO;
import ec.edu.epn.polidinner.persistencia.PedidoDAO;

/**
 * Trazabilidad: Diagrama de robustez CU05 - controlador "Reporte de ventas".
 * Diagrama de secuencia CU05 - mensaje 17: tras cerrar la venta se redirige al reporte del día.
 * Diagrama de clases Inc. 3 - ReporteVentas(fecha, pedidos) y calcularTotalVendido().
 * También se abre desde el panel en cualquier momento: si la venta sigue abierta muestra el total parcial.
 */
@WebServlet("/personal/reporte")
public class ReporteVentasServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/reporteVentas.jsp";
    private static final String FORMATO_FECHA = "yyyy-MM-dd";

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final MenuDAO menuDAO = new MenuDAO();

    /**
     * Trazabilidad: Diagrama de secuencia CU05 - mensaje 17 (muestra el reporte del día):
     * PedidoDAO.buscarPorFecha(dia) / pedidos, <<create>> ReporteVentas(dia, pedidos) y calcularTotalVendido().
     * Parámetro opcional "fecha" (yyyy-MM-dd) para consultar otro día; por defecto, hoy.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Date hoy = inicioDelDia(new Date());
        Date dia = leerFecha(request.getParameter("fecha"));
        if (dia == null) {
            request.setAttribute("error", "La fecha indicada no es válida. Se muestra el reporte de hoy.");
            dia = hoy;
        }
        request.setAttribute("fecha", new SimpleDateFormat(FORMATO_FECHA).format(dia));
        request.setAttribute("esHoy", dia.equals(hoy));

        try {
            List<Pedido> pedidos = dia.after(hoy) ? Collections.<Pedido>emptyList() : pedidoDAO.buscarPorFecha(dia);
            ReporteVentas reporte = new ReporteVentas(dia, pedidos);
            request.setAttribute("reporte", reporte);
            request.setAttribute("total", reporte.calcularTotalVendido());
            request.setAttribute("pendientes", contarPendientesDeEntrega(pedidos));
            request.setAttribute("ventaCerrada", menuDAO.buscarMenuCerrado(dia) != null);
            request.setAttribute("esFutura", dia.after(hoy));
        } catch (RuntimeException e) {
            log("No se pudo consultar el reporte de ventas", e);
            request.setAttribute("error", "No se pudo consultar el reporte de ventas. Intente de nuevo en unos minutos.");
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /**
     * Soporte técnico (no modelado): convierte el parámetro "fecha" (yyyy-MM-dd) en el inicio de ese día.
     *
     * @return hoy si el parámetro está vacío; null si no es una fecha válida
     */
    static Date leerFecha(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return inicioDelDia(new Date());
        }
        SimpleDateFormat formato = new SimpleDateFormat(FORMATO_FECHA);
        formato.setLenient(false);
        try {
            return inicioDelDia(formato.parse(valor.trim()));
        } catch (ParseException e) {
            return null;
        }
    }

    /** Pedidos ya cobrados que todavía no se retiran: se pueden seguir entregando después del cierre (CU 04). */
    private static int contarPendientesDeEntrega(List<Pedido> pedidos) {
        int pendientes = 0;
        for (Pedido pedido : pedidos) {
            if ("Pagado".equals(pedido.getEstado())) {
                pendientes++;
            }
        }
        return pendientes;
    }

    private static Date inicioDelDia(Date fecha) {
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(fecha);
        calendario.set(Calendar.HOUR_OF_DAY, 0);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario.getTime();
    }
}
