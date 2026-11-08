package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.time.LocalDate;

import javax.persistence.PersistenceException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.exception.ConstraintViolationException;

import ec.edu.epn.polidinner.modelo.DiaComedor;
import ec.edu.epn.polidinner.modelo.Menu;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Plato;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.persistencia.MenuDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

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
    private final ProductoDAO productoDAO = new ProductoDAO();

    /**
     * Trazabilidad: Diagrama de secuencia CU01 - mensajes 8 (muestra formulario de ítems)
     * y 18 (lista de ítems agregados).
     * Corrección Paquete 2: además, las existencias de productos que se pueden ofrecer hoy.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DiaComedor hoy = RelojAplicacion.diaComedor();
        Menu menuHoy = obtenerMenuHoy(request.getSession(), hoy);
        request.setAttribute(MENU_HOY, menuHoy);
        request.setAttribute("hoy", hoy.getFecha());
        if (menuHoy == null) {
            // CU05: si la venta de hoy ya se cerró, la vista no ofrece iniciar otro menú
            request.setAttribute("ventaCerrada", menuDAO.buscarMenuCerrado(hoy) != null);
        } else {
            request.setAttribute("productosSeleccionables", productoDAO.listarSeleccionables(hoy.getFecha(), menuHoy));
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        String accion = request.getParameter("accion");
        DiaComedor hoy = RelojAplicacion.diaComedor();

        if ("iniciar".equals(accion)) {
            PersonalComedor personal = (PersonalComedor) sesion.getAttribute("personal");
            Menu menuHoy = iniciarCreacion(personal, hoy);
            if (menuHoy == null) {
                sesion.setAttribute("flashError", "La venta de hoy ya se cerró. El próximo menú se crea mañana.");
            } else {
                sesion.setAttribute(MENU_HOY, menuHoy.getId());
            }
            redirigir(request, response);
            return;
        }

        Menu menuHoy = obtenerMenuHoy(sesion, hoy);
        if (menuHoy == null) {
            sesion.setAttribute("flashError", "Primero inicie la creación del menú del día.");
            redirigir(request, response);
            return;
        }

        if ("agregar".equals(accion)) {
            procesarAgregarItem(request, response, menuHoy, hoy);
        } else if ("agregarProducto".equals(accion)) {
            String error = agregarProducto(menuHoy.getId(), leerEntero(request.getParameter("idProducto")),
                    hoy.getFecha());
            if (error == null) {
                sesion.setAttribute("flashExito", "Producto agregado al menú de hoy.");
            } else {
                sesion.setAttribute("flashError", error);
            }
            redirigir(request, response);
        } else if ("publicar".equals(accion)) {
            if (publicar(menuHoy.getId())) {
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
     * Validación del CU 05: si la venta de hoy ya se cerró, no se crea otro menú (devuelve null),
     * porque publicarlo reabriría la venta.
     */
    private Menu iniciarCreacion(PersonalComedor personal, DiaComedor hoy) {
        // Corrección Paquete 1a: un solo día del comedor para buscar, comprobar el cierre y crear el menú
        Menu existente = menuDAO.buscarMenuDelDia(hoy);
        if (existente != null) {
            return existente;
        }
        if (menuDAO.buscarMenuCerrado(hoy) != null) {
            return null;
        }
        Menu menuHoy = personal.crearMenuDiario(hoy);
        menuDAO.guardar(menuHoy);
        return menuHoy;
    }

    /**
     * Trazabilidad: Secuencia "Crear Menú" - loop [Por cada plato a preparar]:
     * mensaje 5 el personal ingresa nombre, descripción y precio (el formulario además pide las porciones);
     * mensajes 6-7 <<create>> Plato(nombre, precio, estado, descripcion) / retorna nuevoPlato;
     * mensajes 8-9 menuHoy.agregarItem(nuevoPlato) / confirma adición.
     * Corrección Paquete 2: aquí solo se crean platos (de esta jornada); los productos se registran en
     * «Productos» y se agregan con agregarProducto(). Se guarda en una transacción sobre el menú gestionado
     * (persist de la oferta y del plato nuevo), sin merge del menú.
     *
     * @return null si se agregó; el motivo si no
     */
    private String agregarPlato(int idMenu, String nombre, String descripcion, double precio, int porciones) {
        try (Transaccion tx = Transaccion.iniciar()) {
            Menu menu = menuDAO.buscarPorId(idMenu, tx);
            // Mensajes 6-7: <<create>> Plato(nombre, precio, estado, descripcion)
            Plato nuevoPlato = new Plato(nombre, precio, "Disponible", descripcion);
            nuevoPlato.setStockActual(porciones);
            // Mensajes 8-9: agregarItem(nuevoPlato) / confirma adición
            if (menu == null || !menu.agregarItem(nuevoPlato)) {
                return "No se pudo agregar el ítem: el menú está cerrado.";
            }
            tx.confirmar();
            return null;
        }
    }

    /**
     * Corrección Paquete 2: ofrece en el menú de hoy una existencia ya registrada en «Productos», sin copiarla
     * ni pedir de nuevo sus unidades. El servidor exige que sea un Producto habilitado, vigente y con stock.
     * El índice único menú + ítem impide duplicarla aunque el formulario se envíe dos veces a la vez.
     *
     * @return null si se agregó; el motivo si no
     */
    private String agregarProducto(int idMenu, Integer idProducto, LocalDate hoy) {
        if (idProducto == null) {
            return "Seleccione un producto del inventario.";
        }
        try (Transaccion tx = Transaccion.iniciar()) {
            Menu menu = menuDAO.buscarPorId(idMenu, tx);
            Producto producto = productoDAO.buscarProducto(idProducto, tx);
            if (menu == null || producto == null) {
                return "Seleccione un producto registrado en Productos.";
            }
            String nombre = "\"" + producto.getNombre() + "\"";
            if (!producto.isHabilitado()) {
                return nombre + " es un registro anterior y no se puede volver a ofrecer."
                        + " Si quedan unidades, regístrelas en Productos.";
            }
            if (!producto.estaVigente(hoy)) {
                return nombre + " ya caducó y no se puede ofrecer.";
            }
            if (!producto.hayStock(1)) {
                return nombre + " no tiene unidades disponibles.";
            }
            if (menu.perteneceAlMenu(producto.getId())) {
                return nombre + " ya está en el menú de hoy.";
            }
            if (!menu.agregarItem(producto)) {
                return "No se pudo agregar el ítem: el menú está cerrado.";
            }
            tx.confirmar();
            return null;
        } catch (PersistenceException e) {
            if (esViolacionDeUnicidad(e)) {
                return "Ese producto ya está en el menú de hoy.";
            }
            throw e;
        }
    }

    /**
     * Trazabilidad: Secuencia "Crear Menú" - mensaje 10 publicarParaVenta(), mensaje 11 confirma publicación
     * (el estado del menú cambia a "Publicado") y mensaje 12 "Menú publicado exitosamente" (lo muestra doPost).
     * El controlador envía el mensaje 10 en nombre del personal del comedor.
     * Validación técnica no modelada: no se publica un menú sin ítems.
     * Soporte técnico: se cambia el estado del menú leído dentro de una Transaccion (solo se actualiza su fila).
     */
    private boolean publicar(int idMenu) {
        try (Transaccion tx = Transaccion.iniciar()) {
            Menu menu = menuDAO.buscarPorId(idMenu, tx);
            if (menu == null || !menu.publicarParaVenta()) {
                return false;
            }
            tx.confirmar();
            return true;
        }
    }

    /** Lee y valida los datos del plato (mensaje 9 "Ingresa datos del ítem") antes de agregarlo. */
    private void procesarAgregarItem(HttpServletRequest request, HttpServletResponse response, Menu menuHoy,
                                     DiaComedor hoy) throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        if ("Producto".equals(request.getParameter("tipo"))) {
            sesion.setAttribute("flashError", "Los productos se registran en «Productos» y luego se agregan al menú"
                    + " desde «Agregar producto del inventario».");
            redirigir(request, response);
            return;
        }
        FormularioItemMenu formulario = FormularioItemMenu.desde(request, "Plato");

        if (!formulario.esValido()) {
            request.setAttribute("errores", formulario.getErrores());
            request.setAttribute(MENU_HOY, menuHoy);
            request.setAttribute("hoy", hoy.getFecha());
            request.setAttribute("productosSeleccionables", productoDAO.listarSeleccionables(hoy.getFecha(), menuHoy));
            request.getRequestDispatcher(VISTA).forward(request, response);
            return;
        }

        String error = agregarPlato(menuHoy.getId(), formulario.getNombre(), formulario.getDescripcion(),
                formulario.getPrecio(), formulario.getStock());
        if (error == null) {
            sesion.setAttribute("flashExito", "Plato \"" + formulario.getNombre() + "\" agregado al menú.");
        } else {
            sesion.setAttribute("flashError", error);
        }
        redirigir(request, response);
    }

    /**
     * Recupera menuHoy a partir del id guardado en la sesión; lo descarta si ya no existe, está cerrado o es
     * de otro día (una sesión abierta desde ayer no debe agregar platos al menú de ayer).
     */
    private Menu obtenerMenuHoy(HttpSession sesion, DiaComedor hoy) {
        Object id = sesion.getAttribute(MENU_HOY);
        if (!(id instanceof Integer)) {
            return null;
        }
        Menu menu = menuDAO.buscarPorId((Integer) id);
        if (menu == null || Menu.CERRADO.equals(menu.getEstado()) || !hoy.getFecha().equals(menu.getFechaCivil())) {
            sesion.removeAttribute(MENU_HOY);
            return null;
        }
        return menu;
    }

    private static boolean esViolacionDeUnicidad(Throwable e) {
        for (Throwable causa = e; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException) {
                return true;
            }
        }
        return false;
    }

    private static Integer leerEntero(String valor) {
        try {
            return valor == null ? null : Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void redirigir(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/personal/crear-menu");
    }

}
