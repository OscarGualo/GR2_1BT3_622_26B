# Refactorizaciones de PoliDinner

Proyecto **PoliDinner** – sistema de pedidos del comedor universitario (EPN).
Este documento describe cinco refactorizaciones propuestas sobre el código actual (rama `main`). Cada una se
presenta con los mismos cuatro criterios:

1. **Nombre de la refactorización**
2. **Código antes de la refactorización**
3. **Explicación breve de cada paso**, señalando el código que se refactoriza (marcado con `// ◄──`)
4. **Código después de la refactorización**

Ninguna refactorización cambia el comportamiento del sistema: solo mejora la estructura interna del código.
Los nombres de clases y métodos que vienen de los diagramas UML (`confirmarPago()`, `agregarItem()`,
`esDeHoy()`, `obtenerItems()`…) se mantienen exactamente iguales.

## Resumen

| # | Refactorización | Categoría | Dónde se aplica | Olor de código que resuelve |
|---|---|---|---|---|
| 1 | Extract Method | Composing Methods | `Pedido.confirmarPago()` | Método largo que hace varias cosas |
| 2 | Replace Temp with Query | Composing Methods | `Pedido.agregarItem()` | Variable temporal que esconde una consulta |
| 3 | Substitute Algorithm | Composing Methods | `Comprobante.esDeHoy()` | Algoritmo con rodeo (comparar fechas como texto) |
| 4 | Move Method | Moving Features Between Objects | `RetirarItemMenuServlet.perteneceAlMenu()` → `Menu.buscarItem()` | Feature Envy y código duplicado |
| 5 | Extract Class | Moving Features Between Objects | `CrearMenuServlet` → nueva clase `FormularioItemMenu` | Clase con dos responsabilidades |

---

## 1. Extract Method

**Archivo:** `src/main/java/ec/edu/epn/polidinner/modelo/Pedido.java` (líneas 137-156)
**Método:** `Pedido.confirmarPago()` (Diagrama de secuencia CU03 – Realizar pedido y pagar)

### Código antes de la refactorización

```java
public boolean confirmarPago() {
    if (!"Creado".equals(estado) || detalles.isEmpty()) {
        return false;
    }
    calcularTotal();
    for (DetallePedido detalle : detalles) {                       // ◄── fragmento A: verificar stock
        if (!detalle.getItem().hayStock(detalle.getCantidad())) {  // ◄──
            return false;                                          // ◄──
        }                                                          // ◄──
    }                                                              // ◄──
    if (!policuenta.debitar(total)) {
        return false;
    }
    for (DetallePedido detalle : detalles) {                       // ◄── fragmento B: reducir stock
        detalle.getItem().reducirStock(detalle.getCantidad());     // ◄──
    }                                                              // ◄──
    estado = "Pagado";
    generarCodigoEntrega();
    return true;
}
```

### Pasos de la refactorización

1. **Identificar los fragmentos que hacen una tarea propia.** `confirmarPago()` mezcla tres pasos del pago:
   verificar el stock de todos los ítems (fragmento A), debitar la PoliCuenta y reducir el stock (fragmento B).
   Los dos bucles `for` obligan a leer el método línea por línea para entender el flujo.
2. **Crear un método nuevo por fragmento, con un nombre que diga *qué* hace** (no *cómo*):
   `hayStockDeTodosLosItems()` para el fragmento A y `reducirStockDeItems()` para el fragmento B.
3. **Copiar cada bucle, sin cambios, al método nuevo.** Los bucles solo usan el atributo `detalles`,
   así que los métodos no necesitan parámetros. El fragmento A devuelve `boolean` porque decide si el pago sigue.
4. **Reemplazar cada fragmento en `confirmarPago()` por la llamada al método extraído.**
5. **Ejecutar las pruebas** (`PedidoTest`, que ya cubre `confirmarPago()`) para confirmar que el comportamiento no cambió.

### Código después de la refactorización

```java
public boolean confirmarPago() {
    if (!"Creado".equals(estado) || detalles.isEmpty()) {
        return false;
    }
    calcularTotal();
    if (!hayStockDeTodosLosItems()) {      // ◄── reemplaza el fragmento A
        return false;
    }
    if (!policuenta.debitar(total)) {
        return false;
    }
    reducirStockDeItems();                 // ◄── reemplaza el fragmento B
    estado = "Pagado";
    generarCodigoEntrega();
    return true;
}

/**
 * Soporte técnico NO modelado – Refactorización: Extract Method (desde confirmarPago()).
 * Diagrama de secuencia CU03: hayStock(cantidad) de cada ítem antes de debitar.
 */
private boolean hayStockDeTodosLosItems() {
    for (DetallePedido detalle : detalles) {
        if (!detalle.getItem().hayStock(detalle.getCantidad())) {
            return false;
        }
    }
    return true;
}

/**
 * Soporte técnico NO modelado – Refactorización: Extract Method (desde confirmarPago()).
 * Diagrama de secuencia CU03: reducirStock(cantidad) de cada ítem después del débito.
 */
private void reducirStockDeItems() {
    for (DetallePedido detalle : detalles) {
        detalle.getItem().reducirStock(detalle.getCantidad());
    }
}
```

**Beneficio:** `confirmarPago()` ahora se lee como la secuencia del diagrama (calcular → verificar stock →
debitar → reducir stock → generar comprobante), y cada paso se puede entender por separado.

---

## 2. Replace Temp with Query

**Archivo:** `src/main/java/ec/edu/epn/polidinner/modelo/Pedido.java` (líneas 97-117)
**Método:** `Pedido.agregarItem(ItemMenu item, int cantidad)`

### Código antes de la refactorización

```java
public DetallePedido agregarItem(ItemMenu item, int cantidad) {
    if (!"Creado".equals(estado)) {
        throw new IllegalStateException("El pedido ya fue pagado");
    }
    if (cantidad < 1) {
        throw new IllegalArgumentException("La cantidad debe ser al menos 1");
    }
    DetallePedido existente = buscarDetalle(item);
    int cantidadTotal = cantidad + (existente == null ? 0 : existente.getCantidad());  // ◄── variable temporal
    if (!item.hayStock(cantidadTotal)) {                                               // ◄── uso de la temporal
        throw new IllegalStateException("No hay stock suficiente de \"" + item.getNombre() + "\"");
    }
    if (existente != null) {
        existente.sumarCantidad(cantidad);
        return existente;
    }
    DetallePedido detalle = new DetallePedido(cantidad, item.getPrecio());
    detalle.asignarItem(item);
    detalles.add(detalle);
    return detalle;
}
```

### Pasos de la refactorización

1. **Identificar la variable temporal.** `cantidadTotal` guarda el resultado de una expresión que responde a
   una pregunta del dominio: *¿cuántas unidades de este ítem ya están en el pedido?* Esa idea queda escondida
   dentro de un operador ternario.
2. **Comprobar que la temporal se asigna una sola vez** y no se modifica después. Así es, por lo que se
   puede reemplazar por una consulta sin cambiar el resultado.
3. **Crear el método de consulta `cantidadEnPedido(item)`** con la parte de la expresión que calcula lo que
   ya hay en el pedido. Reutiliza el método existente `buscarDetalle(item)`.
4. **Reemplazar la temporal por la llamada a la consulta** (`cantidad + cantidadEnPedido(item)`) y borrar
   la variable `cantidadTotal`.
5. **Ejecutar las pruebas** (`PedidoTest`: el caso "agregar dos veces el mismo ítem suma la cantidad" y el
   caso "sin stock lanza excepción" deben seguir pasando).

### Código después de la refactorización

```java
public DetallePedido agregarItem(ItemMenu item, int cantidad) {
    if (!"Creado".equals(estado)) {
        throw new IllegalStateException("El pedido ya fue pagado");
    }
    if (cantidad < 1) {
        throw new IllegalArgumentException("La cantidad debe ser al menos 1");
    }
    if (!item.hayStock(cantidad + cantidadEnPedido(item))) {   // ◄── la consulta reemplaza la temporal
        throw new IllegalStateException("No hay stock suficiente de \"" + item.getNombre() + "\"");
    }
    DetallePedido existente = buscarDetalle(item);
    if (existente != null) {
        existente.sumarCantidad(cantidad);
        return existente;
    }
    DetallePedido detalle = new DetallePedido(cantidad, item.getPrecio());
    detalle.asignarItem(item);
    detalles.add(detalle);
    return detalle;
}

/**
 * Soporte técnico NO modelado – Refactorización: Replace Temp with Query (desde agregarItem()).
 * Unidades de este ítem que ya están en el pedido (0 si todavía no se agregó).
 */
private int cantidadEnPedido(ItemMenu item) {
    DetallePedido existente = buscarDetalle(item);
    return existente == null ? 0 : existente.getCantidad();
}
```

**Beneficio:** la condición de stock se lee en lenguaje del dominio ("lo que pide más lo que ya hay en el
pedido") y la consulta `cantidadEnPedido()` puede reutilizarse en otros métodos de `Pedido`.

---

## 3. Substitute Algorithm

**Archivo:** `src/main/java/ec/edu/epn/polidinner/modelo/Comprobante.java` (líneas 112-116)
**Método:** `Comprobante.esDeHoy()` (lo usa `validarCodigoEntrega()`, CU04 – Habilitar entrega)

### Código antes de la refactorización

```java
/** Soporte de validarCodigoEntrega() y de los mensajes de rechazo: ¿se generó en la fecha actual? */
public boolean esDeHoy() {
    SimpleDateFormat dia = new SimpleDateFormat("yyyyMMdd");                                // ◄── algoritmo actual
    return fechaGeneracion != null && dia.format(fechaGeneracion).equals(dia.format(new Date()));  // ◄──
}
```

### Pasos de la refactorización

1. **Analizar el algoritmo actual.** Para saber si dos fechas son el mismo día, convierte ambas a texto
   (por ejemplo `"20261010"`) y compara las cadenas. Además crea un `SimpleDateFormat` nuevo en cada llamada.
   Funciona, pero da un rodeo: la pregunta es sobre *días*, no sobre *textos*.
2. **Elegir un algoritmo más claro que dé el mismo resultado.** Java 17 (versión del proyecto) incluye
   `java.time.LocalDate`, que representa exactamente un día del calendario y se compara con `equals()`.
3. **Reemplazar el cuerpo completo del método** por el nuevo algoritmo: convertir `fechaGeneracion` a
   `LocalDate` en la zona horaria del sistema y compararla con `LocalDate.now()`. Se conserva la verificación
   de `null`.
4. **Ajustar los imports:** se quita `java.text.SimpleDateFormat` y se agregan `java.time.LocalDate`
   y `java.time.ZoneId`.
5. **Ejecutar las pruebas** (`ComprobanteTest`, `PersonalComedorEntregaTest`) y agregar un caso con la fecha de
   ayer, que debe devolver `false`.

### Código después de la refactorización

```java
/**
 * Soporte de validarCodigoEntrega() y de los mensajes de rechazo: ¿se generó en la fecha actual?
 * Refactorización: Substitute Algorithm (compara días con LocalDate en lugar de textos).
 */
public boolean esDeHoy() {
    return fechaGeneracion != null
            && fechaGeneracion.toInstant()                 // ◄── algoritmo nuevo
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .equals(LocalDate.now());
}
```

**Beneficio:** el código expresa directamente la intención ("¿la fecha de generación es hoy?"), no crea
objetos de formato innecesarios y usa la API de fechas moderna de Java.

---

## 4. Move Method

**Origen:** `src/main/java/ec/edu/epn/polidinner/controlador/RetirarItemMenuServlet.java` (líneas 145-153)
**Destino:** `src/main/java/ec/edu/epn/polidinner/modelo/Menu.java`
**También se beneficia:** `RealizarPedidoServlet.buscarItemOfrecido()` (líneas 258-269), que repetía el mismo bucle.

### Código antes de la refactorización

```java
// RetirarItemMenuServlet.java
/** Validación técnica no modelada: el ítem debe estar en menuHoy.obtenerItems(). */
private static boolean perteneceAlMenu(Menu menu, int idItem) {   // ◄── método en la clase equivocada:
    for (ItemMenu item : menu.obtenerItems()) {                    // ◄── solo usa datos de Menu
        if (item.getId() == idItem) {                              // ◄──
            return true;                                           // ◄──
        }                                                          // ◄──
    }                                                              // ◄──
    return false;                                                  // ◄──
}

// uso dentro de retirarItem(...)
if (menuHoy == null || !perteneceAlMenu(menuHoy, idItem)) {        // ◄── llamada al método
    sesion.setAttribute("flashError", "El ítem no pertenece al menú de hoy.");
    return;
}
```

```java
// RealizarPedidoServlet.java – el mismo recorrido, duplicado
private ItemMenu buscarItemOfrecido(int idItem) {
    Menu menuHoy = menuDAO.buscarMenuDelDia();
    if (menuHoy == null || !"Publicado".equals(menuHoy.getEstado())) {
        return null;
    }
    for (ItemMenu item : menuHoy.obtenerItems()) {                 // ◄── bucle duplicado
        if (item.getId() == idItem && "Disponible".equals(item.getEstado())) {
            return item;
        }
    }
    return null;
}
```

### Pasos de la refactorización

1. **Detectar el olor "Feature Envy" (envidia de características).** `perteneceAlMenu()` está en un servlet,
   pero todo lo que usa pertenece a `Menu` (su lista de ítems). Además, `RealizarPedidoServlet` repite el
   mismo recorrido para buscar un ítem por id.
2. **Crear el método en la clase destino (`Menu`)** con el nombre `buscarItem(int idItem)`. En lugar de
   devolver `boolean`, devuelve el `ItemMenu` encontrado (o `null`), para que sirva a los dos servlets.
   Dentro de `Menu` recorre directamente el atributo `items`, sin pasar el menú como parámetro.
3. **Documentar el método** con su Javadoc de trazabilidad como método técnico no modelado
   (no aparece en el diagrama de clases).
4. **Cambiar la llamada en el origen:** `!perteneceAlMenu(menuHoy, idItem)` pasa a ser
   `menuHoy.buscarItem(idItem) == null`.
5. **Eliminar el método `perteneceAlMenu()`** del servlet.
6. **Reutilizar el método movido en `RealizarPedidoServlet`**, que deja de tener su propio bucle.
7. **Ejecutar las pruebas** (`RetirarItemTest`, `CrearMenuTest`) y agregar una prueba de `Menu.buscarItem()`.

### Código después de la refactorización

```java
// Menu.java – el método ahora vive en la clase que tiene los datos
/**
 * Soporte técnico NO modelado – Refactorización: Move Method (desde RetirarItemMenuServlet.perteneceAlMenu()).
 * Busca un ítem del menú por su id; devuelve null si no pertenece a este menú.
 */
public ItemMenu buscarItem(int idItem) {          // ◄── método movido
    for (ItemMenu item : items) {
        if (item.getId() == idItem) {
            return item;
        }
    }
    return null;
}
```

```java
// RetirarItemMenuServlet.java – dentro de retirarItem(...)
if (menuHoy == null || menuHoy.buscarItem(idItem) == null) {       // ◄── usa el método movido
    sesion.setAttribute("flashError", "El ítem no pertenece al menú de hoy.");
    return;
}
// (el método perteneceAlMenu() se elimina)
```

```java
// RealizarPedidoServlet.java
private ItemMenu buscarItemOfrecido(int idItem) {
    Menu menuHoy = menuDAO.buscarMenuDelDia();
    if (menuHoy == null || !"Publicado".equals(menuHoy.getEstado())) {
        return null;
    }
    ItemMenu item = menuHoy.buscarItem(idItem);                    // ◄── reutiliza, sin bucle propio
    return item != null && "Disponible".equals(item.getEstado()) ? item : null;
}
```

**Beneficio:** la responsabilidad de buscar un ítem queda en `Menu`, que es quien conoce sus ítems; se
elimina el código duplicado entre dos controladores y los servlets quedan más cortos.

---

## 5. Extract Class

**Origen:** `src/main/java/ec/edu/epn/polidinner/controlador/CrearMenuServlet.java`
(método `procesarAgregarItem()`, líneas 143-214, y los auxiliares `recortar()` e `inicioDeHoy()`, líneas 234-245)
**Clase nueva:** `src/main/java/ec/edu/epn/polidinner/controlador/FormularioItemMenu.java`

### Código antes de la refactorización

```java
/** Lee y valida los datos del ítem (mensaje 9 "Ingresa datos del ítem") antes de llamar a agregarItem. */
private void procesarAgregarItem(HttpServletRequest request, HttpServletResponse response, Menu menuHoy)
        throws ServletException, IOException {
    String tipo = "Producto".equals(request.getParameter("tipo")) ? "Producto" : "Plato";   // ◄── leer campos
    String nombre = recortar(request.getParameter("nombre"));                               // ◄──
    String descripcion = recortar(request.getParameter("descripcion"));                     // ◄──
    String textoPrecio = recortar(request.getParameter("precio"));                          // ◄──
    String textoStock = recortar(request.getParameter("stock"));                            // ◄──
    String textoFecha = recortar(request.getParameter("fechaCaducidad"));                   // ◄──

    Map<String, String> errores = new LinkedHashMap<>();                                    // ◄── validar nombre
    if (nombre.isEmpty()) {                                                                 // ◄──
        errores.put("nombre", "Ingrese el nombre del ítem.");                               // ◄──
    } else if (nombre.length() > 100) {                                                     // ◄──
        errores.put("nombre", "El nombre admite máximo 100 caracteres.");                   // ◄──
    }                                                                                       // ◄──
    if (descripcion.length() > 255) {                                                       // ◄── validar descripción
        errores.put("descripcion", "La descripción admite máximo 255 caracteres.");         // ◄──
    }                                                                                       // ◄──

    double precio = 0;                                                                      // ◄── validar precio
    try {                                                                                   // ◄──
        precio = Double.parseDouble(textoPrecio.replace(',', '.'));                         // ◄──
        if (!(precio > 0)) {                                                                // ◄──
            errores.put("precio", "El precio debe ser mayor que 0.");                       // ◄──
        }                                                                                   // ◄──
    } catch (NumberFormatException e) {                                                     // ◄──
        errores.put("precio", "Ingrese un precio numérico, por ejemplo 2.50.");             // ◄──
    }                                                                                       // ◄──

    String nombreStock = "Producto".equals(tipo) ? "stock" : "número de porciones";         // ◄── validar stock
    int stock = 0;                                                                          // ◄──
    try {                                                                                   // ◄──
        stock = Integer.parseInt(textoStock);                                               // ◄──
        if (stock < 1) {                                                                    // ◄──
            errores.put("stock", "El " + nombreStock + " debe ser al menos 1.");            // ◄──
        } else if (stock > 10000) {                                                         // ◄──
            errores.put("stock", "El " + nombreStock + " admite máximo 10000.");            // ◄──
        }                                                                                   // ◄──
    } catch (NumberFormatException e) {                                                     // ◄──
        errores.put("stock", "Ingrese el " + nombreStock + " como número entero.");         // ◄──
    }                                                                                       // ◄──
    Date fechaCaducidad = null;                                                             // ◄── validar fecha
    if ("Producto".equals(tipo)) {                                                          // ◄──
        try {                                                                               // ◄──
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");                  // ◄──
            formato.setLenient(false);                                                      // ◄──
            fechaCaducidad = formato.parse(textoFecha);                                     // ◄──
            if (fechaCaducidad.before(inicioDeHoy())) {                                     // ◄──
                errores.put("fechaCaducidad", "La fecha de caducidad no puede ser anterior a hoy."); // ◄──
            }                                                                               // ◄──
        } catch (ParseException e) {                                                        // ◄──
            errores.put("fechaCaducidad", "Seleccione la fecha de caducidad del producto."); // ◄──
        }                                                                                   // ◄──
    }                                                                                       // ◄──

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

private static String recortar(String valor) {               // ◄── solo lo usa la lectura del formulario
    return valor == null ? "" : valor.trim();
}

private static Date inicioDeHoy() {                          // ◄── solo lo usa la validación de la fecha
    Calendar hoy = Calendar.getInstance();
    hoy.set(Calendar.HOUR_OF_DAY, 0);
    hoy.set(Calendar.MINUTE, 0);
    hoy.set(Calendar.SECOND, 0);
    hoy.set(Calendar.MILLISECOND, 0);
    return hoy.getTime();
}
```

### Pasos de la refactorización

1. **Detectar que la clase tiene dos responsabilidades.** `CrearMenuServlet` controla la secuencia
   "Crear Menú" (iniciar, agregar, publicar) y, además, lee y valida los seis campos del formulario del ítem.
   Todo el código marcado con `// ◄──` pertenece a esta segunda responsabilidad.
2. **Crear la clase nueva `FormularioItemMenu`** en el paquete `controlador`, como soporte técnico de la
   frontera "Interfaz Crear Menú" (mensaje "ingresa datos del ítem" del diagrama de secuencia).
3. **Mover los datos:** las variables locales `tipo`, `nombre`, `descripcion`, `precio`, `stock`,
   `fechaCaducidad` y el mapa `errores` se convierten en atributos de `FormularioItemMenu`.
4. **Mover el comportamiento:** cada bloque de validación pasa a un método privado de la clase nueva
   (`validarNombre()`, `validarDescripcion()`, `leerPrecio()`, `leerStock()`, `leerFechaCaducidad()`),
   junto con los auxiliares `recortar()` e `inicioDeHoy()`. Los mensajes de error y las reglas no cambian.
5. **Definir una interfaz pública mínima:** un constructor que recibe los textos del formulario (permite
   probar la clase sin un servidor), la fábrica `desde(request)`, `esValido()`, `getErrores()` y los getters.
6. **Hacer que el servlet delegue en la clase nueva** y borrar de `CrearMenuServlet` el código movido.
   Los métodos trazados al diagrama de secuencia (`agregarItem()` y `publicar()`) no cambian.
7. **Ejecutar las pruebas** y agregar `FormularioItemMenuTest`: precio `"2,50"` válido; precio 0, stock 0,
   nombre vacío y fecha pasada producen los mismos mensajes de error que antes; un Plato no exige fecha.

### Código después de la refactorización

```java
// CrearMenuServlet.java – solo controla el flujo del caso de uso
/** Recibe los datos del ítem (mensaje "ingresa datos del ítem") y los valida con FormularioItemMenu. */
private void procesarAgregarItem(HttpServletRequest request, HttpServletResponse response, Menu menuHoy)
        throws ServletException, IOException {
    FormularioItemMenu formulario = FormularioItemMenu.desde(request);       // ◄── delega en la clase extraída
    if (!formulario.esValido()) {
        request.setAttribute("errores", formulario.getErrores());
        request.setAttribute(MENU_HOY, menuHoy);
        request.getRequestDispatcher(VISTA).forward(request, response);
        return;
    }

    HttpSession sesion = request.getSession();
    if (agregarItem(menuHoy, formulario.getTipo(), formulario.getNombre(), formulario.getDescripcion(),
                    formulario.getPrecio(), formulario.getStock(), formulario.getFechaCaducidad())) {
        sesion.setAttribute("flashExito", formulario.getTipo() + " \"" + formulario.getNombre() + "\" agregado al menú.");
    } else {
        sesion.setAttribute("flashError", "No se pudo agregar el ítem: el menú está cerrado.");
    }
    redirigir(request, response);
}
// (recortar() e inicioDeHoy() se eliminan del servlet)
```

```java
// FormularioItemMenu.java – clase extraída
package ec.edu.epn.polidinner.controlador;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Soporte técnico NO modelado – Refactorización: Extract Class (desde CrearMenuServlet).
 * Frontera "Interfaz Crear Menú": lee y valida los datos del ítem que ingresa el personal
 * (nombre, descripción, precio, porciones o stock y, para un Producto, la fecha de caducidad).
 */
public class FormularioItemMenu {

    private static final String PRODUCTO = "Producto";

    private final String tipo;
    private final String nombre;
    private final String descripcion;
    private double precio;
    private int stock;
    private Date fechaCaducidad;
    private final Map<String, String> errores = new LinkedHashMap<>();

    public FormularioItemMenu(String tipo, String nombre, String descripcion,
                              String textoPrecio, String textoStock, String textoFecha) {
        this.tipo = PRODUCTO.equals(tipo) ? PRODUCTO : "Plato";
        this.nombre = recortar(nombre);
        this.descripcion = recortar(descripcion);
        validarNombre();
        validarDescripcion();
        leerPrecio(recortar(textoPrecio));
        leerStock(recortar(textoStock));
        if (esProducto()) {
            leerFechaCaducidad(recortar(textoFecha));
        }
    }

    /** Crea el formulario con los parámetros de la petición HTTP. */
    public static FormularioItemMenu desde(HttpServletRequest request) {
        return new FormularioItemMenu(request.getParameter("tipo"), request.getParameter("nombre"),
                request.getParameter("descripcion"), request.getParameter("precio"),
                request.getParameter("stock"), request.getParameter("fechaCaducidad"));
    }

    public boolean esValido() {
        return errores.isEmpty();
    }

    private void validarNombre() {
        if (nombre.isEmpty()) {
            errores.put("nombre", "Ingrese el nombre del ítem.");
        } else if (nombre.length() > 100) {
            errores.put("nombre", "El nombre admite máximo 100 caracteres.");
        }
    }

    private void validarDescripcion() {
        if (descripcion.length() > 255) {
            errores.put("descripcion", "La descripción admite máximo 255 caracteres.");
        }
    }

    private void leerPrecio(String texto) {
        try {
            precio = Double.parseDouble(texto.replace(',', '.'));
            if (!(precio > 0)) {
                errores.put("precio", "El precio debe ser mayor que 0.");
            }
        } catch (NumberFormatException e) {
            errores.put("precio", "Ingrese un precio numérico, por ejemplo 2.50.");
        }
    }

    /** Plato: porciones disponibles; Producto: unidades en stock (ItemMenu.stockActual). */
    private void leerStock(String texto) {
        try {
            stock = Integer.parseInt(texto);
            if (stock < 1) {
                errores.put("stock", "El " + nombreStock() + " debe ser al menos 1.");
            } else if (stock > 10000) {
                errores.put("stock", "El " + nombreStock() + " admite máximo 10000.");
            }
        } catch (NumberFormatException e) {
            errores.put("stock", "Ingrese el " + nombreStock() + " como número entero.");
        }
    }

    private void leerFechaCaducidad(String texto) {
        try {
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
            formato.setLenient(false);
            fechaCaducidad = formato.parse(texto);
            if (fechaCaducidad.before(inicioDeHoy())) {
                errores.put("fechaCaducidad", "La fecha de caducidad no puede ser anterior a hoy.");
            }
        } catch (ParseException e) {
            errores.put("fechaCaducidad", "Seleccione la fecha de caducidad del producto.");
        }
    }

    private boolean esProducto() {
        return PRODUCTO.equals(tipo);
    }

    private String nombreStock() {
        return esProducto() ? "stock" : "número de porciones";
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

    public Map<String, String> getErrores() { return errores; }
    public String getTipo() { return tipo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public Date getFechaCaducidad() { return fechaCaducidad; }
}
```

**Beneficio:** cada clase tiene una sola responsabilidad. `CrearMenuServlet` pasa de unas 70 líneas en
`procesarAgregarItem()` a unas 15 y solo controla el caso de uso; las reglas de validación del formulario
quedan en una clase pequeña que se puede probar con JUnit sin levantar Tomcat.

---

## Cómo se verifica que el comportamiento no cambia

1. **Pruebas existentes** (JUnit 5): `PedidoTest`, `ComprobanteTest`, `CrearMenuTest`, `RetirarItemTest`,
   `ItemMenuStockTest`, `PersonalComedorEntregaTest`, `PolicuentaTest`, `ClienteUniversitarioTest` y
   `HabilitarEntregaServletTest`. Hoy pasan las 53 pruebas y deben seguir pasando después de cada refactorización.
2. **Pruebas nuevas:** `Menu.buscarItem()`, `Comprobante.esDeHoy()` con la fecha de ayer y `FormularioItemMenuTest`.
3. **Comando:**
   ```
   ./mvnw clean test
   ```
4. **Prueba manual** en Tomcat (`http://localhost:8080/polidinner/`): crear y publicar un menú (con datos
   válidos e inválidos), retirar un ítem, realizar y pagar un pedido como cliente y validar el código
   `PD-xxxx` como personal del comedor.

## Convenciones respetadas

- No se renombra ningún método ni clase de los diagramas UML (`Pedido.confirmarPago()`,
  `Pedido.agregarItem()`, `Comprobante.esDeHoy()`, `Menu.obtenerItems()`, y `agregarItem()` / `publicar()`
  de `CrearMenuServlet`).
- Cada método o clase nueva (`hayStockDeTodosLosItems()`, `reducirStockDeItems()`, `cantidadEnPedido()`,
  `Menu.buscarItem()`, `FormularioItemMenu`) lleva Javadoc que la identifica como **soporte técnico no
  modelado** e indica la refactorización que la originó.
- Las refactorizaciones no agregan accesos a la base de datos dentro de las entidades: los servlets siguen
  cargando los datos con los DAOs.
