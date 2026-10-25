package ec.edu.epn.polidinner.controlador;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ec.edu.epn.polidinner.modelo.ClienteUniversitario;
import ec.edu.epn.polidinner.modelo.Reserva;
import ec.edu.epn.polidinner.persistencia.ReservaDAO;

/**
 * Trazabilidad: Diagrama de robustez HU-02 - control "Cancelar Reserva" entre la frontera
 * "Interfaz de reservas de mesa" (miReserva.jsp) y la "Salida". Tarea T2.5.
 */
@WebServlet("/cliente/mi-reserva")
public class MiReservaServlet extends HttpServlet {

    private static final String VISTA = "/vistas/cliente/miReserva.jsp";

    private final ReservaDAO reservaDAO = new ReservaDAO();

    /**
     * Diagrama de actividades HU-02 - "Buscar reserva del cliente en el registro", decisión "¿Tiene reserva activa?".
     * Sin reserva activa, la vista muestra "No tiene reservas activas para hoy".
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ClienteUniversitario cliente = (ClienteUniversitario) request.getSession().getAttribute("cliente");
        request.setAttribute("reserva", reservaDAO.buscarActivaDelCliente(cliente.getCedula(), RelojAplicacion.fechaActual()));
        RelojAplicacion.exponer(request);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /**
     * Diagrama de actividades HU-02 - "Revisar hora actual y franja", "Marcar reserva como cancelada",
     * "Liberar la mesa en esa franja" e "Informar cancelación al cliente".
     * HU-02 / Escenarios 1.1-1.3 y 2.3. Patrón POST-redirect-GET con flashExito / flashError.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        ClienteUniversitario cliente = (ClienteUniversitario) sesion.getAttribute("cliente");
        Reserva reserva = buscarReserva(request.getParameter("idReserva"));
        if (reserva == null || !reserva.getCliente().getCedula().equals(cliente.getCedula())) {
            sesion.setAttribute("flashError", "No se encontró esa reserva entre las suyas.");
        } else if (!reserva.cancelar(RelojAplicacion.horaActual()) || !reservaDAO.guardarCancelacion(reserva)) {
            // HU-02 / Escenario 2.3: ya estaba cancelada, o otra petición la canceló en ese mismo momento
            sesion.setAttribute("flashError", "La reserva ya fue cancelada");
        } else {
            sesion.setAttribute("flashExito", reserva.restringeReservasDelDia()
                    ? "Reserva cancelada fuera de plazo. No podrá reservar otra mesa hoy."
                    : "Reserva cancelada");
        }
        response.sendRedirect(request.getContextPath() + "/cliente/mi-reserva");
    }

    private Reserva buscarReserva(String valor) {
        try {
            return valor == null ? null : reservaDAO.buscarPorId(Integer.valueOf(valor.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}