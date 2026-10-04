package ec.edu.epn.polidinner.persistencia;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Soporte técnico de acceso a datos (no pertenece al modelo de dominio).
 * Una transacción que abarca varias operaciones de distintos DAO, para que un caso de uso se
 * guarde completo o no se guarde nada (CU 03: pago con PoliCuenta; CU 04: entrega del pedido).
 * Uso: try (Transaccion tx = Transaccion.iniciar()) { ...; tx.confirmar(); }
 * Si no se llama a confirmar(), close() deshace todo (rollback).
 */
public final class Transaccion implements AutoCloseable {

    private final EntityManager em;
    private final EntityTransaction tx;

    private Transaccion() {
        this.em = JPAUtil.getEntityManager();
        this.tx = em.getTransaction();
        tx.begin();
    }

    public static Transaccion iniciar() {
        return new Transaccion();
    }

    EntityManager em() {
        return em;
    }

    /** Hace commit de todas las operaciones de la transacción. */
    public void confirmar() {
        tx.commit();
    }

    /** Deshace todo si la transacción sigue activa (no se confirmó o falló el commit) y libera recursos. */
    @Override
    public void close() {
        try {
            if (tx.isActive()) {
                tx.rollback();
            }
        } finally {
            em.close();
        }
    }
}
