package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.time.LocalDate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ec.edu.epn.polidinner.modelo.ClienteUniversitario;
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
        request.setAttribute("reserva", reservaDAO.buscarActivaDelCliente(cliente.getCedula(), LocalDate.now()));
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}