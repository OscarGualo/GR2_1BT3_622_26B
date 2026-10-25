package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Trazabilidad: Diagrama de robustez HU-01,
 * control "Realizar reserva". Tareas T1.4 y T2.4.
 *
 * Coordina la comprobación de ocupación y restricciones
 * del cliente antes de incorporar una reserva.
 * No es una entidad JPA.
 *
 * El llamador debe proporcionar las reservas de la fecha
 * correspondiente, por ejemplo mediante ReservaDAO.buscarPorFecha().
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
        validarClienteNoRestringido(cliente);
        validarClienteSinReservaActiva(cliente);
        Reserva reserva = new Reserva(cliente, mesa, fecha, franja);
        reservas.add(reserva);
        return reserva;
    }

    /**
     * HU-01, escenario 1.2: rechaza una mesa con reserva activa
     * en la misma franja. Prueba PU-02.
     *
     * HU-02, escenario 2.1: una reserva cancelada
     * deja de bloquear esa mesa. Prueba PU-04.
     */

    private void validarMesaLibre(Mesa mesa, Franja franja) {
        for (Reserva r : reservas) {
            final boolean esLaMismaMesa = r.getMesa().getNumero() == mesa.getNumero();
            final boolean esLaMismaFranja = r.getFranja() == franja;
            final boolean ocupaLaMesa = r.estaActiva();
            if (esLaMismaMesa && esLaMismaFranja && ocupaLaMesa) {
                throw new ReservaRechazadaException("La mesa " + mesa.getNumero() + " ya está reservada en esa franja");
            }
        }
    }

    /**
     * HU-01, escenario 1.3: rechaza una nueva reserva
     * cuando el cliente ya tiene una activa.
     * Prueba PU-03.
     */

    private void validarClienteSinReservaActiva(ClienteUniversitario cliente) {
        for (Reserva r : reservas) {
            if (r.getCliente().getCedula().equals(cliente.getCedula()) && r.estaActiva()) {
                throw new ReservaRechazadaException("Ya tiene una reserva activa para hoy");
            }
        }
    }

    /**
     * HU-02, escenario 1.3. Tarea T2.4.
     * Comprueba la restricción derivada de una cancelación tardía
     * antes de permitir una nueva reserva.
     * Prueba PU-04.
     */

    private void validarClienteNoRestringido(ClienteUniversitario cliente) {
        for (Reserva r : reservas) {
            if (r.getCliente().getCedula().equals(cliente.getCedula()) && r.restringeReservasDelDia()) {
                throw new ReservaRechazadaException("Canceló una reserva fuera de plazo; no puede reservar otra mesa hoy");
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