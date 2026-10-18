package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
/**
 * T6 (TDD con mocks) - CU 06 Cancelar pedido con reembolso a PoliCuenta.
 * Se prueba un Pedido real; sus colaboradores (Policuenta, ItemMenu y ClienteUniversitario)
 * son objetos simulados con Mockito.
 */
class PedidoCancelarMockTest {

    /** T6 (TDD con mocks): cancelar() devuelve el total a la PoliCuenta y repone el stock de cada ítem. */
    @Test
    void cancelarReembolsaElTotalYReponeElStockDeCadaItem() {
        // Objetos simulados (mocks) de los colaboradores de Pedido
        ClienteUniversitario cliente = Mockito.mock(ClienteUniversitario.class);
        Policuenta policuenta = Mockito.mock(Policuenta.class);
        ItemMenu arroz = Mockito.mock(ItemMenu.class);
        ItemMenu jugo = Mockito.mock(ItemMenu.class);

        // Respuestas preparadas: precios, stock disponible y débito exitoso de $4.25
        Mockito.when(cliente.getPolicuenta()).thenReturn(policuenta);
        Mockito.when(arroz.getPrecio()).thenReturn(3.50);
        Mockito.when(arroz.hayStock(1)).thenReturn(true);
        Mockito.when(jugo.getPrecio()).thenReturn(0.75);
        Mockito.when(jugo.hayStock(1)).thenReturn(true);
        Mockito.when(policuenta.debitar(4.25)).thenReturn(true);

        // Pedido REAL de Andrea: arroz con pollo $3.50 + jugo de naranja $0.75 = $4.25, pagado
        Pedido pedido = new Pedido(new Date(), "Creado", cliente);
        pedido.agregarItem(arroz, 1);
        pedido.agregarItem(jugo, 1);
        assertTrue(pedido.confirmarPago());

        assertTrue(pedido.cancelar());

        // Comprobar que Pedido llamó a sus colaboradores con los valores correctos
        Mockito.verify(policuenta).acreditar(4.25);
        Mockito.verify(arroz).reponerStock(1);
        Mockito.verify(jugo).reponerStock(1);
    }
}