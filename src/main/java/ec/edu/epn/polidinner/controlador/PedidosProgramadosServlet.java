package ec.edu.epn.polidinner.controlador;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ec.edu.epn.polidinner.servicio.ServicioFranjas;

/**
 * Trazabilidad: Examen 1B, HU13 «Ver pedidos programados por franja» (T13.1-T13.3).
 * Solo lectura. El acceso exclusivo del personal (Esc. 3.1 y 3.2) lo resuelve FiltroPersonal sobre
 * /personal/*: un estudiante o un visitante sin sesión son redirigidos al inicio de sesión del personal.
 */
@WebServlet("/personal/programados")
public class PedidosProgramadosServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/pedidosProgramados.jsp";

    private final ServicioFranjas servicioFranjas = new ServicioFranjas();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("fechaHoy", RelojAplicacion.fechaActual());
        request.setAttribute("franjas", servicioFranjas.pedidosProgramadosDelDia(RelojAplicacion.fechaActual()));
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}
