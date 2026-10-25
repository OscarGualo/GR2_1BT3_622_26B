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
     * HU-01 / Escenario 1.1: la reserva se crea en estado "Activa" (prueba PU-01).
     *
     * @throws ReservaRechazadaException con el mensaje que se muestra al cliente
     */
    public Reserva reservar(ClienteUniversitario cliente, Mesa mesa, Franja franja) {
        validarMesaLibre(mesa, franja);
        validarClienteSinReservaActiva(cliente);
        Reserva reserva = new Reserva(cliente, mesa, fecha, franja);
        reservas.add(reserva);
        return reserva;
    }

    /** HU-01 / Escenario 1.2: la mesa no puede estar reservada en esa franja (prueba PU-02). */
    private void validarMesaLibre(Mesa mesa, Franja franja) {
        for (Reserva r : reservas) {
            if (r.getMesa().getNumero() == mesa.getNumero() && r.getFranja() == franja) {
                throw new ReservaRechazadaException("La mesa " + mesa.getNumero() + " ya está reservada en esa franja");
            }
        }
    }

    /** HU-01 / Escenario 1.3: el cliente no puede tener otra reserva activa hoy (prueba PU-03). */
    private void validarClienteSinReservaActiva(ClienteUniversitario cliente) {
        for (Reserva r : reservas) {
            if (r.getCliente().getCedula().equals(cliente.getCedula())) {
                throw new ReservaRechazadaException("Ya tiene una reserva activa para hoy");
            }
        }
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }
}