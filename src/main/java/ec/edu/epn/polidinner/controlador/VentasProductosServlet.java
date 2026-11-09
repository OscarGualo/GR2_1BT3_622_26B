package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ec.edu.epn.polidinner.modelo.DiaComedor;
import ec.edu.epn.polidinner.modelo.FilaVentaProducto;
import ec.edu.epn.polidinner.modelo.PeriodoVentas;
import ec.edu.epn.polidinner.servicio.ServicioVentasProducto;

/**
 * HU18 Consultar ventas de productos - pantalla «Ventas de productos» del personal (protegida por
 * FiltroPersonal en /personal/*). Parámetro "periodo": hoy, 7dias o rango (con "inicio" y "fin").
 * El día y la zona son los del comedor (RelojAplicacion), no los del servidor.
 */
@WebServlet("/personal/ventas-productos")
public class VentasProductosServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/ventasProductos.jsp";

    private final ServicioVentasProducto servicio = new ServicioVentasProducto();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DiaComedor hoy = RelojAplicacion.diaComedor();
        String opcion = request.getParameter("periodo");
        request.setAttribute("opcion", opcion == null ? "hoy" : opcion);
        if (opcion != null) {
            try {
                PeriodoVentas periodo = leerPeriodo(request, opcion, hoy.getFecha());
                List<FilaVentaProducto> filas = servicio.consultar(periodo, hoy.getZona());
                request.setAttribute("periodo", periodo);
                request.setAttribute("filas", filas);
                request.setAttribute("consultado", true);
            } catch (DateTimeParseException e) {
                request.setAttribute("error", "Ingrese fechas válidas (día, mes y año).");
            } catch (IllegalArgumentException e) {
                request.setAttribute("error", e.getMessage());
            } catch (RuntimeException e) {
                log("No se pudo consultar las ventas de productos", e);
                request.setAttribute("error", "No se pudo consultar las ventas. Intente de nuevo en unos minutos.");
            }
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private static PeriodoVentas leerPeriodo(HttpServletRequest request, String opcion, LocalDate hoy) {
        switch (opcion) {
            case "hoy":
                return PeriodoVentas.hoy(hoy);
            case "7dias":
                return PeriodoVentas.ultimos7Dias(hoy);
            default:
                return new PeriodoVentas(fecha(request.getParameter("inicio")), fecha(request.getParameter("fin")));
        }
    }

    private static LocalDate fecha(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : LocalDate.parse(valor.trim());
    }
}