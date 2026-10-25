package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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

    private void validarClienteSinReservaActiva(ClienteUniversitario cliente) {
        for (Reserva r : reservas) {
            if (r.getCliente().getCedula().equals(cliente.getCedula()) && r.estaActiva()) {
                throw new ReservaRechazadaException("Ya tiene una reserva activa para hoy");
            }
        }
    }


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