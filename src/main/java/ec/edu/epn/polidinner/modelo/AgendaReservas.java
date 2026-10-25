package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Trazabilidad: Diagrama de robustez HU-01,
 * control "Realizar reserva". Tareas T1.4 y T2.4.
 *
 * Lleva las reservas del día y delega en ValidadorReserva la
 * comprobación de las reglas antes de incorporar una reserva.
 * No es una entidad JPA.
 *
 * El llamador debe proporcionar las reservas de la fecha
 * correspondiente, por ejemplo mediante ReservaDAO.buscarPorFecha().
 */

public class AgendaReservas {

    private final LocalDate fecha;
    private final List<Reserva> reservas;
    private final ValidadorReserva validador;

    public AgendaReservas(LocalDate fecha, List<Reserva> reservasDelDia) {
        this.fecha = fecha;
        this.reservas = new ArrayList<>(reservasDelDia);
        this.validador = new ValidadorReserva(reservas);
    }

    /**
     * Trazabilidad: Diagrama de actividades HU-01 - "Reservar mesa y franja de horario elegida por el cliente".
     * HU-01 / Escenario 1.1: la reserva se crea en estado "Activa" (prueba PU-01).
     *
     * @throws ReservaRechazadaException con el mensaje que se muestra al cliente
     */
    public Reserva reservar(ClienteUniversitario cliente, Mesa mesa, Franja franja) {
        validador.validar(cliente, mesa, franja);
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