package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Soporte técnico (no modelado): panel "Modo demostración" (vistas/comun/horaDemostracion.jsp).
 * Permite demostrar los escenarios de HU-01 y HU-02 a cualquier hora del día fijando la hora del comedor,
 * sin cambiar la fecha del servidor ni del contenedor Docker. No altera las reglas de negocio: solo cambia
 * la hora que RelojAplicacion entrega a Franja, AgendaReservas y Reserva. Se apaga con POLIDINNER_DEMO=false.
 */
@WebServlet("/cliente/hora-demostracion")
public class HoraDemostracionServlet extends HttpServlet {

    /** acciones: fijar (con el parámetro hora, HH:mm) / real. Patrón POST-redirect-GET con flashExito / flashError. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!RelojAplicacion.modoDemostracionActivo()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        HttpSession sesion = request.getSession();
        if ("real".equals(request.getParameter("accion"))) {
            RelojAplicacion.quitarHoraSimulada();
            sesion.setAttribute("flashExito", "Se usa de nuevo la hora real del comedor");
        } else {
            LocalTime hora = leerHora(request.getParameter("hora"));
            if (hora == null) {
                sesion.setAttribute("flashError", "Escriba una hora válida, por ejemplo 11:30.");
            } else {
                RelojAplicacion.fijarHoraSimulada(hora);
                sesion.setAttribute("flashExito", "Hora del comedor simulada: " + RelojAplicacion.horaActual());
            }
        }
        response.sendRedirect(request.getContextPath() + destino(request.getParameter("volver")));
    }

    /** Solo vuelve a las pantallas de reservas, para no permitir redirecciones a otros sitios. */
    static String destino(String volver) {
        return "/cliente/mi-reserva".equals(volver) ? "/cliente/mi-reserva" : "/cliente/reservar";
    }

    private static LocalTime leerHora(String valor) {
        try {
            return valor == null ? null : LocalTime.parse(valor.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
