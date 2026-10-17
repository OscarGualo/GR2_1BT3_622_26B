# PoliDinner – Plan Incremento 3 con TDD

Repositorio público: https://github.com/OscarGualo/GR2_1BT3_622_26Bx

## 1. Qué pide la tarea y cómo la cumplimos

| Pedido del docente | Cómo se cumple |
| --- | --- |
| Desarrollar 1 incremento con funcionalidades nuevas | Incremento 3: **CU 05 Cerrar menú y reporte de ventas del día** y **CU 06 Cancelar pedido con reembolso a PoliCuenta** |
| Usar TDD | Cada método nuevo del modelo nace de un test que falla primero. Evidencia: 3 commits por test (`[RED]`, `[GREEN]`, `[REFACTOR]`) |
| 4 tests unitarios | T1 `Menu.cerrarParaVenta()`, T2 `ReporteVentas.calcularTotalVendido()`, T3 `ItemMenu.reponerStock()`, T4 `Pedido.cancelar()` |
| 1 test con mocks | T6 `Pedido.cancelar()` con `Policuenta` e `ItemMenu` simulados con Mockito |
| 1 test con parámetros | T5 `Policuenta.acreditar()` con `@ParameterizedTest` + `@CsvSource` |
| Informe: cada implementación TDD, máx. 2 hojas por test, cada ciclo Red-Green-Refactor | Plantilla única por test (sección 8) |
| Enlace al GitHub público | Portada del informe y README |

Interpretación: son **6 tests** (4 + 1 + 1). Cada "test" es **un método de prueba** (`@Test` o `@ParameterizedTest`) = **un ciclo TDD** = una sección de máximo 2 hojas en el informe.

## 2. Funcionalidades del Incremento 3

### CU 05 Cerrar menú y generar reporte de ventas del día (actor: PersonalComedor)
1. El personal abre "Cerrar venta del día" en su panel.
2. El sistema muestra el menú del día y su estado.
3. El personal confirma el cierre. `Menu.cerrarParaVenta()` solo cierra un menú `Publicado` (un `Borrador` no se cierra).
4. Al cerrar, ningún cliente puede seguir pagando pedidos (`MenuDAO.buscarMenuDelDia()` ya excluye los menús `Cerrado`).
5. El sistema muestra el reporte del día: total vendido (solo pedidos `Pagado` y `Entregado`) y la lista de pedidos con su estado.

### CU 06 Cancelar pedido con reembolso a PoliCuenta (actor: ClienteUniversitario)
1. El cliente abre "Mis pedidos".
2. El sistema lista sus pedidos (fecha, código, total y estado).
3. El cliente cancela un pedido `Pagado` que aún no se entregó.
4. En **una sola transacción**: se devuelve el total a la PoliCuenta (`acreditar`), se repone el stock de cada ítem (`reponerStock`), el comprobante queda usado (no se puede retirar la comida) y el pedido pasa a `Cancelado`.
5. Un pedido `Entregado`, `Creado` o ya `Cancelado` no se puede cancelar.

### Cambios al modelo (diagrama de clases del Incremento 3)

| Clase | Cambio | Nace del test |
| --- | --- | --- |
| `Menu` | `cerrarParaVenta(): void` → `cerrarParaVenta(): boolean` (solo desde `Publicado`) | T1 |
| `ReporteVentas` (nueva, no es entidad JPA) | `ReporteVentas(fecha: Date, pedidos: List<Pedido>)`, `calcularTotalVendido(): double`, `getPedidos()` | T2 |
| `ItemMenu` | `reponerStock(cantidad: int)` | T3 |
| `Pedido` | `cancelar(): boolean`; nuevo estado `Cancelado` (`Creado` → `Pagado` → `Entregado` / `Cancelado`) | T4 y T6 |
| `Policuenta` | `acreditar(monto: double)` (void: devolver dinero no puede fallar por saldo) | T5 |

No se crean tablas nuevas ni columnas: `ReporteVentas` no se guarda y los estados son textos. No hace falta script de migración (`hbm2ddl.auto=update`).

## 3. Reparto entre las 5 personas

| Persona | Rol | Test(s) TDD | Código fuera de TDD (web y datos) | Diagramas | Rama |
| --- | --- | --- | --- | --- | --- |
| **P1** | Base del incremento, coordinación e informe | **T5 parametrizado** `Policuenta.acreditar()` | Mockito en `pom.xml`; README con enlace | Casos de uso y clases del Inc. 3 | `feature/inc3-base`, `feature/inc3-acreditar` |
| **P2** | CU 05 cierre | **T1 unitario** `Menu.cerrarParaVenta()` | `CerrarMenuServlet`, `cerrarMenu.jsp`, enlaces en `panel.jsp`, bloqueo de pago en `RealizarPedidoServlet` | Secuencia CU 05 | `feature/cu05-cerrar-menu` |
| **P3** | CU 05 reporte | **T2 unitario** `ReporteVentas.calcularTotalVendido()` | `ReporteVentasServlet`, `reporteVentas.jsp`, `PedidoDAO.buscarPorFecha()` | Robustez CU 05 | `feature/cu05-reporte-ventas` |
| **P4** | CU 06 modelo | **T3 unitario** `ItemMenu.reponerStock()` y **T4 unitario** `Pedido.cancelar()` | — | Robustez CU 06 | `feature/cu06-cancelar-modelo` |
| **P5** | CU 06 colaboración y web | **T6 con mocks** `Pedido.cancelar()` | `CancelarPedidoServlet`, `misPedidos.jsp`, `PedidoDAO.buscarPorCliente()`, enlace "Mis pedidos" | Secuencia CU 06 | `feature/cu06-cancelar-pedido` |

P4 tiene dos tests porque no tiene parte web. P1 tiene un solo test porque además arma la base, los diagramas y el informe final.

## 4. Reglas comunes de TDD (todos)

### 4.1 El ciclo
1. **RED**: escribir **solo el test**. Ejecutarlo y verlo fallar. Captura de pantalla del fallo.
   - Primero falla por compilación (el método no existe). Para que la captura muestre un fallo **de aserción** (más claro en el informe), crear el método vacío que devuelva un valor por defecto (`return false;`, `return 0;`, cuerpo vacío) y volver a ejecutar. Esa segunda ejecución es la captura del RED.
2. **GREEN**: escribir el **mínimo código** que hace pasar el test. Nada de casos que el test no pida. Ejecutar y captura en verde.
3. **REFACTOR**: mejorar el código sin cambiar su comportamiento (nombres, constantes, extraer métodos, Javadoc de trazabilidad, eliminar duplicación). Ejecutar **toda la suite** (`./mvnw test`) y captura en verde.

### 4.2 Commits (evidencia en GitHub)
Exactamente 3 commits por test, en este orden:
```
CU05 [RED]: T1 test cerrarParaVentaSoloCierraUnMenuPublicado falla
CU05 [GREEN]: T1 Menu.cerrarParaVenta() devuelve boolean y solo cierra un menu Publicado
CU05 [REFACTOR]: T1 constantes de estado en Menu y Javadoc de trazabilidad
```
- El commit RED puede no compilar o tener tests en rojo: es correcto en una rama feature. **Nunca** se hace PR a `develop` en rojo.
- No mezclar código web (servlets, JSP) en los 3 commits del ciclo. El código web va en commits aparte: `CU05: CerrarMenuServlet segun diagrama de secuencia`.
- Copiar el enlace de cada commit (GitHub → commit → URL) para el informe.

### 4.3 Comandos
```
./mvnw test -Dtest=CerrarMenuTest                              # una clase
./mvnw test -Dtest=CerrarMenuTest#cerrarParaVentaSoloCierraUnMenuPublicado   # un método
./mvnw test                                                    # toda la suite
```
En IntelliJ también sirve el botón verde junto al método; la captura debe mostrar el nombre del test y el mensaje del fallo.

### 4.4 Convenciones del proyecto (CLAUDE.md)
- `javax.*`, nunca `jakarta.*`.
- Nombres en español, iguales a los del diagrama de clases del Incremento 3.
- Javadoc de trazabilidad en cada clase y método nuevo, por ejemplo:
  ```java
  /**
   * Trazabilidad: Diagrama de clases Inc. 3 - Pedido.cancelar(): boolean.
   * Diagrama de secuencia CU06 - mensaje 8: cancelar().
   * Origen TDD: PedidoCancelarTest (T4) y PedidoCancelarMockTest (T6).
   */
  ```
- Dinero en `double`; en los tests `assertEquals(esperado, real, 0.001)`.
- Datos de prueba de siempre: Andrea Gómez, PoliCuenta `2024-0153` con $10.00, "Arroz con pollo" $3.50 y "Jugo de naranja" $0.75 (total $4.25).

## 5. Orden de trabajo y dependencias

```
Día 1      P1: feature/inc3-base (pom con Mockito + diagramas de casos de uso y clases) → PR a develop
           Todos: reunión de 30 min para fijar firmas (tabla de la sección 2)
Días 2-3   En paralelo desde develop:
             P1 T5 acreditar       P2 T1 cerrarParaVenta     P3 T2 calcularTotalVendido
             P4 T3 reponerStock → T4 cancelar
             P5 diagrama de secuencia CU06 + misPedidos.jsp + buscarPorCliente (sin ciclo todavía)
Día 3      P1 y P4 hacen PR a develop (P5 los necesita)
Días 3-4   P5 T6 con mocks sobre develop actualizado (su RED falla porque cancelar() aún no reembolsa)
           P2 y P3 código web del CU 05; P5 código web del CU 06
Día 5      Integración en develop, prueba manual completa (sección 9)
Días 5-6   Cada uno escribe su sección del informe; P1 une el PDF
Día 6      develop → main, etiqueta v3.0-tdd, verificar que el repo es público
```

Dependencias duras:
- **Todos** dependen de `feature/inc3-base` (Mockito y diagrama de clases).
- **P5 (T6)** necesita `Policuenta.acreditar()` (P1, T5), `ItemMenu.reponerStock()` (P4, T3) y `Pedido.cancelar()` (P4, T4) ya en `develop`.
- **P3** usa pedidos `Cancelado` en el reporte solo en la prueba manual; su test T2 no depende de P4.
- **P2 y P3** comparten `panel.jsp`: solo **P2** lo edita y agrega los dos enlaces (cerrar y reporte).

---

## 6. Detalle por persona

### P1 – Base del incremento, T5 parametrizado e informe

**Base (`feature/inc3-base`, antes que nadie):**
1. Agregar Mockito al `pom.xml` (dentro de `<dependencies>`; trae `mockito-core` por transitividad):
   ```xml
   <dependency>
       <groupId>org.mockito</groupId>
       <artifactId>mockito-junit-jupiter</artifactId>
       <version>5.14.2</version>
       <scope>test</scope>
   </dependency>
   ```
   `junit-jupiter` 5.10.2 ya trae `@ParameterizedTest`, no hay que agregar nada para T5. Comprobar con `./mvnw test` que los tests actuales siguen en verde. (Si con JDK 21+ aparece la advertencia de "dynamic agent loading" de Mockito, es solo un aviso.)
2. Diagrama de casos de uso actualizado (CU 05 y CU 06) y diagrama de clases del Incremento 3 con los cambios de la sección 2 (marcar en color lo nuevo).
3. Commit: `INC3: agrega Mockito y diagramas de casos de uso y clases del incremento 3`.

**T5 – `Policuenta.acreditar()` con parámetros (`feature/inc3-acreditar`).** Archivo: `src/test/java/ec/edu/epn/polidinner/modelo/PolicuentaAcreditarTest.java`.

RED:
```java
class PolicuentaAcreditarTest {

    /** T5 (TDD, parametrizado): acreditar(monto) suma el monto al saldo de la PoliCuenta. */
    @ParameterizedTest(name = "saldo {0} + acreditar {1} = {2}")
    @CsvSource({
            "5.75, 4.25, 10.00",   // reembolso del caso de prueba de Andrea
            "0.00, 3.50, 3.50",    // cuenta en cero
            "10.00, 0.75, 10.75",
            "2.10, 0.20, 2.30"     // decimales que en double dan 2.3000000000000003
    })
    void acreditarSumaElMontoAlSaldo(double saldoInicial, double monto, double saldoEsperado) {
        Policuenta cuenta = new Policuenta("2024-0153", saldoInicial);

        cuenta.acreditar(monto);

        assertEquals(saldoEsperado, cuenta.consultarSaldo(), 0.001);
    }
}
```
Imports: `org.junit.jupiter.params.ParameterizedTest`, `org.junit.jupiter.params.provider.CsvSource`. Stub para el RED de aserción: `public void acreditar(double monto) { }` → fallan las 4 filas ("expected 10.0 but was 5.75").

GREEN (mínimo):
```java
public void acreditar(double monto) {
    saldo += monto;
}
```
REFACTOR: `debitar()` ya redondea con `Math.round(... * 100) / 100.0`. Aplicar **Extract Method**: crear `private static double redondear(double valor)` y usarlo en `debitar()` y `acreditar()` (quita la duplicación y deja el saldo en 2 decimales exactos). Javadoc de trazabilidad. Toda la suite en verde (incluye `PolicuentaTest`).

Extra opcional (otro ciclo, no se documenta): `@ParameterizedTest @ValueSource(doubles = {0, -1.5})` → `assertThrows(IllegalArgumentException.class, ...)` y validación `monto > 0` igual a `debitar()`.

En el informe explicar: qué es `@ParameterizedTest`, cómo `@CsvSource` convierte cada fila en una ejecución, el atributo `name` y por qué 4 filas cubren más que 1 test.

**Informe (P1 arma el PDF):** portada, enlace al repo, introducción a TDD, descripción del incremento y diagramas (sección 8).

**Cierre:** README con la sección "Incremento 3", PR `develop` → `main`, tag `v3.0-tdd`, comprobar en GitHub → Settings que el repositorio es **público**.

---

### P2 – CU 05 cierre del menú (T1)

**T1 – `Menu.cerrarParaVenta()` solo cierra un menú Publicado.** Archivo: `src/test/java/ec/edu/epn/polidinner/modelo/CerrarMenuTest.java`.

RED:
```java
class CerrarMenuTest {

    private PersonalComedor personal;

    @BeforeEach
    void preparar() {
        personal = new PersonalComedor("1700000001", "Carlos", "Pérez", "Cocinero", "clave");
    }

    /** T1 (TDD): solo se cierra la venta de un menú "Publicado"; un "Borrador" no se cierra. */
    @Test
    void cerrarParaVentaSoloCierraUnMenuPublicado() {
        Menu borrador = personal.crearMenuDiario();
        assertFalse(borrador.cerrarParaVenta());
        assertEquals("Borrador", borrador.getEstado());

        Menu publicado = personal.crearMenuDiario();
        publicado.agregarItem(new Plato("Almuerzo Ejecutivo", 2.50, "Disponible", "Sopa, segundo y jugo", 10));
        publicado.publicarParaVenta();
        assertTrue(publicado.cerrarParaVenta());
        assertEquals("Cerrado", publicado.getEstado());
    }
}
```
RED por compilación (`cerrarParaVenta()` es `void`). Stub para el RED de aserción: cambiar la firma a `boolean` y dejar `this.estado = "Cerrado"; return true;` → falla en `assertFalse(borrador.cerrarParaVenta())`.

GREEN:
```java
public boolean cerrarParaVenta() {
    if (!"Publicado".equals(estado)) {
        return false;
    }
    this.estado = "Cerrado";
    return true;
}
```
**Efecto en tests existentes:** `CrearMenuTest.cerrarParaVentaCambiaEstadoACerradoYYaNoSePublica()` (línea 119) cierra un menú en Borrador y ahora falla. Es correcto: el requisito cambió. Dentro del commit GREEN, actualizar ese test para publicar el menú antes de cerrarlo y revisar el uso de la línea 88. Explicarlo en el informe (TDD también protege lo anterior).

REFACTOR: constantes `BORRADOR`, `PUBLICADO`, `CERRADO` en `Menu` y usarlas en `publicarParaVenta()`, `agregarItem()` y `cerrarParaVenta()`; actualizar el Javadoc (Diagrama de clases Inc. 3 + secuencia CU05). Suite completa en verde.

**Código web (commits aparte, después del ciclo):**
1. `controlador/CerrarMenuServlet.java`, `@WebServlet("/personal/cerrar-menu")`:
   - `doGet`: `menuDAO.buscarMenuDelDia()` → atributo `menuHoy` → `vistas/personal/cerrarMenu.jsp`.
   - `doPost` con `accion=cerrar`: buscar el menú del día; si es `null` → `flashError` "No hay menú abierto hoy"; si `!menuHoy.cerrarParaVenta()` → `flashError` "Solo se cierra un menú publicado"; si cierra → `menuDAO.actualizar(menuHoy)`, `flashExito` y `sendRedirect(contexto + "/personal/reporte")`.
   - Mismo patrón que `CrearMenuServlet` (un `doPost` con `accion`, mensajes `flashError`/`flashExito`).
2. `vistas/personal/cerrarMenu.jsp`: encabezado/pie comunes, `mensajes.jsp`, tarjeta con fecha, estado e ítems del menú y botón "Cerrar venta del día" (formulario POST con `accion=cerrar`).
3. `panel.jsp`: dos tarjetas nuevas, "Cerrar venta del día" (`/personal/cerrar-menu`) y "Reporte de ventas" (`/personal/reporte`), con el mismo marcado de las tarjetas existentes.
4. `RealizarPedidoServlet.confirmar()`: antes de `Transaccion.iniciar()`, si `menuDAO.buscarMenuDelDia()` es `null` → `flashError` "El comedor ya cerró la venta de hoy" y volver al carrito (evita pagar un carrito armado antes del cierre).
5. Diagrama de secuencia CU 05 (mensajes numerados: frontera → `CerrarMenuServlet` → `MenuDAO` → `Menu.cerrarParaVenta()` → redirección al reporte).

---

### P3 – CU 05 reporte de ventas (T2)

**T2 – `ReporteVentas.calcularTotalVendido()` suma solo pedidos pagados o entregados.** Archivo: `src/test/java/ec/edu/epn/polidinner/modelo/ReporteVentasTest.java`.

RED:
```java
class ReporteVentasTest {

    private ClienteUniversitario andrea;
    private Plato arroz;
    private Producto jugo;
    private PersonalComedor personal;

    @BeforeEach
    void preparar() {
        andrea = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));
        arroz = new Plato("Arroz con pollo", 3.50, "Disponible", "Arroz, pollo y ensalada", 20);
        jugo = new Producto("Jugo de naranja", 0.75, "Disponible", "Vaso 300 ml", 30, new Date());
        personal = new PersonalComedor("1700000001", "Carlos", "Pérez", "Cocinero", "clave");
    }

    /** T2 (TDD): el total vendido suma pedidos "Pagado" y "Entregado"; un pedido "Creado" no cuenta. */
    @Test
    void calcularTotalVendidoSumaSoloPedidosPagadosYEntregados() {
        Pedido pagado = andrea.crearPedido();
        pagado.agregarItem(arroz, 1);
        pagado.agregarItem(jugo, 1);
        pagado.confirmarPago();                       // 4.25, "Pagado"

        Pedido entregado = andrea.crearPedido();
        entregado.agregarItem(jugo, 2);
        entregado.confirmarPago();                    // 1.50
        personal.entregarPedido(entregado);           // "Entregado"

        Pedido creado = andrea.crearPedido();
        creado.agregarItem(arroz, 1);
        creado.calcularTotal();                       // 3.50 sin pagar: no es venta

        ReporteVentas reporte = new ReporteVentas(new Date(), Arrays.asList(pagado, entregado, creado));

        assertEquals(5.75, reporte.calcularTotalVendido(), 0.001);
    }
}
```
RED por compilación (no existe `ReporteVentas`). Stub para el RED de aserción: clase con constructor y `calcularTotalVendido() { return 0; }` → "expected 5.75 but was 0.0".

GREEN (`src/main/java/ec/edu/epn/polidinner/modelo/ReporteVentas.java`):
```java
public class ReporteVentas {

    private final Date fecha;
    private final List<Pedido> pedidos;

    public ReporteVentas(Date fecha, List<Pedido> pedidos) {
        this.fecha = fecha;
        this.pedidos = pedidos;
    }

    public double calcularTotalVendido() {
        double suma = 0;
        for (Pedido pedido : pedidos) {
            if ("Pagado".equals(pedido.getEstado()) || "Entregado".equals(pedido.getEstado())) {
                suma += pedido.getTotal();
            }
        }
        return suma;
    }
}
```
REFACTOR: **Extract Method** de la condición a `private boolean esVenta(Pedido pedido)`; redondear con `Math.round(suma * 100) / 100.0` como `Pedido.calcularTotal()`; copiar la lista en el constructor (`new ArrayList<>(pedidos)`) y agregar `getFecha()` y `getPedidos()` (lista no modificable) para la JSP; Javadoc. Suite en verde.

**Código web (commits aparte):**
1. `PedidoDAO.buscarPorFecha(Date dia)`: `fecha` es `TIMESTAMP`, así que se filtra por rango del día:
   `SELECT p FROM Pedido p WHERE p.fecha >= :inicio AND p.fecha < :fin ORDER BY p.id` (inicio = 00:00 del día, fin = 00:00 del día siguiente, con `Calendar`). Mismo patrón de `EntityManager` que `MenuDAO.buscarMenuDelDia()`.
2. `controlador/ReporteVentasServlet.java`, `@WebServlet("/personal/reporte")`, solo `doGet`: `new ReporteVentas(hoy, pedidoDAO.buscarPorFecha(hoy))` → atributo `reporte` → `vistas/personal/reporteVentas.jsp`.
3. `vistas/personal/reporteVentas.jsp`: total vendido destacado y tabla con `<c:forEach items="${reporte.pedidos}">` (código del comprobante, cliente, estado, total con `fmt:formatNumber pattern="0.00"`, igual que `carrito.jsp`).
4. Diagrama de robustez del CU 05 (frontera Cerrar menú / Reporte, controladores `CerrarMenuServlet` y `ReporteVentasServlet`, entidades `Menu`, `Pedido`, `ReporteVentas`).

---

### P4 – CU 06 modelo (T3 y T4)

**T3 – `ItemMenu.reponerStock()`.** Archivo: `src/test/java/ec/edu/epn/polidinner/modelo/ItemMenuReponerStockTest.java`.

RED:
```java
class ItemMenuReponerStockTest {

    /** T3 (TDD): reponer porciones suma al stock y reactiva un ítem que se agotó por falta de stock. */
    @Test
    void reponerStockDevuelveLasPorcionesYReactivaElItemAgotado() {
        Plato seco = new Plato("Seco de pollo", 3.00, "Disponible", "Arroz, seco y maduro", 2);
        seco.reducirStock(2);                         // stock 0 → "Agotado" (regla del Inc. 2)

        seco.reponerStock(2);

        assertEquals(2, seco.getStockActual());
        assertEquals("Disponible", seco.getEstado());
    }
}
```
Stub para el RED de aserción: `public void reponerStock(int cantidad) { }` → "expected 2 but was 0".

GREEN:
```java
public void reponerStock(int cantidad) {
    boolean agotadoPorStock = stockActual == 0 && "Agotado".equals(estado);
    stockActual += cantidad;
    if (agotadoPorStock) {
        cambiarEstado("Disponible");
    }
}
```
Regla: solo se reactiva si se agotó **por stock** (stock en 0). Un ítem retirado a mano en el CU 02 con stock > 0 sigue `Agotado`.
REFACTOR: validar `cantidad < 1` → `IllegalArgumentException` (mismo estilo que `Pedido.agregarItem`), Javadoc de trazabilidad, nombre de variable claro. Suite en verde (incluye `ItemMenuStockTest`).

**T4 – `Pedido.cancelar()`: reglas de estado.** Archivo: `src/test/java/ec/edu/epn/polidinner/modelo/PedidoCancelarTest.java`.

RED:
```java
class PedidoCancelarTest {

    private ClienteUniversitario andrea;
    private Plato arroz;
    private Producto jugo;

    @BeforeEach
    void preparar() {
        andrea = new ClienteUniversitario("1750000001", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));
        arroz = new Plato("Arroz con pollo", 3.50, "Disponible", "Arroz, pollo y ensalada", 20);
        jugo = new Producto("Jugo de naranja", 0.75, "Disponible", "Vaso 300 ml", 30, new Date());
    }

    /** T4 (TDD): un pedido pagado se cancela, su código de entrega queda inutilizable. */
    @Test
    void cancelarUnPedidoPagadoLoDejaCanceladoConElCodigoAnulado() {
        Pedido pedido = andrea.crearPedido();
        pedido.agregarItem(arroz, 1);
        pedido.agregarItem(jugo, 1);
        assertTrue(pedido.confirmarPago());

        assertTrue(pedido.cancelar());

        assertEquals("Cancelado", pedido.getEstado());
        assertTrue(pedido.getComprobante().isUsado());
        assertFalse(pedido.getComprobante().validarCodigoEntrega());
    }
}
```
Stub: `public boolean cancelar() { return false; }`.

GREEN:
```java
public boolean cancelar() {
    if (!"Pagado".equals(estado)) {
        return false;
    }
    estado = "Cancelado";
    comprobante.marcarCodigoUsado();
    return true;
}
```
¿Por qué `marcarCodigoUsado()` (método que ya existe)? Además de anular el código, cambia el `Comprobante`, que tiene `@Version`. Así, si el cliente cancela y el personal entrega el mismo pedido al mismo tiempo, una de las dos transacciones recibe `OptimisticLockException` (el mismo criterio de concurrencia del CU 02).
REFACTOR: Javadoc de la clase `Pedido` con el nuevo estado (`Pagado` → `Cancelado`), comprobar `comprobante != null` por seguridad, constantes de estado si el equipo las adopta. Suite en verde.

**Importante:** T4 **no** comprueba el reembolso ni el stock. Eso lo exige el T6 de P5 con mocks; así el ciclo de P5 tiene un RED real. No adelantar ese código.

Diagrama de robustez del CU 06.

---

### P5 – CU 06 colaboración con mocks (T6) y parte web

**T6 – `Pedido.cancelar()` reembolsa y repone stock (Mockito).** Archivo: `src/test/java/ec/edu/epn/polidinner/modelo/PedidoCancelarMockTest.java`. Empezar cuando T3, T4 y T5 estén en `develop`.

Por qué mocks aquí: PoliCuenta es un **sistema externo** (en el proyecto se simula con una tabla). El test quiere comprobar **la colaboración** de `Pedido` con `Policuenta` e `ItemMenu` (que les llame con los valores correctos), aislado de su lógica interna.

RED:
```java
@ExtendWith(MockitoExtension.class)
class PedidoCancelarMockTest {

    @Mock private ClienteUniversitario cliente;
    @Mock private Policuenta policuenta;
    @Mock private ItemMenu arroz;
    @Mock private ItemMenu jugo;

    /** T6 (TDD con mocks): cancelar() devuelve el total a la PoliCuenta y repone el stock de cada ítem. */
    @Test
    void cancelarReembolsaElTotalYReponeElStockDeCadaItem() {
        when(cliente.getPolicuenta()).thenReturn(policuenta);
        when(arroz.hayStock(1)).thenReturn(true);
        when(arroz.getPrecio()).thenReturn(3.50);
        when(jugo.hayStock(1)).thenReturn(true);
        when(jugo.getPrecio()).thenReturn(0.75);
        when(policuenta.debitar(4.25)).thenReturn(true);

        Pedido pedido = new Pedido(new Date(), "Creado", cliente);
        pedido.agregarItem(arroz, 1);
        pedido.agregarItem(jugo, 1);
        assertTrue(pedido.confirmarPago());

        assertTrue(pedido.cancelar());

        verify(policuenta).acreditar(4.25);
        verify(arroz).reponerStock(1);
        verify(jugo).reponerStock(1);
    }
}
```
Imports: `org.junit.jupiter.api.extension.ExtendWith`, `org.mockito.Mock`, `org.mockito.junit.jupiter.MockitoExtension`, `static org.mockito.Mockito.*`.
Notas técnicas:
- `ItemMenu` es abstracta y `Policuenta`/`ClienteUniversitario` no son `final`: Mockito las simula sin problema.
- Se usa el constructor `Pedido(fecha, estado, cliente)` porque toma la PoliCuenta de `cliente.getPolicuenta()`: así el pedido recibe el mock.
- `MockitoExtension` usa *strict stubs*: todo `when(...)` debe usarse. Si se agrega un stub que no se usa, falla con `UnnecessaryStubbingException`.
- `reducirStock()` en los mocks no hace nada (los mocks devuelven valores por defecto).

RED esperado (con `cancelar()` de P4 ya en develop): `Wanted but not invoked: policuenta.acreditar(4.25)`. Captura de ese mensaje: es el mejor ejemplo de un fallo de mock para el informe.

GREEN (en `Pedido.cancelar()`):
```java
public boolean cancelar() {
    if (!"Pagado".equals(estado)) {
        return false;
    }
    policuenta.acreditar(total);
    for (DetallePedido detalle : detalles) {
        detalle.getItem().reponerStock(detalle.getCantidad());
    }
    estado = "Cancelado";
    comprobante.marcarCodigoUsado();
    return true;
}
```
REFACTOR: **Extract Method** del ciclo a `private void reponerStockDeItems()`, simétrico al `reducirStockDeItems()` que ya existe en `confirmarPago()`; Javadoc con la secuencia CU 06. Toda la suite en verde: T4 de P4 sigue pasando con objetos reales.

Extra opcional: segundo test con un pedido `Entregado` (`when(...)` para `entregarPedido` no hace falta: usar un `PersonalComedor` real) y `verify(policuenta, never()).acreditar(anyDouble())`.

En el informe explicar: qué es un mock, `@Mock` y `MockitoExtension`, `when(...).thenReturn(...)` (preparar respuestas) frente a `verify(...)` (comprobar llamadas), y por qué no se usa la PoliCuenta real.

**Código web (commits aparte):**
1. `PedidoDAO.buscarPorCliente(String cedula)`: `SELECT p FROM Pedido p WHERE p.cliente.cedula = :cedula ORDER BY p.fecha DESC`.
2. `controlador/CancelarPedidoServlet.java`, `@WebServlet("/cliente/cancelar")`:
   - `doGet`: pedidos del cliente en sesión → `vistas/cliente/misPedidos.jsp`.
   - `doPost` con `accion=cancelar` e `idPedido`, en **una transacción** como `RealizarPedidoServlet.confirmar()`:
     ```java
     try (Transaccion tx = Transaccion.iniciar()) {
         Pedido pedido = pedidoDAO.buscarPorId(idPedido, tx);   // queda gestionado con su PoliCuenta e ítems
         // si es null o no es del cliente en sesión → flashError y salir (rollback automático)
         if (!pedido.cancelar()) {
             // flashError "Solo se puede cancelar un pedido pagado que aún no se entregó"
         } else {
             tx.confirmar();
             // flashExito "Se devolvieron $X.XX a su PoliCuenta"
         }
     } catch (OptimisticLockException | RollbackException e) {
         // flashError "El pedido acaba de ser entregado; no se pudo cancelar"
     }
     ```
     Como los objetos están gestionados por el `EntityManager`, el commit guarda el saldo, el stock, el comprobante y el estado juntos; si algo falla, `close()` hace rollback.
3. `vistas/cliente/misPedidos.jsp`: tabla con fecha, código, total y estado; botón "Cancelar" (POST) solo si `pedido.estado == 'Pagado'`.
4. Enlace "Mis pedidos" en la navegación del cliente (`encabezado.jsp`, bloque `sessionScope.cliente`) y en `comprobante.jsp`.
5. Diagrama de secuencia CU 06 numerado (frontera → `CancelarPedidoServlet` → `PedidoDAO.buscarPorId` → `Pedido.cancelar()` → `Policuenta.acreditar()` → loop `ItemMenu.reponerStock()` → `Comprobante.marcarCodigoUsado()` → commit/rollback → mensaje al cliente).

---

## 7. Resumen de archivos

| Archivo | Nuevo / modificado | Responsable |
| --- | --- | --- |
| `pom.xml` | Mod. (Mockito) | P1 |
| `modelo/Policuenta.java` | Mod. `acreditar`, `redondear` | P1 |
| `modelo/Menu.java` | Mod. `cerrarParaVenta(): boolean` | P2 |
| `modelo/ReporteVentas.java` | Nuevo | P3 |
| `modelo/ItemMenu.java` | Mod. `reponerStock` | P4 |
| `modelo/Pedido.java` | Mod. `cancelar` (P4 estado, P5 reembolso y stock) | P4, P5 |
| `persistencia/PedidoDAO.java` | Mod. `buscarPorFecha` (P3), `buscarPorCliente` (P5) | P3, P5 |
| `controlador/CerrarMenuServlet.java` | Nuevo | P2 |
| `controlador/ReporteVentasServlet.java` | Nuevo | P3 |
| `controlador/CancelarPedidoServlet.java` | Nuevo | P5 |
| `controlador/RealizarPedidoServlet.java` | Mod. bloqueo tras el cierre | P2 |
| `vistas/personal/cerrarMenu.jsp`, `panel.jsp` | Nuevo / mod. | P2 |
| `vistas/personal/reporteVentas.jsp` | Nuevo | P3 |
| `vistas/cliente/misPedidos.jsp`, `encabezado.jsp`, `comprobante.jsp` | Nuevo / mod. | P5 |
| `test/.../PolicuentaAcreditarTest.java` | Nuevo (T5) | P1 |
| `test/.../CerrarMenuTest.java` + ajuste en `CrearMenuTest.java` | Nuevo (T1) | P2 |
| `test/.../ReporteVentasTest.java` | Nuevo (T2) | P3 |
| `test/.../ItemMenuReponerStockTest.java`, `PedidoCancelarTest.java` | Nuevos (T3, T4) | P4 |
| `test/.../PedidoCancelarMockTest.java` | Nuevo (T6) | P5 |

`PedidoDAO.java` lo tocan P3 y P5: cada uno agrega su método al final; quien haga el segundo PR resuelve el conflicto (trivial).

## 8. Informe

### Estructura y responsables

| Sección | Responsable | Contenido | Extensión |
| --- | --- | --- | --- |
| Portada | P1 | Integrantes, materia, fecha, **enlace al repositorio público** | 1 hoja |
| 1. Introducción a TDD | P1 | Qué es TDD, el ciclo Red-Green-Refactor, herramientas (JUnit 5.10.2, `junit-jupiter-params`, Mockito 5.14.2), convención de commits `[RED]/[GREEN]/[REFACTOR]` | 1 hoja |
| 2. Incremento 3 | P1 | CU 05 y CU 06, diagrama de casos de uso, diagrama de clases (lo nuevo resaltado), secuencias de P2 y P5 | 1-2 hojas |
| 3.1 T1 `cerrarParaVenta` | P2 | Plantilla | máx. 2 hojas |
| 3.2 T2 `calcularTotalVendido` | P3 | Plantilla | máx. 2 hojas |
| 3.3 T3 `reponerStock` | P4 | Plantilla | máx. 2 hojas |
| 3.4 T4 `cancelar` (estado) | P4 | Plantilla | máx. 2 hojas |
| 3.5 T5 parametrizado `acreditar` | P1 | Plantilla + explicación de `@ParameterizedTest`/`@CsvSource` | máx. 2 hojas |
| 3.6 T6 mocks `cancelar` | P5 | Plantilla + explicación de `@Mock`, `when`, `verify` | máx. 2 hojas |
| 4. Funcionamiento | P2, P3, P5 | Capturas: cerrar menú, reporte, mis pedidos, cancelación y saldo devuelto | 1 hoja |
| 5. Resultado de la suite y conclusiones | Todos | Captura de `./mvnw test` con todo en verde (~59 tests), una conclusión por integrante | 1 hoja |

### Plantilla de cada test (máx. 2 hojas)
1. **Encabezado:** "Test N – `Clase#metodo` – Tipo (unitario / parametrizado / con mocks) – CU – Responsable".
2. **Requisito** (2-3 líneas): qué regla de negocio se quiere y el criterio de aceptación con datos concretos (por ejemplo, "con $5.75 de saldo, acreditar $4.25 deja $10.00").
3. **RED:** código del test; captura del fallo con el mensaje (`expected ... but was ...` o `Wanted but not invoked`); 2 líneas de por qué falla.
4. **GREEN:** código mínimo agregado; captura en verde; 2 líneas de por qué es el mínimo (qué **no** se hizo aún).
5. **REFACTOR:** antes/después del fragmento mejorado, nombre de la técnica (Extract Method, constantes, etc.) y captura de la suite completa en verde.
6. **Evidencia:** enlaces a los 3 commits en GitHub.

Capturas: recortadas a la ventana de ejecución de IntelliJ o a la consola de Maven, con el nombre del test visible. Código en fuente monoespaciada a 9-10 pt para no pasar de 2 hojas.

## 9. Verificación final (P1 con todos, en `develop`)
1. `./mvnw clean test` → todos en verde (53 actuales + 6 nuevos).
2. En `git log --oneline` aparecen los 6 tríos `[RED]`/`[GREEN]`/`[REFACTOR]` en orden.
3. Prueba manual en Tomcat (`http://localhost:8080/polidinner/`):
   1. Personal: crear y publicar el menú del día.
   2. Cliente (Andrea, $10.00): pagar arroz + jugo → saldo $5.75, código PD-XXXX.
   3. Cliente: "Mis pedidos" → cancelar → mensaje "Se devolvieron $4.25", saldo $10.00, stock de los ítems repuesto.
   4. Personal: validar el código del pedido cancelado → rechazado.
   5. Cliente: hacer y pagar otro pedido; personal: entregarlo; cliente: intentar cancelarlo → rechazado.
   6. Personal: cerrar la venta del día → reporte con total vendido que no incluye el pedido cancelado.
   7. Cliente: intentar pagar después del cierre → "El comedor ya cerró la venta de hoy".
4. Repositorio público y enlace funcionando en el informe y el README.
