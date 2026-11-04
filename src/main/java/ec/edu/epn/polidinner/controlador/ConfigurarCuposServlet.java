package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ec.edu.epn.polidinner.servicio.ReglaPedidoException;
import ec.edu.epn.polidinner.servicio.ReglasFranja;
import ec.edu.epn.polidinner.servicio.ServicioFranjas;

/**
 * Trazabilidad: Examen 1B, HU15 «Configurar el cupo de las franjas» (T15.3).
 * Acciones de POST (patrón PRG): cupoGeneral, cupoFranja, cerrar y abrir. Las reglas del cupo efectivo
 * viven en ServicioFranjas; esta clase solo traduce el formulario. El acceso lo restringe FiltroPersonal.
 */
@WebServlet("/personal/cupos")
public class ConfigurarCuposServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/configurarCupos.jsp";

    private final ServicioFranjas servicioFranjas = new ServicioFranjas();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LocalDate hoy = RelojAplicacion.fechaActual();
        request.setAttribute("fechaHoy", hoy);
        request.setAttribute("cupoGeneral", servicioFranjas.cupoGeneral());
        request.setAttribute("franjas", servicioFranjas.franjasDelDia(hoy));
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        String accion = request.getParameter("accion");
        LocalDate hoy = RelojAplicacion.fechaActual();
        try {
            if ("cupoGeneral".equals(accion)) {
                servicioFranjas.configurarCupoGeneral(ServicioFranjas.leerCupo(request.getParameter("cupo")));
                sesion.setAttribute("flashExito", "Cupo general guardado: " + servicioFranjas.cupoGeneral()
                        + " pedidos por franja.");
            } else if ("cupoFranja".equals(accion)) {
                LocalTime inicio = leerFranja(request.getParameter("franja"));
                servicioFranjas.fijarCupoFranja(hoy, inicio, ServicioFranjas.leerCupo(request.getParameter("cupo")));
                sesion.setAttribute("flashExito", "Cupo de la franja " + ReglasFranja.etiqueta(inicio) + " guardado.");
            } else if ("cerrar".equals(accion)) {
                LocalTime inicio = leerFranja(request.getParameter("franja"));
                servicioFranjas.cerrarFranja(hoy, inicio);
                sesion.setAttribute("flashExito", "Franja " + ReglasFranja.etiqueta(inicio)
                        + " cerrada: no admite pedidos nuevos y los ya asignados siguen vigentes.");
            } else if ("abrir".equals(accion)) {
                LocalTime inicio = leerFranja(request.getParameter("franja"));
                servicioFranjas.abrirFranja(hoy, inicio);
                sesion.setAttribute("flashExito", "Franja " + ReglasFranja.etiqueta(inicio) + " abierta de nuevo.");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no reconocida");
                return;
            }
        } catch (ReglaPedidoException e) {
            sesion.setAttribute("flashError", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/personal/cupos");
    }

    private static LocalTime leerFranja(String valor) {
        try {
            LocalTime inicio = LocalTime.parse(valor == null ? "" : valor.trim());
            if (!ReglasFranja.iniciosDelDia().contains(inicio)) {
                throw new ReglaPedidoException("La franja no existe");
            }
            return inicio;
        } catch (DateTimeParseException e) {
            throw new ReglaPedidoException("La franja no existe");
        }
    }
}
