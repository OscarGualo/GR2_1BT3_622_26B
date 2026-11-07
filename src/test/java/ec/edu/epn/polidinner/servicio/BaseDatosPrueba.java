package ec.edu.epn.polidinner.servicio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;

import ec.edu.epn.polidinner.modelo.ClienteUniversitario;
import ec.edu.epn.polidinner.modelo.FranjaRetiro;
import ec.edu.epn.polidinner.modelo.ItemMenu;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.modelo.Plato;
import ec.edu.epn.polidinner.modelo.Policuenta;
import ec.edu.epn.polidinner.persistencia.FranjaRetiroDAO;
import ec.edu.epn.polidinner.persistencia.JPAUtil;

/**
 * Soporte de pruebas: cada prueba usa una base H2 en memoria propia (esquema creado por Hibernate), de modo
 * que los escenarios del Examen 1B no se contaminan entre sí. Datos de los escenarios: Andrea Gómez
 * (PoliCuenta 2024-0153, $10.00), Luis Torres (2024-0201, $8.00), «Arroz con pollo» ($3.50),
 * «Seco de pollo» ($4.00) y «Jugo de naranja» ($0.75).
 */
abstract class BaseDatosPrueba {

    static final LocalDate LUNES = LocalDate.of(2026, 11, 16);

    ClienteUniversitario andrea;
    ClienteUniversitario luis;
    Plato arroz;
    Plato seco;
    Plato jugo;

    final FranjaRetiroDAO franjas = new FranjaRetiroDAO();
    final ServicioPedidos servicio = new ServicioPedidos();

    /** Reinicia JPA con una H2 nueva y siembra los clientes y los productos de los escenarios. */
    void prepararBase() {
        JPAUtil.cerrar();
        System.setProperty("polidinner.db.url",
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        andrea = crearCliente("1750000001", "Andrea", "Gómez", "2024-0153", 10.00);
        luis = crearCliente("1750000002", "Luis", "Torres", "2024-0201", 8.00);
        arroz = crearPlato("Arroz con pollo", 3.50, 20);
        seco = crearPlato("Seco de pollo", 4.00, 20);
        jugo = crearPlato("Jugo de naranja", 0.75, 30);
    }

    void cerrarBase() {
        JPAUtil.cerrar();
        System.clearProperty("polidinner.db.url");
    }

    static LocalDateTime a(int hora, int minuto) {
        return LocalDateTime.of(LUNES, LocalTime.of(hora, minuto));
    }

    static LocalTime franja(int hora, int minuto) {
        return LocalTime.of(hora, minuto);
    }

    ClienteUniversitario crearCliente(String cedula, String nombres, String apellidos, String numero, double saldo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Policuenta cuenta = new Policuenta(numero, saldo);
            em.persist(cuenta);
            ClienteUniversitario cliente = new ClienteUniversitario(cedula, nombres, apellidos, "clave", cuenta);
            em.persist(cliente);
            em.getTransaction().commit();
            return cliente;
        } finally {
            em.close();
        }
    }

    Plato crearPlato(String nombre, double precio, int stock) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Plato plato = new Plato(nombre, precio, "Disponible", nombre, stock);
            em.persist(plato);
            em.getTransaction().commit();
            return plato;
        } finally {
            em.close();
        }
    }

    /** Pedido en estado Creado con los ítems indicados (cantidad 1 cada uno salvo que se pase otra). */
    Pedido pedidoDe(ClienteUniversitario cliente, Object... itemsYCantidades) {
        Pedido pedido = new Pedido(new Date(), "Creado", cliente);
        for (int i = 0; i < itemsYCantidades.length; i += 2) {
            pedido.agregarItem((ItemMenu) itemsYCantidades[i], (Integer) itemsYCantidades[i + 1]);
        }
        return pedido;
    }

    /** Programa y paga un pedido; devuelve el pedido guardado con su código PD-xxxx. */
    Pedido programar(ClienteUniversitario cliente, LocalDateTime ahora, LocalTime inicio, Object... items) {
        Pedido pedido = pedidoDe(cliente, items);
        servicio.programarYPagar(pedido, ahora.toLocalDate(), inicio, ahora);
        return pedido;
    }

    double saldo(String numeroCuenta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Policuenta.class, numeroCuenta).consultarSaldo();
        } finally {
            em.close();
        }
    }

    int stock(ItemMenu item) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(ItemMenu.class, item.getId()).getStockActual();
        } finally {
            em.close();
        }
    }

    Pedido pedido(int id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Pedido.class, id);
        } finally {
            em.close();
        }
    }

    FranjaRetiro franjaGuardada(LocalTime inicio) {
        franjas.asegurarFranjasDelDia(LUNES);
        return franjas.listarDelDia(LUNES).stream()
                .filter(f -> f.getHoraInicio().equals(inicio))
                .findFirst().orElseThrow();
    }

    int asignados(LocalTime inicio) {
        return franjaGuardada(inicio).getAsignados();
    }

    /** Deja la franja con el número indicado de pedidos asignados (para los escenarios «14 de 15»). */
    void asignar(LocalTime inicio, int cantidad) {
        FranjaRetiro franja = franjaGuardada(inicio);
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("UPDATE FranjaRetiro f SET f.asignados = :n WHERE f.id = :id")
                    .setParameter("n", cantidad).setParameter("id", franja.getId()).executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
