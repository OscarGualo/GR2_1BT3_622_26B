package ec.edu.epn.polidinner.persistencia;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;

import ec.edu.epn.polidinner.modelo.ConfiguracionCupos;
import ec.edu.epn.polidinner.modelo.FranjaRetiro;
import ec.edu.epn.polidinner.servicio.ReglasFranja;

/**
 * Trazabilidad: Examen 1B, "Control de cupos (Iteración 2)" - T15.1, T16.2 y T17.2.
 * Acceso a la franja persistida. Reservar, liberar y reducir el cupo son actualizaciones condicionadas
 * sobre la fila de la franja: la condición se evalúa en la base de datos, de modo que dos solicitudes
 * simultáneas por el último lugar nunca lo ocupan las dos (el que llega segundo obtiene 0 filas).
 */
public class FranjaRetiroDAO extends GenericDAO<FranjaRetiro, Integer> {

    /** Evita que dos hilos de esta JVM intenten crear las franjas del día a la vez. */
    private static final Object CREACION = new Object();

    public FranjaRetiroDAO() {
        super(FranjaRetiro.class);
    }

    /**
     * T15.1: garantiza que existan la configuración de cupos y las 16 franjas del día (11:00 a 15:00).
     * Corre en su propia transacción: si otra solicitud las creó antes, la restricción única
     * (fecha, hora_inicio) rechaza el duplicado y simplemente se vuelve a consultar.
     */
    public void asegurarFranjasDelDia(LocalDate fecha) {
        synchronized (CREACION) {
            try {
                enTransaccion(em -> {
                    if (em.find(ConfiguracionCupos.class, ConfiguracionCupos.ID_UNICO) == null) {
                        em.persist(new ConfiguracionCupos());
                    }
                    for (LocalTime inicio : ReglasFranja.iniciosDelDia()) {
                        if (buscarEn(em, fecha, inicio) == null) {
                            em.persist(new FranjaRetiro(fecha, inicio));
                        }
                    }
                    return null;
                });
            } catch (PersistenceException e) {
                // Otra instancia de la aplicación las creó al mismo tiempo: ya existen
            }
        }
    }

    /** Franjas del día en orden de hora, para el selector (HU16) y la pantalla de cupos (HU15). */
    public List<FranjaRetiro> listarDelDia(LocalDate fecha) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT f FROM FranjaRetiro f WHERE f.fecha = :fecha ORDER BY f.horaInicio",
                            FranjaRetiro.class)
                    .setParameter("fecha", fecha)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public int cupoGeneral() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            ConfiguracionCupos configuracion = em.find(ConfiguracionCupos.class, ConfiguracionCupos.ID_UNICO);
            return configuracion == null ? ConfiguracionCupos.CUPO_GENERAL_INICIAL : configuracion.getCupoGeneral();
        } finally {
            em.close();
        }
    }

    public FranjaRetiro buscar(LocalDate fecha, LocalTime inicio, Transaccion tx) {
        return buscarEn(tx.em(), fecha, inicio);
    }

    /** Mantiene el cupo estable hasta confirmar o revertir la reserva o reprogramación. */
    public int cupoGeneral(Transaccion tx) {
        return bloquearConfiguracion(tx).getCupoGeneral();
    }

    /** HU15/HU16: lectores que reservan y cambios de cupo toman el mismo bloqueo en la base. */
    private ConfiguracionCupos bloquearConfiguracion(Transaccion tx) {
        EntityManager em = tx.em();
        ConfiguracionCupos configuracion = em.find(ConfiguracionCupos.class,
                ConfiguracionCupos.ID_UNICO, LockModeType.PESSIMISTIC_WRITE);
        if (configuracion == null) {
            configuracion = new ConfiguracionCupos();
            em.persist(configuracion);
            em.flush();
        }
        return configuracion;
    }

    /**
     * T16.2: ocupa un lugar solo si la franja está abierta y todavía tiene cupo. Devuelve false si otra
     * transacción ocupó el último lugar (o cerró la franja) antes que esta.
     */
    public boolean reservar(int idFranja, int cupoGeneral, Transaccion tx) {
        int filas = tx.em().createQuery("UPDATE FranjaRetiro f SET f.asignados = f.asignados + 1"
                        + " WHERE f.id = :id AND f.cerrada = false"
                        + " AND f.asignados < COALESCE(f.cupoEspecifico, :general)")
                .setParameter("id", idFranja)
                .setParameter("general", cupoGeneral)
                .executeUpdate();
        return filas == 1;
    }

    /** T16.5 / T17.2: devuelve un lugar a la franja; nunca deja el contador por debajo de cero. */
    public boolean liberar(int idFranja, Transaccion tx) {
        int filas = tx.em().createQuery("UPDATE FranjaRetiro f SET f.asignados = f.asignados - 1"
                        + " WHERE f.id = :id AND f.asignados > 0")
                .setParameter("id", idFranja)
                .executeUpdate();
        return filas == 1;
    }

    /** HU15 CA2-CA3: fija el cupo específico solo si no es menor que los pedidos ya asignados. */
    public boolean fijarCupoEspecifico(int idFranja, int cupo, Transaccion tx) {
        int filas = tx.em().createQuery("UPDATE FranjaRetiro f SET f.cupoEspecifico = :cupo"
                        + " WHERE f.id = :id AND f.asignados <= :cupo")
                .setParameter("id", idFranja)
                .setParameter("cupo", cupo)
                .executeUpdate();
        return filas == 1;
    }

    /** HU15 CA2: cierra o reabre la franja; los pedidos ya asignados no se tocan (CA3). */
    public void cambiarCierre(int idFranja, boolean cerrada, Transaccion tx) {
        tx.em().createQuery("UPDATE FranjaRetiro f SET f.cerrada = :cerrada WHERE f.id = :id")
                .setParameter("cerrada", cerrada)
                .setParameter("id", idFranja)
                .executeUpdate();
    }

    /**
     * HU15 CA1: cambia el cupo general solo si ninguna franja que lo usa (sin cupo específico) tiene más
     * pedidos asignados que el nuevo cupo. Devuelve false si lo impide alguna franja.
     */
    public boolean fijarCupoGeneral(int cupo, Transaccion tx) {
        EntityManager em = tx.em();
        bloquearConfiguracion(tx);
        int filas = em.createQuery("UPDATE ConfiguracionCupos c SET c.cupoGeneral = :cupo WHERE c.id = :id"
                        + " AND NOT EXISTS (SELECT f.id FROM FranjaRetiro f"
                        + " WHERE f.cupoEspecifico IS NULL AND f.asignados > :cupo)")
                .setParameter("cupo", cupo)
                .setParameter("id", ConfiguracionCupos.ID_UNICO)
                .executeUpdate();
        return filas == 1;
    }

    /** Franja sin cupo específico con más pedidos asignados que el cupo indicado (la que impide bajarlo). */
    public FranjaRetiro franjaQueSuperaElCupoGeneral(int cupo, Transaccion tx) {
        List<FranjaRetiro> franjas = tx.em().createQuery("SELECT f FROM FranjaRetiro f"
                        + " WHERE f.cupoEspecifico IS NULL AND f.asignados > :cupo"
                        + " ORDER BY f.asignados DESC, f.fecha, f.horaInicio", FranjaRetiro.class)
                .setParameter("cupo", cupo)
                .setMaxResults(1)
                .getResultList();
        return franjas.isEmpty() ? null : franjas.get(0);
    }

    /** Lee la franja de nuevo desde la base (las actualizaciones condicionadas no refrescan la entidad). */
    public FranjaRetiro refrescar(FranjaRetiro franja, Transaccion tx) {
        FranjaRetiro actual = tx.em().find(FranjaRetiro.class, franja.getId());
        tx.em().refresh(actual);
        return actual;
    }

    private static FranjaRetiro buscarEn(EntityManager em, LocalDate fecha, LocalTime inicio) {
        List<FranjaRetiro> encontradas = em.createQuery(
                        "SELECT f FROM FranjaRetiro f WHERE f.fecha = :fecha AND f.horaInicio = :inicio",
                        FranjaRetiro.class)
                .setParameter("fecha", fecha)
                .setParameter("inicio", inicio)
                .getResultList();
        return encontradas.isEmpty() ? null : encontradas.get(0);
    }
}
