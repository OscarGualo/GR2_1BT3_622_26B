package ec.edu.epn.polidinner.servicio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ec.edu.epn.polidinner.modelo.DetallePedido;
import ec.edu.epn.polidinner.modelo.FranjaRetiro;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.persistencia.FranjaRetiroDAO;
import ec.edu.epn.polidinner.persistencia.PedidoDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

/**
 * Soporte técnico NO modelado - Examen 1B: reglas del cupo efectivo y consultas por franja.
 * Trazabilidad: HU13 (T13.1 pedidos agrupados por franja), HU15 (T15.2 reglas del cupo efectivo) y
 * HU16 (T16.1 disponibilidad por franja).
 */
public class ServicioFranjas {

    public static final String CUPO_NO_VALIDO = "El cupo debe ser un número entero mayor que cero";

    /** Estado visible de una franja en el selector (HU16 CA1) y en la pantalla de cupos (HU15). */
    public enum Estado { DISPONIBLE, LLENA, CERRADA }

    private final FranjaRetiroDAO franjaDAO;
    private final PedidoDAO pedidoDAO;

    public ServicioFranjas() {
        this(new FranjaRetiroDAO(), new PedidoDAO());
    }

    ServicioFranjas(FranjaRetiroDAO franjaDAO, PedidoDAO pedidoDAO) {
        this.franjaDAO = franjaDAO;
        this.pedidoDAO = pedidoDAO;
    }

    /** HU16 T16.1: una franja con su cupo efectivo, sus lugares libres y su estado. */
    public static final class DisponibilidadFranja {
        private final FranjaRetiro franja;
        private final int cupoEfectivo;

        DisponibilidadFranja(FranjaRetiro franja, int cupoGeneral) {
            this.franja = franja;
            this.cupoEfectivo = franja.cupoEfectivo(cupoGeneral);
        }

        public LocalDate getFecha() {
            return franja.getFecha();
        }

        public LocalTime getHoraInicio() {
            return franja.getHoraInicio();
        }

        /** Hora de inicio como texto HH:mm, el valor que viaja en el formulario. */
        public String getInicio() {
            return franja.getHoraInicio().toString();
        }

        public String getEtiqueta() {
            return ReglasFranja.etiqueta(franja.getHoraInicio());
        }

        public int getAsignados() {
            return franja.getAsignados();
        }

        public int getCupoEfectivo() {
            return cupoEfectivo;
        }

        public Integer getCupoEspecifico() {
            return franja.getCupoEspecifico();
        }

        public int getLugares() {
            return Math.max(0, cupoEfectivo - franja.getAsignados());
        }

        public boolean isCerrada() {
            return franja.isCerrada();
        }

        public Estado getEstado() {
            if (franja.isCerrada()) {
                return Estado.CERRADA;
            }
            return franja.getAsignados() >= cupoEfectivo ? Estado.LLENA : Estado.DISPONIBLE;
        }

        public boolean isElegible() {
            return getEstado() == Estado.DISPONIBLE;
        }

        /** HU16 Esc. 1.1: «Quedan 6 lugares» / «Queda 1 lugar». */
        public String getTextoLugares() {
            switch (getEstado()) {
                case CERRADA:
                    return "Cerrada";
                case LLENA:
                    return "Llena";
                default:
                    return getLugares() == 1 ? "Queda 1 lugar" : "Quedan " + getLugares() + " lugares";
            }
        }
    }

    /** HU15: todas las franjas del día (11:00 a 15:00) con su cupo, para la pantalla de configuración. */
    public List<DisponibilidadFranja> franjasDelDia(LocalDate fecha) {
        franjaDAO.asegurarFranjasDelDia(fecha);
        int cupoGeneral = franjaDAO.cupoGeneral();
        List<DisponibilidadFranja> lista = new ArrayList<>();
        for (FranjaRetiro franja : franjaDAO.listarDelDia(fecha)) {
            lista.add(new DisponibilidadFranja(franja, cupoGeneral));
        }
        return lista;
    }

    /** HU11 Esc. 1.2 y HU16 CA1: franjas de hoy que todavía no empezaron, con su disponibilidad. */
    public List<DisponibilidadFranja> franjasOfrecidas(LocalDateTime ahora) {
        List<DisponibilidadFranja> ofrecidas = new ArrayList<>();
        for (DisponibilidadFranja disponibilidad : franjasDelDia(ahora.toLocalDate())) {
            if (disponibilidad.getHoraInicio().isAfter(ahora.toLocalTime())) {
                ofrecidas.add(disponibilidad);
            }
        }
        return ofrecidas;
    }

    public int cupoGeneral() {
        return franjaDAO.cupoGeneral();
    }

    /** HU15 Esc. 1.2: el cupo llega como texto del formulario; solo se acepta un entero mayor que cero. */
    public static int leerCupo(String valor) {
        try {
            int cupo = Integer.parseInt(valor == null ? "" : valor.trim());
            if (cupo > 0) {
                return cupo;
            }
        } catch (NumberFormatException e) {
            // se informa abajo con el mismo mensaje
        }
        throw new ReglaPedidoException(CUPO_NO_VALIDO);
    }

    /** HU15 CA1 (Esc. 1.1, 1.2): define el cupo que rige en las franjas sin cupo específico. */
    public void configurarCupoGeneral(int cupo) {
        exigirCupoValido(cupo);
        try (Transaccion tx = Transaccion.iniciar()) {
            if (!franjaDAO.fijarCupoGeneral(cupo, tx)) {
                FranjaRetiro franja = franjaDAO.franjaQueSuperaElCupoGeneral(cupo, tx);
                throw rechazoPorPedidosAsignados(franja);
            }
            tx.confirmar();
        }
    }

    /** HU15 CA2-CA3 (Esc. 2.1, 3.1): cupo propio de una franja, nunca menor que sus pedidos asignados. */
    public void fijarCupoFranja(LocalDate fecha, LocalTime inicio, int cupo) {
        exigirCupoValido(cupo);
        franjaDAO.asegurarFranjasDelDia(fecha);
        try (Transaccion tx = Transaccion.iniciar()) {
            FranjaRetiro franja = franjaDAO.buscar(fecha, inicio, tx);
            if (!franjaDAO.fijarCupoEspecifico(franja.getId(), cupo, tx)) {
                throw rechazoPorPedidosAsignados(franjaDAO.refrescar(franja, tx));
            }
            tx.confirmar();
        }
    }

    /** HU15 Esc. 2.2 y 3.2: la franja deja de admitir pedidos nuevos; los ya asignados siguen Pagados. */
    public void cerrarFranja(LocalDate fecha, LocalTime inicio) {
        cambiarCierre(fecha, inicio, true);
    }

    public void abrirFranja(LocalDate fecha, LocalTime inicio) {
        cambiarCierre(fecha, inicio, false);
    }

    private void cambiarCierre(LocalDate fecha, LocalTime inicio, boolean cerrada) {
        franjaDAO.asegurarFranjasDelDia(fecha);
        try (Transaccion tx = Transaccion.iniciar()) {
            FranjaRetiro franja = franjaDAO.buscar(fecha, inicio, tx);
            franjaDAO.cambiarCierre(franja.getId(), cerrada, tx);
            tx.confirmar();
        }
    }

    private static void exigirCupoValido(int cupo) {
        if (cupo < 1) {
            throw new ReglaPedidoException(CUPO_NO_VALIDO);
        }
    }

    private static ReglaPedidoException rechazoPorPedidosAsignados(FranjaRetiro franja) {
        return new ReglaPedidoException("La franja " + ReglasFranja.etiqueta(franja.getHoraInicio())
                + " ya tiene " + franja.getAsignados() + " pedidos asignados; el cupo no puede ser menor que "
                + franja.getAsignados());
    }

    /** HU13: una franja con sus pedidos Pagados y el total de cada producto, para preparar la comida. */
    public static final class ResumenFranja {
        private final LocalTime horaInicio;
        private final List<Pedido> pedidos = new ArrayList<>();
        private final Map<String, Integer> totales = new TreeMap<>();

        ResumenFranja(LocalTime horaInicio) {
            this.horaInicio = horaInicio;
        }

        void agregar(Pedido pedido) {
            pedidos.add(pedido);
            for (DetallePedido detalle : pedido.getDetalles()) {
                totales.merge(detalle.getItem().getNombre(), detalle.getCantidad(), Integer::sum);
            }
        }

        public String getEtiqueta() {
            return ReglasFranja.etiqueta(horaInicio);
        }

        public LocalTime getHoraInicio() {
            return horaInicio;
        }

        public List<Pedido> getPedidos() {
            return pedidos;
        }

        public int getCantidadPedidos() {
            return pedidos.size();
        }

        /** Producto → unidades, en orden alfabético («Arroz con pollo × 2»). */
        public Map<String, Integer> getTotales() {
            return totales;
        }
    }

    /** HU13 (Esc. 1.1, 2.1, 2.2): pedidos programados Pagados del día agrupados por franja, en orden de hora. */
    public List<ResumenFranja> pedidosProgramadosDelDia(LocalDate fecha) {
        Map<LocalTime, ResumenFranja> porFranja = new LinkedHashMap<>();
        for (Pedido pedido : pedidoDAO.buscarProgramadosDelDia(fecha)) {
            LocalTime inicio = pedido.getFranja().getHoraInicio();
            porFranja.computeIfAbsent(inicio, ResumenFranja::new).agregar(pedido);
        }
        return new ArrayList<>(porFranja.values());
    }
}
