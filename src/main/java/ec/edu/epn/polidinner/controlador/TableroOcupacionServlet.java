package ec.edu.epn.polidinner.controlador;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ec.edu.epn.polidinner.modelo.Franja;
import ec.edu.epn.polidinner.modelo.TableroOcupacion;
import ec.edu.epn.polidinner.persistencia.MesaDAO;
import ec.edu.epn.polidinner.persistencia.ReservaDAO;

/**
 * Trazabilidad: Diagrama de robustez HU-03 - control "Consultar disponibilidad de mesas" entre la frontera
 * "Interfaz de gestión de reservas" (tableroOcupacion.jsp) y la "Salida". Tarea T3.4.
 * Diagrama de actividades HU-03 - "Colocar el tablero en un lugar visible".
 */
@WebServlet("/personal/tablero")
public class TableroOcupacionServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/tableroOcupacion.jsp";

    private final MesaDAO mesaDAO = new MesaDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("tablero", new TableroOcupacion(RelojAplicacion.fechaActual(), mesaDAO, reservaDAO));
        request.setAttribute("franjas", Franja.values());
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}