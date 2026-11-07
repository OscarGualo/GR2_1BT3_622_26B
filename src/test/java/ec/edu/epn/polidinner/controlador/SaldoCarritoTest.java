package ec.edu.epn.polidinner.controlador;

import static org.mockito.Mockito.*;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedConstruction;
import ec.edu.epn.polidinner.modelo.*;
import ec.edu.epn.polidinner.persistencia.ClienteUniversitarioDAO;
import ec.edu.epn.polidinner.servicio.ServicioFranjas;

class SaldoCarritoTest {
    @ParameterizedTest
    @CsvSource({"4.00, false", "5.00, true", "10.00, true"})
    void hu07_laVistaRecibeElSaldoActualDeLaBase(double saldoActual, boolean suficiente) throws Exception {
        // La sesión conserva un saldo anterior: para decidir se debe consultar la base.
        ClienteUniversitario enSesion = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 100.00));
        ClienteUniversitario actualizado = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", saldoActual));
        Pedido pedido = enSesion.crearPedido();
        pedido.agregarItem(new Plato("Almuerzo", 5.00, "Disponible", "Prueba", 20), 1);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession sesion = mock(HttpSession.class);
        RequestDispatcher vista = mock(RequestDispatcher.class);
        when(request.getSession()).thenReturn(sesion);
        when(request.getParameter("vista")).thenReturn("carrito");
        when(sesion.getAttribute("cliente")).thenReturn(enSesion);
        when(sesion.getAttribute("pedido")).thenReturn(pedido);
        when(request.getRequestDispatcher("/vistas/cliente/carrito.jsp")).thenReturn(vista);

        try (MockedConstruction<ClienteUniversitarioDAO> clientes = mockConstruction(ClienteUniversitarioDAO.class,
                    (dao, contexto) -> when(dao.buscarPorCedula("1750000001")).thenReturn(actualizado));
             MockedConstruction<ServicioFranjas> franjas = mockConstruction(ServicioFranjas.class,
                    (servicio, contexto) -> when(servicio.franjasOfrecidas(any())).thenReturn(List.of()))) {
            new RealizarPedidoServlet().doGet(request, response);
            verify(request).setAttribute("saldo", saldoActual);
            verify(request).setAttribute("total", 5.00);
            verify(request).setAttribute("saldoSuficiente", suficiente);
            verify(vista).forward(request, response);
        }
    }
}
