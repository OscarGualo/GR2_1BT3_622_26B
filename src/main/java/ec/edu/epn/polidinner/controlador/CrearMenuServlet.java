package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ec.edu.epn.polidinner.modelo.ItemMenu;
import ec.edu.epn.polidinner.modelo.Menu;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Plato;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.persistencia.MenuDAO;

/**
 * Trazabilidad: Diagrama de robustez - controlador "Crear menú".
 * Diagrama de secuencia CU01 - participante "Controlador (Crear Menú)".
 * doPost despacha según el parámetro "accion" a los tres mensajes que recibe el controlador:
 * iniciarCreacion() (msj 2), agregarItem(...) (msj 10) y publicar() (msj 20).
 */
@WebServlet("/personal/crear-menu")
public class CrearMenuServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/crearMenu.jsp";
    /** Atributo de sesión con el id del menú en construcción (objeto menuHoy de la secuencia). */
    private static final String MENU_HOY = "menuHoy";

    private final MenuDAO menuDAO = new MenuDAO();

    /**
     * Trazabilidad: Diagrama de secuencia CU01 - mensajes 8 (muestra formulario de ítems)
     * y 18 (lista de ítems agregados).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute(MENU_HOY, obtenerMenuHoy(request.getSession()));
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        String accion = request.getParameter("accion");

        if ("iniciar".equals(accion)) {
            PersonalComedor personal = (PersonalComedor) sesion.getAttribute("personal");
            Menu menuHoy = iniciarCreacion(personal);
            sesion.setAttribute(MENU_HOY, menuHoy.getId());
            redirigir(request, response);
            return;
        }

        Menu menuHoy = obtenerMenuHoy(sesion);
        if (menuHoy == null) {
            sesion.setAttribute("flashError", "Primero inicie la creación del menú del día.");
            redirigir(request, response);
            return;
        }

        if ("agregar".equals(accion)) {
            procesarAgregarItem(request, response, menuHoy);
        } else if ("publicar".equals(accion)) {
            if (publicar(menuHoy)) {
                // Mensajes 24-25: texto exacto del diagrama de secuencia
                sesion.setAttribute("flashExito", "Menú publicado exitosamente");
            } else {
                sesion.setAttribute("flashError", "No se puede publicar un menú sin ítems. Agregue al menos un plato o producto.");
            }
            redirigir(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no reconocida");
        }
    }

    /**
 * Trazabilidad: Secuencia "Crear Menú" - mensaje 1 (el personal inicia el proceso de creación de menú),
            * mensaje 2 crearMenuDiario(), mensajes 3-4 <<create>> Menu(Fecha=hoy, Estado="Borrador") / retorna menuHoy.
            * Validación técnica no modelada: si ya existe el menú del día, se reutiliza en lugar de crear otro.
 */
    private Menu iniciarCreacion(PersonalComedor personal) {
        Menu existente = menuDAO.buscarMenuDelDia();
        if (existente != null) {
            return existente;
        }
        Menu menuHoy = personal.crearMenuDiario();
        menuDAO.guardar(menuHoy);
        return menuHoy;
    }

    /**
     * Trazabilidad: Secuencia "Crear Menú" - loop [Por cada plato a preparar]:
     * mensaje 5 el personal ingresa nombre, descripción y precio (el formulario además pide las porciones);
     * mensajes 6-7 <<create>> Plato(nombre, precio, estado, descripcion) / retorna nuevoPlato;
     * mensajes 8-9 menuHoy.agregarItem(nuevoPlato) / confirma adición.
     * Alternativa no modelada en la secuencia: si el formulario indica "Producto", se crea un Producto
     * con stock y fecha de caducidad (clase Producto del diagrama de clases).
     */
    private boolean agregarItem(Menu menuHoy, String tipo, String nombre, String descripcion,
                                double precio, int stock, Date fechaCaducidad) {
        ItemMenu nuevoItem;
        if ("Producto".equals(tipo)) {
            nuevoItem = new Producto(nombre, precio, "Disponible", descripcion, stock, fechaCaducidad);
        } else {
            // Mensajes 6-7: <<create>> Plato(nombre, precio, estado, descripcion)
            Plato nuevoPlato = new Plato(nombre, precio, "Disponible", descripcion);
            nuevoPlato.setStockActual(stock);
            nuevoItem = nuevoPlato;
        }
        // Mensajes 8-9: agregarItem(nuevoPlato) / confirma adición
        if (!menuHoy.agregarItem(nuevoItem)) {
            return false;
        }
        menuDAO.actualizar(menuHoy);
        return true;
    }

    /**
     * Trazabilidad: Secuencia "Crear Menú" - mensaje 10 publicarParaVenta(), mensaje 11 confirma publicación
     * (el estado del menú cambia a "Publicado") y mensaje 12 "Menú publicado exitosamente" (lo muestra doPost).
     * El controlador envía el mensaje 10 en nombre del personal del comedor.
     * Validación técnica no modelada: no se publica un menú sin ítems.
     */
    private boolean publicar(Menu menuHoy) {
        if (!menuHoy.publicarParaVenta()) {
            return false;
        }
        menuDAO.actualizar(menuHoy);
        return true;
    }

    /** Lee y valida los datos del ítem (mensaje 9 "Ingresa datos del ítem") antes de llamar a agregarItem. */
    private void procesarAgregarItem(HttpServletRequest request, HttpServletResponse response, Menu menuHoy)
            throws ServletException, IOException {
        String tipo = "Producto".equals(request.getParameter("tipo")) ? "Producto" : "Plato";
        String nombre = recortar(request.getParameter("nombre"));
        String descripcion = recortar(request.getParameter("descripcion"));
        String textoPrecio = recortar(request.getParameter("precio"));
        String textoStock = recortar(request.getParameter("stock"));
        String textoFecha = recortar(request.getParameter("fechaCaducidad"));

        Map<String, String> errores = new LinkedHashMap<>();
        if (nombre.isEmpty()) {
            errores.put("nombre", "Ingrese el nombre del ítem.");
        } else if (nombre.length() > 100) {
            errores.put("nombre", "El nombre admite máximo 100 caracteres.");
        }
        if (descripcion.length() > 255) {
            errores.put("descripcion", "La descripción admite máximo 255 caracteres.");
        }

        double precio = 0;
        try {
            precio = Double.parseDouble(textoPrecio.replace(',', '.'));
            if (!(precio > 0)) {
                errores.put("precio", "El precio debe ser mayor que 0.");
            }
        } catch (NumberFormatException e) {
            errores.put("precio", "Ingrese un precio numérico, por ejemplo 2.50.");
        }

        // Plato: porciones disponibles; Producto: unidades en stock (ItemMenu.stockActual, Fig. 15)
        String nombreStock = "Producto".equals(tipo) ? "stock" : "número de porciones";
        int stock = 0;
        try {
            stock = Integer.parseInt(textoStock);
            if (stock < 1) {
                errores.put("stock", "El " + nombreStock + " debe ser al menos 1.");
            } else if (stock > 10000) {
                errores.put("stock", "El " + nombreStock + " admite máximo 10000.");
            }
        } catch (NumberFormatException e) {
            errores.put("stock", "Ingrese el " + nombreStock + " como número entero.");
        }
        Date fechaCaducidad = null;
        if ("Producto".equals(tipo)) {
            try {
                SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
                formato.setLenient(false);
                fechaCaducidad = formato.parse(textoFecha);
                if (fechaCaducidad.before(inicioDeHoy())) {
                    errores.put("fechaCaducidad", "La fecha de caducidad no puede ser anterior a hoy.");
                }
            } catch (ParseException e) {
                errores.put("fechaCaducidad", "Seleccione la fecha de caducidad del producto.");
            }
        }

        if (!errores.isEmpty()) {
            request.setAttribute("errores", errores);
            request.setAttribute(MENU_HOY, menuHoy);
            request.getRequestDispatcher(VISTA).forward(request, response);
            return;
        }

        HttpSession sesion = request.getSession();
        if (agregarItem(menuHoy, tipo, nombre, descripcion, precio, stock, fechaCaducidad)) {
            sesion.setAttribute("flashExito", tipo + " \"" + nombre + "\" agregado al menú.");
        } else {
            sesion.setAttribute("flashError", "No se pudo agregar el ítem: el menú está cerrado.");
        }
        redirigir(request, response);
    }

    /** Recupera menuHoy a partir del id guardado en la sesión; lo descarta si ya no existe o está cerrado. */
    private Menu obtenerMenuHoy(HttpSession sesion) {
        Object id = sesion.getAttribute(MENU_HOY);
        if (!(id instanceof Integer)) {
            return null;
        }
        Menu menu = menuDAO.buscarPorId((Integer) id);
        if (menu == null || "Cerrado".equals(menu.getEstado())) {
            sesion.removeAttribute(MENU_HOY);
            return null;
        }
        return menu;
    }

    private void redirigir(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/personal/crear-menu");
    }

    private static String recortar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static Date inicioDeHoy() {
        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0);
        hoy.set(Calendar.MILLISECOND, 0);
        return hoy.getTime();
    }
}
