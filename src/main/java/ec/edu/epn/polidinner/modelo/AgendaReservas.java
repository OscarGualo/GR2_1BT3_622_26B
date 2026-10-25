package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Trazabilidad: Diagrama de robustez HU-01 - control "Realizar reserva" (tarea T1.4).
 * No es entidad JPA: se arma con las reservas del día (ReservaDAO.buscarPorFecha) y valida
 * cada nueva reserva contra ellas antes de guardarla. Origen: prueba PU-01.
 */
public class AgendaReservas {

    private final LocalDate fecha;
    private final List<Reserva> reservas;

    public AgendaReservas(LocalDate fecha, List<Reserva> reservasDelDia) {
        this.fecha = fecha;
        this.reservas = new ArrayList<>(reservasDelDia);
    }

    /**
     * Trazabilidad: Diagrama de actividades HU-01 - "Reservar mesa y franja de horario elegida por el cliente".
     * HU-01 / Escenario 1.1: la reserva se crea en estado "Activa".
     */
    public Reserva reservar(ClienteUniversitario cliente, Mesa mesa, Franja franja) {
        Reserva reserva = new Reserva(cliente, mesa, fecha, franja);
        reservas.add(reserva);
        return reserva;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }
}