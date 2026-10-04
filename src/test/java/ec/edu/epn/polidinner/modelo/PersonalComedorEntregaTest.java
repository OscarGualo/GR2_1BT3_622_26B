package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de PersonalComedor en el CU 04: validarCodigo() y entregarPedido().
 * Trazabilidad: Diagrama de clases (Fig. 15) y Diagrama de secuencia CU04 - mensajes 5-9.
 */
class PersonalComedorEntregaTest {

    private PersonalComedor maria;
    private Pedido pedido;
    private Comprobante comprobante;

    @BeforeEach
    void prepararPedidoPagado() {
        maria = new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "clave");
        ClienteUniversitario andrea = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));
        pedido = andrea.crearPedido();
        pedido.agregarItem(new Plato("Arroz con pollo", 3.50, "Disponible", "", 20), 1);
        pedido.confirmarPago();
        asignarId(pedido, 457);
        comprobante = pedido.generarCodigoEntrega();
    }

    /** validarCodigo confirma el código; entregarPedido registra quién validó ("valida"). */
    @Test
    void validarCodigoCorrectoYEntregaRegistraLaValidacion() {
        assertTrue(maria.validarCodigo("pd-0457", comprobante));

        maria.entregarPedido(comprobante.getPedido());

        assertSame(maria, comprobante.getPersonalValida());
    }

    @Test
    void validarCodigoQueNoCoincideDevuelveFalse() {
        assertFalse(maria.validarCodigo("PD-0458", comprobante));
        assertFalse(maria.validarCodigo(null, comprobante));
        assertFalse(maria.validarCodigo("PD-0457", null));
        assertNull(comprobante.getPersonalValida());
    }

    /** Caso de prueba: PD-0457 ingresado por segunda vez es rechazado. */
    @Test
    void validarUnCodigoYaUsadoDevuelveFalse() {
        comprobante.marcarCodigoUsado();

        assertFalse(maria.validarCodigo("PD-0457", comprobante));
    }

    /** Mensaje 9: entregarPedido(pedido) deja el pedido "Entregado" y asociado al personal ("entrega"). */
    @Test
    void entregarPedidoCambiaElEstadoYAsociaAlPersonal() {
        maria.entregarPedido(pedido);

        assertEquals("Entregado", pedido.getEstado());
        assertSame(maria, pedido.getPersonalEntrega());
    }

    @Test
    void noSeEntregaUnPedidoQueNoEstaPagado() {
        Pedido sinPagar = new ClienteUniversitario("1750000002", "Luis", "Mora", "clave",
                new Policuenta("2024-0999", 1.00)).crearPedido();

        assertThrows(IllegalStateException.class, () -> maria.entregarPedido(sinPagar));
    }

    /** Soporte de prueba: simula el id que JPA asigna al guardar el pedido. */
    private static void asignarId(Pedido pedido, int id) {
        try {
            java.lang.reflect.Field campo = Pedido.class.getDeclaredField("id");
            campo.setAccessible(true);
            campo.setInt(pedido, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
