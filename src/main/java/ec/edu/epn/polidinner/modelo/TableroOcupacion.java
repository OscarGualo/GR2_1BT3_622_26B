package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        for (Reserva r : reservas) {
            if (r.getMesa().getNumero() == numeroMesa && r.getFranja() == franja) {
                return "Reservada · " + r.getCliente().getNombres() + " " + r.getCliente().getApellidos();
            }
        }
        return LIBRE;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public List<Mesa> getMesas() {
        return Collections.unmodifiableList(mesas);
    }
}
