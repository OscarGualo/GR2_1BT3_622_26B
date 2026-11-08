package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * Soporte técnico NO modelado - corrección de productos persistentes (Paquete 1a): el día civil del comedor.
 * Se resuelve una sola vez en el punto de entrada (el servlet, con RelojAplicacion) y se pasa al modelo,
 * a los servicios y a las consultas, para que el menú, el cierre, el comprobante y la caducidad usen el
 * mismo día aunque el servidor esté en otra zona (por ejemplo, en UTC después de las 19:00 de Ecuador).
 * <ul>
 *   <li>Columnas DATE (Menu.fecha): guardan una fecha civil, se convierten con {@link #comoFechaCivil()}
 *       sin aplicar zona; convertirlas con la zona del comedor correría el día si la JVM está en UTC.</li>
 *   <li>Instantes TIMESTAMP (Comprobante.fechaGeneracion, Pedido.fecha): se comparan con los límites del
 *       día en la zona del comedor, {@link #inicio()} y {@link #fin()}.</li>
 * </ul>
 */
public final class DiaComedor implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate fecha;
    private final ZoneId zona;

    public DiaComedor(LocalDate fecha, ZoneId zona) {
        if (fecha == null || zona == null) {
            throw new IllegalArgumentException("El día del comedor necesita fecha y zona");
        }
        this.fecha = fecha;
        this.zona = zona;
    }

    /** Día del comedor que corresponde a un instante. */
    public static DiaComedor de(Instant instante, ZoneId zona) {
        return new DiaComedor(LocalDate.ofInstant(instante, zona), zona);
    }

    /**
     * Día según el reloj y la zona del servidor: lo usan las firmas anteriores a esta corrección
     * (buscarMenuDelDia(), esDeHoy(), crearMenuDiario()...), que conservan su comportamiento.
     */
    public static DiaComedor delSistema() {
        return new DiaComedor(LocalDate.now(), ZoneId.systemDefault());
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public ZoneId getZona() {
        return zona;
    }

    /** 00:00 de este día en la zona del comedor. */
    public Date inicio() {
        return Date.from(fecha.atStartOfDay(zona).toInstant());
    }

    /** 00:00 del día siguiente en la zona del comedor (límite exclusivo). */
    public Date fin() {
        return Date.from(fecha.plusDays(1).atStartOfDay(zona).toInstant());
    }

    /** ¿El instante (un TIMESTAMP, como la generación de un comprobante) cae dentro de este día del comedor? */
    public boolean contiene(Date instante) {
        return instante != null && !instante.before(inicio()) && instante.before(fin());
    }

    /** Fecha civil para las columnas DATE (Menu.fecha): no depende de la zona de la JVM. */
    public Date comoFechaCivil() {
        return java.sql.Date.valueOf(fecha);
    }

    /**
     * Fecha civil de un valor de columna DATE (Menu.fecha, Producto.fechaCaducidad). JDBC lo entrega como
     * java.sql.Date; un java.util.Date creado en memoria se lee en la zona de la JVM, la misma que usa JDBC.
     */
    public static LocalDate fechaCivilDe(Date fecha) {
        if (fecha == null) {
            return null;
        }
        if (fecha instanceof java.sql.Date) {
            return ((java.sql.Date) fecha).toLocalDate();
        }
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof DiaComedor)) {
            return false;
        }
        DiaComedor dia = (DiaComedor) otro;
        return fecha.equals(dia.fecha) && zona.equals(dia.zona);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fecha, zona);
    }

    @Override
    public String toString() {
        return fecha + " (" + zona + ")";
    }
}
