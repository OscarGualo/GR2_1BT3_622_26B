package ec.edu.epn.polidinner.persistencia;

import java.util.List;

import javax.persistence.EntityManager;

import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;

/** Soporte técnico NO modelado - HU20: pérdidas por caducidad y su historial. */
public class PerdidaCaducidadDAO extends GenericDAO<PerdidaCaducidad, Integer> {

    private static final String CONTAR_TOKEN =
            "SELECT COUNT(p) FROM PerdidaCaducidad p WHERE p.tokenOperacion = :token";

    public PerdidaCaducidadDAO() {
        super(PerdidaCaducidad.class);
    }

    /** ¿Ya se registró una pérdida con este token de confirmación? (dentro de la transacción) */
    public boolean existeToken(String tokenOperacion, Transaccion tx) {
        return contarToken(tx.em(), tokenOperacion) > 0;
    }

    /** ¿Ya se registró una pérdida con este token de confirmación? (fuera de una transacción) */
    public boolean existeToken(String tokenOperacion) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return contarToken(em, tokenOperacion) > 0;
        } finally {
            em.close();
        }
    }

    /** Historial de pérdidas, de la más reciente a la más antigua (regla 11). */
    public List<PerdidaCaducidad> listarRecientes() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM PerdidaCaducidad p ORDER BY p.fechaRegistro DESC, p.id DESC",
                    PerdidaCaducidad.class).getResultList();
        } finally {
            em.close();
        }
    }

    private static long contarToken(EntityManager em, String tokenOperacion) {
        return em.createQuery(CONTAR_TOKEN, Long.class).setParameter("token", tokenOperacion).getSingleResult();
    }
}