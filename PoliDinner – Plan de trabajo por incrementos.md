# PoliDinner – Plan de trabajo por incrementos

Oct 4, 2026 · @oscasr

## Resumen del reparto

Las Personas 1 y 2 implementan el Incremento 1 (CU 01 y CU 02) y las Personas 3, 4 y 5 el Incremento 2 (CU 03 y CU 04). Cada persona es dueña de un corte vertical completo (entidad o método, servlet, JSP y prueba), así la trazabilidad de su parte se explica sola en el informe.

Stack: Java 17, Maven (empaquetado WAR), JSP + JSTL, Servlets (javax, Servlet 4.0), JPA con Hibernate 5.6, SQL Server, Apache Tomcat 9, IntelliJ IDEA y GitHub Copilot. Repositorio: [OscarGualo/GR2SW\_1BT3\_622\_26B](https://github.com/OscarGualo/GR2SW_1BT3_622_26B).

**Importante sobre Tomcat 9:** usa el paquete `javax.*`, no `jakarta.*`. Por eso se usa Hibernate 5.6 (Hibernate 6 usa `jakarta.persistence` y no funciona en Tomcat 9). Si Copilot sugiere imports `jakarta.`, hay que cambiarlos a `javax.`.

| Persona | Incremento | Responsabilidad principal | Casos de uso / elementos del modelo |
| --- | --- | --- | --- |
| Persona 1 | 1 | Base del proyecto (Fase 0) + modelo de datos + Retirar plato/producto | Entidades Menu, ItemMenu, Plato, Producto, PersonalComedor; CU 02 |
| Persona 2 | 1 | Crear Menú + inicio de sesión del personal + plantilla visual | CU 01; login de PersonalComedor; Interfaz del sistema de comida |
| Persona 3 | 2 | Modelo de datos del incremento 2 + pago con PoliCuenta | Entidades ClienteUniversitario, Pedido, DetallePedido, Policuenta, Comprobante; parte de pago del CU 03 |
| Persona 4 | 2 | Selección de comida y carrito | CU 03 (crearPedido, obtenerItems, agregarItem, calcularTotal); Interfaz de pedidos de comida |
| Persona 5 | 2 | Habilitar entrega de comida + pantalla Salida | CU 04; validarCodigo, entregarPedido, marcarCodigoUsado |

Dos aclaraciones antes de empezar: el enunciado pide el nombre `GRXX_1BT3_622_26B` (XX = número de grupo) y el repositorio se llama `GR2SW_1BT3_622_26B`; confirmen con el docente que ese nombre es aceptado. Y aunque el Incremento 2 va después, sus personas pueden empezar en paralelo apenas la Persona 1 termine la Fase 0 (paso 1 a 6), porque el Incremento 2 reutiliza las entidades del Incremento 1.

## Fase 0: base común del proyecto

La Persona 1 deja el proyecto base funcionando en `develop` y los demás lo clonan; nadie empieza a codificar su caso de uso hasta que la página de prueba abra en Tomcat 9 y Hibernate cree las tablas en SQL Server.

### Lo que cada integrante instala en su máquina

1. IntelliJ IDEA Ultimate (licencia gratuita de estudiante en jetbrains.com; la edición Community no trae soporte para Tomcat ni JSP).
2. JDK 17.
3. Apache Tomcat 9 (descargar el .zip y descomprimirlo, por ejemplo en `C:\tomcat9`).
4. SQL Server (Express o Developer) y SQL Server Management Studio (SSMS).
5. Plugin GitHub Copilot en IntelliJ (Settings > Plugins > GitHub Copilot) e inicio de sesión con la cuenta de GitHub que tenga el beneficio de estudiante.
6. Configurar SQL Server para que Java se conecte: en *SQL Server Configuration Manager* habilitar TCP/IP en el puerto 1433 y reiniciar el servicio; en SSMS activar autenticación mixta (SQL Server y Windows) y crear un usuario, por ejemplo `polidinner` con su contraseña. Cada uno usa su propia contraseña local y **no** la sube a GitHub.

### Estructura de paquetes (alineada con el diagrama de robustez)

La estructura reproduce las tres categorías del diagrama de robustez, que es la base de la trazabilidad:

| Elemento del modelo | Capa en el código | Ubicación |
| --- | --- | --- |
| Entidad (círculo con línea abajo) | Clase JPA `@Entity` | `src/main/java/ec/edu/epn/polidinner/modelo` |
| Controlador (círculo con flecha) | Servlet | `src/main/java/ec/edu/epn/polidinner/controlador` |
| Frontera (círculo con línea a la izquierda) | Página JSP | `src/main/webapp/vistas` |
| Acceso a datos (soporte técnico) | DAO + utilidad JPA | `src/main/java/ec/edu/epn/polidinner/persistencia` |

Los nombres de clases y métodos se copian **exactamente** del diagrama de clases (`crearMenuDiario()`, `publicarParaVenta()`, `agregarItem()`, etc.). No traducir ni renombrar, porque el informe debe poder señalar "este método del diagrama es esta línea del código".

### Pasos de la Persona 1 para crear la base

1. Clonar el repositorio en IntelliJ (File > New > Project from Version Control) y crear la rama `develop` desde `main`.
2. Crear el proyecto Maven con arquetipo web (Jakarta EE / Web application en el asistente, eligiendo versión Java EE 8 para que use `javax`), groupId `ec.edu.epn`, artifactId `polidinner`, empaquetado `war`.
3. Poner en el `pom.xml` estas dependencias (versiones compatibles con Tomcat 9):

```xml
<properties>
  <maven.compiler.source>17</maven.compiler.source>
  <maven.compiler.target>17</maven.compiler.target>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
<dependencies>
  <dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>4.0.1</version>
    <scope>provided</scope>
  </dependency>
  <dependency>
    <groupId>javax.servlet.jsp</groupId>
    <artifactId>javax.servlet.jsp-api</artifactId>
    <version>2.3.3</version>
    <scope>provided</scope>
  </dependency>
  <dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>jstl</artifactId>
    <version>1.2</version>
  </dependency>
  <dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>5.6.15.Final</version>
  </dependency>
  <dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.8.1.jre11</version>
  </dependency>
  <dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

4. Crear `src/main/resources/META-INF/persistence.xml` con la unidad de persistencia `PoliDinnerPU`:

```xml
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence" version="2.2">
  <persistence-unit name="PoliDinnerPU" transaction-type="RESOURCE_LOCAL">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
    <properties>
      <property name="javax.persistence.jdbc.driver" value="com.microsoft.sqlserver.jdbc.SQLServerDriver"/>
      <property name="javax.persistence.jdbc.url" value="jdbc:sqlserver://localhost:1433;databaseName=PoliDinner;encrypt=true;trustServerCertificate=true"/>
      <property name="javax.persistence.jdbc.user" value="polidinner"/>
      <property name="javax.persistence.jdbc.password" value="CAMBIAR_LOCALMENTE"/>
      <property name="hibernate.dialect" value="org.hibernate.dialect.SQLServer2012Dialect"/>
      <property name="hibernate.hbm2ddl.auto" value="update"/>
      <property name="hibernate.show_sql" value="true"/>
    </properties>
  </persistence-unit>
</persistence>
```

5. Crear en SSMS la base vacía `PoliDinner` (las tablas las genera Hibernate con `hbm2ddl.auto=update`).
6. Crear `persistencia/JPAUtil.java` (un `EntityManagerFactory` único para toda la aplicación) y `persistencia/GenericDAO.java` con `guardar`, `actualizar`, `buscarPorId`, `listarTodos` y `eliminar`. Estas dos clases las usan las cinco personas; pedírselas a Copilot y revisarlas.
7. Configurar Tomcat en IntelliJ: Run > Edit Configurations > + > Tomcat Server > Local, apuntar a la carpeta de Tomcat 9, en *Deployment* agregar el artefacto `polidinner:war exploded` y poner el contexto `/polidinner`.
8. Crear una `index.jsp` de prueba, ejecutar, y confirmar que abre `http://localhost:8080/polidinner/`.
9. Agregar `.gitignore` (excluir `target/`, `.idea/`, `*.iml`), hacer commit y push a `develop`, y avisar al grupo.

### Flujo de trabajo en Git

- `main`: solo la versión final que se entrega. `develop`: integración. Cada persona trabaja en su rama y abre un Pull Request hacia `develop`; otra persona del mismo incremento lo revisa antes de unir.
- Ramas: `feature/base-proyecto` (P1), `feature/cu02-retirar-item` (P1), `feature/cu01-crear-menu` (P2), `feature/inc2-modelo-pago` (P3), `feature/cu03-realizar-pedido` (P4), `feature/cu04-habilitar-entrega` (P5).
- Cada commit empieza con el caso de uso o elemento que toca, por ejemplo `CU01: implementa Menu.publicarParaVenta() segun diagrama de secuencia`. Así el historial de GitHub también sirve como evidencia de trazabilidad.
- Cada uno hace `git pull origin develop` antes de empezar a trabajar cada día para evitar conflictos.

### Convención de trazabilidad en el código

Cada clase y cada método implementado lleva un Javadoc corto que dice de qué modelo sale:

```java
/**
 * Trazabilidad: Diagrama de clases (Fig. 7) - clase Menu.
 * Diagrama de secuencia CU01 (Fig. 8) - mensaje 10: publicarParaVenta().
 */
public void publicarParaVenta() { ... }
```

Con esto, al armar el informe basta con capturar el diagrama y el fragmento de código lado a lado.

## Incremento 1 · Persona 1: base, modelo y CU 02 Retirar plato y/o producto

La Persona 1 entrega la Fase 0, las cinco entidades del Diagrama de clases del primer incremento (Fig. 7) y el CU 02 completo. Los pasos marcados con **\[Copilot\]** se hacen con GitHub Copilot y se captura la sugerencia para el informe.

### Bloque A: Fase 0 (rama `feature/base-proyecto`)

1. Ejecutar los 9 pasos de la sección anterior y unir a `develop`. Es lo primero, porque bloquea a los otros cuatro.

### Bloque B: Entidades del Incremento 1 (Fig. 7)

2. **\[Copilot\]** Crear en `modelo` la clase abstracta `ItemMenu` con `@Entity` y `@Inheritance(strategy = InheritanceType.JOINED)`. Atributos del diagrama: `nombre`, `precio`, `estado`, `descripcion`. Agregar un `id` técnico (`@Id @GeneratedValue`) y un campo `@Version` (bloqueo optimista; ver paso 12). Método del diagrama: `cambiarEstado(String nuevoEstado)`.
3. **\[Copilot\]** Crear `Plato extends ItemMenu` (atributo `id`, método `getId()`) y `Producto extends ItemMenu` (atributos `stock`, `fechaCaducidad`; método `getStock()`). Como `ItemMenu` ya tiene el `id` técnico, en `Plato` el `getId()` simplemente lo hereda; anotarlo en el Javadoc.
4. **\[Copilot\]** Crear `Menu` con `fecha` (`LocalDate`) y `estado`. Relación "contiene" 1 a muchos con `ItemMenu`: `@OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)` en `Menu` y `@ManyToOne` en `ItemMenu`. Métodos: `publicarParaVenta()` (pone estado "Publicado"), `cerrarParaVenta()` (pone "Cerrado"), `agregarItem(ItemMenu item)` (lo añade a la lista y fija `item.setMenu(this)`), `obtenerItem()`.
5. **\[Copilot\]** Crear `PersonalComedor` con `nombres`, `apellidos`, `rol`, `cedula`. Relación "administra": un `PersonalComedor` tiene muchos `Menu` (`@OneToMany` / `@ManyToOne`). Métodos del diagrama: `crearMenuDiario()` (devuelve un `Menu` nuevo con fecha de hoy y estado "Borrador"), `eliminarPlato(Plato p)` y `eliminarProducto(Producto p)`, que llaman a `cambiarEstado("Agotado")`. No se borra el registro: el caso de prueba dice que el estado pasa a inactivo o agotado.
6. Agregar a `PersonalComedor` un atributo `clave` para el inicio de sesión que usará la Persona 2. Documentarlo en el informe como atributo técnico no modelado, justificado por la entrada del caso de prueba "el personal se identifica exitosamente".
7. Crear los DAO concretos en `persistencia`: `MenuDAO` (con `buscarMenuDelDia()`), `ItemMenuDAO` (con `listarPorMenu(Menu m)`) y `PersonalComedorDAO` (con `buscarPorCedula(String cedula)`), extendiendo `GenericDAO`.
8. Ejecutar la aplicación y verificar en SSMS que Hibernate creó las tablas `Menu`, `ItemMenu`, `Plato`, `Producto` y `PersonalComedor`. Capturar esa pantalla para el informe.
9. Crear `src/main/resources/datos_iniciales.sql` con un usuario del personal de prueba, unir a `develop` y avisar a la Persona 2 y al equipo del Incremento 2.

### Bloque C: CU 02 Retirar plato y/o producto del menú (rama `feature/cu02-retirar-item`)

10. El informe no tiene diagrama de secuencia del CU 02 y la tarea exige implementar los métodos a partir de uno. Antes de codificar, dibujar ese diagrama con el mismo estilo de la Fig. 8. Mensajes sugeridos: el personal abre la lista de ítems; el controlador llama a `obtenerItem()` del menú del día; el personal elige el ítem; el controlador llama a `eliminarPlato(plato)` o `eliminarProducto(producto)`; ese método llama a `cambiarEstado("Agotado")`; el controlador guarda y muestra "Ítem retirado del menú". Numerar los mensajes.
11. **\[Copilot\]** Crear `controlador/RetirarItemMenuServlet` (`@WebServlet("/personal/retirar")`), que corresponde al controlador "Retirar plato y/ producto del menú" del diagrama de robustez. `doGet`: busca el menú del día y envía sus ítems disponibles a la JSP. `doPost`: recibe el id del ítem, lo carga, invoca `eliminarPlato` o `eliminarProducto` según su tipo (`instanceof`), lo actualiza con el DAO y redirige con un mensaje.
12. Manejar la condición del caso de prueba (no retirar un ítem mientras un cliente confirma el pago): gracias al `@Version` del paso 2, si dos transacciones tocan el mismo ítem a la vez, Hibernate lanza `OptimisticLockException`. El servlet la captura y muestra "El ítem está siendo comprado, intente de nuevo". Coordinar con la Persona 3, que hace el pago.
13. **\[Copilot\]** Crear `vistas/personal/retirarItem.jsp` (parte de la frontera "Interfaz del sistema de comida"): tabla con nombre, descripción, precio, estado y un botón "Retirar" por fila. Usar JSTL (`<c:forEach>`) y la plantilla visual de la Persona 2.
14. **\[Copilot\]** Escribir pruebas JUnit en `src/test/java` para `cambiarEstado`, `eliminarPlato` y `eliminarProducto` (verifican que el estado queda "Agotado").
15. Ejecutar el caso de prueba del informe: crear el plato "Seco de Pollo" disponible, retirarlo y verificar que su estado cambia en la base y que ya no aparece en la lista. Capturar cada paso.
16. Abrir el Pull Request hacia `develop` y pedir revisión a la Persona 2.

### Entregables de la Persona 1

- [ ] Proyecto base funcionando en `develop`
- [ ] Entidades Menu, ItemMenu, Plato, Producto, PersonalComedor con DAOs
- [ ] Diagrama de secuencia nuevo del CU 02
- [ ] Servlet, JSP y pruebas del CU 02
- [ ] Capturas y texto de trazabilidad de su parte para el informe

## Incremento 1 · Persona 2: CU 01 Crear Menú, inicio de sesión y plantilla visual

La Persona 2 entrega el CU 01 siguiendo mensaje por mensaje el Diagrama de secuencia de la Fig. 8, además del inicio de sesión del personal y la plantilla visual que reutilizan los otros cuatro. Mientras la Persona 1 termina la Fase 0, la Persona 2 avanza con los pasos 1 a 3, que no dependen del código.

### Bloque A: Preparación (mientras se arma la base)

1. Instalar todo lo de la Fase 0 y probar la conexión a SQL Server desde SSMS con el usuario `polidinner`.
2. Corregir la inconsistencia de la Fig. 8: el texto dice que `publicarParaVenta()` pasa por el controlador, pero el diagrama muestra al actor llamando directo a `menuHoy`. Como en el diagrama de robustez solo los controladores tocan entidades, redibujar el mensaje 10 para que vaya actor → Controlador (Crear Menú) → `menuHoy`. Guardar la versión corregida para el informe.
3. Hacer una tabla de trazabilidad del CU 01 en borrador: cada mensaje numerado de la Fig. 8 (1 al 12) y el método o línea de código que lo implementará. Se completa al terminar el bloque C.

### Bloque B: Plantilla visual e inicio de sesión (rama `feature/cu01-crear-menu`)

4. **\[Copilot\]** Crear `vistas/comun/encabezado.jsp`, `vistas/comun/pie.jsp` y `css/estilos.css` con los colores de la EPN. Todas las JSP del proyecto los incluyen con `<jsp:include>`. Avisar al grupo apenas esté en `develop`.
5. **\[Copilot\]** Crear `vistas/personal/login.jsp` (cédula y clave) y `controlador/LoginPersonalServlet` (`/personal/login`): busca con `PersonalComedorDAO.buscarPorCedula`, compara la clave y guarda el objeto en sesión con `session.setAttribute("personal", p)`.
6. **\[Copilot\]** Crear `controlador/FiltroPersonal` (`@WebFilter("/personal/*")`), que redirige al login si no hay `personal` en sesión, y un `LogoutServlet`.
7. Crear `vistas/personal/panel.jsp`: es la frontera "Interfaz del sistema de comida" del diagrama de robustez. Botones a Crear menú, Retirar plato/producto y (para el Incremento 2) Habilitar entrega.

### Bloque C: CU 01 Crear Menú

8. **\[Copilot\]** Crear `controlador/CrearMenuServlet` (`@WebServlet("/personal/crear-menu")`), que corresponde al controlador "Crear menú". Un único `doPost` con un parámetro `accion` que sigue el orden de la Fig. 8:
   1. `accion=iniciar` (mensajes 1 a 4): toma el personal de la sesión, llama a `personal.crearMenuDiario()`, que crea el `Menu` con fecha de hoy y estado "Borrador"; lo guarda con `MenuDAO` y deja su id en la sesión como `menuHoy`. Si ya existe un menú de hoy, lo reutiliza.
   2. `accion=agregar` (mensajes 5 a 9, el `loop`): lee nombre, descripción y precio; crea `new Plato(nombre, precio, "Disponible", descripcion)` igual que el `<<create>>` del mensaje 6; llama a `menuHoy.agregarItem(nuevoPlato)` y actualiza. Vuelve al formulario con el ítem ya listado, para que el ciclo se repita.
   3. `accion=publicar` (mensajes 10 a 12): llama a `menuHoy.publicarParaVenta()`, guarda y muestra "Menú publicado exitosamente", el texto exacto del mensaje 12.
9. El diagrama de secuencia solo crea objetos `Plato`, pero el diagrama de clases y el CU también incluyen `Producto`. Agregar al formulario un selector Plato/Producto; si es Producto, pedir además stock y fecha de caducidad y crear `new Producto(...)`. Explicarlo en el informe como extensión del mismo `loop`.
10. Validar entradas en el servlet: nombre no vacío, precio numérico mayor que 0. Si falla, volver al formulario con el mensaje de error (cubre la entrada "datos válidos" del caso de prueba).
11. **\[Copilot\]** Crear `vistas/personal/crearMenu.jsp`: formulario del ítem, tabla de ítems ya agregados al borrador y botón "Publicar menú".
12. **\[Copilot\]** Pruebas JUnit para `crearMenuDiario()` (estado "Borrador" y fecha de hoy), `agregarItem()` (la lista crece y el ítem apunta al menú) y `publicarParaVenta()` (estado "Publicado").
13. Ejecutar el caso de prueba: iniciar sesión, crear el plato "Almuerzo Ejecutivo" a $2.50, publicar y verificar en SSMS y en la pantalla que aparece "Disponible". Capturar cada paso.
14. Completar la tabla de trazabilidad del paso 3, abrir el Pull Request y pedir revisión a la Persona 1. Al final del incremento, unir `develop` y probar juntos CU 01 → CU 02 de punta a punta.

### Entregables de la Persona 2

- [ ] Plantilla visual común en `develop`
- [ ] Login, filtro y panel del personal
- [ ] Servlet, JSP y pruebas del CU 01
- [ ] Fig. 8 corregida y tabla de trazabilidad mensaje → código
- [ ] Capturas y texto de su parte para el informe

## Incremento 2 · Persona 3: modelo del incremento 2 y pago con PoliCuenta

La Persona 3 entrega las entidades nuevas del Diagrama de clases del segundo incremento (Fig. 15) y la parte de pago del CU 03 (mensajes `confirmarPago()` en adelante de la Fig. 16). Su primer objetivo es dejar el modelo en `develop` rápido, porque las Personas 4 y 5 lo necesitan.

### Bloque A: Decisiones de modelo (antes de codificar, junto con 4 y 5)

1. Resolver la inconsistencia de nombres: la Fig. 16 usa `:CodigoEntrega`, pero el diagrama de clases tiene la clase `Comprobante` y no existe `CodigoEntrega`. Como la tarea dice que la estructura sale del diagrama de clases, se codifica `Comprobante` y se corrige la Fig. 16 (cambiar `:CodigoEntrega` por `:Comprobante`) y el tipo de retorno de `generarCodigoEntrega()` a `Comprobante`. Dejar ambas figuras corregidas para el informe.
2. Registrar la evolución de `ItemMenu` entre incrementos: en la Fig. 15 gana `stockActual`, `hayStock(int)` y `reducirStock(int)`, y `Producto` deja el atributo `stock`. Esto se presenta en el informe como evolución natural del modelo en el Proceso Unificado, no como error.

### Bloque B: Entidades del Incremento 2 (rama `feature/inc2-modelo-pago`)

3. Actualizar `ItemMenu` (código de la Persona 1): agregar `stockActual` y los métodos `hayStock(int cantidad)` y `reducirStock(int cantidad)` (lanza excepción si no alcanza). En `Producto`, `getStock()` pasa a devolver `stockActual`. Avisar a la Persona 1 y correr sus pruebas para no romper el CU 02.
4. **\[Copilot\]** Crear `ClienteUniversitario` (`cedula`, `nombres`, `apellidos` + `clave` técnica para el login, igual que en el personal). Relaciones: "posee" 1 a 1 con `Policuenta` y "realiza" 1 a muchos con `Pedido`. Métodos: `crearPedido()` (devuelve un `Pedido` nuevo, con fecha de hoy, estado "Creado" y el cliente asignado) y `consultarSaldo()` (delega en su `Policuenta`).
5. **\[Copilot\]** Crear `Policuenta` (`numero`, `saldo`) con `consultarSaldo()` y `debitar(double monto)`: si `saldo >= monto` descuenta y devuelve `true`; si no, devuelve `false`. PoliCuenta es un sistema externo, así que se simula con esta tabla; decirlo explícitamente en el informe.
6. **\[Copilot\]** Crear el esqueleto completo de `Pedido` (`id`, `fecha`, `estado`, `total`; `@OneToMany` "contiene" con `DetallePedido` en cascada; `@ManyToOne` al cliente) con los cuatro métodos del diagrama declarados. `agregarItem` y `calcularTotal` los deja vacíos con un `// TODO Persona 4`.
7. **\[Copilot\]** Crear `DetallePedido` (`cantidad`, `precioUnitario`; `@ManyToOne` "corresponde a" con `ItemMenu`; `calcularSubtotal()` con `// TODO Persona 4`) y `Comprobante` (`idComprobante`, `codigo`, `fechaGeneracion`, `usado`; `@OneToOne` con `Pedido`; `validarCodigoEntrega()` y `marcarCodigoUsado()` con `// TODO Persona 5`).
8. Agregar a `PersonalComedor` las relaciones "entrega" (con `Pedido`) y "valida" (con `Comprobante`) y declarar `validarCodigo(String codigo)` y `entregarPedido(Pedido pedido)` con `// TODO Persona 5`.
9. Crear los DAO: `ClienteUniversitarioDAO` (`buscarPorCedula`), `PedidoDAO`, `PolicuentaDAO` (`buscarPorNumero`) y `ComprobanteDAO` (`buscarPorCodigo`).
10. Ampliar `datos_iniciales.sql` con los datos del caso de prueba: clienta Andrea Gómez con PoliCuenta 2024-0153 y saldo $10.00, y en el menú del día "Arroz con pollo" ($3.50) y "Jugo de naranja" ($0.75) con stock. Ejecutar, revisar las tablas en SSMS, capturar, unir a `develop` y avisar a 4 y 5. **Este paso desbloquea a los demás.**

### Bloque C: Pago del CU 03 (mensajes de `confirmarPago()` en adelante, Fig. 16)

11. Implementar `Pedido.confirmarPago()` en este orden, que replica la Fig. 16: verificar con `hayStock` que cada detalle tiene stock; llamar a `cliente.getPolicuenta().debitar(total)` (mensaje `debitar(monto)`); si devuelve `true` (mensaje "saldo descontado"), poner estado "Pagado" y llamar a `reducirStock(cantidad)` en cada ítem. Devuelve `true` o `false`.
12. Implementar `Pedido.generarCodigoEntrega()`: crea el `Comprobante` (mensaje `<<create>>`) con código `"PD-" + String.format("%04d", id)` (así el pedido 457 da PD-0457, como en el caso de prueba), fecha actual y `usado = false`.
13. **\[Copilot\]** Agregar a `RealizarPedidoServlet` (de la Persona 4; coordinar para no editar el mismo método a la vez) la acción `accion=confirmar`: abre una transacción, llama a `confirmarPago()` y `generarCodigoEntrega()`, hace `commit` y, ante cualquier error, `rollback` para que no se descuente saldo sin pedido. Si el saldo no alcanza, vuelve con "Saldo insuficiente en PoliCuenta".
14. **\[Copilot\]** Crear `vistas/cliente/comprobante.jsp`: número de pedido, detalle, total, saldo restante y el código de retiro en grande.
15. **\[Copilot\]** Pruebas JUnit: `debitar` con saldo suficiente e insuficiente, `confirmarPago` (estado y stock) y `generarCodigoEntrega` (formato PD-0000).
16. Ejecutar la parte final del caso de prueba: confirmar el pedido de Andrea y verificar que el saldo queda en $5.75, que se genera el código y que el stock baja en uno. Capturar.

### Entregables de la Persona 3

- [ ] Entidades del incremento 2 y datos de prueba en `develop`
- [ ] Figuras 15 y 16 corregidas (Comprobante)
- [ ] confirmarPago, generarCodigoEntrega, debitar con transacción
- [ ] comprobante.jsp y pruebas JUnit
- [ ] Capturas y texto de su parte para el informe

## Incremento 2 · Persona 4: CU 03 selección de comida y carrito

La Persona 4 entrega la primera mitad del CU 03, desde `crearPedido()` hasta `calcularTotal()` en la Fig. 16, más el inicio de sesión del cliente y la frontera "Interfaz de pedidos de comida". La Persona 3 continúa desde `confirmarPago()` sobre el mismo servlet.

### Bloque A: Preparación (mientras la Persona 3 sube el modelo)

1. Instalar todo lo de la Fase 0 y clonar `develop`.
2. Hacer el borrador de la tabla de trazabilidad del CU 03: cada mensaje de la Fig. 16 (de `crearPedido()` a "total del pedido") con el método que lo implementará. La Persona 3 agrega luego sus filas.
3. Anotar otra evolución del modelo para el informe: en la Fig. 7 el menú tiene `obtenerItem()` y en la Fig. 15 `obtenerItems()`. Se implementa `obtenerItems()` (nombre del incremento 2) y se explica como refinamiento.

### Bloque B: Inicio de sesión del cliente (rama `feature/cu03-realizar-pedido`)

4. **\[Copilot\]** Crear `vistas/cliente/login.jsp`, `controlador/LoginClienteServlet` (`/cliente/login`, busca con `ClienteUniversitarioDAO.buscarPorCedula`, guarda `cliente` en sesión) y `controlador/FiltroCliente` (`@WebFilter("/cliente/*")`). Reutilizar la plantilla de la Persona 2.

### Bloque C: Selección y carrito (Fig. 16, primera mitad)

5. Implementar `Menu.obtenerItems()`: devuelve solo los ítems con estado "Disponible" de un menú "Publicado". Esto conecta con el CU 02: un plato retirado ya no aparece, que es una condición del caso de prueba.
6. Implementar `DetallePedido.calcularSubtotal()`: `cantidad * precioUnitario`.
7. Implementar `Pedido.agregarItem(ItemMenu item, int cantidad)`: si `item.hayStock(cantidad)`, crea `new DetallePedido(cantidad, item.getPrecio())` (el `<<create>>` de la Fig. 16), lo enlaza al ítem y al pedido y lo añade a la lista. Si el ítem ya estaba, suma la cantidad. Si no hay stock, lanza una excepción con mensaje claro.
8. Implementar `Pedido.calcularTotal()`: recorre los detalles, suma `calcularSubtotal()` de cada uno (mensajes `calcularSubtotal()` y "subtotal"), guarda el resultado en `total` y lo devuelve.
9. **\[Copilot\]** Crear `controlador/RealizarPedidoServlet` (`@WebServlet("/cliente/pedido")`), que es el controlador "Realizar pedido de comida". El `Pedido` vive en la sesión mientras se arma y se guarda en la base recién al confirmar (paso de la Persona 3).
   1. `doGet` / `accion=iniciar`: llama a `cliente.crearPedido()` (mensajes `crearPedido()` y `<<create>> Pedido()`), luego a `menuDelDia.obtenerItems()` y envía la lista a la vista.
   2. `accion=agregar` (el `loop` "por cada ítem seleccionado"): recibe id del ítem y cantidad, llama a `pedido.agregarItem(item, cantidad)` y vuelve con "Ítem agregado".
   3. `accion=quitar`: elimina un detalle (necesario en la práctica; anotarlo como extra no modelado).
   4. `accion=calcular`: llama a `pedido.calcularTotal()` y muestra el total junto con el saldo de la PoliCuenta (`cliente.consultarSaldo()`) y el botón "Confirmar y pagar", que dispara la acción de la Persona 3.
10. **\[Copilot\]** Crear `vistas/cliente/menuDia.jsp` (tarjetas o tabla de ítems disponibles con selector de cantidad y botón "Agregar") y `vistas/cliente/carrito.jsp` (detalles con subtotal, total y saldo). Ambas forman la frontera "Interfaz de pedidos de comida".
11. **\[Copilot\]** Pruebas JUnit: `agregarItem` con y sin stock, `calcularSubtotal` y `calcularTotal`. Incluir el caso del informe: arroz con pollo $3.50 + jugo $0.75 debe dar exactamente $4.25 (comparar con tolerancia, por ser `double`).
12. Ejecutar la primera parte del caso de prueba: Andrea inicia sesión, elige "Arroz con pollo" y "Jugo de naranja" y el sistema muestra $4.25. Luego, junto con la Persona 3, completar el flujo hasta el comprobante. Capturar.
13. Abrir el Pull Request y pedir revisión a la Persona 3.

### Entregables de la Persona 4

- [ ] Login y filtro del cliente
- [ ] obtenerItems, agregarItem, calcularSubtotal, calcularTotal
- [ ] RealizarPedidoServlet, menuDia.jsp y carrito.jsp
- [ ] Pruebas JUnit y tabla de trazabilidad del CU 03 (su mitad)
- [ ] Capturas y texto de su parte para el informe

## Incremento 2 · Persona 5: CU 04 Habilitar entrega de comida y pantalla Salida

La Persona 5 entrega el CU 04 completo, su diagrama de secuencia (que falta en el informe), la frontera común "Salida" del diagrama de robustez (Fig. 13) y la integración final en `main` con el README.

### Bloque A: Modelo faltante del CU 04 (antes de codificar)

1. Dibujar el diagrama de secuencia del CU 04 con el estilo de la Fig. 16, a partir de la Fig. 11 (actividades) y los métodos de la Fig. 15. Mensajes sugeridos y numerados: el personal ingresa el código; el controlador "Habilitar entrega de comida" busca el `Comprobante` por código; llama a `personal.validarCodigo(...)`, que a su vez llama a `comprobante.validarCodigoEntrega()`; si es válido, llama a `personal.entregarPedido(pedido)` y a `comprobante.marcarCodigoUsado()`; finalmente envía "Pedido autorizado para despacho" a la Salida. Incluir un fragmento `alt` para el caso de código inválido (equivale al nodo de decisión \[No\] de la Fig. 11).
2. Decidir con el grupo la firma de `validarCodigo`. En la Fig. 15 es `validarCodigo(codigo: String): boolean`, pero una entidad no debe consultar la base. Opción recomendada: el servlet busca el comprobante con el DAO y llama a `validarCodigo(String codigo, Comprobante comprobante)`. Si se adopta, actualizar la Fig. 15 y explicarlo en el informe: el modelo se ajusta para que modelo y código sigan coincidiendo.
3. Corregir los detalles del informe que tocan al Incremento 2: el título de la sección 2 dice "PRIMER INCREMENTO" (debe decir SEGUNDO), la Fig. 11 dice "Diagrama de clases" (es de actividades) y en la Fig. 12 el segundo `<<trace>>` dice "Retirar plato y/o producto del menú" (debe ser "Habilitar entrega de comida").

### Bloque B: CU 04 (rama `feature/cu04-habilitar-entrega`)

4. Implementar `Comprobante.validarCodigoEntrega()`: devuelve `true` solo si `usado == false`, la `fechaGeneracion` es de hoy y el pedido asociado está "Pagado". Son exactamente las condiciones del caso de prueba ("el código es correcto, del día y no usado").
5. Implementar `Comprobante.marcarCodigoUsado()`: pone `usado = true`.
6. Implementar en `PersonalComedor`: `validarCodigo(...)` (compara el código ingresado con el del comprobante y delega en `validarCodigoEntrega()`) y `entregarPedido(Pedido pedido)` (pone el pedido en "Entregado" y lo asocia al personal por la relación "entrega").
7. **\[Copilot\]** Crear `controlador/HabilitarEntregaServlet` (`@WebServlet("/personal/entrega")`), que es el controlador "Habilitar entrega de comida". `doGet`: muestra el formulario. `doPost`: recibe el código, busca el comprobante, valida, entrega y marca como usado en **una sola transacción**, y envía el resultado a la Salida. Mensajes distintos para: código inexistente, código ya usado, código de otro día y pedido no pagado.
8. **\[Copilot\]** Crear `vistas/personal/habilitarEntrega.jsp` (forma parte de la "Interfaz del sistema de comida"): campo para el código y botón "Validar". Agregar el enlace en el panel de la Persona 2.
9. **\[Copilot\]** Crear `vistas/comun/salida.jsp`, la frontera "Salida" de la Fig. 13 que ven ambos actores: muestra un mensaje de éxito o error y, si aplica, el detalle del pedido (cliente, ítems, total). Ofrecerla a las Personas 3 y 4 para sus mensajes de resultado, así la trazabilidad con la Fig. 13 queda completa.
10. **\[Copilot\]** Pruebas JUnit: `validarCodigoEntrega` con código válido, usado, de ayer y con pedido no pagado; `marcarCodigoUsado`; `entregarPedido`.
11. Ejecutar el caso de prueba: con el pedido N.º 0457 ya pagado (flujo de las Personas 3 y 4), el personal ingresa PD-0457 y el pedido queda autorizado; verificar en SSMS que `usado = 1`. Probar además ingresar PD-0457 por segunda vez y comprobar que el sistema lo rechaza. Capturar ambos.

### Bloque C: Integración final

12. Cuando los cinco Pull Requests estén unidos en `develop`, ejecutar con el grupo el recorrido completo: CU 01 → CU 03 → CU 04, y CU 02 retirando un plato y comprobando que el cliente ya no lo ve.
13. **\[Copilot\]** Escribir el `README.md` del repositorio: descripción de PoliDinner, integrantes, tecnologías y versiones, cómo configurar SQL Server y Tomcat 9, cómo ejecutar, usuarios de prueba, y una tabla que relacione cada caso de uso con sus clases, servlets y JSP.
14. Unir `develop` a `main`, verificar que el repositorio es público y crear una *release* o etiqueta `v1.0-entrega`, cuyo enlace se comparte en la entrega.

### Entregables de la Persona 5

- [ ] Diagrama de secuencia nuevo del CU 04 y figuras corregidas
- [ ] Métodos de Comprobante y PersonalComedor del CU 04
- [ ] HabilitarEntregaServlet, habilitarEntrega.jsp y salida.jsp
- [ ] Pruebas JUnit, README y versión final en `main`
- [ ] Capturas y texto de su parte para el informe

## Uso de GitHub Copilot

Copilot se usa para el código repetitivo (entidades, servlets, JSP, pruebas) y siempre se le da como entrada el modelo: atributos y métodos del diagrama de clases y el orden de mensajes del diagrama de secuencia. Así el código generado ya nace trazable y en el informe se puede mostrar "prompt con el modelo → código sugerido".

### Cómo usarlo en IntelliJ

- **Sugerencias en línea:** escribir el Javadoc de trazabilidad y la firma del método; Copilot propone el cuerpo y se acepta con Tab.
- **Copilot Chat** (panel lateral): para generar una clase completa o una JSP. Pegar el texto de la clase del diagrama en el prompt.
- **Revisar siempre:** Copilot suele sugerir `jakarta.*` (no sirve en Tomcat 9; cambiar a `javax.*`), nombres en inglés (cambiarlos por los del diagrama) o métodos que el diagrama no tiene (quitarlos o justificarlos).

### Prompts sugeridos por tipo de tarea

| Tarea | Prompt de ejemplo para Copilot Chat |
| --- | --- |
| Entidad JPA | Crea la entidad JPA en Java (javax.persistence, Hibernate 5.6) llamada Menu con atributos privados fecha: LocalDate y estado: String, relación OneToMany "contiene" con la clase abstracta ItemMenu y los métodos publicarParaVenta(), cerrarParaVenta(), agregarItem(ItemMenu item) y obtenerItems(). No agregues métodos que no estén en esta lista, salvo getters y setters. |
| Método desde la secuencia | Implementa Pedido.confirmarPago() siguiendo este orden: 1) verificar hayStock de cada DetallePedido, 2) llamar a policuenta.debitar(total), 3) si devuelve true poner estado "Pagado" y llamar a reducirStock en cada ítem, 4) devolver el resultado. |
| Servlet | Crea un HttpServlet con javax.servlet para Tomcat 9, mapeado en /personal/crear-menu, con doPost que según el parámetro accion (iniciar, agregar, publicar) llame a los métodos que te indico y reenvíe a /vistas/personal/crearMenu.jsp. |
| JSP | Crea una JSP con JSTL (taglib http://java.sun.com/jsp/jstl/core) que muestre en una tabla la lista "items" (nombre, descripcion, precio, estado) con un botón Retirar por fila que envíe el id por POST a /personal/retirar. Incluye /vistas/comun/encabezado.jsp y pie.jsp. |
| Pruebas | Escribe pruebas JUnit 5 para Pedido.calcularTotal(): un pedido con un ítem de 3.50 y otro de 0.75, cantidad 1 cada uno, debe dar 4.25 (usa assertEquals con tolerancia 0.001). |
| Commit | Usar el botón de Copilot en la ventana de commit de IntelliJ para generar el mensaje y luego anteponer el código del caso de uso (CU01, CU02…). |

### Evidencia para el informe

Cada persona guarda al menos tres capturas de Copilot en su parte: el prompt con los datos del modelo, la sugerencia generada y el código final con las correcciones que hizo (por ejemplo, el cambio de `jakarta` a `javax`). Mostrar qué se corrigió demuestra criterio propio y no copia ciega.

## Informe PDF y orden de trabajo

Cada persona escribe la sección de lo que implementó y la Persona 2 une todo en un solo PDF, porque al final del Incremento 1 tiene menos carga que el resto.

### Estructura del informe y responsables

| Sección del informe | Responsable | Contenido |
| --- | --- | --- |
| 1. Introducción y entorno | Persona 1 | Objetivo, tecnologías y versiones, capturas de IntelliJ, Tomcat 9 y las tablas en SQL Server, enlace al repositorio |
| 2. Arquitectura y criterio de trazabilidad | Persona 1 | Tabla robustez → paquetes (entidad, controlador, frontera), convención de Javadoc y de commits |
| 3.1 CU 01 Crear Menú | Persona 2 | Fig. 8 corregida junto al código, tabla mensaje → método, Copilot, caso de prueba "Almuerzo Ejecutivo" |
| 3.2 CU 02 Retirar plato/producto | Persona 1 | Diagrama de secuencia nuevo, código, manejo de concurrencia, caso de prueba "Seco de Pollo" |
| 4.1 Modelo del incremento 2 y pago | Persona 3 | Evolución de ItemMenu, corrección Comprobante, confirmarPago y debitar, saldo de $10.00 a $5.75 |
| 4.2 CU 03 Selección y carrito | Persona 4 | Fig. 16 (primera mitad) junto al código, total de $4.25 |
| 4.3 CU 04 Habilitar entrega | Persona 5 | Diagrama de secuencia nuevo, código, salida.jsp, caso de prueba PD-0457 y rechazo del código reutilizado |
| 5. Funcionamiento completo y conclusiones | Persona 5 + todos | Recorrido de punta a punta con capturas, enlace al repositorio público, conclusiones de cada integrante |
| Unión, formato y revisión final | Persona 2 | Numeración de figuras, índice, exportación a PDF |

Para cada método importante, la trazabilidad se muestra con el mismo patrón de tres partes: el fragmento del diagrama (clase o mensaje numerado), el código con su Javadoc de trazabilidad, y la pantalla funcionando o la prueba JUnit en verde.

### Orden de trabajo

1. **Fase 0** (Persona 1): proyecto base en `develop`. Mientras tanto, las Personas 2 a 5 instalan herramientas y corrigen los diagramas que les tocan.
2. **Modelos** (en paralelo): la Persona 1 sube las entidades del Incremento 1; luego la Persona 3 sube las del Incremento 2 y los datos de prueba. Las Personas 2, 4 y 5 preparan sus tablas de trazabilidad y diagramas nuevos.
3. **Casos de uso** (en paralelo): CU 01 (P2), CU 02 (P1), CU 03 carrito (P4), CU 03 pago (P3) y CU 04 (P5), cada uno en su rama con Pull Request hacia `develop`.
4. **Integración del Incremento 1**: Personas 1 y 2 prueban CU 01 → CU 02 juntos.
5. **Integración del Incremento 2**: Personas 3, 4 y 5 prueban CU 03 → CU 04 juntos y verifican que un plato retirado (CU 02) no se pueda pedir.
6. **Cierre**: la Persona 5 une a `main` con el README y la etiqueta `v1.0-entrega`; la Persona 2 arma el PDF con las secciones de todos.

Pendiente para el grupo: fijar las fechas de cada fase según la fecha de entrega y confirmar con el docente el nombre del repositorio.
