package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ec.edu.epn.polidinner.modelo.ExistenciaProducto;
import ec.edu.epn.polidinner.modelo.FranjaCaducidad;
import ec.edu.epn.polidinner.servicio.ServicioCaducidad;

/**
 * HU19 Consultar productos por franja de caducidad - pantalla «Caducidad de productos» del personal
 * (protegida por FiltroPersonal en /personal/*). Parámetro "franja": un valor de FranjaCaducidad.
 */
@WebServlet("/personal/caducidad-productos")
public class CaducidadProductosServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/caducidadProductos.jsp";

    private final ServicioCaducidad servicio = new ServicioCaducidad();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("franjas", FranjaCaducidad.values());
        String valor = request.getParameter("franja");
        if (valor != null) {
            try {
                FranjaCaducidad franja = FranjaCaducidad.valueOf(valor);
                List<ExistenciaProducto> existencias = servicio.consultar(franja, RelojAplicacion.fechaActual());
                request.setAttribute("franjaSeleccionada", franja);
                request.setAttribute("existencias", existencias);
                request.setAttribute("consultado", true);
            } catch (IllegalArgumentException e) {
                request.setAttribute("error", "Seleccione una franja válida.");
            } catch (RuntimeException e) {
                log("No se pudo consultar la caducidad de productos", e);
                request.setAttribute("error", "No se pudo consultar la caducidad. Intente de nuevo en unos minutos.");
            }
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}