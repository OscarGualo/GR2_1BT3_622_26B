package ec.edu.epn.polidinner.controlador;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.ValidadorDescarte;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;
import ec.edu.epn.polidinner.servicio.ServicioPerdidas;

/**
 * Prueba de soporte del formulario de HU20 (no es una de las 12 pruebas TDD). Protege la refactorización R2:
 * debe seguir en verde antes y después de extraer SolicitudDescarte.
 */
class PerdidasCaducidadServletTest {

    private final ServicioPerdidas servicio = Mockito.mock(ServicioPerdidas.class);
    private final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    private final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    private final HttpSession sesion = Mockito.mock(HttpSession.class);
    private final PersonalComedor maria =
            new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "comedor123");
    private final PerdidasCaducidadServlet servlet = new PerdidasCaducidadServlet(servicio,
            Mockito.mock(ProductoDAO.class), Mockito.mock(PerdidaCaducidadDAO.class), new ValidadorDescarte());

    @BeforeEach
    void preparar() {
        Mockito.when(request.getSession()).thenReturn(sesion);
        Mockito.when(request.getContextPath()).thenReturn("/polidinner");
        Mockito.when(sesion.getAttribute("personal")).thenReturn(maria);
    }

    @Test
    void registraLaPerdidaConLosDatosDelFormularioYElPersonalDeLaSesion() throws Exception {
        Mockito.when(request.getParameter("referencia")).thenReturn("202");
        Mockito.when(request.getParameter("unidades")).thenReturn("6");
        Mockito.when(request.getParameter("token")).thenReturn("op-1");
        Mockito.when(servicio.registrar(ArgumentMatchers.eq(202), ArgumentMatchers.eq(6), ArgumentMatchers.same(maria),
                ArgumentMatchers.eq("op-1"), ArgumentMatchers.any(LocalDateTime.class))).thenReturn(true);

        servlet.doPost(request, response);

        Mockito.verify(sesion).setAttribute("flashExito", PerdidasCaducidadServlet.REGISTRADA);
        Mockito.verify(response).sendRedirect("/polidinner/personal/perdidas");
    }

    @Test
    void rechazaUnidadesNoNumericasSinRegistrar() throws Exception {
        Mockito.when(request.getParameter("referencia")).thenReturn("202");
        Mockito.when(request.getParameter("unidades")).thenReturn("seis");
        Mockito.when(request.getParameter("token")).thenReturn("op-1");

        servlet.doPost(request, response);

        Mockito.verify(servicio, Mockito.never()).registrar(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(),
                ArgumentMatchers.any(), ArgumentMatchers.anyString(), ArgumentMatchers.any());
        Mockito.verify(sesion).setAttribute("flashError", "Las unidades descartadas deben ser un número entero.");
        Mockito.verify(response).sendRedirect("/polidinner/personal/perdidas");
    }
}