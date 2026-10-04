# Trazabilidad del Incremento 2: diagramas ↔ código

Rutas relativas a `src/main/java/ec/edu/epn/polidinner/` (Java) y `src/main/webapp/` (JSP).
Cada clase y método citado lleva además un Javadoc "Trazabilidad: ..." con la misma referencia.

Diagramas de secuencia usados (corregidos y numerados en Mermaid, fuente `.mmd` junto a cada PNG):
- `Diagrama de secuencia CU03 parte 1 seleccion y carrito.png` (mensajes 1-31)
- `Diagrama de secuencia CU03 parte 2 pago.png` (mensajes 32-51)
- `Diagrama de secuencia CU04 habilitar entrega.png` (mensajes 1-15; no existía en el informe)

El PNG original `diagrama de secuencia iteracion 2.png` queda como versión previa (ver sección 7).

## 1. Diagrama de clases del incremento 2 (Fig. 15)

| Elemento del diagrama | Código |
|---|---|
| **ItemMenu** (abstracta): -stockActual : int | `modelo/ItemMenu.java:44` |
| +hayStock(cantidad: int): boolean | `ItemMenu.java:81` |
| +reducirStock(cantidad: int) | `ItemMenu.java:90` |
| +cambiarEstado() | `ItemMenu.java:69` |
| **Menu**: +obtenerItems() | `modelo/Menu.java:108` |
| Asociación "contiene" (1 Menu ◆ 0..* ItemMenu) | `Menu.java:52` |
| Asociación "administra" (1 PersonalComedor - 0..* Menu) | `Menu.java:44` |
| **ClienteUniversitario** | `modelo/ClienteUniversitario.java:19` |
| -cedula / -nombres / -apellidos | `ClienteUniversitario.java:24` · `:26` · `:28` |
| +crearPedido(): Pedido | `ClienteUniversitario.java:58` |
| +consultarSaldo(): double | `ClienteUniversitario.java:66` |
| Asociación "posee" (1 - 1 Policuenta) | `ClienteUniversitario.java:37` |
| **Pedido** | `modelo/Pedido.java:34` |
| -id / -fecha / -estado / -total | `Pedido.java:40` · `:43` · `:45` · `:47` |
| +agregarItem(item: ItemMenu, cantidad: int) | `Pedido.java:97` |
| +calcularTotal(): double | `Pedido.java:124` |
| +confirmarPago(): boolean | `Pedido.java:140` |
| +generarCodigoEntrega(): Comprobante | `Pedido.java:167` |
| Asociación "realiza" (1 ClienteUniversitario - 0..* Pedido) | `Pedido.java:50` |
| Asociación "utiliza para pagar" (0..* Pedido - 1 Policuenta) | `Pedido.java:55` |
| Asociación "contiene" (1 Pedido ◆ 1..* DetallePedido) | `Pedido.java:60` |
| Asociación "genera" (1 Pedido - 1 Comprobante) | `Pedido.java:66` (extremo inverso) y `Comprobante.java:45` (dueño) |
| Asociación "entrega" (1 PersonalComedor - 0..* Pedido) | `Pedido.java:70` |
| **DetallePedido** | `modelo/DetallePedido.java:20` |
| -cantidad / -precioUnitario | `DetallePedido.java:29` · `:31` |
| +calcularSubtotal(): double | `DetallePedido.java:54` |
| Asociación "corresponde a" (* DetallePedido - 1 ItemMenu) | `DetallePedido.java:34` |
| **Policuenta** | `modelo/Policuenta.java:17` |
| -numero / -saldo | `Policuenta.java:22` · `:24` |
| +consultarSaldo(): double | `Policuenta.java:45` |
| +debitar(monto: double): boolean | `Policuenta.java:54` |
| **Comprobante** | `modelo/Comprobante.java:27` |
| -idComprobante / -codigo / -fechaGeneracion / -usado | `Comprobante.java:34` · `:37` · `:40` · `:42` |
| +validarCodigoEntrega(): boolean | `Comprobante.java:82` |
| +marcarCodigoUsado() | `Comprobante.java:90` |
| Asociación "valida" (1 PersonalComedor - 0..* Comprobante) | `Comprobante.java:50` |
| **PersonalComedor**: +validarCodigo(codigo, comprobante): boolean | `modelo/PersonalComedor.java:83` |
| +entregarPedido(pedido: Pedido) | `PersonalComedor.java:100` |
| **Producto**: +getStock() (devuelve stockActual) | `modelo/Producto.java:40` |

## 2. Diagrama de robustez del incremento 2

| Elemento | Código |
|---|---|
| Actor: Cliente universitario | Sesión `cliente` (`controlador/LoginClienteServlet.java`, `FiltroCliente.java`) |
| Actor: Personal del comedor | Sesión `personal` (`controlador/LoginPersonalServlet.java`, `FiltroPersonal.java`) |
| Frontera: Interfaz de pedidos de comida | `vistas/cliente/menuDia.jsp`, `vistas/cliente/carrito.jsp`, `vistas/cliente/login.jsp` |
| Frontera: Interfaz del sistema de comida | `vistas/personal/habilitarEntrega.jsp`, `vistas/personal/panel.jsp` |
| Frontera: Salida | `vistas/comun/salida.jsp` (+ fragmentos `vistas/cliente/comprobante.jsp` y `vistas/comun/detallePedido.jsp`) |
| Controlador: Realizar pedido de comida | `controlador/RealizarPedidoServlet.java` (`/cliente/pedido`) |
| Controlador: Habilitar entrega de comida | `controlador/HabilitarEntregaServlet.java` (`/personal/entrega`) |
| Entidades: Menú, Plato, Producto, Pedido, Comprobante, Cliente universitario, Personal del comedor | Clases `@Entity` de `modelo/` (además `DetallePedido` y `Policuenta` del diagrama de clases) |

## 3. Diagrama de secuencia CU 03, parte 1: selección y carrito

| # | Mensaje | Código |
|---|---|---|
| 1 | Solicita realizar pedido | `index.jsp` → `vistas/cliente/login.jsp` → `/cliente/pedido` |
| 2 | iniciar() | `RealizarPedidoServlet.java:51` (`doGet`) → `iniciar()` `:93` |
| 3 | crearPedido() | `RealizarPedidoServlet.java:95` → `ClienteUniversitario.java:58` |
| 4 | «create» Pedido(fecha=hoy, estado="Creado", cliente) | `Pedido.java:82` |
| 5-6 | pedido | retorno de `crearPedido()` |
| 7-8 | buscarMenuDelDia() / menuHoy | `RealizarPedidoServlet.java:106` → `persistencia/MenuDAO.java:26` |
| 9-10 | obtenerItems() / lista de ItemMenu | `RealizarPedidoServlet.java:109` → `Menu.java:108` |
| 11 | muestra ítems "Disponible" con stock | `RealizarPedidoServlet.java:110` (filtro `hayStock(1)`) → `menuDia.jsp:45-83` |
| 12 | Elige ítem y cantidad | `menuDia.jsp:63` (selector de cantidad y botón Agregar) |
| 13 | agregarItem(idItem, cantidad) | `RealizarPedidoServlet.java:68` (`doPost`, `accion=agregar`) → `agregarItem()` `:124` |
| 14 | agregarItem(item, cantidad) | `RealizarPedidoServlet.java:136` → `Pedido.java:97` |
| 15-16 | hayStock(cantidad) / true | `Pedido.java:97` (dentro de `agregarItem`) → `ItemMenu.java:81` |
| 17-18 | «create» DetallePedido(cantidad, precioUnitario) | `DetallePedido.java:45` (creado en `agregarItem`) |
| 19 | confirmación de ítem agregado | retorno de `Pedido.agregarItem()` |
| 20 | "Ítem agregado" y carrito actualizado | `RealizarPedidoServlet.java:86` (redirect) → `mensajes.jsp` + barra `menuDia.jsp:95` |
| 21 | Revisa el carrito | `menuDia.jsp:95` (botón "Ver pedido y pagar") |
| 22 | calcular() | `RealizarPedidoServlet.java:51` (`vista=carrito`) → `calcular()` `:163` |
| 23 | calcularTotal() | `RealizarPedidoServlet.java:166` → `Pedido.java:124` |
| 24-25 | loop calcularSubtotal() / subtotal | `Pedido.java:124` → `DetallePedido.java:54` |
| 26 | total del pedido | retorno de `calcularTotal()` |
| 27 | consultarSaldo() | `RealizarPedidoServlet.java:169` → `ClienteUniversitario.java:66` |
| 28-29 | consultarSaldo() / saldo (Policuenta) | `ClienteUniversitario.java:66` → `Policuenta.java:45` |
| 30 | saldo | retorno de `ClienteUniversitario.consultarSaldo()` |
| 31 | muestra total y saldo de PoliCuenta | `RealizarPedidoServlet.java:56` (forward) → `carrito.jsp:75-91` |

## 4. Diagrama de secuencia CU 03, parte 2: pago

| # | Mensaje | Código |
|---|---|---|
| 32 | Confirma y paga | `carrito.jsp:102` (botón + diálogo de confirmación) |
| 33 | confirmar() | `RealizarPedidoServlet.java:68` (`accion=confirmar`) → `confirmar()` `:185` |
| — | Inicia la transacción | `Transaccion.iniciar()` (`persistencia/Transaccion.java:24`) |
| 34 | adjuntar(pedido) | `RealizarPedidoServlet.java:192` → `persistencia/PedidoDAO.java:26` |
| 35 | confirmarPago() | `RealizarPedidoServlet.java:193` → `Pedido.java:140` |
| 36 | calcularTotal() | dentro de `Pedido.confirmarPago()` → `Pedido.java:124` |
| 37-38 | loop hayStock(cantidad) / true | dentro de `confirmarPago()` → `ItemMenu.java:81` |
| 39-40 | debitar(monto) / true (saldo descontado) | dentro de `confirmarPago()` → `Policuenta.java:54` |
| 41 | loop reducirStock(cantidad) | dentro de `confirmarPago()` → `ItemMenu.java:90` |
| 42 | true (estado "Pagado") | retorno de `confirmarPago()` |
| 43 | guardar(pedido) | `RealizarPedidoServlet.java:198` → `GenericDAO.java:65` |
| 44 | generarCodigoEntrega() | `RealizarPedidoServlet.java:199` → `Pedido.java:167` |
| 45-46 | «create» Comprobante(codigo, fechaGeneracion, pedido) | `Comprobante.java:68` |
| 47 | comprobante | retorno de `generarCodigoEntrega()` |
| 48 | guardar(comprobante) | `RealizarPedidoServlet.java:200` → `GenericDAO.java:65` |
| — | commit | `RealizarPedidoServlet.java:201` → `Transaccion.java:33` |
| 49 | muestra comprobante con el código de retiro | `mostrarComprobante()` `RealizarPedidoServlet.java:220` → `salida.jsp:26` → `comprobante.jsp:9` |
| 50 | alt [rechazo] "Saldo insuficiente en PoliCuenta" o motivo (rollback) | `RealizarPedidoServlet.java:195` (rollback al cerrar `Transaccion`, `Transaccion.java:39`) |
| 51 | Muestra comprobante o mensaje | `salida.jsp:23` / `mensajes.jsp` |

## 5. Diagrama de secuencia CU 04: Habilitar entrega de comida

| # | Mensaje | Código |
|---|---|---|
| 1 | Ingresa el código del comprobante | `habilitarEntrega.jsp:20` (formulario) |
| 2 | habilitarEntrega(codigo) | `HabilitarEntregaServlet.java:54` (`doPost`) → `habilitarEntrega()` `:77` |
| 3-4 | buscarPorCodigo(codigo) / comprobante | `HabilitarEntregaServlet.java:79` → `persistencia/ComprobanteDAO.java:24` |
| 5 | validarCodigo(codigo, comprobante) | `HabilitarEntregaServlet.java:85` → `PersonalComedor.java:83` |
| 6-7 | validarCodigoEntrega() / true | `PersonalComedor.java:83` → `Comprobante.java:82` |
| 8 | true | retorno de `validarCodigo()` |
| 9 | entregarPedido(pedido) (estado "Entregado") | `HabilitarEntregaServlet.java:88` → `PersonalComedor.java:100` |
| 10 | marcarCodigoUsado() | `HabilitarEntregaServlet.java:89` → `Comprobante.java:90` |
| 11 | actualizar(comprobante) | `HabilitarEntregaServlet.java:90` → `GenericDAO.java:71` |
| — | commit | `HabilitarEntregaServlet.java:91` → `Transaccion.java:33` |
| 12 | "Pedido autorizado para despacho" y detalle | `mostrarSalida()` `HabilitarEntregaServlet.java:108` → `salida.jsp:29` |
| 13 | alt [usado / de otro día / no pagado] motivo del rechazo | `HabilitarEntregaServlet.java:86` (`motivoRechazo`) |
| 14 | alt [inexistente] "El código no existe" | `HabilitarEntregaServlet.java:82` |
| 15 | Muestra el resultado | `HabilitarEntregaServlet.java:117` (forward) → `salida.jsp:23` |

## 6. Diagramas de actividades ↔ diagramas de secuencia

Las notas amarillas de los diagramas de secuencia copian el texto exacto de las actividades.

| Actividad (diagrama de actividades) | Secuencia | Código |
|---|---|---|
| CU 03: "Acercarse al despacho del comedor", "Revisar platos en la cartelera" | Nota previa al msj 1 | `index.jsp`, `menuDia.jsp` |
| CU 03: "Elegir platos o productos", "Solicitar pedido al personal" | Loop msj 12-20 | `menuDia.jsp` → `agregarItem()` |
| CU 03: "Comprobar disponibilidad del plato o producto" | Msj 15-16 | `ItemMenu.hayStock()` |
| CU 03: [No] "Preguntar si desea elegir otro plato o producto" | Nota tras el loop | Mensaje de error de `RealizarPedidoServlet.agregarItem()` |
| CU 03: "Informar valor a pagar" | Msj 31 | `carrito.jsp` |
| CU 03: "Pagar el pedido", "Recibir el dinero" | Msj 32-40 | `confirmar()` → `Policuenta.debitar()` |
| CU 03: [No] fin (el dinero no alcanza) | alt msj 50 | "Saldo insuficiente en PoliCuenta" + rollback |
| CU 03: "Anotar pedido" | Msj 43 | `PedidoDAO.guardar(pedido)` |
| CU 03: "Emitir Comprobante" / "Recibir comprobante" | Msj 44-51 | `generarCodigoEntrega()` → `salida.jsp` + `comprobante.jsp` |
| CU 04: "Solicitar comprobante al cliente", "Entregar/Recibir el comprobante" | Nota previa al msj 1 | Actividad presencial; el código llega en `habilitarEntrega.jsp` |
| CU 04: "Comprobar veracidad del comprobante" | Msj 5-8 | `validarCodigo()` → `validarCodigoEntrega()` |
| CU 04: [Sí] "Autorizar despacho del pedido" | Msj 9-12 | `entregarPedido()`, `marcarCodigoUsado()` |
| CU 04: [No] fin | alt msj 13-14 | Motivo del rechazo en `salida.jsp` |

## 7. Correcciones al modelo y evolución entre incrementos (para el informe)

| Cambio | Motivo |
|---|---|
| Secuencia CU 03: `:CodigoEntrega` → `:Comprobante` | La clase modelada en la Fig. 15 es `Comprobante`; no existe `CodigoEntrega`. **Corregir también** en la Fig. 15 el retorno `generarCodigoEntrega(): CodigoEntrega` → `Comprobante` |
| Secuencia CU 03: `confirmarPago()` devuelve `boolean` y el controlador llama luego a `generarCodigoEntrega()` | En el diagrama original `confirmarPago()` devolvía la instancia del comprobante, contradiciendo la Fig. 15 (`confirmarPago(): boolean`) y omitiendo `generarCodigoEntrega()` |
| Secuencia CU 03: se agregan frontera, controlador, DAOs, Salida, `hayStock`/`reducirStock` y numeración | Para que cada mensaje tenga su línea de código y siga el diagrama de robustez |
| `validarCodigo(codigo: String)` → `validarCodigo(codigo: String, comprobante: Comprobante)` | Una entidad no consulta la base: el controlador busca el Comprobante con `ComprobanteDAO`. **Actualizar la Fig. 15** |
| `Menu.obtenerItem()` (Fig. 7) → `obtenerItems()` (Fig. 15) | Refinamiento del modelo en el incremento 2 (Proceso Unificado); se actualizó también el diagrama del CU 02 |
| `Producto.stock` → `ItemMenu.stockActual` | Evolución de la Fig. 15; `Producto.getStock()` devuelve `stockActual`. Migración: `src/main/resources/migracion_incremento2.sql` |
| `Plato(nombre, precio, estado, descripcion)` → `Plato(..., porciones)` | Con `stockActual` en ItemMenu, el plato también registra porciones (evolución del mensaje 11 del CU 01) |
| `reducirStock()` deja el ítem "Agotado" al llegar a 0 | Extensión documentada que reutiliza el estado del CU 02 |
| PNG de la Fig. 15 recortado (no se ven Plato y Producto) | Volver a exportar desde draw.io |

## 8. Pruebas JUnit ↔ mensajes

| Prueba | Cubre |
|---|---|
| `PolicuentaTest` (4) | `debitar` con saldo suficiente, exacto, insuficiente y monto inválido (CU 03 msj 39-40) |
| `ItemMenuStockTest` (5) | `hayStock`, `reducirStock`, paso a "Agotado", ítem retirado sin stock (msj 15-16, 37-38, 41) |
| `PedidoTest` (12) | `agregarItem` con y sin stock, suma de cantidad, plato retirado, `calcularSubtotal`, **total $4.25**, `confirmarPago` (saldo **$5.75**, stock −1), saldo insuficiente, retiro durante el pago, `generarCodigoEntrega` → **PD-0457**, `quitarItem` |
| `ClienteUniversitarioTest` (2) | `crearPedido` (msj 3-6) y `consultarSaldo` (msj 27-30) |
| `ComprobanteTest` (4) | `validarCodigoEntrega` válido, usado (**PD-0457 por segunda vez**), de ayer y no pagado; `marcarCodigoUsado` (CU 04 msj 6-7, 10) |
| `PersonalComedorEntregaTest` (5) | `validarCodigo` correcto, incorrecto y usado; `entregarPedido` (CU 04 msj 5-9) |
| `HabilitarEntregaServletTest` (2) | Formato del código ingresado (CU 04 msj 1) |

## 9. Elementos técnicos no modelados (justificación)

| Elemento | Motivo |
|---|---|
| `ClienteUniversitario.clave`, `verificarClave()`, `LoginClienteServlet`, `LogoutClienteServlet`, `FiltroCliente` | Precondición "login" del CU 03 |
| `@Version` en `Policuenta` y `Comprobante` (además del de `ItemMenu`) | Concurrencia: dos pagos con la misma PoliCuenta o dos validaciones del mismo código; el segundo recibe `OptimisticLockException` y se hace rollback |
| `persistencia/Transaccion` y sobrecargas `guardar/actualizar/buscarPorId(..., Transaccion)` en `GenericDAO` | El pago (CU 03) y la entrega (CU 04) se guardan completos o no se guarda nada |
| `PedidoDAO.adjuntar()`, `DetallePedido.reemplazarItem()`, `Pedido.reemplazarPolicuenta()` | El pedido se arma en la sesión; antes de pagar se usan el stock y el saldo actuales de la base |
| `Pedido.quitarItem()` (acción "quitar") | Corregir el carrito antes de pagar |
| `DetallePedido.id`, `Comprobante.idComprobante` como UUID | Claves primarias técnicas (`idComprobante: String` sí está en la Fig. 15) |
| `Comprobante.esDeHoy()` y `motivoRechazo()` del controlador | Mensajes distintos para código usado, de otro día o pedido no pagado |
| `Pedido.registrarEntrega()`, `Comprobante.registrarValidacion()` (visibilidad de paquete) | Implementan las asociaciones "entrega" y "valida" desde los métodos del diagrama |
| `index.jsp` (portal) | Lleva a cada actor a su frontera |
