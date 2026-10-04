package ec.edu.epn.polidinner.persistencia;

import java.util.List;

import javax.persistence.EntityManager;

import ec.edu.epn.polidinner.modelo.Comprobante;

/**
 * Trazabilidad: Diagrama de secuencia CU03 - participante ComprobanteDAO (mensaje 48 guardar(comprobante)).
 * Diagrama de secuencia CU04 - participante ComprobanteDAO (mensajes 3-4 buscarPorCodigo(codigo) / comprobante
 * y 11 actualizar(comprobante)).
 */
public class ComprobanteDAO extends GenericDAO<Comprobante, String> {

    public ComprobanteDAO() {
        super(Comprobante.class);
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU04 - mensajes 3-4 buscarPorCodigo(codigo) / comprobante.
     * Busca dentro de la transacción del caso de uso; devuelve null si el código no existe.
     */
    public Comprobante buscarPorCodigo(String codigo, Transaccion tx) {
        return buscar(tx.em(), codigo);
    }

    /** Búsqueda fuera de transacción (lo usa la pantalla del comprobante del cliente). */
    public Comprobante buscarPorCodigo(String codigo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return buscar(em, codigo);
        } finally {
            em.close();
        }
    }

    private static Comprobante buscar(EntityManager em, String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }
        List<Comprobante> encontrados = em.createQuery(
                        "SELECT c FROM Comprobante c WHERE UPPER(c.codigo) = :codigo", Comprobante.class)
                .setParameter("codigo", codigo.trim().toUpperCase())
                .getResultList();
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }
}
