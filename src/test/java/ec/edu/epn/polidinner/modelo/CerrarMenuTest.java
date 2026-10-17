package ec.edu.epn.polidinner.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CerrarMenuTest {
    private PersonalComedor personal;
    @BeforeEach
    void preparar() {
        personal = new PersonalComedor("1700000001", "Carlos", "Pérez", "Cocinero", "clave");
    }
    @Test
    void cerrarParaVentaSoloCierraUnMenuPublicado() {
        //Escenario 1: El menu borrador no se puede cerrar ya que no ha sido publicado
        Menu borrador = personal.crearMenuDiario();
        assertFalse(borrador.cerrarParaVenta());
       assertEquals("Borrador", borrador.getEstado());
        //Escenario 2: El menu publicado puede cambiar de estado a cerrar ya que fue publicado para los clientes.
       Menu publicado = personal.crearMenuDiario();
       publicado.agregarItem(new Plato("Almuerzo Ejecutivo", 2.50, "Disponible", "Sopa, segundo y jugo", 10));
       publicado.publicarParaVenta();
        assertTrue(publicado.cerrarParaVenta());
       assertEquals("Cerrado", publicado.getEstado());

}

}
