package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;

/**
 * Corrección de productos persistentes (Paquete 2) - pantalla «Productos» del personal (protegida por
 * FiltroPersonal en /personal/*). Registra existencias de productos independientes del menú diario y las
 * lista. Registrar no crea ninguna oferta: el producto se agrega al menú desde «Crear menú».
 * Se puede usar aunque no exista menú o la venta de hoy esté cerrada; eso no habilita compras.
 * doGet: lista con filtro (vigentes por defecto, agotados, caducados, históricos). doPost (accion=registrar):
 * alta con las mismas validaciones del formulario de ítems (patrón POST-redirect-GET).
 */
@WebServlet("/personal/productos")
public class ProductosServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/productos.jsp";

    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        mostrarLista(request);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"registrar".equals(request.getParameter("accion"))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no reconocida");
            return;
        }
        FormularioItemMenu formulario = FormularioItemMenu.desde(request, "Producto");
        if (!formulario.esValido()) {
            // Se conservan los datos ingresados y se muestran los errores (sin crear un registro parcial)
            request.setAttribute("errores", formulario.getErrores());
            mostrarLista(request);
            request.getRequestDispatcher(VISTA).forward(request, response);
            return;
        }
        Producto producto = Producto.registrar(formulario.getNombre(), formulario.getPrecio(),
                formulario.getDescripcion(), formulario.getStock(), formulario.getFechaCaducidad());
        productoDAO.guardar(producto);

        HttpSession sesion = request.getSession();
        sesion.setAttribute("flashExito", "Producto \"" + producto.getNombre() + "\" registrado con "
                + producto.getStockActual() + " unidades (caduca el "
                + new SimpleDateFormat("dd/MM/yyyy").format(producto.getFechaCaducidad())
                + "). Agréguelo al menú del día desde «Crear menú».");
        response.sendRedirect(request.getContextPath() + "/personal/productos");
    }

    private void mostrarLista(HttpServletRequest request) {
        LocalDate hoy = RelojAplicacion.fechaActual();
        ProductoDAO.Filtro filtro = leerFiltro(request.getParameter("filtro"));
        request.setAttribute("filtro", filtro.name());
        request.setAttribute("productos", productoDAO.listar(filtro, hoy));
        request.setAttribute("hoy", hoy);
    }

    /** Filtro de la lista; uno vacío o desconocido muestra las existencias vigentes con stock. */
    static ProductoDAO.Filtro leerFiltro(String valor) {
        if (valor != null) {
            for (ProductoDAO.Filtro filtro : ProductoDAO.Filtro.values()) {
                if (filtro.name().equalsIgnoreCase(valor.trim())) {
                    return filtro;
                }
            }
        }
        return ProductoDAO.Filtro.VIGENTES;
    }
}
