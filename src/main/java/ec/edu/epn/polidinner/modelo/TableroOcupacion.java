package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ec.edu.epn.polidinner.persistencia.MesaDAO;
import ec.edu.epn.polidinner.persistencia.ReservaDAO;

/**
 * Trazabilidad: Diagrama de robustez HU-03 - control "Consultar disponibilidad de mesas" (tareas T3.1-T3.3).
 * No es entidad JPA: se calcula con las mesas y las reservas activas del día. Origen: prueba PU-05.
 */
public class TableroOcupacion {

    public static final String LIBRE = "Libre";

    private final LocalDate fecha;
    private final List<Mesa> mesas;
    private final List<Reserva> reservas;

    /**
     * Trazabilidad: Diagrama de actividades HU-03 - "Revisar los registros de reservas y cancelaciones del día".
     * Solo se piden las reservas activas: las canceladas no ocupan la mesa (HU-03 / Escenarios 1.2 y 1.3).
     */
    public TableroOcupacion(LocalDate fecha, MesaDAO mesaDAO, ReservaDAO reservaDAO) {
        this.fecha = fecha;
        this.mesas = new ArrayList<>(mesaDAO.listarTodos());
        this.reservas = new ArrayList<>(reservaDAO.buscarActivasPorFecha(fecha));
    }

    /**
     * Trazabilidad: Diagrama de actividades HU-03 - decisión "¿La mesa tiene una reserva vigente?",
     * "Anotar: Reserva / Libre". HU-03 / Escenario 1.1.
     */
    public String obtenerEstado(int numeroMesa, Franja franja) {
        return buscarReserva(numeroMesa, franja)
                .map(r -> "Reservada · " + r.getCliente().getNombres() + " " + r.getCliente().getApellidos())
                .orElse(LIBRE);
    }

    /**
     * Busca la reserva activa de la mesa en la franja indicada.
     * Extraído de obtenerEstado() con la refactorización Extract Method.
     */
    private Optional<Reserva> buscarReserva(int numeroMesa, Franja franja) {
        for (Reserva reserva : reservas) {
            if (reserva.getMesa().getNumero() == numeroMesa && reserva.getFranja() == franja) {
                return Optional.of(reserva);
            }
        }
        return Optional.empty();
    }

    /**
     * Trazabilidad: HU-03, escenario 2.2. Tarea T3.3.
     * Prueba PU-05.
     *
     * Cuenta las mesas clasificadas como libres utilizando
     * el mismo criterio de disponibilidad que obtenerEstado().
     */

    public int contarMesasLibres(Franja franja) {
        return (int) mesas.stream()
                .filter(mesa -> LIBRE.equals(obtenerEstado(mesa.getNumero(), franja)))
                .count();
    }

    /** HU-03 / Escenario 2.1: si no hay reservas activas hoy, la vista muestra "No hay reservas para hoy". */
    public boolean hayReservas() {
        return !reservas.isEmpty();
    }

    /** HU-03 / Escenario 2.3: una franja sin mesas libres se marca como "Completa". */
    public boolean estaCompleta(Franja franja) {
        return contarMesasLibres(franja) == 0;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public List<Mesa> getMesas() {
        return Collections.unmodifiableList(mesas);
    }
}
