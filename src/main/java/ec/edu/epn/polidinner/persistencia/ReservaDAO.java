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
}