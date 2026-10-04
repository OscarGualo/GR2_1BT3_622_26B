package ec.edu.epn.polidinner.persistencia;

import java.util.List;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Soporte técnico de acceso a datos (no pertenece al modelo de dominio).
 * Operaciones CRUD comunes; cada operación abre su propio EntityManager y transacción.
 *
 * @param <T>  entidad JPA
 * @param <ID> tipo de la clave primaria
 */
public class GenericDAO<T, ID> {

    private final Class<T> clase;

    public GenericDAO(Class<T> clase) {
        this.clase = clase;
    }

    public void guardar(T entidad) {
        enTransaccion(em -> {
            em.persist(entidad);
            return null;
        });
    }

    /** Devuelve la entidad gestionada tras el merge (incluye ids y versión actualizados). */
    public T actualizar(T entidad) {
        return enTransaccion(em -> em.merge(entidad));
    }

    public T buscarPorId(ID id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(clase, id);
        } finally {
            em.close();
        }
    }

    public List<T> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT e FROM " + clase.getSimpleName() + " e", clase).getResultList();
        } finally {
            em.close();
        }
    }

    public void eliminar(ID id) {
        enTransaccion(em -> {
            T entidad = em.find(clase, id);
            if (entidad != null) {
                em.remove(entidad);
            }
            return null;
        });
    }

    protected <R> R enTransaccion(Function<EntityManager, R> operacion) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R resultado = operacion.apply(em);
            tx.commit();
            return resultado;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
