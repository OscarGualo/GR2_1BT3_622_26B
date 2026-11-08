package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ec.edu.epn.polidinner.modelo.DescarteRechazadoException;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.modelo.ValidadorDescarte;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;
import ec.edu.epn.polidinner.servicio.ServicioPerdidas;

/**
 * HU20 Registrar pérdidas por caducidad - pantalla «Pérdidas por caducidad» del personal (protegida por
 * FiltroPersonal en /personal/*). doGet: existencias que admiten descarte, historial y un token nuevo para
 * la confirmación. doPost: registra la pérdida a nombre del personal de la sesión (POST-redirect-GET).
 */
@WebServlet("/personal/perdidas")
public class PerdidasCaducidadServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/perdidasCaducidad.jsp";
    static final String REGISTRADA = "Pérdida registrada.";
    static final String REPETIDA = "Esa confirmación ya se había registrado; no se descontó otra vez.";

    private final ServicioPerdidas servicio;
    private final ProductoDAO productoDAO;
    private final PerdidaCaducidadDAO perdidaDAO;
    private final ValidadorDescarte validador;

    public PerdidasCaducidadServlet() {
        this(new ServicioPerdidas(), new ProductoDAO(), new PerdidaCaducidadDAO(), new ValidadorDescarte());
    }

    PerdidasCaducidadServlet(ServicioPerdidas servicio, ProductoDAO productoDAO, PerdidaCaducidadDAO perdidaDAO,
                             ValidadorDescarte validador) {
        this.servicio = servicio;
        this.productoDAO = productoDAO;
        this.perdidaDAO = perdidaDAO;
        this.validador = validador;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LocalDate hoy = RelojAplicacion.fechaActual();
        List<Producto> descartables = productoDAO.listar(ProductoDAO.Filtro.CADUCADOS, hoy).stream()
                .filter(producto -> validador.admiteDescarte(producto, hoy))
                .toList();
        request.setAttribute("descartables", descartables);
        request.setAttribute("historial", perdidaDAO.listarRecientes());
        request.setAttribute("token", UUID.randomUUID().toString());
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession sesion = request.getSession();
        String referenciaTexto = request.getParameter("referencia");
        String unidadesTexto = request.getParameter("unidades");
        String token = request.getParameter("token");
        try {
            if (referenciaTexto == null || referenciaTexto.trim().isEmpty()
                    || unidadesTexto == null || unidadesTexto.trim().isEmpty()) {
                throw new DescarteRechazadoException("Seleccione la existencia e indique las unidades descartadas.");
            }
            int referencia;
            int unidades;
            try {
                referencia = Integer.parseInt(referenciaTexto.trim());
                unidades = Integer.parseInt(unidadesTexto.trim());
            } catch (NumberFormatException e) {
                throw new DescarteRechazadoException("Las unidades descartadas deben ser un número entero.");
            }
            PersonalComedor personal = (PersonalComedor) sesion.getAttribute("personal");
            boolean registrada = servicio.registrar(referencia, unidades, personal, token, RelojAplicacion.ahora());
            sesion.setAttribute("flashExito", registrada ? REGISTRADA : REPETIDA);
        } catch (DescarteRechazadoException e) {
            sesion.setAttribute("flashError", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/personal/perdidas");
    }
}