# Trazabilidad del Incremento 1: diagramas ↔ código

Rutas relativas a `src/main/java/ec/edu/epn/polidinner/` (Java) y `src/main/webapp/` (JSP).
Cada clase y método citado lleva además un Javadoc "Trazabilidad: ..." con la misma referencia.

## 1. Diagrama de clases (Fig. 7)

Las líneas corresponden al código actual (incremento 2). Los cambios que trajo la Fig. 15 están en
`Diagramas iteracion 2/Trazabilidad incremento 2.md`, sección 7.

| Elemento del diagrama | Código |
|---|---|
| **Menu** | `modelo/Menu.java:30` (`@Entity`) |
| -Fecha : Date / -id : int / -Estado : String | `Menu.java:39` `fecha` · `:36` `id` · `:41` `estado` |
| +publicarParaVenta() | `Menu.java:75` |
| +cerrarParaVenta() | `Menu.java:87` |
| +agregarItem() | `Menu.java:95` |
| +obtenerItem() (renombrado `obtenerItems()` en la Fig. 15) | `Menu.java:108` |
| Asociación "contiene" 1 Menu ◇— * ItemMenu | `Menu.java:52` `@OneToMany(cascade = ALL)` + `@JoinColumn(name = "menu_id")` |
| Asociación "administra" * Menu — 1 PersonalComedor | `Menu.java:44` `@ManyToOne PersonalComedor personal` |
| **«ItemMenu» Abstract** | `modelo/ItemMenu.java:23` (`abstract`, `@Inheritance(JOINED)`) |
| -nombre / -precio / -id / -estado / -descripcion | `ItemMenu.java:31` · `:33` · `:29` · `:35` · `:37` |
| +cambiarEstado() | `ItemMenu.java:69` |
| **Plato** "es un" ItemMenu | `modelo/Plato.java:10` (`extends ItemMenu`) |
| +getId() | `Plato.java:31` |
| **Producto** "es un" ItemMenu | `modelo/Producto.java:15` (`extends ItemMenu`) |
| -stock : int / -fechaCaducidad : Date | `stock` pasó a `ItemMenu.stockActual` (`ItemMenu.java:44`, Fig. 15) · `Producto.java:20` |
| +getStock() | `Producto.java:40` |
| **PersonalComedor** | `modelo/PersonalComedor.java:18` |
| -nombres / -apellidos / -rol / -cedula | `PersonalComedor.java:25` · `:27` · `:29` · `:23` (`@Id`) |
| +crearMenuDiario() | `PersonalComedor.java:54` |
| +eliminarPlato() | `PersonalComedor.java:63` |
| +eliminarProducto() | `PersonalComedor.java:72` |

## 2. Diagrama de robustez

| Elemento | Código |
|---|---|
| Actor: Personal del comedor | Sesión `personal` (`controlador/LoginPersonalServlet.java`) |
| Frontera: Interfaz del sistema de comida | `vistas/personal/panel.jsp`, `crearMenu.jsp`, `retirarItem.jsp` y la plantilla `vistas/comun/*` |
| Controlador: Crear menú | `controlador/CrearMenuServlet.java` (`/personal/crear-menu`) |
| Controlador: Retirar plato y/o producto del menú | `controlador/RetirarItemMenuServlet.java` (`/personal/retirar`) |
| Entidades: Plato, Menú, Producto, Personal del comedor | `modelo/Plato.java`, `Menu.java`, `Producto.java`, `PersonalComedor.java` |

## 3. Diagrama de secuencia CU 01 Crear Menú (Fig. 8)

| # | Mensaje | Código |
|---|---|---|
| 1 | Inicia proceso de creación de menú | `crearMenu.jsp:22-29` (botón `accion=iniciar`) |
| 2 | iniciarCreacion() | `CrearMenuServlet.java:57` → `iniciarCreacion()` `:93` |
| 3 | crearMenuDiario() | `CrearMenuServlet.java:98` → `PersonalComedor.java:54` |
| 4 | «create» Menu(fecha=hoy, estado="Borrador", personal) | `Menu.java:64` (constructor), invocado en `PersonalComedor.java:55` |
| 5-6 | menuHoy | retorno de `crearMenuDiario()` |
| 7 | guardar(menuHoy) | `CrearMenuServlet.java:99` → `GenericDAO.java:24` |
| 8 | muestra formulario de ítems | `CrearMenuServlet.java:45` (`doGet`) → `crearMenu.jsp:40` |
| 9 | Ingresa datos del ítem | `crearMenu.jsp:44` y siguientes (formulario; desde el incremento 2 pide también porciones del plato) |
| 10 | agregarItem(tipo, nombre, descripcion, precio, stock, fechaCaducidad) | `CrearMenuServlet.java:72` → `procesarAgregarItem()` `:138` → `agregarItem()` `:109` |
| 11-12 | alt [Plato] «create» Plato(...) (+ porciones, Fig. 15) | `CrearMenuServlet.java:116` → `Plato.java:22` |
| 13-14 | alt [Producto] «create» Producto(...) | `CrearMenuServlet.java:113` → `Producto.java:30` |
| 15-16 | agregarItem(nuevoItem) / true | `CrearMenuServlet.java:118` → `Menu.java:95` |
| 17 | actualizar(menuHoy) | `CrearMenuServlet.java:121` → `GenericDAO.java:32` |
| 18 | lista de ítems agregados | `crearMenu.jsp:103-146` (`<c:forEach>` sobre `obtenerItems()`) |
| 19 | Solicita publicar menú | `crearMenu.jsp:153` (`accion=publicar`) |
| 20 | publicar() | `CrearMenuServlet.java:74` → `publicar()` `:129` |
| 21-22 | publicarParaVenta() / true | `CrearMenuServlet.java:130` → `Menu.java:75` |
| 23 | actualizar(menuHoy) | `CrearMenuServlet.java:133` |
| 24-25 | "Menú publicado exitosamente" / Muestra mensaje | `CrearMenuServlet.java:77` → `vistas/comun/mensajes.jsp` |

## 4. Diagrama de secuencia CU 02 Retirar plato y/o producto

Diagrama: `Diagrama de secuencia CU02 retirar item.png` (fuente Mermaid: `.mmd`).

| # | Mensaje | Código |
|---|---|---|
| 1 | Solicita retirar plato/producto | `panel.jsp` (opción "Retirar plato y/o producto" → `/personal/retirar`) |
| 2 | iniciarRetiro() | `RetirarItemMenuServlet.java:43` (`doGet`) → `iniciarRetiro()` `:54` |
| 3-4 | buscarMenuDelDia() / menuHoy | `RetirarItemMenuServlet.java:55` → `persistencia/MenuDAO.java:26` |
| 5-6 | obtenerItems() / lista de ítems | `RetirarItemMenuServlet.java:59` → `Menu.java:108` |
| 7 | muestra ítems con estado "Disponible" | `RetirarItemMenuServlet.java:60` (filtro) → `retirarItem.jsp:34-95` |
| 8 | Selecciona ítem y confirma el retiro | `retirarItem.jsp:82` (botón Retirar) + diálogo de confirmación `retirarItem.jsp:107` |
| 9 | retirarItem(idItem) | `RetirarItemMenuServlet.java:73` (`doPost`, `accion=retirar`) → `retirarItem()` `:103` |
| 10-11 | buscarPorId(idItem) / item | `RetirarItemMenuServlet.java:110` → `GenericDAO.java:36` |
| 12 | alt [Plato] eliminarPlato(plato) | `RetirarItemMenuServlet.java:127` → `PersonalComedor.java:63` |
| 13 | cambiarEstado("Agotado") | `PersonalComedor.java:64` → `ItemMenu.java:69` |
| 14 | alt [Producto] eliminarProducto(producto) | `RetirarItemMenuServlet.java:129` → `PersonalComedor.java:72` |
| 15 | cambiarEstado("Agotado") | `PersonalComedor.java:73` → `ItemMenu.java:69` |
| 16 | actualizar(item) | `RetirarItemMenuServlet.java:133` → `GenericDAO.java:32` |
| 17 | alt [actualización correcta] "Ítem retirado del menú" | `RetirarItemMenuServlet.java:135` |
| 18 | alt [versión desactualizada u OptimisticLockException] "El ítem está siendo comprado, intente de nuevo" | `RetirarItemMenuServlet.java:122` (versión de la vista desactualizada) y `:141` (`OptimisticLockException` / `StaleStateException`) |
| 19 | Muestra mensaje y la lista sin el ítem retirado | `RetirarItemMenuServlet.java:90` (redirect) → `mensajes.jsp` + `retirarItem.jsp` |

## 5. Pruebas JUnit ↔ mensajes

| Prueba | Cubre |
|---|---|
| `CrearMenuTest` (9 pruebas) | CU 01 mensajes 3-6, 11, 13, 15-16, 21-22 y `cerrarParaVenta()` |
| `RetirarItemTest.cambiarEstadoCambiaElEstado`, `cambiarEstadoRechazaEstadoVacioONulo` | `ItemMenu.cambiarEstado()` |
| `RetirarItemTest.eliminarPlatoDejaElPlatoAgotado` | CU 02 mensajes 12-13 |
| `RetirarItemTest.eliminarProductoDejaElProductoAgotadoYConservaElStock` | CU 02 mensajes 14-15 |
| `RetirarItemTest.secoDePolloRetiradoNoApareceEntreLosDisponibles` | Caso de prueba "Seco de Pollo" (mensajes 5-7) |

## 6. Elementos técnicos no modelados (justificación)

| Elemento | Motivo |
|---|---|
| `ItemMenu.version` (`@Version`) | Bloqueo optimista: condición del caso de prueba del CU 02 (no retirar mientras se compra) |
| Campo oculto `version` en `retirarItem.jsp` | Detecta que el ítem cambió entre que se mostró la lista y se confirmó el retiro (mensaje 18) |
| `PersonalComedor.clave`, `verificarClave()` | Precondición "login" de la secuencia; caso de prueba "el personal se identifica exitosamente" |
| `ItemMenu.getTipo()`, `getVersion()` | Soporte de las vistas JSP |
| `Serializable` en las entidades | El `PersonalComedor` vive en la sesión HTTP y Tomcat la serializa al reiniciar |
| `LoginPersonalServlet`, `LogoutServlet`, `FiltroPersonal` | Autenticación del actor (precondición) |
| `JPAUtil`, `GenericDAO`, `MenuDAO`, `ItemMenuDAO`, `PersonalComedorDAO`, `CierreJPAListener` | Acceso a datos y ciclo de vida de JPA |
| Asociación "contiene" unidireccional (`@JoinColumn` en `Menu`) | Evita agregar a `ItemMenu` un atributo `menu` que el diagrama de clases no tiene |
| `perteneceAlMenu()` (`RetirarItemMenuServlet.java:146`) y la segunda llamada a `buscarMenuDelDia()` (`:104`) | Validación de seguridad: rechaza ids manipulados en el formulario que no pertenecen al menú de hoy |
| No se implementó `ItemMenuDAO.listarPorMenu(Menu m)` (sugerido en el plan de trabajo) | Se usa `Menu.obtenerItem()` (`obtenerItems()` desde la Fig. 15), que **sí** está en el diagrama de clases; un método DAO paralelo sería código muerto |
| Retirar = `cambiarEstado("Agotado")`, no DELETE | El diagrama de actividades dice "Borrar … de la cartelera"; en el sistema eso significa dejar de ofrecer el ítem. El caso de prueba exige que el estado pase a agotado y el registro se conserve |
| Atributos `fecha` / `estado` en minúscula | El diagrama escribe `Fecha` / `Estado`; se sigue la convención Java (camelCase) sin cambiar el nombre |

## 7. Diagrama de actividades ↔ diagramas de secuencia

Las notas amarillas de los diagramas de secuencia copian el texto exacto de las actividades.

| Actividad (diagrama de actividades) | Secuencia | Código |
|---|---|---|
| CU 01: "Revisar ingredientes y productos disponibles en cocina" | Nota previa al msj 1 | Actividad manual (fuera del sistema); texto de apoyo en `crearMenu.jsp` |
| CU 01: "Definir platos" + "Escribir nombre / descripción / precio" | Loop msj 9-18 | Formulario de `crearMenu.jsp` → `CrearMenuServlet.agregarItem()` |
| CU 01: "Ubicar cartelera en lugar visible para el comensal" | Nota previa a msj 19-25 | `Menu.publicarParaVenta()` (estado "Publicado") |
| CU 02: "Revisar el estado de las porciones restantes en la línea de servicio" | Nota previa al msj 1 | Actividad manual (fuera del sistema) |
| CU 02: "Ubicar plato en el menú de la cartelera" | Nota previa al msj 8 | Tabla de ítems disponibles en `retirarItem.jsp` |
| CU 02: "Borrar nombre, descripción y precio del plato de la cartelera" | Nota sobre msj 12-16 | `eliminarPlato` / `eliminarProducto` → `cambiarEstado("Agotado")` |
| CU 02: [No se agotaron] "Continuar despachando el producto" | — (el actor no inicia el caso de uso) | — |
