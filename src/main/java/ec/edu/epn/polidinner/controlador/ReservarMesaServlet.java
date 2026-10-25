package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.time.LocalDate;

import javax.persistence.PersistenceException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ec.edu.epn.polidinner.modelo.AgendaReservas;
import ec.edu.epn.polidinner.modelo.ClienteUniversitario;
import ec.edu.epn.polidinner.modelo.Franja;
import ec.edu.epn.polidinner.modelo.Mesa;
import ec.edu.epn.polidinner.modelo.Reserva;
import ec.edu.epn.polidinner.modelo.ReservaRechazadaException;
import ec.edu.epn.polidinner.persistencia.MesaDAO;
import ec.edu.epn.polidinner.persistencia.ReservaDAO;

/**
 * Trazabilidad: Diagrama de robustez HU-01 - control "Realizar reserva" entre la frontera
 * "Interfaz de reservas de mesa" (reservarMesa.jsp) y la "Salida" (mensajes de éxito o rechazo). Tarea T1.5.
 */
@WebServlet("/cliente/reservar")
public class ReservarMesaServlet extends HttpServlet {

    private static final String VISTA = "/vistas/cliente/reservarMesa.jsp";

    /** Destino después de reservar. En HU-02 pasa a "/cliente/mi-reserva", cuando esa pantalla ya exista. */
    private static final String DESTINO_TRAS_RESERVAR = "/cliente/reservar";

    private final MesaDAO mesaDAO = new MesaDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    /**
     * Diagrama de actividades HU-01 - "Ver disponibilidad de mesas" y "Mostrar las mesas y los horarios disponibles".
     * HU-01 / Escenario 2.3: si ya pasaron todas las franjas, la vista muestra "No hay franjas disponibles para hoy".
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("franjas", Franja.noComenzadas(RelojAplicacion.horaActual()));
        request.setAttribute("mesas", mesaDAO.listarTodos());
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /**
     * Diagrama de actividades HU-01 - "Reservar mesa y franja de horario elegida por el cliente".
     * HU-01 / Escenarios 1.1-1.3, 2.1 y 2.2. Patrón POST-redirect-GET con flashExito / flashError.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        ClienteUniversitario cliente = (ClienteUniversitario) sesion.getAttribute("cliente");
        Mesa mesa = buscarMesa(request.getParameter("mesa"));
        Franja franja = leerFranja(request.getParameter("franja"));
        if (mesa == null || franja == null) {
            sesion.setAttribute("flashError", "Elija una mesa y una franja válidas.");
            response.sendRedirect(request.getContextPath() + "/cliente/reservar");
            return;
        }
        LocalDate hoy = LocalDate.now();
        AgendaReservas agenda = new AgendaReservas(hoy, reservaDAO.buscarPorFecha(hoy));
        try {
            Reserva reserva = agenda.reservar(cliente, mesa, franja, RelojAplicacion.horaActual());
            reservaDAO.guardar(reserva);
            sesion.setAttribute("flashExito", "Mesa " + mesa.getNumero() + " reservada de "
                    + franja.getInicio() + " a " + franja.getFin());
            response.sendRedirect(request.getContextPath() + DESTINO_TRAS_RESERVAR);
        } catch (ReservaRechazadaException e) {
            sesion.setAttribute("flashError", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/cliente/reservar");
        } catch (PersistenceException e) {
            // Dos peticiones a la vez: los índices únicos de migracion_reservas.sql rechazan la segunda
            log("Reserva no guardada por un conflicto en la base de datos", e);
            sesion.setAttribute("flashError", "Otra persona acaba de reservar esa mesa o usted ya tiene una reserva activa."
                    + " Revise e intente de nuevo.");
            response.sendRedirect(request.getContextPath() + "/cliente/reservar");
        }
    }

    private Mesa buscarMesa(String valor) {
        try {
            return valor == null ? null : mesaDAO.buscarPorId(Integer.valueOf(valor.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Franja leerFranja(String valor) {
        try {
            return valor == null ? null : Franja.valueOf(valor);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}