# Trazabilidad del Incremento 1: diagramas ↔ código

Rutas relativas a `src/main/java/ec/edu/epn/polidinner/` (Java) y `src/main/webapp/` (JSP).
Cada clase y método citado lleva además un Javadoc "Trazabilidad: ..." con la misma referencia.

## 1. Diagrama de clases (Fig. 7)

| Elemento del diagrama | Código |
|---|---|
| **Menu** | `modelo/Menu.java:29` (`@Entity`) |
| -Fecha : Date / -id : int / -Estado : String | `Menu.java:38` `fecha` · `:35` `id` · `:40` `estado` |
| +publicarParaVenta() | `Menu.java:74` |
| +cerrarParaVenta() | `Menu.java:86` |
| +agregarItem() | `Menu.java:94` |
| +obtenerItem() | `Menu.java:105` |
| Asociación "contiene" 1 Menu ◇— * ItemMenu | `Menu.java:51` `@OneToMany(cascade = ALL)` + `@JoinColumn(name = "menu_id")` |
| Asociación "administra" * Menu — 1 PersonalComedor | `Menu.java:43` `@ManyToOne PersonalComedor personal` |
| **«ItemMenu» Abstract** | `modelo/ItemMenu.java:20` (`abstract`, `@Inheritance(JOINED)`) |
| -nombre / -precio / -id / -estado / -descripcion | `ItemMenu.java:28` · `:30` · `:26` · `:32` · `:34` |
| +cambiarEstado() | `ItemMenu.java:58` |
| **Plato** "es un" ItemMenu | `modelo/Plato.java:10` (`extends ItemMenu`) |
| +getId() | `Plato.java:28` |
| **Producto** "es un" ItemMenu | `modelo/Producto.java:14` (`extends ItemMenu`) |
| -stock : int / -fechaCaducidad : Date | `Producto.java:16` · `:19` |
| +getStock() | `Producto.java:36` |
| **PersonalComedor** | `modelo/PersonalComedor.java:16` |
| -nombres / -apellidos / -rol / -cedula | `PersonalComedor.java:23` · `:25` · `:27` · `:21` (`@Id`) |
| +crearMenuDiario() | `PersonalComedor.java:52` |
| +eliminarPlato() | `PersonalComedor.java:61` |
| +eliminarProducto() | `PersonalComedor.java:70` |

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
| 2 | iniciarCreacion() | `CrearMenuServlet.java:57-59` → `iniciarCreacion()` `:93` |
| 3 | crearMenuDiario() | `CrearMenuServlet.java:98` → `PersonalComedor.java:52` |
| 4 | «create» Menu(fecha=hoy, estado="Borrador", personal) | `Menu.java:63` (constructor), invocado en `PersonalComedor.java:53` |
| 5-6 | menuHoy | retorno de `crearMenuDiario()` |
| 7 | guardar(menuHoy) | `CrearMenuServlet.java:99` → `GenericDAO.java:24` |
| 8 | muestra formulario de ítems | `CrearMenuServlet.java:45` (`doGet`) → `crearMenu.jsp:40` |
| 9 | Ingresa datos del ítem | `crearMenu.jsp:44` y siguientes (formulario) |
| 10 | agregarItem(tipo, nombre, descripcion, precio, stock, fechaCaducidad) | `CrearMenuServlet.java:72` → `procesarAgregarItem()` `:137` → `agregarItem()` `:109` |
| 11-12 | alt [Plato] «create» Plato(...) | `CrearMenuServlet.java:115` → `Plato.java:19` |
| 13-14 | alt [Producto] «create» Producto(...) | `CrearMenuServlet.java:113` → `Producto.java:28` |
| 15-16 | agregarItem(nuevoItem) / true | `CrearMenuServlet.java:117` → `Menu.java:94` |
| 17 | actualizar(menuHoy) | `CrearMenuServlet.java:120` → `GenericDAO.java:32` |
| 18 | lista de ítems agregados | `crearMenu.jsp:102-145` (`<c:forEach>` sobre `obtenerItem()`) |
| 19 | Solicita publicar menú | `crearMenu.jsp:152` (`accion=publicar`) |
| 20 | publicar() | `CrearMenuServlet.java:74` → `publicar()` `:128` |
| 21-22 | publicarParaVenta() / true | `CrearMenuServlet.java:129` → `Menu.java:74` |
| 23 | actualizar(menuHoy) | `CrearMenuServlet.java:132` |
| 24-25 | "Menú publicado exitosamente" / Muestra mensaje | `CrearMenuServlet.java:77` → `vistas/comun/mensajes.jsp` |

## 4. Diagrama de secuencia CU 02 Retirar plato y/o producto

Diagrama: `Diagrama de secuencia CU02 retirar item.png` (fuente Mermaid: `.mmd`).

| # | Mensaje | Código |
|---|---|---|
| 1 | Solicita retirar plato/producto | `panel.jsp` (opción "Retirar plato y/o producto" → `/personal/retirar`) |
| 2 | iniciarRetiro() | `RetirarItemMenuServlet.java:47` (`doGet`) |
| 3-4 | buscarMenuDelDia() / menuHoy | `RetirarItemMenuServlet.java:49` → `persistencia/MenuDAO.java:26` |
| 5-6 | obtenerItem() / lista de ítems | `RetirarItemMenuServlet.java:53` → `Menu.java:105` |
| 7 | muestra ítems con estado "Disponible" | `RetirarItemMenuServlet.java:54` (filtro) → `retirarItem.jsp:34-95` |
| 8 | Selecciona ítem y confirma el retiro | `retirarItem.jsp:82` (botón Retirar) + diálogo de confirmación `retirarItem.jsp:107` |
| 9 | retirarItem(idItem) | `RetirarItemMenuServlet.java:68` (`doPost`, `accion=retirar`) → `retirarItem()` `:95` |
| 10-11 | buscarPorId(idItem) / item | `RetirarItemMenuServlet.java:102` → `GenericDAO.java:36` |
| 12 | alt [Plato] eliminarPlato(plato) | `RetirarItemMenuServlet.java:119` → `PersonalComedor.java:61` |
| 13 | cambiarEstado("Agotado") | `PersonalComedor.java:62` → `ItemMenu.java:58` |
| 14 | alt [Producto] eliminarProducto(producto) | `RetirarItemMenuServlet.java:121` → `PersonalComedor.java:70` |
| 15 | cambiarEstado("Agotado") | `PersonalComedor.java:71` → `ItemMenu.java:58` |
| 16 | actualizar(item) | `RetirarItemMenuServlet.java:125` → `GenericDAO.java:32` |
| 17 | "Ítem retirado del menú" | `RetirarItemMenuServlet.java:127` |
| 18 | alt [OptimisticLockException] "El ítem está siendo comprado, intente de nuevo" | `RetirarItemMenuServlet.java:114` (versión de la vista desactualizada) y `:133` (`OptimisticLockException` / `StaleStateException`) |
| 19 | Muestra mensaje y la lista sin el ítem retirado | `RetirarItemMenuServlet.java:85` (redirect) → `mensajes.jsp` + `retirarItem.jsp` |

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
