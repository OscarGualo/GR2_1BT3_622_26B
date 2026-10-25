package ec.edu.epn.polidinner.persistencia;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;

import ec.edu.epn.polidinner.modelo.Reserva;

/**
 * Trazabilidad: Diagramas de robustez HU-01, HU-02 y HU-03 - acceso a la entidad Reserva (T1.1 y T3.1).
 */
public class ReservaDAO extends GenericDAO<Reserva, Integer> {

    public ReservaDAO() {
        super(Reserva.class);
    }

    /**
     * Todas las reservas de la fecha, en cualquier estado. Las usa AgendaReservas para validar,
     * porque una reserva "Cancelada fuera de plazo" también cuenta (restringe al cliente ese día).
     */
    public List<Reserva> buscarPorFecha(LocalDate fecha) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT r FROM Reserva r WHERE r.fecha = :fecha", Reserva.class)
                    .setParameter("fecha", fecha)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * T3.1: reservas activas de la fecha, con su mesa y su cliente (relaciones EAGER).
     * No filtra por hora: el tablero muestra también las franjas que ya comenzaron.
     */
    public List<Reserva> buscarActivasPorFecha(LocalDate fecha) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Reserva r WHERE r.fecha = :fecha AND r.estado = :estado",
                            Reserva.class)
                    .setParameter("fecha", fecha)
                    .setParameter("estado", Reserva.ACTIVA)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    /**
     * T2.1 - Diagrama de actividades HU-02 "Buscar reserva del cliente en el registro":
     * la reserva activa del cliente en la fecha, o null si no tiene ("No tiene reservas activas para hoy").
     */
    public Reserva buscarActivaDelCliente(String cedula, LocalDate fecha) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Reserva> reservas = em.createQuery(
                            "SELECT r FROM Reserva r WHERE r.cliente.cedula = :cedula"
                                    + " AND r.fecha = :fecha AND r.estado = :estado", Reserva.class)
                    .setParameter("cedula", cedula)
                    .setParameter("fecha", fecha)
                    .setParameter("estado", Reserva.ACTIVA)
                    .getResultList();
            return reservas.isEmpty() ? null : reservas.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * T2.2 - Guarda la cancelación solo si la reserva sigue "Activa" en la base, en una sola sentencia.
     * Si dos peticiones cancelan la misma reserva a la vez, solo una la actualiza y la otra recibe false
     * (HU-02 / Escenario 2.3: "La reserva ya fue cancelada").
     */
    public boolean guardarCancelacion(Reserva reserva) {
        return enTransaccion(em -> em.createQuery(
                        "UPDATE Reserva r SET r.estado = :nuevoEstado WHERE r.id = :id AND r.estado = :activa")
                .setParameter("nuevoEstado", reserva.getEstado())
                .setParameter("id", reserva.getId())
                .setParameter("activa", Reserva.ACTIVA)
                .executeUpdate() == 1);
    }
}