package ec.edu.epn.polidinner.modelo;

import java.util.List;


/**
 * Trazabilidad: Diagrama de robustez HU-01 - reglas del control "Realizar reserva" (tarea T1.4)
 * y Diagrama de actividades HU-02 - "Anotar restricción de reservas por el día" (tarea T2.4).
 *
 * Clase extraída de AgendaReservas (refactorización Extract Class): decide si una nueva
 * reserva cumple las reglas, mientras AgendaReservas solo lleva las reservas del día.
 * No es una entidad JPA.
 */

public class ValidadorReserva {

    private final List<Reserva> reservasDelDia;

    public ValidadorReserva(List<Reserva> reservasDelDia) {
        this.reservasDelDia = reservasDelDia;
    }

    /**
     * Comprueba todas las reglas de una nueva reserva, en el orden
     * mesa libre, cliente no restringido y cliente sin reserva activa.
     *
     * @throws ReservaRechazadaException con el mensaje que se muestra al cliente
     */
    public void validar(ClienteUniversitario cliente, Mesa mesa, Franja franja) {
        validarMesaLibre(mesa, franja);
        validarClienteNoRestringido(cliente);
        validarClienteSinReservaActiva(cliente);
    }

    /**
     * HU-01, escenario 1.2: rechaza una mesa con reserva activa
     * en la misma franja. Prueba PU-02.
     *
     * HU-02, escenario 2.1: una reserva cancelada
     * deja de bloquear esa mesa. Prueba PU-04.
     */

    private void validarMesaLibre(Mesa mesa, Franja franja) {
        for (Reserva r : reservasDelDia) {
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
        for (Reserva r : reservasDelDia) {
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
        for (Reserva r : reservasDelDia) {
            if (r.getCliente().getCedula().equals(cliente.getCedula()) && r.restringeReservasDelDia()) {
                throw new ReservaRechazadaException("Canceló una reserva fuera de plazo; no puede reservar otra mesa hoy");
            }
        }
    }
}
