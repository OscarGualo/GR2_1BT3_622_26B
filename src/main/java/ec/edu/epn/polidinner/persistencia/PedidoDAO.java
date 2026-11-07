package ec.edu.epn.polidinner.persistencia;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import ec.edu.epn.polidinner.modelo.DetallePedido;
import ec.edu.epn.polidinner.modelo.DiaComedor;
import ec.edu.epn.polidinner.modelo.ItemMenu;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.modelo.Policuenta;

/**
 * Trazabilidad: Diagrama de secuencia CU03 - participante PedidoDAO
 * (mensajes 34 adjuntar(pedido) y 43 guardar(pedido), este último heredado de GenericDAO).
 */
public class PedidoDAO extends GenericDAO<Pedido, Integer> {

    public PedidoDAO() {
        super(Pedido.class);
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU03 - mensaje 34 adjuntar(pedido).
     * Soporte técnico: el pedido se armó en la sesión con copias de los ítems y de la PoliCuenta.
     * Antes de confirmarPago() se reemplazan por su versión actual, gestionada por la transacción,
     * para validar stock y saldo reales y que los cambios se guarden con bloqueo optimista (@Version).
     */
    public void adjuntar(Pedido pedido, Transaccion tx) {
        EntityManager em = tx.em();
        for (DetallePedido detalle : pedido.getDetalles()) {
            ItemMenu actual = em.find(ItemMenu.class, detalle.getItem().getId());
            if (actual == null) {
                throw new IllegalStateException("\"" + detalle.getItem().getNombre() + "\" ya no existe en el menú");
            }
            detalle.reemplazarItem(actual);
        }
        pedido.reemplazarPolicuenta(em.find(Policuenta.class, pedido.getPolicuenta().getNumero()));
    }

    /**
     * Trazabilidad: Secuencia "Realizar Pedido y Pagar" - guarda el Pedido y el Comprobante (CodigoEntrega)
     * que creó confirmarPago(). Primero el pedido, para que la base de datos le asigne el id; luego el comprobante,
     * cuyo @PrePersist arma el código PD-0000 con ese id. Un solo flush al final.
     */
    public void guardarConComprobante(Pedido pedido, Transaccion tx) {
        EntityManager em = tx.em();
        em.persist(pedido);
        em.persist(pedido.getComprobante());
        em.flush();
    }

    /**
     * Examen 1B, HU13 (T13.1): pedidos programados Pagados de una fecha, en orden de franja y de pedido.
     * Excluye los cancelados (Esc. 2.2) y los inmediatos del Release 1 (sin franja).
     */
    public List<Pedido> buscarProgramadosDelDia(LocalDate fecha) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Pedido p WHERE p.franja.fecha = :fecha AND p.estado = 'Pagado'"
                            + " ORDER BY p.franja.horaInicio, p.id", Pedido.class)
                    .setParameter("fecha", fecha)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Examen 1B, HU14 (T14.1): pedidos programados pendientes de retiro del estudiante, por hora de retiro.
     * Solo Pagado: el Cancelado y el Entregado ya no están pendientes (CA2) y los de otros estudiantes
     * no aparecen (CA3).
     */
    public List<Pedido> buscarProgramadosPendientes(String cedula) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Pedido p WHERE p.cliente.cedula = :cedula"
                            + " AND p.franja IS NOT NULL AND p.estado = 'Pagado'"
                            + " ORDER BY p.franja.fecha, p.franja.horaInicio, p.id", Pedido.class)
                    .setParameter("cedula", cedula)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU06 - mensajes 3-4: buscarPorCliente(cedula) / lista de pedidos.
     * Devuelve los pedidos del cliente, del más reciente al más antiguo, para la vista "Mis pedidos".
     */
    public List<Pedido> buscarPorCliente(String cedula) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Pedido p WHERE p.cliente.cedula = :cedula ORDER BY p.fecha DESC",
                            Pedido.class)
                    .setParameter("cedula", cedula)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Trazabilidad: Diagrama de robustez CU05 - consulta de los pedidos del día para el reporte de ventas
     * (ReporteVentas(fecha, pedidos) del diagrama de clases Inc. 3).
     * Pedido.fecha guarda fecha y hora, por eso se busca en el rango [00:00 del día, 00:00 del día siguiente).
     * Orden ascendente por id, que es el orden en que se pagaron.
     */
    public List<Pedido> buscarPorFecha(Date dia) {
        ZoneId zona = ZoneId.systemDefault();
        return buscarPorFecha(new DiaComedor(dia.toInstant().atZone(zona).toLocalDate(), zona));
    }

    /** Corrección Paquete 1a: pedidos del día del comedor, [00:00, 00:00 del día siguiente) en su zona. */
    public List<Pedido> buscarPorFecha(DiaComedor dia) {
        return buscarEntre(dia.inicio(), dia.fin());
    }

    private List<Pedido> buscarEntre(Date inicio, Date fin) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Pedido p WHERE p.fecha >= :inicio AND p.fecha < :fin ORDER BY p.id",
                            Pedido.class)
                    .setParameter("inicio", inicio, TemporalType.TIMESTAMP)
                    .setParameter("fin", fin, TemporalType.TIMESTAMP)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
