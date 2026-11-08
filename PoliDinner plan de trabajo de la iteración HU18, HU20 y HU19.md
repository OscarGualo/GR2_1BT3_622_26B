# PoliDinner: plan de trabajo de la iteración HU18, HU20 y HU19

Guía de implementación sobre el repositorio `OscarGualo/GR2_1BT3_622_26B` (rama `main`, commit `5277373`). El código usa lo que ya existe en el proyecto: paquete `ec.edu.epn.polidinner`, `javax.*` con Tomcat 9 y Hibernate 5.6, las entidades `ItemMenu`/`Producto` (con `stockActual` y `@Version`), `Pedido`, `DetallePedido.item` y `Comprobante.fechaGeneracion`, `JPAUtil`, `Transaccion`, `GenericDAO`, `ProductoDAO`, `RelojAplicacion`/`DiaComedor` para el día del comedor, el personal autenticado en la sesión como `"personal"`, `FiltroPersonal` en `/personal/*`, y la base H2 de las pruebas y del contenedor.

El alcance corresponde a **una iteración de una semana**. El trabajo se separa en **cuatro bloques de evidencia**, con las dependencias indicadas:

| Bloque | Qué incluye | Evidencia |
| --- | --- | --- |
| **1. TDD** | **12 pruebas**: 8 normales, 2 con Mockito y 2 parametrizadas; cada ciclo tiene su propio cambio REFACTOR | Capturas y commits RED, GREEN y REFACTOR de cada ciclo |
| **2. Refactorizaciones** | Solo las **4** técnicas R1–R4 | Antes, después, diff y pruebas verdes; un commit exclusivo cada una |
| **3. Desarrollo normal** | Servlets, JSP, DAO, servicios transaccionales, datos de demostración y comprobaciones de soporte | Solo código y commits; sin capturas TDD |
| **4. Integración** | **1 integración por HU** (3 en total), con Jenkins | Tramo de commits, PR fusionado y build con SHA |

### Alcance de esta guía

Se conserva el alcance funcional y el trabajo paralelo del documento de partida. Para esta entrega se han planificado **12 ciclos con un cambio de refactorización concreto**, más **cuatro refactorizaciones adicionales distintas**. Refactorizar las pruebas también es válido: PU07 mejora su preparación sin debilitar aserciones.

Esta es una decisión de organización y demostración del equipo. TDD en general no obliga a cambiar código en cada ciclo si el diseño ya es adecuado. Aquí se muestran mejoras concretas y no se deja ninguna «pendiente para R1–R4».

La verificación independiente y sus límites se registran al final. No atribuir a los integrantes las ejecuciones de revisión de esta guía: deberán generar sus propios commits y capturas al implementar.

---

### Cómo ejecutar los comandos en Windows

Los bloques `bash` usan Git Bash. En PowerShell, ejecutar cada orden en una línea y cambiar `./mvnw` por `.\mvnw.cmd`; los ejemplos Docker con `\` al final de línea también son de Bash. En IntelliJ se puede ejecutar cada método de prueba con su botón y la suite desde Maven → Lifecycle → test. No capturar un error de terminal como si fuera el RED del comportamiento.

Ficha funcional que debe conservarse: [HU18, HU19 y HU20 con sus criterios y escenarios](#ficha-funcional). No se cambian historias ni condiciones de aceptación mediante una refactorización.

## 1. Responsabilidades

### 1.1 Las 12 pruebas con TDD

| ID | HU | Tipo | Clase de prueba | Responsable | Comprueba | REFACTOR dentro del ciclo |
| --- | --- | --- | --- | --- | --- | --- |
| PU01 | HU18 | Normal | `modelo/CalculadoraVentasProductoTest` | Jean | Suma pedidos del mismo registro sin mezclar registros (18.1.1, 18.1.2) | Extraer la acumulación por referencia. |
| PU02 | HU18 | Normal | `modelo/CalculadoraVentasProductoTest` | Jean | Orden por unidades y desempate por referencia (regla 6) | Extraer el paso de ordenación. |
| PU03 | HU18 | Normal | `modelo/FiltroVentasValidasTest` | Jorge | Solo cuentan Pagados y Entregados (18.3.1) | Extraer el predicado de venta válida. |
| PU04 | HU18 | Normal | `modelo/PeriodoVentasTest` | Mauricio | «Últimos 7 días» = hoy y seis días anteriores (18.2.2) | Introduce Explaining Variable para el límite inicial del periodo. |
| PM01 | HU18 | Mockito | `servicio/ServicioVentasProductoMockTest` | Christian | Consulta por fecha de pago y resume solo ventas válidas (18.2) | Extraer la consulta temporal del servicio. |
| PU07 | HU20 | Normal | `modelo/ValidadorDescarteTest` | Jean | Se ofrece la existencia caducada y no la vigente (20.1.1, 20.1.2) | Extraer la preparación de datos de la prueba, conservando aserciones. |
| PU08 | HU20 | Normal | `modelo/ValidadorDescarteTest` | Jean | No se descarta sin stock ni un histórico (regla 9) | Mover la elegibilidad a Producto y conservar la delegación. |
| PP02 | HU20 | Parametrizada | `modelo/ValidadorDescarteTest` | Jean | Cantidades inválidas y su mensaje (20.2.2) | Extraer la guarda de cantidad positiva. |
| PM02 | HU20 | Mockito | `servicio/ServicioPerdidasMockTest` | Christian | Valida existencia y cantidad antes de descontar (20.2.1) | Extraer el descuento de unidades del servicio. |
| PU05 | HU19 | Normal | `modelo/FiltroCaducidadTest` | Jean | Solo la franja elegida, ordenada (19.1.2, 19.2.2) | Extraer la pertenencia a la franja. |
| PU06 | HU19 | Normal | `modelo/FiltroCaducidadTest` | Jean | Excluye agotados e históricos (19.3.1) | Extraer la disponibilidad para la consulta. |
| PP01 | HU19 | Parametrizada | `modelo/ClasificadorCaducidadTest` | Jorge | Límites de las cuatro franjas | Dar nombre al límite de los próximos tres días. |

### Técnicas admitidas para esta entrega

Solo se acreditan las presentadas en clase: **Extract Method, Inline Method, Replace Temp with Query, Introduce Explaining Variable, Split Temporary Variable, Remove Assignments to Parameters, Substitute Algorithm, Move Method, Move Field y Extract Class**. No contar Rename, cambios de comentarios ni la sustitución de un literal por una constante como una técnica adicional no incluida en este material.

Introduce Explaining Variable extrae una expresión a una variable nueva con significado; Rename solo cambia un identificador. No son la misma transformación. Esta guía usa la primera en PU04, PP01 y R4.

### 1.2 Las 4 refactorizaciones aparte

| ID | Técnica | HU | Dónde | Responsable | Después de |
| --- | --- | --- | --- | --- | --- |
| R1 | Extract Method | HU18 | `CargaDatosIniciales`: lectura de varios recursos | Oscar | Código de carga y suite verdes |
| R2 | Extract Class | HU20 | `PerdidasCaducidadServlet` → `SolicitudDescarte` | Mauricio | Servlet y comprobaciones verdes |
| R3 | Replace Temp with Query | HU19 | `ServicioCaducidad`: consulta de existencias | Christian | Servicio y comprobación H2 verdes |
| R4 | Introduce Explaining Variable | HU20 | `ServicioPerdidas`: confirmación ya registrada | Christian | Registro transaccional y comprobaciones verdes |
|  |  |  |  |  |  |

Son cuatro transformaciones distintas de las 12 de TDD; no se reserva ni se repite una mejora de aquellos ciclos. La consigna pide cuatro refactorizaciones; su separación adicional respecto de TDD es una decisión expresa del equipo.

### 1.3 Commits de cada integrante por HU (mínimo 2)

| Integrante | HU18 | HU20 | HU19 |
| --- | --- | --- | --- |
| **Jean** | PU01 RED · PU01 GREEN · PU02 RED · PU02 GREEN · REFACTOR de cada ciclo | PU07, PU08 y PP02 (RED y GREEN de cada una) · REFACTOR de cada ciclo | PU05 RED · PU05 GREEN · PU05 REFACTOR · PU06 RED · PU06 GREEN · PU06 REFACTOR |
| **Jorge** | PU03 RED · PU03 GREEN · PU03 REFACTOR · vista de ventas | DAO de pérdidas · vista del historial | PP01 RED · PP01 GREEN · PP01 REFACTOR |
| **Mauricio** | PU04 RED · PU04 GREEN · PU04 REFACTOR · servlet de ventas | servlet, vista y prueba de soporte · **R2** | servlet de caducidad · vista de caducidad |
| **Christian** | PM01 RED · PM01 GREEN · PM01 REFACTOR · datos de demostración | PM02 RED · PM02 GREEN · PM02 REFACTOR · registro transaccional · **R4** | servicio de caducidad · comprobación con H2 · **R3** |
| **Oscar** | DAO de ventas y comprobación con H2 · **R1** · acceso en el panel | datos de demostración · acceso en el panel | acceso en el panel · datos de demostración |

- **Oscar** además hace el **commit de contrato** (Fase 0).
- **Mauricio** además hace las **tres integraciones con Jenkins** y las imágenes de Docker Hub. Eso no reemplaza sus commits de desarrollo.

---

## 2. Ramas y trabajo en paralelo

### 2.1 Estructura de ramas

```text
develop
├── (commit de contrato de Oscar: Fase 0)
├── feature/hu18-ventas-productos
│   └── trabajo/hu18-jean, trabajo/hu18-jorge, trabajo/hu18-mauricio, trabajo/hu18-christian, trabajo/hu18-oscar
├── feature/hu20-perdidas-caducidad
│   └── trabajo/hu20-<nombre>   (una por integrante)
└── feature/hu19-franjas-caducidad
    └── trabajo/hu19-<nombre>   (una por integrante)
```

- Cada integrante trabaja **solo en su rama `trabajo/...`**. Los commits RED pueden tener pruebas en rojo: es correcto en esa rama.
- Antes del PR, incorpora la última `feature` a su rama de trabajo, resuelve conflictos y ejecuta las pruebas. Cuando el resultado combinado está en verde, abre un PR `trabajo/... → feature/...` con **«Create a merge commit»**. **Nunca squash**, para conservar los autores. **Nunca se integra en rojo.**
- La `feature` se integra a `develop` con otro PR (merge commit). Esa es **la integración** de la HU, y sobre ese `develop` Mauricio ejecuta Jenkins.

### 2.2 Qué permite el contrato y qué no

El **commit de contrato** de la Fase 0 deja en `develop` todas las clases nuevas con sus métodos. Los métodos que se desarrollan con TDD devuelven un valor por defecto (`null`, `false`, lista vacía o cuerpo vacío), igual que en el incremento 3. Con eso:

- Todos pueden compilar y escribir su prueba desde el inicio, sin esperar el código de otro.
- Cada RED falla **por aserción**, no por compilación.
- Las pruebas con Mockito y con dobles de prueba avanzan aunque el colaborador real todavía no exista.
- Cada persona edita principalmente archivos distintos, lo que reduce los conflictos (no los elimina).

**Lo que el contrato no permite:** los esqueletos no demuestran funcionalidad. Las pantallas completas, las comprobaciones con base de datos y la demostración necesitan el código real de los colaboradores.

**Esperas obligatorias:**

| Quién espera | Qué espera | Motivo |
| --- | --- | --- |
| Todos | El commit de contrato de Oscar | Base común para compilar. |
| Christian (HU20, registro transaccional) | El DAO de Jorge y el GREEN de PP02 de Jean en `feature/hu20` | Su comprobación con H2 usa el historial y los mensajes reales. |
| Christian (HU19, comprobación) | PP01 de Jorge y PU05–PU06 de Jean en `feature/hu19` | Su comprobación usa el clasificador y el filtro reales. |
| Cada refactorización | El código previo y las pruebas pertinentes verdes | R3 y R4 esperan las comprobaciones de integración que se indican en su sección. |
| Recorrido web de cada HU | Todos los PR de esa HU fusionados en su `feature` | La pantalla necesita servlet, vista, servicio y DAO. |
| Cada integración | La integración anterior verde en `develop` | Orden HU18 → HU20 → HU19. Antes de cada PR se incorpora el último `develop` y se repiten las verificaciones. |

Las tres HU se pueden desarrollar al mismo tiempo; solo las integraciones van en orden. Si el profesor exige empezar una HU después de integrar la anterior, el plan no cambia: solo se crean más tarde las ramas de HU20 y HU19.

### 2.3 Comandos

**Oscar, al terminar la Fase 0:**

```bash
git checkout develop && git pull
git checkout -b feature/hu18-ventas-productos && git push -u origin feature/hu18-ventas-productos
git checkout develop
git checkout -b feature/hu20-perdidas-caducidad && git push -u origin feature/hu20-perdidas-caducidad
git checkout develop
git checkout -b feature/hu19-franjas-caducidad && git push -u origin feature/hu19-franjas-caducidad
```

**Cada integrante, para cada HU** (ejemplo: Jean en HU18):

```bash
git fetch origin
git checkout -b trabajo/hu18-jean origin/feature/hu18-ventas-productos
# ... commits del plan ...
git push -u origin trabajo/hu18-jean
# GitHub: PR trabajo/hu18-jean → feature/hu18-ventas-productos (Create a merge commit)
```

**Quien debe esperar a otros** (por ejemplo, Christian en HU19), cuando ya están fusionados:

```bash
git fetch origin
git merge origin/feature/hu19-franjas-caducidad
```

---

## 3. Reglas de evidencia

### 3.1 Ciclo TDD de cada una de las 12 pruebas

| Fase | Qué hacer | Captura | Commit |
| --- | --- | --- | --- |
| **RED** | Escribir **solo la prueba** y ejecutarla. Falla por aserción porque el método del contrato devuelve un valor por defecto. | Prueba en rojo con el nombre del test y el mensaje del fallo. | `HU18/PU01: RED - <qué prueba>` (solo el archivo de prueba) |
| **GREEN** | Escribir el código mínimo indicado y ejecutar. | Prueba en verde. | `HU18/PU01: GREEN - <qué implementa>` |
| **REFACTOR** | Aplicar la mejora concreta indicada sobre código o pruebas, conservando comportamiento y aserciones. Ejecutar **toda la suite** (`./mvnw test`). | Antes, diff del cambio y suite verde. | `HU18/PU01: REFACTOR - <qué mejora>` |

- Cada ciclo incluye el cambio REFACTOR explicado en su sección. Se realiza antes del siguiente RED. Las cuatro adicionales son otras transformaciones, no los mismos cambios con otra etiqueta. No dejar mejoras pendientes para completar una cuota.
- **Nunca se borra código que funciona para fabricar un RED.**
- Ejecutar una prueba: botón verde junto al método en IntelliJ, o `./mvnw test -Dtest=NombreTest`.

**Nombre de capturas:** `capturas/HU18/PU01-1-RED.png`, `PU01-2-GREEN.png`, `PU01-3-REFACTOR.png`.

### 3.2 Cada refactorización (R1–R4)

1. Con la suite en verde: 📸 código **antes** y 📸 suite verde.
2. Aplicar la técnica **sin agregar funcionalidad**.
3. 📸 código **después** y 📸 **diff** (`git diff`).
4. `./mvnw test`: 📸 suite verde.
5. **Commit exclusivo:** `HU18/R1: Extract Method - <qué se extrajo>`. 📸 commit.

Capturas: `capturas``/HU18/R1-1-ANTES.png`, `R1-2-DESPUES.png`, `R1-3-DIFF.png`, `R1-4-VERDE.png`, `R1-5-COMMIT.png`.

### 3.3 Desarrollo normal

Se ejecuta `./mvnw test`, debe pasar, y se hace commit con mensaje `HU18: <qué agrega>`. **Sin capturas RED/GREEN/REFACTOR.** Las comprobaciones de soporte (pruebas con H2 o del servlet) **no cuentan entre las 12** y se nombran como «comprobación de soporte».

---

## 4. Fase 0 — Commit de contrato (Oscar, sobre `develop`)

Este commit **no implementa el comportamiento** de las 12 pruebas: solo deja las clases y los métodos para que todos compilen.

1. `git checkout develop && git pull`.
2. Crear los archivos nuevos de esta sección y aplicar los tres cambios a archivos existentes (4.6).
3. `./mvnw test` en verde (169 pruebas).
4. Commit: `Contrato de la iteración HU18, HU20 y HU19` y push a `develop`.
5. Crear las tres ramas `feature` (sección 2.3) y avisar al equipo.

Antes de esta fase, confirmar que `develop` contiene la base necesaria del commit `5277373` y ejecutar la suite. Si hay cambios posteriores, revisarlos y adaptar los fragmentos; **no retroceder la rama ni reemplazar código nuevo del equipo**.

### 4.1 Modelo de HU18 (`src/main/java/ec/edu/epn/polidinner/modelo/`)

**`src/main/java/ec/edu/epn/polidinner/modelo/VentaProducto.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.util.Date;

/**
 * Soporte técnico NO modelado - HU18 Consultar ventas de productos (no es entidad JPA).
 * Una línea de pedido de un Producto con el estado actual del pedido y la fecha de pago, que es la
 * fecha de generación de su Comprobante (regla 4). La arma VentaProductoDAO.
 */
public final class VentaProducto {

    private final int referencia;
    private final String nombre;
    private final Date fechaCaducidad;
    private final int cantidad;
    private final String estadoPedido;
    private final Date fechaPago;

    public VentaProducto(int referencia, String nombre, Date fechaCaducidad, int cantidad,
                         String estadoPedido, Date fechaPago) {
        this.referencia = referencia;
        this.nombre = nombre;
        this.fechaCaducidad = fechaCaducidad;
        this.cantidad = cantidad;
        this.estadoPedido = estadoPedido;
        this.fechaPago = fechaPago;
    }

    public int getReferencia() {
        return referencia;
    }

    public String getNombre() {
        return nombre;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getEstadoPedido() {
        return estadoPedido;
    }

    public Date getFechaPago() {
        return fechaPago;
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/modelo/FilaVentaProducto.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.util.Date;

/**
 * Soporte técnico NO modelado - HU18 (regla 6): una fila de la tabla «Ventas de productos».
 * Una fila por referencia (registro de Producto) con sus unidades vendidas en el periodo.
 */
public final class FilaVentaProducto {

    private final int referencia;
    private final String nombre;
    private final Date fechaCaducidad;
    private final int unidades;

    public FilaVentaProducto(int referencia, String nombre, Date fechaCaducidad, int unidades) {
        this.referencia = referencia;
        this.nombre = nombre;
        this.fechaCaducidad = fechaCaducidad;
        this.unidades = unidades;
    }

    public int getReferencia() {
        return referencia;
    }

    public String getNombre() {
        return nombre;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public int getUnidades() {
        return unidades;
    }

    @Override
    public String toString() {
        return "FilaVentaProducto[" + referencia + " " + nombre + ": " + unidades + "]";
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/modelo/PeriodoVentas.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * Soporte técnico NO modelado - HU18 (regla 5): periodo de consulta con las fechas inicial y final
 * completas. Los límites se calculan con DiaComedor en la zona del comedor (America/Guayaquil).
 */
public final class PeriodoVentas {

    private final LocalDate inicio;
    private final LocalDate fin;

    public PeriodoVentas(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Seleccione la fecha inicial y la final");
        }
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
        this.inicio = inicio;
        this.fin = fin;
    }

    public static PeriodoVentas hoy(LocalDate hoy) {
        return new PeriodoVentas(hoy, hoy);
    }

    /** «Últimos 7 días»: hoy y los seis días anteriores. Contrato: pendiente de PU04 (Mauricio). */
    public static PeriodoVentas ultimos7Dias(LocalDate hoy) {
        return null;
    }

    /** 00:00 del día inicial en la zona del comedor (incluido). */
    public Date desde(ZoneId zona) {
        return new DiaComedor(inicio, zona).inicio();
    }

    /** 00:00 del día siguiente al final en la zona del comedor (excluido). */
    public Date hastaExclusivo(ZoneId zona) {
        return new DiaComedor(fin, zona).fin();
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFin() {
        return fin;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof PeriodoVentas)) {
            return false;
        }
        PeriodoVentas periodo = (PeriodoVentas) otro;
        return inicio.equals(periodo.inicio) && fin.equals(periodo.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inicio, fin);
    }

    @Override
    public String toString() {
        return "PeriodoVentas[" + inicio + " al " + fin + "]";
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/modelo/CalculadoraVentasProducto.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.util.Collections;
import java.util.List;

/**
 * HU18 (criterio 18.1 y regla 6): resume las ventas válidas en una fila por referencia.
 * Contrato de la iteración: pendiente de PU01 y PU02 (Jean).
 */
public class CalculadoraVentasProducto {

    public List<FilaVentaProducto> resumir(List<VentaProducto> ventas) {
        return Collections.emptyList();
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/modelo/FiltroVentasValidas.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.util.Collections;
import java.util.List;

/**
 * HU18 (escenario 18.3.1 y regla 4): conserva solo las ventas de pedidos que siguen siendo válidos.
 * Contrato de la iteración: pendiente de PU03 (Jorge).
 */
public class FiltroVentasValidas {

    public List<VentaProducto> filtrar(List<VentaProducto> ventas) {
        return Collections.emptyList();
    }
}
```

### 4.2 Modelo de HU20

**`src/main/java/ec/edu/epn/polidinner/modelo/DescarteRechazadoException.java`**

```java
package ec.edu.epn.polidinner.modelo;

/** Soporte técnico NO modelado - HU20: el descarte no cumple las reglas; el mensaje se muestra al personal. */
public class DescarteRechazadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DescarteRechazadoException(String mensaje) {
        super(mensaje);
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/modelo/ValidadorDescarte.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/**
 * HU20 (criterios 20.1 y 20.2): reglas para descartar unidades caducadas de un registro de Producto.
 * Contrato de la iteración: pendiente de PU07, PU08 y PP02 (Jean).
 */
public class ValidadorDescarte {

    public boolean admiteDescarte(Producto producto, LocalDate hoy) {
        return false;
    }

    public void validarCantidad(Producto producto, int cantidad) {
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/modelo/PerdidaCaducidad.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * HU20 Registrar pérdidas por caducidad (reglas 9 a 12): unidades descartadas de un registro de Producto.
 * Conserva el producto de origen, su nombre y caducidad, las unidades, la fecha del registro y el personal
 * responsable. El token identifica la confirmación del formulario: es único, así que una misma
 * confirmación enviada dos veces no genera dos pérdidas (escenario 20.3.2).
 */
@Entity
@Table(name = "perdida_caducidad")
public class PerdidaCaducidad implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_caducidad")
    private Date fechaCaducidad;

    @Column(nullable = false)
    private int unidades;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "responsable_cedula")
    private PersonalComedor responsable;

    @Column(name = "token_operacion", nullable = false, unique = true, length = 40)
    private String tokenOperacion;

    protected PerdidaCaducidad() {
    }

    public PerdidaCaducidad(Producto producto, int unidades, PersonalComedor responsable,
                            LocalDateTime fechaRegistro, String tokenOperacion) {
        this.producto = producto;
        this.nombreProducto = producto.getNombre();
        this.fechaCaducidad = producto.getFechaCaducidad();
        this.unidades = unidades;
        this.responsable = responsable;
        this.fechaRegistro = fechaRegistro;
        this.tokenOperacion = tokenOperacion;
    }

    public int getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    /** Referencia del registro de Producto de origen (su id). */
    public int getReferencia() {
        return producto.getId();
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    /** Caducidad en formato dd/MM/yyyy para el historial. */
    public String getCaducidadTexto() {
        LocalDate fecha = DiaComedor.fechaCivilDe(fechaCaducidad);
        return fecha == null ? "Sin fecha" : fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public int getUnidades() {
        return unidades;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    /** Fecha y hora del registro en formato dd/MM/yyyy HH:mm para el historial. */
    public String getFechaRegistroTexto() {
        return fechaRegistro.format(FECHA_HORA);
    }

    public PersonalComedor getResponsable() {
        return responsable;
    }

    public String getTokenOperacion() {
        return tokenOperacion;
    }
}
```

### 4.3 Modelo de HU19

**`src/main/java/ec/edu/epn/polidinner/modelo/ExistenciaProducto.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Soporte técnico NO modelado - HU19: vista de un registro de Producto para la consulta por franjas.
 * La referencia es el id existente del registro (regla 2); no es un código comercial nuevo.
 */
public final class ExistenciaProducto {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int referencia;
    private final String nombre;
    private final int stock;
    private final LocalDate caducidad;
    private final boolean habilitado;

    public ExistenciaProducto(int referencia, String nombre, int stock, LocalDate caducidad, boolean habilitado) {
        this.referencia = referencia;
        this.nombre = nombre;
        this.stock = stock;
        this.caducidad = caducidad;
        this.habilitado = habilitado;
    }

    /** Copia los datos que necesita la consulta desde el registro de Producto. */
    public static ExistenciaProducto desde(Producto producto) {
        return new ExistenciaProducto(producto.getId(), producto.getNombre(), producto.getStockActual(),
                DiaComedor.fechaCivilDe(producto.getFechaCaducidad()), producto.isHabilitado());
    }

    public int getReferencia() {
        return referencia;
    }

    public String getNombre() {
        return nombre;
    }

    public int getStock() {
        return stock;
    }

    public LocalDate getCaducidad() {
        return caducidad;
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    /** Caducidad en formato dd/MM/yyyy para las vistas («Sin fecha» si no tiene). */
    public String getCaducidadTexto() {
        return caducidad == null ? "Sin fecha" : caducidad.format(FORMATO);
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/modelo/FranjaCaducidad.java`**

```java
package ec.edu.epn.polidinner.modelo;

/** Soporte técnico NO modelado - HU19: franjas de caducidad (no son franjas de retiro). */
public enum FranjaCaducidad {
    CADUCADOS("Caducados"),
    CADUCAN_HOY("Caducan hoy"),
    PROXIMOS_3_DIAS("Próximos 3 días"),
    MAS_ADELANTE("Más adelante");

    private final String etiqueta;

    FranjaCaducidad(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/modelo/ClasificadorFranja.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/** Soporte técnico NO modelado - HU19: decide la franja de una fecha de caducidad respecto de hoy. */
public interface ClasificadorFranja {

    FranjaCaducidad clasificar(LocalDate caducidad, LocalDate hoy);
}
```

**`src/main/java/ec/edu/epn/polidinner/modelo/ClasificadorCaducidad.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/**
 * HU19 (tabla de franjas): ubica cada fecha de caducidad en su franja.
 * Contrato de la iteración: pendiente de PP01 (Jorge).
 */
public class ClasificadorCaducidad implements ClasificadorFranja {

    @Override
    public FranjaCaducidad clasificar(LocalDate caducidad, LocalDate hoy) {
        return null;
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/modelo/FiltroCaducidad.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * HU19 (criterios 19.1 a 19.3): existencias de la franja elegida.
 * Contrato de la iteración: pendiente de PU05 y PU06 (Jean).
 */
public class FiltroCaducidad {

    private final ClasificadorFranja clasificador;

    public FiltroCaducidad(ClasificadorFranja clasificador) {
        this.clasificador = clasificador;
    }

    public List<ExistenciaProducto> filtrar(List<ExistenciaProducto> existencias, FranjaCaducidad franja,
                                            LocalDate hoy) {
        return Collections.emptyList();
    }
}
```

### 4.4 Servicios (`servicio/`)

**`src/main/java/ec/edu/epn/polidinner/servicio/ServicioVentasProducto.java`**

```java
package ec.edu.epn.polidinner.servicio;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import ec.edu.epn.polidinner.modelo.CalculadoraVentasProducto;
import ec.edu.epn.polidinner.modelo.FilaVentaProducto;
import ec.edu.epn.polidinner.modelo.FiltroVentasValidas;
import ec.edu.epn.polidinner.modelo.PeriodoVentas;
import ec.edu.epn.polidinner.persistencia.VentaProductoDAO;

/**
 * Soporte técnico NO modelado - HU18 (criterio 18.2): consulta las ventas por fecha de pago y entrega
 * solo las válidas al resumen. Contrato de la iteración: pendiente de PM01 (Christian).
 */
public class ServicioVentasProducto {

    private final VentaProductoDAO ventaDAO;
    private final FiltroVentasValidas filtro;
    private final CalculadoraVentasProducto calculadora;

    public ServicioVentasProducto() {
        this(new VentaProductoDAO(), new FiltroVentasValidas(), new CalculadoraVentasProducto());
    }

    ServicioVentasProducto(VentaProductoDAO ventaDAO, FiltroVentasValidas filtro,
                           CalculadoraVentasProducto calculadora) {
        this.ventaDAO = ventaDAO;
        this.filtro = filtro;
        this.calculadora = calculadora;
    }

    public List<FilaVentaProducto> consultar(PeriodoVentas periodo, ZoneId zona) {
        return Collections.emptyList();
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/servicio/ServicioPerdidas.java`**

```java
package ec.edu.epn.polidinner.servicio;

import java.time.LocalDateTime;

import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.modelo.ValidadorDescarte;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;

/**
 * Soporte técnico NO modelado - HU20 Registrar pérdidas por caducidad (reglas 9 a 11).
 * Contrato de la iteración: aplicarDescarte pendiente de PM02 y registrar pendiente (Christian, HU20).
 */
public class ServicioPerdidas {

    private final PerdidaCaducidadDAO perdidaDAO;
    private final ProductoDAO productoDAO;
    private final ValidadorDescarte validador;

    public ServicioPerdidas() {
        this(new PerdidaCaducidadDAO(), new ProductoDAO(), new ValidadorDescarte());
    }

    ServicioPerdidas(PerdidaCaducidadDAO perdidaDAO, ProductoDAO productoDAO, ValidadorDescarte validador) {
        this.perdidaDAO = perdidaDAO;
        this.productoDAO = productoDAO;
        this.validador = validador;
    }

    /** Valida el descarte, descuenta las unidades del registro y arma la pérdida (sin guardarla). */
    PerdidaCaducidad aplicarDescarte(Producto producto, int unidades, PersonalComedor responsable,
                                     LocalDateTime momento, String tokenOperacion) {
        return null;
    }

    /**
     * Registra la pérdida y descuenta el stock en una sola transacción.
     *
     * @return true si se registró; false si esa misma confirmación ya estaba registrada
     */
    public boolean registrar(int idProducto, int unidades, PersonalComedor responsable, String tokenOperacion,
                             LocalDateTime ahora) {
        return false;
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/servicio/ServicioCaducidad.java`**

```java
package ec.edu.epn.polidinner.servicio;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import ec.edu.epn.polidinner.modelo.ClasificadorCaducidad;
import ec.edu.epn.polidinner.modelo.ExistenciaProducto;
import ec.edu.epn.polidinner.modelo.FiltroCaducidad;
import ec.edu.epn.polidinner.modelo.FranjaCaducidad;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;

/**
 * Soporte técnico NO modelado - HU19: existencias de la franja elegida a partir de los registros de
 * Producto. Contrato de la iteración: pendiente (Christian, HU19).
 */
public class ServicioCaducidad {

    private final ProductoDAO productoDAO;
    private final FiltroCaducidad filtro;

    public ServicioCaducidad() {
        this(new ProductoDAO(), new FiltroCaducidad(new ClasificadorCaducidad()));
    }

    ServicioCaducidad(ProductoDAO productoDAO, FiltroCaducidad filtro) {
        this.productoDAO = productoDAO;
        this.filtro = filtro;
    }

    public List<ExistenciaProducto> consultar(FranjaCaducidad franja, LocalDate hoy) {
        return Collections.emptyList();
    }
}
```

### 4.5 DAO (`persistencia/`)

**`src/main/java/ec/edu/epn/polidinner/persistencia/VentaProductoDAO.java`**

```java
package ec.edu.epn.polidinner.persistencia;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import ec.edu.epn.polidinner.modelo.VentaProducto;

/**
 * Soporte técnico NO modelado - HU18: líneas de pedido de Productos cuyo comprobante se generó en el periodo.
 * Contrato de la iteración: pendiente (Oscar, HU18).
 */
public class VentaProductoDAO {

    /** Ventas de productos con Comprobante.fechaGeneracion en [desde, hasta). */
    public List<VentaProducto> buscarPorFechaDePago(Date desde, Date hasta) {
        return Collections.emptyList();
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/persistencia/PerdidaCaducidadDAO.java`**

```java
package ec.edu.epn.polidinner.persistencia;

import java.util.Collections;
import java.util.List;

import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;

/**
 * Soporte técnico NO modelado - HU20: pérdidas por caducidad y su historial.
 * Contrato de la iteración: pendiente (Jorge, HU20).
 */
public class PerdidaCaducidadDAO extends GenericDAO<PerdidaCaducidad, Integer> {

    public PerdidaCaducidadDAO() {
        super(PerdidaCaducidad.class);
    }

    /** ¿Ya se registró una pérdida con este token de confirmación? (dentro de la transacción) */
    public boolean existeToken(String tokenOperacion, Transaccion tx) {
        return false;
    }

    /** ¿Ya se registró una pérdida con este token de confirmación? (fuera de una transacción) */
    public boolean existeToken(String tokenOperacion) {
        return false;
    }

    /** Historial de pérdidas, de la más reciente a la más antigua (regla 11). */
    public List<PerdidaCaducidad> listarRecientes() {
        return Collections.emptyList();
    }
}
```

### 4.6 Cambios en archivos existentes

**`src/main/resources/META-INF/persistence.xml`**: agregar la entidad nueva después de `ConfiguracionCupos`:

```xml
        <class>ec.edu.epn.polidinner.modelo.PerdidaCaducidad</class>
```

**`persistencia/CargaDatosIniciales.java`**: admitir varios scripts separados por coma (para cargar los datos de demostración en el contenedor). Agregar al Javadoc de la clase la línea «Iteración HU18-HU20: admite varios scripts separados por coma, que se ejecutan en ese orden» y reemplazar el inicio de `cargarSiCorresponde()`:

```java
    /** Ejecuta el script indicado en POLIDINNER_DATOS_INICIALES; sin la variable no hace nada. */
    public static int cargarSiCorresponde() throws IOException {
        String script = System.getenv(VARIABLE_SCRIPT);
        if (script == null || script.isEmpty()) {
            return 0;
        }
        List<String> sentencias = new ArrayList<>();
        for (String nombre : script.split(",")) {
            if (!nombre.trim().isEmpty()) {
                sentencias.addAll(separarSentencias(leerRecurso(nombre.trim())));
            }
        }
        // ... (el resto del método no cambia)
```

**`src/main/webapp/vistas/personal/panel.jsp`**: en la sección «Servicio y ventas», después del `</li>` de «Ocupación de mesas», agregar:

```jsp
                <%-- Iteración HU18-HU20: cada acceso se completa en la integración de su HU --%>
                <%@ include file="accesos/ventasProductos.jspf" %>
                <%@ include file="accesos/perdidasCaducidad.jspf" %>
                <%@ include file="accesos/caducidadProductos.jspf" %>
```

### 4.7 Fragmentos JSP vacíos

Cada acceso del panel y el historial tienen su propio archivo, para que nadie edite `panel.jsp` en las HU. La primera línea fija la codificación (sin ella, las tildes del fragmento se ven mal):

**`src/main/webapp/vistas/personal/accesos/ventasProductos.jspf`**

```jsp
<%@ page pageEncoding="UTF-8" %>
<%-- Acceso del panel: se completa en la HU correspondiente. --%>
```

**`src/main/webapp/vistas/personal/accesos/perdidasCaducidad.jspf`**

```jsp
<%@ page pageEncoding="UTF-8" %>
<%-- Acceso del panel: se completa en la HU correspondiente. --%>
```

**`src/main/webapp/vistas/personal/accesos/caducidadProductos.jspf`**

```jsp
<%@ page pageEncoding="UTF-8" %>
<%-- Acceso del panel: se completa en la HU correspondiente. --%>
```

**`src/main/webapp/vistas/personal/historialPerdidas.jspf`**

```jsp
<%@ page pageEncoding="UTF-8" %>
<%-- Historial de pérdidas: se completa en HU20 (Jorge). --%>
```

---

## 5. HU18 — Consultar ventas de productos

Rama compartida: `feature/hu18-ventas-productos`. Todos empiezan al mismo tiempo.

### 5.1 Jean — PU01 y PU02 (TDD)

Rama: `trabajo/hu18-jean`

**PU01 — RED.** Crear `src/test/java/ec/edu/epn/polidinner/modelo/CalculadoraVentasProductoTest.java`:

**`src/test/java/ec/edu/epn/polidinner/modelo/CalculadoraVentasProductoTest.java`**

```java
package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * HU18 Consultar ventas de productos - TDD de CalculadoraVentasProducto (PU01 y PU02).
 * Datos de la demostración: Agua 500 ml (referencias 101 y 102) y Galletas 100 g (103).
 */
class CalculadoraVentasProductoTest {

    private static final Date MANANA = Date.valueOf(LocalDate.of(2026, 11, 9));
    private static final Date HOY_10H = new Date(java.sql.Timestamp.valueOf("2026-11-08 10:00:00").getTime());

    private final CalculadoraVentasProducto calculadora = new CalculadoraVentasProducto();

    private static VentaProducto venta(int referencia, String nombre, int cantidad) {
        return new VentaProducto(referencia, nombre, MANANA, cantidad, "Pagado", HOY_10H);
    }

    /** PU01 - Escenarios 18.1.1 y 18.1.2: suma los pedidos del mismo registro sin mezclar registros. */
    @Test
    void sumaLasUnidadesDeVariosPedidosDelMismoRegistroSinMezclarRegistros() {
        List<VentaProducto> ventas = List.of(
                venta(101, "Agua 500 ml", 6),
                venta(101, "Agua 500 ml", 4),
                venta(102, "Agua 500 ml", 4));

        List<FilaVentaProducto> filas = calculadora.resumir(ventas);

        assertEquals(2, filas.size());
        assertEquals(101, filas.get(0).getReferencia());
        assertEquals(10, filas.get(0).getUnidades());
        assertEquals(102, filas.get(1).getReferencia());
        assertEquals(4, filas.get(1).getUnidades());
    }
}
```

Falla con `expected: <2> but was: <0>`. 📸 `PU01-1-RED`. Commit `HU18/PU01: RED - suma las ventas del mismo registro`.

**PU01 — GREEN.** Reemplazar `CalculadoraVentasProducto`:

**`src/main/java/ec/edu/epn/polidinner/modelo/CalculadoraVentasProducto.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HU18 (criterio 18.1 y regla 6): resume las ventas válidas en una fila por referencia.
 * Origen TDD: CalculadoraVentasProductoTest (PU01 y PU02).
 */
public class CalculadoraVentasProducto {

    public List<FilaVentaProducto> resumir(List<VentaProducto> ventas) {
        Map<Integer, FilaVentaProducto> filas = new LinkedHashMap<>();
        for (VentaProducto venta : ventas) {
            FilaVentaProducto actual = filas.get(venta.getReferencia());
            int unidades = venta.getCantidad() + (actual == null ? 0 : actual.getUnidades());
            filas.put(venta.getReferencia(), new FilaVentaProducto(venta.getReferencia(), venta.getNombre(),
                    venta.getFechaCaducidad(), unidades));
        }
        return new ArrayList<>(filas.values());
    }
}
```

Pasa. 📸 `PU01-2-GREEN`. Commit `HU18/PU01: GREEN - acumula unidades por referencia`.

**PU01 — REFACTOR.** **Técnica: Extract Method.** Extraer la acumulación permite leer `resumir` como una operación de resumen y deja el recorrido del mapa en un método con nombre.

Con las pruebas verdes, capture el código GREEN antes de modificarlo. Aplique este cambio sin alterar las condiciones ni los resultados de la prueba:

**`src/main/java/ec/edu/epn/polidinner/modelo/CalculadoraVentasProducto.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HU18 (criterio 18.1 y regla 6): resume las ventas válidas en una fila por referencia.
 * Origen TDD: CalculadoraVentasProductoTest (PU01 y PU02).
 */
public class CalculadoraVentasProducto {

    public List<FilaVentaProducto> resumir(List<VentaProducto> ventas) {
        return new ArrayList<>(acumularUnidadesPorReferencia(ventas).values());
    }

    private Map<Integer, FilaVentaProducto> acumularUnidadesPorReferencia(List<VentaProducto> ventas) {
        Map<Integer, FilaVentaProducto> filas = new LinkedHashMap<>();
        for (VentaProducto venta : ventas) {
            FilaVentaProducto actual = filas.get(venta.getReferencia());
            int unidades = venta.getCantidad() + (actual == null ? 0 : actual.getUnidades());
            filas.put(venta.getReferencia(), new FilaVentaProducto(venta.getReferencia(), venta.getNombre(),
                    venta.getFechaCaducidad(), unidades));
        }
        return filas;
    }
}
```

Ejecute la prueba de `CalculadoraVentasProductoTest` y después `./mvnw test`. Capture el diff y el resultado verde como `PU01-3-REFACTOR`. Commit **`HU18/PU01: REFACTOR - Extract Method: extrae la acumulación por referencia`**. Este cambio pertenece al ciclo PU01; **no cuenta entre R1–R4**.

**PU02 — RED.** Agregar el segundo método; la clase queda así:

**`src/test/java/ec/edu/epn/polidinner/modelo/CalculadoraVentasProductoTest.java`**

```java
package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * HU18 Consultar ventas de productos - TDD de CalculadoraVentasProducto (PU01 y PU02).
 * Datos de la demostración: Agua 500 ml (referencias 101 y 102) y Galletas 100 g (103).
 */
class CalculadoraVentasProductoTest {

    private static final Date MANANA = Date.valueOf(LocalDate.of(2026, 11, 9));
    private static final Date HOY_10H = new Date(java.sql.Timestamp.valueOf("2026-11-08 10:00:00").getTime());

    private final CalculadoraVentasProducto calculadora = new CalculadoraVentasProducto();

    private static VentaProducto venta(int referencia, String nombre, int cantidad) {
        return new VentaProducto(referencia, nombre, MANANA, cantidad, "Pagado", HOY_10H);
    }

    /** PU01 - Escenarios 18.1.1 y 18.1.2: suma los pedidos del mismo registro sin mezclar registros. */
    @Test
    void sumaLasUnidadesDeVariosPedidosDelMismoRegistroSinMezclarRegistros() {
        List<VentaProducto> ventas = List.of(
                venta(101, "Agua 500 ml", 6),
                venta(101, "Agua 500 ml", 4),
                venta(102, "Agua 500 ml", 4));

        List<FilaVentaProducto> filas = calculadora.resumir(ventas);

        assertEquals(2, filas.size());
        assertEquals(101, filas.get(0).getReferencia());
        assertEquals(10, filas.get(0).getUnidades());
        assertEquals(102, filas.get(1).getReferencia());
        assertEquals(4, filas.get(1).getUnidades());
    }

    /** PU02 - Regla 6: unidades de mayor a menor y, en empate, referencia ascendente. */
    @Test
    void ordenaPorUnidadesDescendentesYDesempataPorReferencia() {
        List<VentaProducto> ventas = List.of(
                venta(105, "Agua 500 ml", 1),
                venta(103, "Galletas 100 g", 5),
                venta(101, "Agua 500 ml", 5));

        List<FilaVentaProducto> filas = calculadora.resumir(ventas);

        assertEquals(List.of(101, 103, 105), filas.stream().map(FilaVentaProducto::getReferencia).toList());
    }
}
```

PU02 falla con `expected: <[101, 103, 105]> but was: <[105, 103, 101]>`; PU01 sigue verde. 📸 `PU02-1-RED`. Commit `HU18/PU02: RED - orden por unidades y referencia`.

**PU02 — GREEN.**

**`src/main/java/ec/edu/epn/polidinner/modelo/CalculadoraVentasProducto.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HU18 (criterio 18.1 y regla 6): resume las ventas válidas en una fila por referencia.
 * Origen TDD: CalculadoraVentasProductoTest (PU01 y PU02).
 */
public class CalculadoraVentasProducto {

    public List<FilaVentaProducto> resumir(List<VentaProducto> ventas) {
        List<FilaVentaProducto> resultado = new ArrayList<>(acumularUnidadesPorReferencia(ventas).values());
        resultado.sort(Comparator.comparingInt(FilaVentaProducto::getUnidades).reversed()
                .thenComparingInt(FilaVentaProducto::getReferencia));
        return resultado;
    }

    private Map<Integer, FilaVentaProducto> acumularUnidadesPorReferencia(List<VentaProducto> ventas) {
        Map<Integer, FilaVentaProducto> filas = new LinkedHashMap<>();
        for (VentaProducto venta : ventas) {
            FilaVentaProducto actual = filas.get(venta.getReferencia());
            int unidades = venta.getCantidad() + (actual == null ? 0 : actual.getUnidades());
            filas.put(venta.getReferencia(), new FilaVentaProducto(venta.getReferencia(), venta.getNombre(),
                    venta.getFechaCaducidad(), unidades));
        }
        return filas;
    }
}
```

Pasan las dos. 📸 `PU02-2-GREEN`. Commit `HU18/PU02: GREEN - ordena el resumen de ventas`.

**PU02 — REFACTOR.** **Técnica: Extract Method.** Extraer la ordenación separa los dos pasos de `resumir`: acumular y ordenar. Se conserva el orden descendente y el desempate por referencia.

Con las pruebas verdes, capture el código GREEN antes de modificarlo. Aplique este cambio sin alterar las condiciones ni los resultados de la prueba:

**`src/main/java/ec/edu/epn/polidinner/modelo/CalculadoraVentasProducto.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HU18 (criterio 18.1 y regla 6): resume las ventas válidas en una fila por referencia.
 * Origen TDD: CalculadoraVentasProductoTest (PU01 y PU02).
 */
public class CalculadoraVentasProducto {

    public List<FilaVentaProducto> resumir(List<VentaProducto> ventas) {
        List<FilaVentaProducto> resultado = new ArrayList<>(acumularUnidadesPorReferencia(ventas).values());
        ordenarPorUnidadesYReferencia(resultado);
        return resultado;
    }

    private Map<Integer, FilaVentaProducto> acumularUnidadesPorReferencia(List<VentaProducto> ventas) {
        Map<Integer, FilaVentaProducto> filas = new LinkedHashMap<>();
        for (VentaProducto venta : ventas) {
            FilaVentaProducto actual = filas.get(venta.getReferencia());
            int unidades = venta.getCantidad() + (actual == null ? 0 : actual.getUnidades());
            filas.put(venta.getReferencia(), new FilaVentaProducto(venta.getReferencia(), venta.getNombre(),
                    venta.getFechaCaducidad(), unidades));
        }
        return filas;
    }

    private void ordenarPorUnidadesYReferencia(List<FilaVentaProducto> filas) {
        filas.sort(Comparator.comparingInt(FilaVentaProducto::getUnidades).reversed()
                .thenComparingInt(FilaVentaProducto::getReferencia));
    }
}
```

Ejecute la prueba de `CalculadoraVentasProductoTest` y después `./mvnw test`. Capture el diff y el resultado verde como `PU02-3-REFACTOR`. Commit **`HU18/PU02: REFACTOR - Extract Method: nombra el paso de ordenación`**. Este cambio pertenece al ciclo PU02; **no cuenta entre R1–R4**.

### 5.2 Jorge — PU03 (TDD) y vista

Rama: `trabajo/hu18-jorge`

**RED.** Crear `src/test/java/ec/edu/epn/polidinner/modelo/FiltroVentasValidasTest.java`:

**`src/test/java/ec/edu/epn/polidinner/modelo/FiltroVentasValidasTest.java`**

```java
package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/** HU18 Consultar ventas de productos - TDD de FiltroVentasValidas (PU03). */
class FiltroVentasValidasTest {

    private static final Date MANANA = Date.valueOf(LocalDate.of(2026, 11, 9));

    private final FiltroVentasValidas filtro = new FiltroVentasValidas();

    private static VentaProducto venta(int cantidad, String estado) {
        return new VentaProducto(103, "Galletas 100 g", MANANA, cantidad, estado, new java.util.Date());
    }

    /** PU03 - Escenario 18.3.1: solo cuentan los pedidos Pagados o Entregados. */
    @Test
    void conservaSoloLasVentasDePedidosPagadosOEntregados() {
        List<VentaProducto> ventas = List.of(
                venta(2, "Pagado"),
                venta(3, "Entregado"),
                venta(4, "Cancelado"),
                venta(1, "Creado"));

        List<VentaProducto> validas = filtro.filtrar(ventas);

        assertEquals(List.of("Pagado", "Entregado"),
                validas.stream().map(VentaProducto::getEstadoPedido).toList());
        assertEquals(5, validas.stream().mapToInt(VentaProducto::getCantidad).sum());
    }
}
```

Falla con `expected: <[Pagado, Entregado]> but was: <[]>`. 📸 `PU03-1-RED`. Commit `HU18/PU03: RED - solo ventas válidas`.

**GREEN.**

**`src/main/java/ec/edu/epn/polidinner/modelo/FiltroVentasValidas.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.util.List;

/**
 * HU18 (escenario 18.3.1 y regla 4): conserva solo las ventas de pedidos que siguen siendo válidos.
 * Origen TDD: FiltroVentasValidasTest (PU03).
 */
public class FiltroVentasValidas {

    public List<VentaProducto> filtrar(List<VentaProducto> ventas) {
        return ventas.stream()
                .filter(venta -> "Pagado".equals(venta.getEstadoPedido())
                        || "Entregado".equals(venta.getEstadoPedido()))
                .toList();
    }
}
```

Pasa. 📸 `PU03-2-GREEN`. Commit `HU18/PU03: GREEN - filtra pedidos Pagados y Entregados`.

**PU03 — REFACTOR.** **Técnica: Extract Method.** Extraer el predicado da nombre a la regla de qué estados cuentan como venta, sin cambiar los estados aceptados.

Con las pruebas verdes, capture el código GREEN antes de modificarlo. Aplique este cambio sin alterar las condiciones ni los resultados de la prueba:

**`src/main/java/ec/edu/epn/polidinner/modelo/FiltroVentasValidas.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.util.List;

/**
 * HU18 (escenario 18.3.1 y regla 4): conserva solo las ventas de pedidos que siguen siendo válidos.
 * Origen TDD: FiltroVentasValidasTest (PU03).
 */
public class FiltroVentasValidas {

    public List<VentaProducto> filtrar(List<VentaProducto> ventas) {
        return ventas.stream()
                .filter(this::esVentaValida)
                .toList();
    }

    private boolean esVentaValida(VentaProducto venta) {
        return "Pagado".equals(venta.getEstadoPedido()) || "Entregado".equals(venta.getEstadoPedido());
    }
}
```

Ejecute la prueba de `FiltroVentasValidasTest` y después `./mvnw test`. Capture el diff y el resultado verde como `PU03-3-REFACTOR`. Commit **`HU18/PU03: REFACTOR - Extract Method: nombra la regla de venta válida`**. Este cambio pertenece al ciclo PU03; **no cuenta entre R1–R4**.

**Vista (normal).** Crear `src/main/webapp/vistas/personal/ventasProductos.jsp`:

**`src/main/webapp/vistas/personal/ventasProductos.jsp`**

```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- HU18 Consultar ventas de productos: unidades vendidas por registro de producto en un periodo.
     Cuentan los pedidos Pagados o Entregados según la fecha de pago (comprobante). --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Ventas de productos" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Ventas de productos
    </nav>
    <div class="titulo-pagina">
        <h1>Ventas de productos</h1>
        <p>Unidades vendidas de cada registro de producto. Cuentan los pedidos pagados o entregados, según la fecha de pago.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <section class="tarjeta" aria-labelledby="titulo-filtro">
        <h2 id="titulo-filtro">Periodo</h2>
        <form method="get" action="${pageContext.request.contextPath}/personal/ventas-productos">
            <fieldset class="campo">
                <legend>Seleccione el periodo</legend>
                <label><input type="radio" name="periodo" value="hoy" <c:if test="${opcion == 'hoy'}">checked</c:if>> Hoy</label>
                <label><input type="radio" name="periodo" value="7dias" <c:if test="${opcion == '7dias'}">checked</c:if>> Últimos 7 días</label>
                <label><input type="radio" name="periodo" value="rango" <c:if test="${opcion == 'rango'}">checked</c:if>> Rango de fechas</label>
            </fieldset>
            <div class="fila-campos">
                <div class="campo">
                    <label for="inicio">Desde</label>
                    <input class="entrada" type="date" id="inicio" name="inicio" value="<c:out value='${param.inicio}'/>">
                </div>
                <div class="campo">
                    <label for="fin">Hasta</label>
                    <input class="entrada" type="date" id="fin" name="fin" value="<c:out value='${param.fin}'/>">
                </div>
            </div>
            <button type="submit" class="btn btn-primario">Consultar</button>
        </form>
    </section>

    <c:if test="${consultado}">
        <section class="tarjeta" aria-labelledby="titulo-resultado">
            <h2 id="titulo-resultado">Del ${periodo.inicio} al ${periodo.fin}</h2>
            <c:choose>
                <c:when test="${empty filas}">
                    <div class="vacio"><p>No hay ventas de productos en el periodo seleccionado</p></div>
                </c:when>
                <c:otherwise>
                    <div class="tabla-contenedor">
                        <table class="tabla">
                            <thead>
                                <tr>
                                    <th scope="col">Referencia</th>
                                    <th scope="col">Producto</th>
                                    <th scope="col">Caduca</th>
                                    <th scope="col" class="numero">Unidades vendidas</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="fila" items="${filas}">
                                    <tr>
                                        <td>${fila.referencia}</td>
                                        <th scope="row"><c:out value="${fila.nombre}"/></th>
                                        <td><fmt:formatDate value="${fila.fechaCaducidad}" pattern="dd/MM/yyyy"/></td>
                                        <td class="numero">${fila.unidades}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </c:if>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
```

Commit `HU18: vista de ventas de productos`. Push y PR.

> `fmt:setLocale` es necesario: sin una configuración regional, `fmt:formatDate` muestra la fecha sin formato (igual que en `productos.jsp`).

### 5.3 Mauricio — PU04 (TDD) y servlet

Rama: `trabajo/hu18-mauricio`

**RED.** Crear `src/test/java/ec/edu/epn/polidinner/modelo/PeriodoVentasTest.java`:

**`src/test/java/ec/edu/epn/polidinner/modelo/PeriodoVentasTest.java`**

```java
package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** HU18 Consultar ventas de productos - TDD de PeriodoVentas (PU04). */
class PeriodoVentasTest {

    /** PU04 - Escenario 18.2.2 y regla 5: «Últimos 7 días» comprende hoy y los seis días anteriores. */
    @Test
    void ultimosSieteDiasComprendeHoyYLosSeisDiasAnteriores() {
        LocalDate hoy = LocalDate.of(2026, 11, 8);

        PeriodoVentas periodo = PeriodoVentas.ultimos7Dias(hoy);

        assertEquals(new PeriodoVentas(LocalDate.of(2026, 11, 2), hoy), periodo);
    }
}
```

Falla con `expected: <PeriodoVentas[2026-11-02 al 2026-11-08]> but was: <null>`. 📸 `PU04-1-RED`. Commit `HU18/PU04: RED - periodo de los últimos 7 días`.

**GREEN.** En `PeriodoVentas` solo cambia `ultimos7Dias`; la clase queda así:

**`src/main/java/ec/edu/epn/polidinner/modelo/PeriodoVentas.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * Soporte técnico NO modelado - HU18 (regla 5): periodo de consulta con las fechas inicial y final
 * completas. Los límites se calculan con DiaComedor en la zona del comedor (America/Guayaquil).
 */
public final class PeriodoVentas {

    private final LocalDate inicio;
    private final LocalDate fin;

    public PeriodoVentas(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Seleccione la fecha inicial y la final");
        }
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
        this.inicio = inicio;
        this.fin = fin;
    }

    public static PeriodoVentas hoy(LocalDate hoy) {
        return new PeriodoVentas(hoy, hoy);
    }

    /** «Últimos 7 días»: hoy y los seis días anteriores. Origen TDD: PeriodoVentasTest (PU04). */
    public static PeriodoVentas ultimos7Dias(LocalDate hoy) {
        return new PeriodoVentas(hoy.minusDays(6), hoy);
    }

    /** 00:00 del día inicial en la zona del comedor (incluido). */
    public Date desde(ZoneId zona) {
        return new DiaComedor(inicio, zona).inicio();
    }

    /** 00:00 del día siguiente al final en la zona del comedor (excluido). */
    public Date hastaExclusivo(ZoneId zona) {
        return new DiaComedor(fin, zona).fin();
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFin() {
        return fin;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof PeriodoVentas)) {
            return false;
        }
        PeriodoVentas periodo = (PeriodoVentas) otro;
        return inicio.equals(periodo.inicio) && fin.equals(periodo.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inicio, fin);
    }

    @Override
    public String toString() {
        return "PeriodoVentas[" + inicio + " al " + fin + "]";
    }
}
```

Pasa. 📸 `PU04-2-GREEN`. Commit `HU18/PU04: GREEN - hoy y los seis días anteriores`.

**PU04 — REFACTOR.** **Técnica: Introduce Explaining Variable.** Aplicar Introduce Explaining Variable: extraer la expresión hoy.minusDays(6) a primerDiaDelPeriodo aclara el límite inicial inclusivo que recibe el constructor. No se renombra una variable existente ni se cambia la fecha calculada.

Con las pruebas verdes, capture el código GREEN antes de modificarlo. Aplique este cambio sin alterar las condiciones ni los resultados de la prueba:

**`src/main/java/ec/edu/epn/polidinner/modelo/PeriodoVentas.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * Soporte técnico NO modelado - HU18 (regla 5): periodo de consulta con las fechas inicial y final
 * completas. Los límites se calculan con DiaComedor en la zona del comedor (America/Guayaquil).
 */
public final class PeriodoVentas {

    private final LocalDate inicio;
    private final LocalDate fin;

    public PeriodoVentas(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Seleccione la fecha inicial y la final");
        }
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
        this.inicio = inicio;
        this.fin = fin;
    }

    public static PeriodoVentas hoy(LocalDate hoy) {
        return new PeriodoVentas(hoy, hoy);
    }

    /** «Últimos 7 días»: hoy y los seis días anteriores. Origen TDD: PeriodoVentasTest (PU04). */
    public static PeriodoVentas ultimos7Dias(LocalDate hoy) {
        LocalDate primerDiaDelPeriodo = hoy.minusDays(6);
        return new PeriodoVentas(primerDiaDelPeriodo, hoy);
    }

    /** 00:00 del día inicial en la zona del comedor (incluido). */
    public Date desde(ZoneId zona) {
        return new DiaComedor(inicio, zona).inicio();
    }

    /** 00:00 del día siguiente al final en la zona del comedor (excluido). */
    public Date hastaExclusivo(ZoneId zona) {
        return new DiaComedor(fin, zona).fin();
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFin() {
        return fin;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof PeriodoVentas)) {
            return false;
        }
        PeriodoVentas periodo = (PeriodoVentas) otro;
        return inicio.equals(periodo.inicio) && fin.equals(periodo.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inicio, fin);
    }

    @Override
    public String toString() {
        return "PeriodoVentas[" + inicio + " al " + fin + "]";
    }
}
```

Ejecute la prueba de `PeriodoVentasTest` y después `./mvnw test`. Capture el diff y el resultado verde como `PU04-3-REFACTOR`. Commit **`HU18/PU04: REFACTOR - Introduce Explaining Variable para el inicio del periodo`**. Este cambio pertenece al ciclo PU04; **no cuenta entre R1–R4**.

**Servlet (normal).** Crear `controlador/VentasProductosServlet.java`:

**`src/main/java/ec/edu/epn/polidinner/controlador/VentasProductosServlet.java`**

```java
package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ec.edu.epn.polidinner.modelo.DiaComedor;
import ec.edu.epn.polidinner.modelo.FilaVentaProducto;
import ec.edu.epn.polidinner.modelo.PeriodoVentas;
import ec.edu.epn.polidinner.servicio.ServicioVentasProducto;

/**
 * HU18 Consultar ventas de productos - pantalla «Ventas de productos» del personal (protegida por
 * FiltroPersonal en /personal/*). Parámetro "periodo": hoy, 7dias o rango (con "inicio" y "fin").
 * El día y la zona son los del comedor (RelojAplicacion), no los del servidor.
 */
@WebServlet("/personal/ventas-productos")
public class VentasProductosServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/ventasProductos.jsp";

    private final ServicioVentasProducto servicio = new ServicioVentasProducto();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DiaComedor hoy = RelojAplicacion.diaComedor();
        String opcion = request.getParameter("periodo");
        request.setAttribute("opcion", opcion == null ? "hoy" : opcion);
        if (opcion != null) {
            try {
                PeriodoVentas periodo = leerPeriodo(request, opcion, hoy.getFecha());
                List<FilaVentaProducto> filas = servicio.consultar(periodo, hoy.getZona());
                request.setAttribute("periodo", periodo);
                request.setAttribute("filas", filas);
                request.setAttribute("consultado", true);
            } catch (DateTimeParseException e) {
                request.setAttribute("error", "Ingrese fechas válidas (día, mes y año).");
            } catch (IllegalArgumentException e) {
                request.setAttribute("error", e.getMessage());
            } catch (RuntimeException e) {
                log("No se pudo consultar las ventas de productos", e);
                request.setAttribute("error", "No se pudo consultar las ventas. Intente de nuevo en unos minutos.");
            }
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private static PeriodoVentas leerPeriodo(HttpServletRequest request, String opcion, LocalDate hoy) {
        switch (opcion) {
            case "hoy":
                return PeriodoVentas.hoy(hoy);
            case "7dias":
                return PeriodoVentas.ultimos7Dias(hoy);
            default:
                return new PeriodoVentas(fecha(request.getParameter("inicio")), fecha(request.getParameter("fin")));
        }
    }

    private static LocalDate fecha(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : LocalDate.parse(valor.trim());
    }
}
```

Commit `HU18: servlet de ventas de productos`. Push y PR.

### 5.4 Christian — PM01 (TDD con Mockito) y datos

Rama: `trabajo/hu18-christian`

**RED.** Crear `src/test/java/ec/edu/epn/polidinner/servicio/ServicioVentasProductoMockTest.java`:

**`src/test/java/ec/edu/epn/polidinner/servicio/ServicioVentasProductoMockTest.java`**

```java
package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ec.edu.epn.polidinner.modelo.CalculadoraVentasProducto;
import ec.edu.epn.polidinner.modelo.DiaComedor;
import ec.edu.epn.polidinner.modelo.FilaVentaProducto;
import ec.edu.epn.polidinner.modelo.FiltroVentasValidas;
import ec.edu.epn.polidinner.modelo.PeriodoVentas;
import ec.edu.epn.polidinner.modelo.VentaProducto;
import ec.edu.epn.polidinner.persistencia.VentaProductoDAO;

/**
 * HU18 Consultar ventas de productos - TDD con mocks de ServicioVentasProducto (PM01).
 * El DAO, el filtro y la calculadora son objetos simulados con Mockito.
 */
class ServicioVentasProductoMockTest {

    private static final ZoneId ZONA = ZoneId.of("America/Guayaquil");

    /** PM01 - Criterio 18.2: consulta por fecha de pago del periodo y resume solo las ventas válidas. */
    @Test
    void consultaPorFechaDePagoDelPeriodoYResumeSoloLasVentasValidas() {
        VentaProductoDAO dao = Mockito.mock(VentaProductoDAO.class);
        FiltroVentasValidas filtro = Mockito.mock(FiltroVentasValidas.class);
        CalculadoraVentasProducto calculadora = Mockito.mock(CalculadoraVentasProducto.class);
        ServicioVentasProducto servicio = new ServicioVentasProducto(dao, filtro, calculadora);

        PeriodoVentas periodo = new PeriodoVentas(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 8));
        java.util.Date desde = new DiaComedor(LocalDate.of(2026, 11, 2), ZONA).inicio();
        java.util.Date hasta = new DiaComedor(LocalDate.of(2026, 11, 8), ZONA).fin();
        Date caducidad = Date.valueOf(LocalDate.of(2026, 11, 10));
        VentaProducto entregada = new VentaProducto(201, "Yogur 150 g", caducidad, 2, "Entregado", desde);
        VentaProducto cancelada = new VentaProducto(201, "Yogur 150 g", caducidad, 3, "Cancelado", desde);
        List<VentaProducto> encontradas = List.of(entregada, cancelada);
        List<VentaProducto> validas = List.of(entregada);
        List<FilaVentaProducto> resumen = List.of(new FilaVentaProducto(201, "Yogur 150 g", caducidad, 2));

        Mockito.when(dao.buscarPorFechaDePago(desde, hasta)).thenReturn(encontradas);
        Mockito.when(filtro.filtrar(encontradas)).thenReturn(validas);
        Mockito.when(calculadora.resumir(validas)).thenReturn(resumen);

        List<FilaVentaProducto> resultado = servicio.consultar(periodo, ZONA);

        assertEquals(resumen, resultado);
        Mockito.verify(dao).buscarPorFechaDePago(desde, hasta);
        Mockito.verify(calculadora).resumir(validas);
    }
}
```

Falla con `expected: <[FilaVentaProducto[201 Yogur 150 g: 2]]> but was: <[]>`. 📸 `PM01-1-RED`. Commit `HU18/PM01: RED - consulta por fecha de pago`.

**GREEN.** En `ServicioVentasProducto` se implementa `consultar`:

**`src/main/java/ec/edu/epn/polidinner/servicio/ServicioVentasProducto.java`**

```java
package ec.edu.epn.polidinner.servicio;

import java.time.ZoneId;
import java.util.List;

import ec.edu.epn.polidinner.modelo.CalculadoraVentasProducto;
import ec.edu.epn.polidinner.modelo.FilaVentaProducto;
import ec.edu.epn.polidinner.modelo.FiltroVentasValidas;
import ec.edu.epn.polidinner.modelo.PeriodoVentas;
import ec.edu.epn.polidinner.modelo.VentaProducto;
import ec.edu.epn.polidinner.persistencia.VentaProductoDAO;

/**
 * Soporte técnico NO modelado - HU18 (criterio 18.2): consulta las ventas por fecha de pago y entrega
 * solo las válidas al resumen. Origen TDD: ServicioVentasProductoMockTest (PM01).
 */
public class ServicioVentasProducto {

    private final VentaProductoDAO ventaDAO;
    private final FiltroVentasValidas filtro;
    private final CalculadoraVentasProducto calculadora;

    public ServicioVentasProducto() {
        this(new VentaProductoDAO(), new FiltroVentasValidas(), new CalculadoraVentasProducto());
    }

    ServicioVentasProducto(VentaProductoDAO ventaDAO, FiltroVentasValidas filtro,
                           CalculadoraVentasProducto calculadora) {
        this.ventaDAO = ventaDAO;
        this.filtro = filtro;
        this.calculadora = calculadora;
    }

    public List<FilaVentaProducto> consultar(PeriodoVentas periodo, ZoneId zona) {
        List<VentaProducto> ventas = ventaDAO.buscarPorFechaDePago(periodo.desde(zona), periodo.hastaExclusivo(zona));
        return calculadora.resumir(filtro.filtrar(ventas));
    }
}
```

Pasa. 📸 `PM01-2-GREEN`. Commit `HU18/PM01: GREEN - delega en DAO, filtro y calculadora`.

**PM01 — REFACTOR.** **Técnica: Extract Method.** Extraer la consulta temporal deja la coordinación consultar → filtrar → resumir separada de cómo se calculan los límites para el DAO. Los mocks deben recibir las mismas fechas.

Con las pruebas verdes, capture el código GREEN antes de modificarlo. Aplique este cambio sin alterar las condiciones ni los resultados de la prueba:

**`src/main/java/ec/edu/epn/polidinner/servicio/ServicioVentasProducto.java`**

```java
package ec.edu.epn.polidinner.servicio;

import java.time.ZoneId;
import java.util.List;

import ec.edu.epn.polidinner.modelo.CalculadoraVentasProducto;
import ec.edu.epn.polidinner.modelo.FilaVentaProducto;
import ec.edu.epn.polidinner.modelo.FiltroVentasValidas;
import ec.edu.epn.polidinner.modelo.PeriodoVentas;
import ec.edu.epn.polidinner.modelo.VentaProducto;
import ec.edu.epn.polidinner.persistencia.VentaProductoDAO;

/**
 * Soporte técnico NO modelado - HU18 (criterio 18.2): consulta las ventas por fecha de pago y entrega
 * solo las válidas al resumen. Origen TDD: ServicioVentasProductoMockTest (PM01).
 */
public class ServicioVentasProducto {

    private final VentaProductoDAO ventaDAO;
    private final FiltroVentasValidas filtro;
    private final CalculadoraVentasProducto calculadora;

    public ServicioVentasProducto() {
        this(new VentaProductoDAO(), new FiltroVentasValidas(), new CalculadoraVentasProducto());
    }

    ServicioVentasProducto(VentaProductoDAO ventaDAO, FiltroVentasValidas filtro,
                           CalculadoraVentasProducto calculadora) {
        this.ventaDAO = ventaDAO;
        this.filtro = filtro;
        this.calculadora = calculadora;
    }

    public List<FilaVentaProducto> consultar(PeriodoVentas periodo, ZoneId zona) {
        List<VentaProducto> ventas = buscarVentasDelPeriodo(periodo, zona);
        return calculadora.resumir(filtro.filtrar(ventas));
    }

    private List<VentaProducto> buscarVentasDelPeriodo(PeriodoVentas periodo, ZoneId zona) {
        return ventaDAO.buscarPorFechaDePago(periodo.desde(zona), periodo.hastaExclusivo(zona));
    }
}
```

Ejecute la prueba de `ServicioVentasProductoMockTest` y después `./mvnw test`. Capture el diff y el resultado verde como `PM01-3-REFACTOR`. Commit **`HU18/PM01: REFACTOR - Extract Method: separa la consulta temporal`**. Este cambio pertenece al ciclo PM01; **no cuenta entre R1–R4**.

> Los colaboradores son *mocks*: Christian no espera a Jean, Jorge ni Oscar.

**Datos de demostración (normal).** Crear `src/main/resources/demo_hu18_ventas_h2.sql`:

**`src/main/resources/demo_hu18_ventas_h2.sql`**

```sql
-- PoliDinner - HU18 Consultar ventas de productos: datos de la demostración (base H2 del contenedor).
-- Se carga junto con los datos iniciales, por ejemplo:
--   POLIDINNER_DATOS_INICIALES=datos_iniciales_h2.sql,demo_hu18_ventas_h2.sql
-- Las fechas son relativas al día de la primera carga. Se puede repetir sin duplicar datos; para volver a
-- fechar los pedidos, crear el contenedor con un volumen nuevo. Cliente: Andrea Gómez (PoliCuenta 2024-0153).
-- Cada sentencia termina en punto y coma; no usar punto y coma dentro de los textos.

-- Registros de producto (las referencias son sus id)
INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 101, 'Agua 500 ml', 0.50, 'Disponible', 'Botella', 3, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 101);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 101, DATEADD('DAY', 1, CURRENT_DATE), TRUE, 'demo-hu18-101' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 101);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 102, 'Agua 500 ml', 0.50, 'Disponible', 'Botella', 7, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 102);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 102, DATEADD('DAY', 4, CURRENT_DATE), TRUE, 'demo-hu18-102' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 102);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 103, 'Galletas 100 g', 0.75, 'Agotado', 'Paquete', 0, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 103);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 103, DATEADD('DAY', 1, CURRENT_DATE), TRUE, 'demo-hu18-103' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 103);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 104, 'Galletas 200 g', 1.25, 'Disponible', 'Paquete', 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 104);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 104, DATEADD('DAY', 4, CURRENT_DATE), TRUE, 'demo-hu18-104' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 104);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 201, 'Yogur 150 g', 0.90, 'Disponible', 'Vaso', 4, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 201);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 201, DATEADD('DAY', 2, CURRENT_DATE), TRUE, 'demo-hu18-201' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 201);

-- Pedidos y comprobantes: la fecha de pago es Comprobante.fechaGeneracion
-- 18.1.1: dos pedidos Pagados hoy de la referencia 101 (6 y 4 unidades)
INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9181, DATEADD('DAY', 0, LOCALTIMESTAMP), 'Pagado', 3.00, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9181);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 6, 0.50, 101, 9181 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9181);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9181', 'PD-9181', DATEADD('DAY', 0, LOCALTIMESTAMP), FALSE, 9181, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9181);

INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9182, DATEADD('DAY', 0, LOCALTIMESTAMP), 'Pagado', 2.00, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9182);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 4, 0.50, 101, 9182 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9182);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9182', 'PD-9182', DATEADD('DAY', 0, LOCALTIMESTAMP), FALSE, 9182, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9182);

-- 18.1.2: 4 unidades de la referencia 102 pagadas hoy
INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9183, DATEADD('DAY', 0, LOCALTIMESTAMP), 'Pagado', 2.00, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9183);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 4, 0.50, 102, 9183 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9183);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9183', 'PD-9183', DATEADD('DAY', 0, LOCALTIMESTAMP), FALSE, 9183, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9183);

-- 18.3.1: referencia 103 agotada: 2 Pagadas, 3 Entregadas y 4 Canceladas, todas pagadas hoy
INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9184, DATEADD('DAY', 0, LOCALTIMESTAMP), 'Pagado', 1.50, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9184);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 2, 0.75, 103, 9184 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9184);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9184', 'PD-9184', DATEADD('DAY', 0, LOCALTIMESTAMP), FALSE, 9184, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9184);

INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9185, DATEADD('DAY', 0, LOCALTIMESTAMP), 'Entregado', 2.25, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9185);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 3, 0.75, 103, 9185 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9185);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9185', 'PD-9185', DATEADD('DAY', 0, LOCALTIMESTAMP), TRUE, 9185, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9185);

INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9186, DATEADD('DAY', 0, LOCALTIMESTAMP), 'Cancelado', 3.00, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9186);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 4, 0.75, 103, 9186 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9186);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9186', 'PD-9186', DATEADD('DAY', 0, LOCALTIMESTAMP), FALSE, 9186, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9186);

-- 18.2.2: referencia 104: 1 unidad pagada hace 6 días y 4 hace 7 días (fuera de «Últimos 7 días»)
INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9187, DATEADD('DAY', -6, LOCALTIMESTAMP), 'Pagado', 1.25, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9187);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 1, 1.25, 104, 9187 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9187);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9187', 'PD-9187', DATEADD('DAY', -6, LOCALTIMESTAMP), FALSE, 9187, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9187);

INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9188, DATEADD('DAY', -7, LOCALTIMESTAMP), 'Pagado', 5.00, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9188);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 4, 1.25, 104, 9188 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9188);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9188', 'PD-9188', DATEADD('DAY', -7, LOCALTIMESTAMP), FALSE, 9188, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9188);

-- 18.2.1: referencia 201: pedido creado hace 10 días, pagado hace 2 días y Entregado
INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9189, DATEADD('DAY', -10, LOCALTIMESTAMP), 'Entregado', 1.80, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9189);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 2, 0.90, 201, 9189 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9189);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9189', 'PD-9189', DATEADD('DAY', -2, LOCALTIMESTAMP), TRUE, 9189, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9189);
```

Commit `HU18: datos de demostración para H2`. Push y PR.

### 5.5 Oscar — DAO de ventas y acceso

Rama: `trabajo/hu18-oscar`

**DAO y comprobación con H2 (normal).** Reemplazar `persistencia/VentaProductoDAO.java` y crear la comprobación de soporte `src/test/java/ec/edu/epn/polidinner/servicio/VentaProductoDAOTest.java` (usa `BaseDatosPrueba`):

**`src/main/java/ec/edu/epn/polidinner/persistencia/VentaProductoDAO.java`**

```java
package ec.edu.epn.polidinner.persistencia;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;

import ec.edu.epn.polidinner.modelo.VentaProducto;

/**
 * Soporte técnico NO modelado - HU18: líneas de pedido de Productos cuyo comprobante se generó en el periodo.
 * La fecha de pago es Comprobante.fechaGeneracion (se crea en Pedido.confirmarPago()); los Platos no se
 * incluyen porque solo se cruzan los ItemMenu que son Producto. El estado del pedido se filtra después.
 */
public class VentaProductoDAO {

    private static final String VENTAS_POR_FECHA_DE_PAGO =
            "SELECT pr.id, pr.nombre, pr.fechaCaducidad, d.cantidad, p.estado, c.fechaGeneracion "
                    + "FROM Pedido p JOIN p.detalles d JOIN p.comprobante c, Producto pr "
                    + "WHERE pr.id = d.item.id AND c.fechaGeneracion >= :desde AND c.fechaGeneracion < :hasta "
                    + "ORDER BY p.id, d.id";

    /** Ventas de productos con Comprobante.fechaGeneracion en [desde, hasta). */
    public List<VentaProducto> buscarPorFechaDePago(Date desde, Date hasta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Object[]> filas = em.createQuery(VENTAS_POR_FECHA_DE_PAGO, Object[].class)
                    .setParameter("desde", desde, TemporalType.TIMESTAMP)
                    .setParameter("hasta", hasta, TemporalType.TIMESTAMP)
                    .getResultList();
            return filas.stream()
                    .map(f -> new VentaProducto((Integer) f[0], (String) f[1], (Date) f[2], (Integer) f[3],
                            (String) f[4], (Date) f[5]))
                    .toList();
        } finally {
            em.close();
        }
    }
}
```

**`src/test/java/ec/edu/epn/polidinner/servicio/VentaProductoDAOTest.java`**

```java
package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.DiaComedor;
import ec.edu.epn.polidinner.modelo.Pedido;
import ec.edu.epn.polidinner.modelo.VentaProducto;
import ec.edu.epn.polidinner.persistencia.JPAUtil;
import ec.edu.epn.polidinner.persistencia.VentaProductoDAO;

/**
 * Comprobación de soporte de HU18 (no es una de las 12 pruebas TDD): la consulta usa las entidades reales
 * en H2, toma la fecha del comprobante y deja fuera los platos.
 */
class VentaProductoDAOTest extends BaseDatosPrueba {

    private final VentaProductoDAO dao = new VentaProductoDAO();

    @BeforeEach
    void preparar() {
        prepararBase();
    }

    @AfterEach
    void cerrar() {
        cerrarBase();
    }

    @Test
    void devuelveLosProductosPagadosEnElPeriodoSegunLaFechaDelComprobante() {
        Pedido pedido = pedidoDe(andrea, jugo, 2, arroz, 1);
        servicio.pagarAhora(pedido, LUNES);
        DiaComedor hoy = DiaComedor.delSistema();

        List<VentaProducto> ventas = dao.buscarPorFechaDePago(hoy.inicio(), hoy.fin());

        assertEquals(1, ventas.size());
        assertEquals(jugo.getId(), ventas.get(0).getReferencia());
        assertEquals(2, ventas.get(0).getCantidad());
        assertEquals("Pagado", ventas.get(0).getEstadoPedido());

        moverPago(pedido, hoy.getFecha().minusDays(8));
        assertTrue(dao.buscarPorFechaDePago(hoy.inicio(), hoy.fin()).isEmpty());
    }

    private void moverPago(Pedido pedido, LocalDate fecha) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("UPDATE Comprobante c SET c.fechaGeneracion = :fecha WHERE c.pedido.id = :id")
                    .setParameter("fecha", java.sql.Timestamp.valueOf(fecha.atTime(10, 0)))
                    .setParameter("id", pedido.getId())
                    .executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
```

`./mvnw test` en verde. Commit `HU18: DAO de ventas por fecha de pago y comprobación con H2`.

**Acceso del panel (normal).** Reemplazar el fragmento:

**`src/main/webapp/vistas/personal/accesos/ventasProductos.jspf`**

```jsp
<%@ page pageEncoding="UTF-8" %>
                <li>
                    <a class="accion-fila" href="${pageContext.request.contextPath}/personal/ventas-productos">
                        <svg class="icono" aria-hidden="true"><use href="#i-billetera"/></svg>
                        <span class="accion-fila__texto">
                            <strong>Ventas de productos</strong>
                            <span>Consulte las unidades vendidas de cada registro de producto en un periodo.</span>
                        </span>
                        <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                    </a>
                </li>
```

Commit `HU18: acceso a ventas de productos en el panel`. Push y PR.

---

### 5.6 Refactorización adicional R1 — Oscar: Extract Method en la carga de scripts

Esta mejora es sobre el código complementario de Fase 0, no sobre los cambios de PU01 o PU02. `cargarSiCorresponde()` coordina la lectura de varios recursos y la transacción SQL. Se separa la lectura.

1. Con la suite verde, capturar el método y el bucle de lectura actuales.
2. Dentro de `cargarSiCorresponde()`, sustituir únicamente el bucle que arma `sentencias` por:

```java
        List<String> sentencias = leerSentenciasDeLosScripts(script);
```

3. Añadir a `persistencia/CargaDatosIniciales.java`, fuera de ese método:

```java
    /** Lee los recursos en el orden configurado; conserva comentarios, separador y política de errores. */
    private static List<String> leerSentenciasDeLosScripts(String script) throws IOException {
        List<String> sentencias = new ArrayList<>();
        for (String nombre : script.split(",")) {
            if (!nombre.trim().isEmpty()) {
                sentencias.addAll(separarSentencias(leerRecurso(nombre.trim())));
            }
        }
        return sentencias;
    }
```

El código de transacción, orden de recursos y propagación de errores permanece igual. No se agrega soporte de scripts aquí: eso ya existe desde Fase 0.

**Comprobación de soporte de R1, antes y después (no pertenece a las 12 pruebas TDD):** arrancar la aplicación con H2 aislada y los mismos scripts disponibles en la HU, en el mismo orden. En HU18 son `datos_iniciales_h2.sql,demo_hu18_ventas_h2.sql`. Usar una base nueva para cada versión, para que datos ya cargados no oculten una omisión. Comprobar el inicio de sesión con el personal inicial y la presencia de las existencias de demostración en la pantalla existente «Productos». La carga debe terminar sin errores y conservar referencias, cantidades y caducidades. Guardar el registro del arranque y los resultados de ambas versiones. No usar una base real del comedor.

La prueba del separador SQL por sí sola no protege la lectura de varios recursos. En la revisión independiente de esta guía se ejecutó además la carga real de los **cuatro scripts**, sobre dos bases separadas, con el cargador anterior y el posterior, y se compararon los mismos 17 resultados web. Esta comprobación de soporte no crea otro hito Jenkins ni exige publicar imágenes adicionales.

4. Ejecutar `./mvnw test`; capturar antes, después, diff, suite verde y commit.
5. Commit exclusivo `HU18/R1: Extract Method - separa la lectura de scripts` y PR con el resto del trabajo de Oscar.

---

## 6. HU20 — Registrar pérdidas por caducidad

Rama compartida: `feature/hu20-perdidas-caducidad`.

### 6.1 Jean — PU07, PU08 y PP02 (TDD)

Rama: `trabajo/hu20-jean`

**PU07 — RED.** Crear `src/test/java/ec/edu/epn/polidinner/modelo/ValidadorDescarteTest.java`:

**`src/test/java/ec/edu/epn/polidinner/modelo/ValidadorDescarteTest.java`**

```java
package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** HU20 Registrar pérdidas por caducidad - TDD de ValidadorDescarte (PU07, PU08 y PP02). */
class ValidadorDescarteTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);

    private final ValidadorDescarte validador = new ValidadorDescarte();

    private static Date fecha(LocalDate dia) {
        return Date.valueOf(dia);
    }

    /** PU07 - Escenarios 20.1.1 y 20.1.2: se ofrece la existencia caducada y no la vigente. */
    @Test
    void admiteDescartarUnaExistenciaCaducadaYNoUnaVigente() {
        Producto yogurCaducado = Producto.registrar("Yogur 150 g", 0.90, "Vaso", 6, fecha(HOY.minusDays(1)));
        Producto aguaVigente = Producto.registrar("Agua 500 ml", 0.50, "Botella", 3, fecha(HOY.plusDays(1)));

        assertTrue(validador.admiteDescarte(yogurCaducado, HOY));
        assertFalse(validador.admiteDescarte(aguaVigente, HOY));
    }
}
```

Falla con `expected: <true> but was: <false>`. 📸 `PU07-1-RED`. Commit `HU20/PU07: RED - solo existencias caducadas`.

**PU07 — GREEN** (mínimo: solo la caducidad):

**`src/main/java/ec/edu/epn/polidinner/modelo/ValidadorDescarte.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/**
 * HU20 (criterios 20.1 y 20.2): reglas para descartar unidades caducadas de un registro de Producto.
 * Origen TDD: ValidadorDescarteTest (PU07, PU08 y PP02).
 */
public class ValidadorDescarte {

    public boolean admiteDescarte(Producto producto, LocalDate hoy) {
        return !producto.estaVigente(hoy);
    }

    public void validarCantidad(Producto producto, int cantidad) {
    }
}
```

Pasa. 📸 `PU07-2-GREEN`. Commit `HU20/PU07: GREEN - admite descarte de caducados`.

**PU07 — REFACTOR.** **Técnica: Extract Method.** Refactorizar también las pruebas es parte de TDD: extraer los datos de preparación permite leer el ejemplo sin los detalles del constructor. Se mantienen los mismos productos, fechas y aserciones.

Con las pruebas verdes, capture el código GREEN antes de modificarlo. Aplique este cambio sin alterar las condiciones ni los resultados de la prueba:

**`src/test/java/ec/edu/epn/polidinner/modelo/ValidadorDescarteTest.java`**

```java
package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** HU20 Registrar pérdidas por caducidad - TDD de ValidadorDescarte (PU07, PU08 y PP02). */
class ValidadorDescarteTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);

    private final ValidadorDescarte validador = new ValidadorDescarte();

    private static Date fecha(LocalDate dia) {
        return Date.valueOf(dia);
    }

    /** PU07 - Escenarios 20.1.1 y 20.1.2: se ofrece la existencia caducada y no la vigente. */
    @Test
    void admiteDescartarUnaExistenciaCaducadaYNoUnaVigente() {
        Producto yogurCaducado = crearYogurCaducado();
        Producto aguaVigente = crearAguaVigente();

        assertTrue(validador.admiteDescarte(yogurCaducado, HOY));
        assertFalse(validador.admiteDescarte(aguaVigente, HOY));
    }

    private static Producto crearYogurCaducado() {
        return Producto.registrar("Yogur 150 g", 0.90, "Vaso", 6, fecha(HOY.minusDays(1)));
    }

    private static Producto crearAguaVigente() {
        return Producto.registrar("Agua 500 ml", 0.50, "Botella", 3, fecha(HOY.plusDays(1)));
    }
}
```

Ejecute la prueba de `ValidadorDescarteTest` y después `./mvnw test`. Capture el diff y el resultado verde como `PU07-3-REFACTOR`. Commit **`HU20/PU07: REFACTOR - Extract Method: nombra los datos de prueba`**. Este cambio pertenece al ciclo PU07; **no cuenta entre R1–R4**.

**PU08 — RED.** Agregar el segundo método; la clase queda así:

**`src/test/java/ec/edu/epn/polidinner/modelo/ValidadorDescarteTest.java`**

```java
package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** HU20 Registrar pérdidas por caducidad - TDD de ValidadorDescarte (PU07, PU08 y PP02). */
class ValidadorDescarteTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);

    private final ValidadorDescarte validador = new ValidadorDescarte();

    private static Date fecha(LocalDate dia) {
        return Date.valueOf(dia);
    }

    /** PU07 - Escenarios 20.1.1 y 20.1.2: se ofrece la existencia caducada y no la vigente. */
    @Test
    void admiteDescartarUnaExistenciaCaducadaYNoUnaVigente() {
        Producto yogurCaducado = crearYogurCaducado();
        Producto aguaVigente = crearAguaVigente();

        assertTrue(validador.admiteDescarte(yogurCaducado, HOY));
        assertFalse(validador.admiteDescarte(aguaVigente, HOY));
    }

    /** PU08 - Regla 9: solo existencias habilitadas y con stock positivo. */
    @Test
    void noAdmiteDescartarUnaExistenciaCaducadaSinStockONoHabilitada() {
        Producto sinStock = Producto.registrar("Galletas 100 g", 0.75, "Paquete", 0, fecha(HOY.minusDays(1)));
        Producto historico = new Producto("Jugo 250 ml", 0.80, "Disponible", "Caja", 5, fecha(HOY.minusDays(1)));

        assertFalse(validador.admiteDescarte(sinStock, HOY));
        assertFalse(validador.admiteDescarte(historico, HOY));
    }

    private static Producto crearYogurCaducado() {
        return Producto.registrar("Yogur 150 g", 0.90, "Vaso", 6, fecha(HOY.minusDays(1)));
    }

    private static Producto crearAguaVigente() {
        return Producto.registrar("Agua 500 ml", 0.50, "Botella", 3, fecha(HOY.plusDays(1)));
    }
}
```

PU08 falla con `expected: <false> but was: <true>`. 📸 `PU08-1-RED`. Commit `HU20/PU08: RED - sin stock o histórico no se descarta`.

**PU08 — GREEN.**

**`src/main/java/ec/edu/epn/polidinner/modelo/ValidadorDescarte.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/**
 * HU20 (criterios 20.1 y 20.2): reglas para descartar unidades caducadas de un registro de Producto.
 * Origen TDD: ValidadorDescarteTest (PU07, PU08 y PP02).
 */
public class ValidadorDescarte {

    public boolean admiteDescarte(Producto producto, LocalDate hoy) {
        return producto.isHabilitado() && producto.getStockActual() > 0 && !producto.estaVigente(hoy);
    }

    public void validarCantidad(Producto producto, int cantidad) {
    }
}
```

Pasa. 📸 `PU08-2-GREEN`. Commit `HU20/PU08: GREEN - exige habilitado y stock`.

**PU08 — REFACTOR.** **Técnica: Move Method.** Mover la decisión de elegibilidad a Producto coloca la regla junto a los datos que utiliza. El validador conserva su interfaz y delega; no se añade ninguna condición.

Con las pruebas verdes, capture el código GREEN antes de modificarlo. Aplique este cambio sin alterar las condiciones ni los resultados de la prueba:

**`src/main/java/ec/edu/epn/polidinner/modelo/Producto.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Trazabilidad: Diagrama de clases (Fig. 7) - clase Producto, "es un" ItemMenu.
 * Atributo del diagrama: fechaCaducidad. Método: getStock().
 * Evolución del incremento 2 (Fig. 15): el atributo stock pasa a ItemMenu como stockActual.
 */
@Entity
public class Producto extends ItemMenu {

    private static final long serialVersionUID = 1L;

    @Temporal(TemporalType.DATE)
    private Date fechaCaducidad;

    /**
     * Corrección Paquete 2: existencia habilitada para ofrecerse en nuevos menús (ver isHabilitado()).
     * El valor por defecto 0 permite agregar la columna a una tabla con datos (los antiguos quedan históricos).
     */
    @Column(nullable = false, columnDefinition = "bit default 0")
    private boolean habilitado;

    /**
     * Atributo técnico NO modelado (Paquete 3): identidad estable de la existencia de demostración que crean
     * los datos iniciales («demo-jugo-naranja»), para no fabricarla de nuevo en cada arranque ni identificarla
     * por su nombre. Es null en los productos registrados por el personal. La unicidad la asegura la migración
     * con un índice filtrado (SQL Server admite un solo NULL en una restricción UNIQUE simple).
     */
    @Column(name = "clave_semilla", length = 40)
    private String claveSemilla;

    protected Producto() {
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU01 - mensaje 13:
     * &lt;&lt;create&gt;&gt; Producto(nombre, precio, "Disponible", descripcion, stock, fechaCaducidad).
     * El stock se guarda en ItemMenu.stockActual (Fig. 15).
     */
    public Producto(String nombre, double precio, String estado, String descripcion,
                    int stock, Date fechaCaducidad) {
        super(nombre, precio, estado, descripcion, stock);
        this.fechaCaducidad = fechaCaducidad;
    }

    /**
     * Trazabilidad: Diagrama de clases (Fig. 7) - Producto.getStock().
     * Desde el incremento 2 devuelve ItemMenu.stockActual (Fig. 15).
     */
    public int getStock() {
        return getStockActual();
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    /**
     * Corrección Paquete 2 (pantalla Productos): registra una existencia independiente del menú, con su
     * cantidad inicial y su caducidad, habilitada para ofrecerla en los menús de una o varias jornadas.
     * Registrarla no crea ninguna oferta.
     */
    public static Producto registrar(String nombre, double precio, String descripcion, int stock, Date fechaCaducidad) {
        Producto producto = new Producto(nombre, precio, "Disponible", descripcion, stock, fechaCaducidad);
        producto.habilitado = true;
        return producto;
    }

    /**
     * ¿Se puede seleccionar para nuevos menús? Solo las existencias registradas en Productos y las que la
     * migración conservó del menú abierto. Los productos de menús antiguos quedan como históricos (false):
     * siguen sirviendo a sus pedidos y cancelaciones, pero no se vuelven a ofrecer.
     */
    public boolean isHabilitado() {
        return habilitado;
    }

    /**
     * Convención propuesta (no es un acuerdo con el comedor): un producto con caducidad D se vende durante
     * D; desde D+1 ya no. Sin fecha registrada se considera vigente.
     */
    public boolean estaVigente(LocalDate hoy) {
        return fechaCaducidad == null || !hoy.isAfter(DiaComedor.fechaCivilDe(fechaCaducidad));
    }

    /** HU20: solo se descartan existencias habilitadas, con stock y ya caducadas. */
    public boolean admiteDescarte(LocalDate hoy) {
        return isHabilitado() && getStockActual() > 0 && !estaVigente(hoy);
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/modelo/ValidadorDescarte.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/** HU20: valida el descarte. PU08 delega la elegibilidad en Producto; PP02 implementará la cantidad. */
public class ValidadorDescarte {
    public boolean admiteDescarte(Producto producto, LocalDate hoy) {
        return producto.admiteDescarte(hoy);
    }

    public void validarCantidad(Producto producto, int cantidad) {
    }
}
```

Ejecute la prueba de `ValidadorDescarteTest` y después `./mvnw test`. Capture el diff y el resultado verde como `PU08-3-REFACTOR`. Commit **`HU20/PU08: REFACTOR - Move Method: mueve la elegibilidad a Producto`**. Este cambio pertenece al ciclo PU08; **no cuenta entre R1–R4**.

**PP02 — RED.** Agregar la prueba parametrizada; la clase completa queda así:

**`src/test/java/ec/edu/epn/polidinner/modelo/ValidadorDescarteTest.java`**

```java
package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** HU20 Registrar pérdidas por caducidad - TDD de ValidadorDescarte (PU07, PU08 y PP02). */
class ValidadorDescarteTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);

    private final ValidadorDescarte validador = new ValidadorDescarte();

    private static Date fecha(LocalDate dia) {
        return Date.valueOf(dia);
    }

    /** PU07 - Escenarios 20.1.1 y 20.1.2: se ofrece la existencia caducada y no la vigente. */
    @Test
    void admiteDescartarUnaExistenciaCaducadaYNoUnaVigente() {
        Producto yogurCaducado = crearYogurCaducado();
        Producto aguaVigente = crearAguaVigente();

        assertTrue(validador.admiteDescarte(yogurCaducado, HOY));
        assertFalse(validador.admiteDescarte(aguaVigente, HOY));
    }

    /** PU08 - Regla 9: solo existencias habilitadas y con stock positivo. */
    @Test
    void noAdmiteDescartarUnaExistenciaCaducadaSinStockONoHabilitada() {
        Producto sinStock = Producto.registrar("Galletas 100 g", 0.75, "Paquete", 0, fecha(HOY.minusDays(1)));
        Producto historico = new Producto("Jugo 250 ml", 0.80, "Disponible", "Caja", 5, fecha(HOY.minusDays(1)));

        assertFalse(validador.admiteDescarte(sinStock, HOY));
        assertFalse(validador.admiteDescarte(historico, HOY));
    }

    /** PP02 - Escenario 20.2.2 y regla 10: cantidades inválidas para un registro con 6 unidades. */
    @ParameterizedTest(name = "{0} unidades -> {1}")
    @CsvSource({
            "8,  La cantidad supera las 6 unidades disponibles",
            "7,  La cantidad supera las 6 unidades disponibles",
            "0,  La cantidad debe ser mayor que cero",
            "-1, La cantidad debe ser mayor que cero"
    })
    void rechazaCantidadesFueraDelStockDisponible(int cantidad, String mensajeEsperado) {
        Producto yogurCaducado = Producto.registrar("Yogur 150 g", 0.90, "Vaso", 6, fecha(HOY.minusDays(1)));

        DescarteRechazadoException error = assertThrows(DescarteRechazadoException.class,
                () -> validador.validarCantidad(yogurCaducado, cantidad));

        assertEquals(mensajeEsperado, error.getMessage());
    }

    private static Producto crearYogurCaducado() {
        return Producto.registrar("Yogur 150 g", 0.90, "Vaso", 6, fecha(HOY.minusDays(1)));
    }

    private static Producto crearAguaVigente() {
        return Producto.registrar("Agua 500 ml", 0.50, "Botella", 3, fecha(HOY.plusDays(1)));
    }
}
```

Los cuatro casos fallan con `Expected ...DescarteRechazadoException to be thrown, but nothing was thrown.` 📸 `PP02-1-RED`. Commit `HU20/PP02: RED - cantidades inválidas`.

**PP02 — GREEN.**

**`src/main/java/ec/edu/epn/polidinner/modelo/ValidadorDescarte.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/**
 * HU20 (criterios 20.1 y 20.2): reglas para descartar unidades caducadas de un registro de Producto.
 * Origen TDD: ValidadorDescarteTest (PU07, PU08 y PP02).
 */
public class ValidadorDescarte {

    public boolean admiteDescarte(Producto producto, LocalDate hoy) {
        return producto.admiteDescarte(hoy);
    }

    public void validarCantidad(Producto producto, int cantidad) {
        if (cantidad <= 0) {
            throw new DescarteRechazadoException("La cantidad debe ser mayor que cero");
        }
        if (cantidad > producto.getStockActual()) {
            throw new DescarteRechazadoException(
                    "La cantidad supera las " + producto.getStockActual() + " unidades disponibles");
        }
    }
}
```

Pasan las seis ejecuciones. 📸 `PP02-2-GREEN`. Commit `HU20/PP02: GREEN - valida la cantidad contra el stock`.

**PP02 — REFACTOR.** **Técnica: Extract Method.** Extraer la primera guarda permite distinguir las dos reglas: cantidad positiva y cantidad no superior al stock. Se conserva el orden de rechazo y los mensajes.

Con las pruebas verdes, capture el código GREEN antes de modificarlo. Aplique este cambio sin alterar las condiciones ni los resultados de la prueba:

**`src/main/java/ec/edu/epn/polidinner/modelo/ValidadorDescarte.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/**
 * HU20 (criterios 20.1 y 20.2): reglas para descartar unidades caducadas de un registro de Producto.
 * Origen TDD: ValidadorDescarteTest (PU07, PU08 y PP02).
 */
public class ValidadorDescarte {

    public boolean admiteDescarte(Producto producto, LocalDate hoy) {
        return producto.admiteDescarte(hoy);
    }

    public void validarCantidad(Producto producto, int cantidad) {
        validarCantidadPositiva(cantidad);
        if (cantidad > producto.getStockActual()) {
            throw new DescarteRechazadoException(
                    "La cantidad supera las " + producto.getStockActual() + " unidades disponibles");
        }
    }

    private void validarCantidadPositiva(int cantidad) {
        if (cantidad <= 0) {
            throw new DescarteRechazadoException("La cantidad debe ser mayor que cero");
        }
    }
}
```

Ejecute la prueba de `ValidadorDescarteTest` y después `./mvnw test`. Capture el diff y el resultado verde como `PP02-3-REFACTOR`. Commit **`HU20/PP02: REFACTOR - Extract Method: separa las reglas de cantidad`**. Este cambio pertenece al ciclo PP02; **no cuenta entre R1–R4**.

### 6.2 Cierre de Jean en HU20

El Move Method ya se hizo en el REFACTOR de PU08. No se repite ni se cuenta entre las cuatro adicionales. Con PP02 cerrado y la suite verde, hacer push y PR de `trabajo/hu20-jean` a la feature. Se conserva el reparto de las 12 pruebas.

### 6.3 Christian — PM02 (TDD con Mockito) y registro transaccional

Rama: `trabajo/hu20-christian`

**RED.** Crear `src/test/java/ec/edu/epn/polidinner/servicio/ServicioPerdidasMockTest.java`:

**`src/test/java/ec/edu/epn/polidinner/servicio/ServicioPerdidasMockTest.java`**

```java
package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.modelo.ValidadorDescarte;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;

/**
 * HU20 Registrar pérdidas por caducidad - TDD con mocks de ServicioPerdidas (PM02).
 * El Producto y el ValidadorDescarte son objetos simulados con Mockito.
 */
class ServicioPerdidasMockTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 8, 15, 30);

    /** PM02 - Escenario 20.2.1: valida la existencia y la cantidad antes de descontar y armar la pérdida. */
    @Test
    void validaLaExistenciaYLaCantidadAntesDeDescontarElStock() {
        Producto yogur = Mockito.mock(Producto.class);
        ValidadorDescarte validador = Mockito.mock(ValidadorDescarte.class);
        PersonalComedor maria = new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "comedor123");
        ServicioPerdidas servicio = new ServicioPerdidas(Mockito.mock(PerdidaCaducidadDAO.class),
                Mockito.mock(ProductoDAO.class), validador);

        Mockito.when(yogur.getNombre()).thenReturn("Yogur 150 g");
        Mockito.when(yogur.getStockActual()).thenReturn(6);
        Mockito.when(validador.admiteDescarte(yogur, HOY)).thenReturn(true);

        PerdidaCaducidad perdida = servicio.aplicarDescarte(yogur, 6, maria, AHORA, "op-1");

        InOrder orden = Mockito.inOrder(validador, yogur);
        orden.verify(validador).admiteDescarte(yogur, HOY);
        orden.verify(validador).validarCantidad(yogur, 6);
        orden.verify(yogur).setStockActual(0);
        assertEquals(6, perdida.getUnidades());
        assertSame(maria, perdida.getResponsable());
        assertEquals(AHORA, perdida.getFechaRegistro());
        assertEquals("op-1", perdida.getTokenOperacion());
    }
}
```

Falla con `Wanted but not invoked: validadorDescarte.admiteDescarte(...)`. 📸 `PM02-1-RED`. Commit `HU20/PM02: RED - valida antes de descontar`.

**GREEN.** En `ServicioPerdidas` se implementa `aplicarDescarte`:

**`src/main/java/ec/edu/epn/polidinner/servicio/ServicioPerdidas.java`**

```java
package ec.edu.epn.polidinner.servicio;

import java.time.LocalDateTime;

import ec.edu.epn.polidinner.modelo.DescarteRechazadoException;
import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.modelo.ValidadorDescarte;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;

/**
 * Soporte técnico NO modelado - HU20 Registrar pérdidas por caducidad (reglas 9 a 11).
 * aplicarDescarte: origen TDD ServicioPerdidasMockTest (PM02). registrar: pendiente (Christian, HU20).
 */
public class ServicioPerdidas {

    static final String NO_ADMITE_DESCARTE = "Solo se registran pérdidas de existencias caducadas, habilitadas y con stock.";

    private final PerdidaCaducidadDAO perdidaDAO;
    private final ProductoDAO productoDAO;
    private final ValidadorDescarte validador;

    public ServicioPerdidas() {
        this(new PerdidaCaducidadDAO(), new ProductoDAO(), new ValidadorDescarte());
    }

    ServicioPerdidas(PerdidaCaducidadDAO perdidaDAO, ProductoDAO productoDAO, ValidadorDescarte validador) {
        this.perdidaDAO = perdidaDAO;
        this.productoDAO = productoDAO;
        this.validador = validador;
    }

    /** Valida el descarte, descuenta las unidades del registro y arma la pérdida (sin guardarla). */
    PerdidaCaducidad aplicarDescarte(Producto producto, int unidades, PersonalComedor responsable,
                                     LocalDateTime momento, String tokenOperacion) {
        if (!validador.admiteDescarte(producto, momento.toLocalDate())) {
            throw new DescarteRechazadoException(NO_ADMITE_DESCARTE);
        }
        validador.validarCantidad(producto, unidades);
        producto.setStockActual(producto.getStockActual() - unidades);
        return new PerdidaCaducidad(producto, unidades, responsable, momento, tokenOperacion);
    }

    /**
     * Registra la pérdida y descuenta el stock en una sola transacción.
     *
     * @return true si se registró; false si esa misma confirmación ya estaba registrada
     */
    public boolean registrar(int idProducto, int unidades, PersonalComedor responsable, String tokenOperacion,
                             LocalDateTime ahora) {
        return false;
    }
}
```

Pasa. 📸 `PM02-2-GREEN`. Commit `HU20/PM02: GREEN - aplica el descarte`.

**PM02 — REFACTOR.** **Técnica: Extract Method.** Extraer el descuento permite leer aplicarDescarte como validar elegibilidad → validar cantidad → descontar → construir la pérdida. No cambia la cantidad, el orden de llamadas ni el guardado.

Con las pruebas verdes, capture el código GREEN antes de modificarlo. Aplique este cambio sin alterar las condiciones ni los resultados de la prueba:

**`src/main/java/ec/edu/epn/polidinner/servicio/ServicioPerdidas.java`**

```java
package ec.edu.epn.polidinner.servicio;

import java.time.LocalDateTime;

import ec.edu.epn.polidinner.modelo.DescarteRechazadoException;
import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.modelo.ValidadorDescarte;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;

/**
 * Soporte técnico NO modelado - HU20 Registrar pérdidas por caducidad (reglas 9 a 11).
 * aplicarDescarte: origen TDD ServicioPerdidasMockTest (PM02). registrar: pendiente (Christian, HU20).
 */
public class ServicioPerdidas {

    static final String NO_ADMITE_DESCARTE = "Solo se registran pérdidas de existencias caducadas, habilitadas y con stock.";

    private final PerdidaCaducidadDAO perdidaDAO;
    private final ProductoDAO productoDAO;
    private final ValidadorDescarte validador;

    public ServicioPerdidas() {
        this(new PerdidaCaducidadDAO(), new ProductoDAO(), new ValidadorDescarte());
    }

    ServicioPerdidas(PerdidaCaducidadDAO perdidaDAO, ProductoDAO productoDAO, ValidadorDescarte validador) {
        this.perdidaDAO = perdidaDAO;
        this.productoDAO = productoDAO;
        this.validador = validador;
    }

    /** Valida el descarte, descuenta las unidades del registro y arma la pérdida (sin guardarla). */
    PerdidaCaducidad aplicarDescarte(Producto producto, int unidades, PersonalComedor responsable,
                                     LocalDateTime momento, String tokenOperacion) {
        if (!validador.admiteDescarte(producto, momento.toLocalDate())) {
            throw new DescarteRechazadoException(NO_ADMITE_DESCARTE);
        }
        validador.validarCantidad(producto, unidades);
        descontarUnidades(producto, unidades);
        return new PerdidaCaducidad(producto, unidades, responsable, momento, tokenOperacion);
    }

    /**
     * Registra la pérdida y descuenta el stock en una sola transacción.
     *
     * @return true si se registró; false si esa misma confirmación ya estaba registrada
     */
    public boolean registrar(int idProducto, int unidades, PersonalComedor responsable, String tokenOperacion,
                             LocalDateTime ahora) {
        return false;
    }
    private void descontarUnidades(Producto producto, int unidades) {
        producto.setStockActual(producto.getStockActual() - unidades);
    }
}
```

Ejecute la prueba de `ServicioPerdidasMockTest` y después `./mvnw test`. Capture el diff y el resultado verde como `PM02-3-REFACTOR`. Commit **`HU20/PM02: REFACTOR - Extract Method: nombra el descuento de unidades`**. Este cambio pertenece al ciclo PM02; **no cuenta entre R1–R4**.

**Registro transaccional (normal, espera obligatoria).** Cuando el DAO de Jorge y el GREEN de PP02 de Jean estén en `feature/hu20-perdidas-caducidad`, hacer `git merge origin/feature/hu20-perdidas-caducidad` e implementar `registrar`. La entidad `Producto` se carga y se modifica **dentro de la misma `Transaccion`** que guarda la pérdida: así actúa su `@Version` y no se usa un `UPDATE` masivo (que no incrementa la versión). La clase queda así:

**`src/main/java/ec/edu/epn/polidinner/servicio/ServicioPerdidas.java`**

```java
package ec.edu.epn.polidinner.servicio;

import java.time.LocalDateTime;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import ec.edu.epn.polidinner.modelo.DescarteRechazadoException;
import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.modelo.ValidadorDescarte;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

/**
 * Soporte técnico NO modelado - HU20 Registrar pérdidas por caducidad (reglas 9 a 11).
 * aplicarDescarte: origen TDD ServicioPerdidasMockTest (PM02).
 * registrar: guarda la pérdida y el nuevo stock en UNA transacción (Transaccion). La entidad Producto se
 * carga y se modifica dentro de ella, así su @Version detecta un cambio simultáneo del mismo registro.
 */
public class ServicioPerdidas {

    static final String SIN_CONFIRMACION = "La confirmación no es válida. Vuelva a abrir el formulario de pérdidas.";
    static final String NO_ENCONTRADO = "No se encontró esa existencia de producto.";
    static final String CAMBIO_CONCURRENTE = "La existencia cambió mientras se registraba la pérdida. No se guardó"
            + " nada; revise el stock e inténtelo de nuevo.";
    static final String NO_ADMITE_DESCARTE = "Solo se registran pérdidas de existencias caducadas, habilitadas y con stock.";

    private final PerdidaCaducidadDAO perdidaDAO;
    private final ProductoDAO productoDAO;
    private final ValidadorDescarte validador;

    public ServicioPerdidas() {
        this(new PerdidaCaducidadDAO(), new ProductoDAO(), new ValidadorDescarte());
    }

    ServicioPerdidas(PerdidaCaducidadDAO perdidaDAO, ProductoDAO productoDAO, ValidadorDescarte validador) {
        this.perdidaDAO = perdidaDAO;
        this.productoDAO = productoDAO;
        this.validador = validador;
    }

    /** Valida el descarte, descuenta las unidades del registro y arma la pérdida (sin guardarla). */
    PerdidaCaducidad aplicarDescarte(Producto producto, int unidades, PersonalComedor responsable,
                                     LocalDateTime momento, String tokenOperacion) {
        if (!validador.admiteDescarte(producto, momento.toLocalDate())) {
            throw new DescarteRechazadoException(NO_ADMITE_DESCARTE);
        }
        validador.validarCantidad(producto, unidades);
        descontarUnidades(producto, unidades);
        return new PerdidaCaducidad(producto, unidades, responsable, momento, tokenOperacion);
    }

    /**
     * Registra la pérdida y descuenta el stock en una sola transacción.
     *
     * @return true si se registró; false si esa misma confirmación ya estaba registrada
     */
    public boolean registrar(int idProducto, int unidades, PersonalComedor responsable, String tokenOperacion,
                             LocalDateTime ahora) {
        if (tokenOperacion == null || tokenOperacion.trim().isEmpty()) {
            throw new DescarteRechazadoException(SIN_CONFIRMACION);
        }
        try (Transaccion tx = Transaccion.iniciar()) {
            if (perdidaDAO.existeToken(tokenOperacion, tx)) {
                return false;
            }
            Producto producto = productoDAO.buscarProducto(idProducto, tx);
            if (producto == null) {
                throw new DescarteRechazadoException(NO_ENCONTRADO);
            }
            PerdidaCaducidad perdida = aplicarDescarte(producto, unidades, responsable, ahora, tokenOperacion);
            perdidaDAO.guardar(perdida, tx);
            tx.confirmar();
            return true;
        } catch (OptimisticLockException e) {
            throw new DescarteRechazadoException(CAMBIO_CONCURRENTE);
        } catch (PersistenceException e) {
            if (perdidaDAO.existeToken(tokenOperacion)) {
                return false; // la misma confirmación llegó dos veces a la vez: solo una se guardó
            }
            throw e;
        }
    }
    private void descontarUnidades(Producto producto, int unidades) {
        producto.setStockActual(producto.getStockActual() - unidades);
    }
}
```

Comprobación de soporte `src/test/java/ec/edu/epn/polidinner/servicio/RegistrarPerdidaTest.java`:

**`src/test/java/ec/edu/epn/polidinner/servicio/RegistrarPerdidaTest.java`**

```java
package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.DescarteRechazadoException;
import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.persistencia.JPAUtil;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;

/**
 * Comprobación de soporte de HU20 (no es una de las 12 pruebas TDD), con H2 y Hibernate reales:
 * la pérdida y el stock se guardan juntos, la misma confirmación no se registra dos veces y una cantidad
 * mayor al stock no cambia nada.
 */
class RegistrarPerdidaTest extends BaseDatosPrueba {

    private static final LocalDateTime AHORA = LUNES.atTime(15, 30);

    private final ServicioPerdidas perdidas = new ServicioPerdidas();
    private final PerdidaCaducidadDAO dao = new PerdidaCaducidadDAO();
    private Producto yogur;
    private PersonalComedor maria;

    @BeforeEach
    void preparar() {
        prepararBase();
        maria = new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "comedor123");
        yogur = Producto.registrar("Yogur 150 g", 0.90, "Vaso", 6, java.sql.Date.valueOf(LUNES.minusDays(1)));
        guardar(maria, yogur);
    }

    @AfterEach
    void cerrar() {
        cerrarBase();
    }

    @Test
    void registraLaPerdidaYDescuentaElStockUnaSolaVezPorConfirmacion() {
        assertTrue(perdidas.registrar(yogur.getId(), 6, maria, "op-1", AHORA));
        assertFalse(perdidas.registrar(yogur.getId(), 6, maria, "op-1", AHORA));

        assertEquals(0, stock(yogur));
        List<PerdidaCaducidad> historial = dao.listarRecientes();
        assertEquals(1, historial.size());
        assertEquals(6, historial.get(0).getUnidades());
        assertEquals("Yogur 150 g", historial.get(0).getNombreProducto());
        assertEquals("1700000001", historial.get(0).getResponsable().getCedula());
        assertEquals("15/11/2026", historial.get(0).getCaducidadTexto());
    }

    @Test
    void unaCantidadMayorAlStockNoCambiaNada() {
        DescarteRechazadoException error = assertThrows(DescarteRechazadoException.class,
                () -> perdidas.registrar(yogur.getId(), 8, maria, "op-2", AHORA));

        assertEquals("La cantidad supera las 6 unidades disponibles", error.getMessage());
        assertEquals(6, stock(yogur));
        assertTrue(dao.listarRecientes().isEmpty());
    }

    private void guardar(Object... entidades) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            for (Object entidad : entidades) {
                em.persist(entidad);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
```

`./mvnw test` en verde. Commit `HU20: registro de pérdidas en una transacción y comprobación con H2`.

**Refactorización adicional R4 — Christian: Introduce Explaining Variable.**

El registro transaccional ya funciona y su comprobación con H2 está verde. Se da nombre al resultado de buscar la confirmación: distingue la decisión de idempotencia del resto de la transacción. Este cambio es distinto de la extracción de `descontarUnidades` realizada en PM02.

Capturar el `if (perdidaDAO.existeToken(tokenOperacion, tx))` anterior. Sustituir la clase por esta versión que **conserva el REFACTOR de PM02**:

**`src/main/java/ec/edu/epn/polidinner/servicio/ServicioPerdidas.java`**

```java
package ec.edu.epn.polidinner.servicio;

import java.time.LocalDateTime;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import ec.edu.epn.polidinner.modelo.DescarteRechazadoException;
import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.modelo.ValidadorDescarte;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

/**
 * Soporte técnico NO modelado - HU20 Registrar pérdidas por caducidad (reglas 9 a 11).
 * aplicarDescarte: origen TDD ServicioPerdidasMockTest (PM02).
 * registrar: guarda la pérdida y el nuevo stock en UNA transacción (Transaccion). La entidad Producto se
 * carga y se modifica dentro de ella, así su @Version detecta un cambio simultáneo del mismo registro.
 */
public class ServicioPerdidas {

    static final String SIN_CONFIRMACION = "La confirmación no es válida. Vuelva a abrir el formulario de pérdidas.";
    static final String NO_ENCONTRADO = "No se encontró esa existencia de producto.";
    static final String CAMBIO_CONCURRENTE = "La existencia cambió mientras se registraba la pérdida. No se guardó"
            + " nada; revise el stock e inténtelo de nuevo.";
    static final String NO_ADMITE_DESCARTE = "Solo se registran pérdidas de existencias caducadas, habilitadas y con stock.";

    private final PerdidaCaducidadDAO perdidaDAO;
    private final ProductoDAO productoDAO;
    private final ValidadorDescarte validador;

    public ServicioPerdidas() {
        this(new PerdidaCaducidadDAO(), new ProductoDAO(), new ValidadorDescarte());
    }

    ServicioPerdidas(PerdidaCaducidadDAO perdidaDAO, ProductoDAO productoDAO, ValidadorDescarte validador) {
        this.perdidaDAO = perdidaDAO;
        this.productoDAO = productoDAO;
        this.validador = validador;
    }

    /** Valida el descarte, descuenta las unidades del registro y arma la pérdida (sin guardarla). */
    PerdidaCaducidad aplicarDescarte(Producto producto, int unidades, PersonalComedor responsable,
                                     LocalDateTime momento, String tokenOperacion) {
        if (!validador.admiteDescarte(producto, momento.toLocalDate())) {
            throw new DescarteRechazadoException(NO_ADMITE_DESCARTE);
        }
        validador.validarCantidad(producto, unidades);
        descontarUnidades(producto, unidades);
        return new PerdidaCaducidad(producto, unidades, responsable, momento, tokenOperacion);
    }

    /**
     * Registra la pérdida y descuenta el stock en una sola transacción.
     *
     * @return true si se registró; false si esa misma confirmación ya estaba registrada
     */
    public boolean registrar(int idProducto, int unidades, PersonalComedor responsable, String tokenOperacion,
                             LocalDateTime ahora) {
        if (tokenOperacion == null || tokenOperacion.trim().isEmpty()) {
            throw new DescarteRechazadoException(SIN_CONFIRMACION);
        }
        try (Transaccion tx = Transaccion.iniciar()) {
            boolean confirmacionYaRegistrada = perdidaDAO.existeToken(tokenOperacion, tx);
            if (confirmacionYaRegistrada) {
                return false;
            }
            Producto producto = productoDAO.buscarProducto(idProducto, tx);
            if (producto == null) {
                throw new DescarteRechazadoException(NO_ENCONTRADO);
            }
            PerdidaCaducidad perdida = aplicarDescarte(producto, unidades, responsable, ahora, tokenOperacion);
            perdidaDAO.guardar(perdida, tx);
            tx.confirmar();
            return true;
        } catch (OptimisticLockException e) {
            throw new DescarteRechazadoException(CAMBIO_CONCURRENTE);
        } catch (PersistenceException e) {
            if (perdidaDAO.existeToken(tokenOperacion)) {
                return false; // la misma confirmación llegó dos veces a la vez: solo una se guardó
            }
            throw e;
        }
    }
    private void descontarUnidades(Producto producto, int unidades) {
        producto.setStockActual(producto.getStockActual() - unidades);
    }
}
```

Ejecutar `./mvnw test`; capturar antes, después, diff y suite verde. Commit exclusivo `HU20/R4: Introduce Explaining Variable - nombra la confirmación registrada`. No modificar cantidades, reglas ni excepciones.

Push y PR.

### 6.4 Jorge — DAO de pérdidas y vista del historial

Rama: `trabajo/hu20-jorge`

**DAO (normal).** Reemplazar `persistencia/PerdidaCaducidadDAO.java`:

**`src/main/java/ec/edu/epn/polidinner/persistencia/PerdidaCaducidadDAO.java`**

```java
package ec.edu.epn.polidinner.persistencia;

import java.util.List;

import javax.persistence.EntityManager;

import ec.edu.epn.polidinner.modelo.PerdidaCaducidad;

/** Soporte técnico NO modelado - HU20: pérdidas por caducidad y su historial. */
public class PerdidaCaducidadDAO extends GenericDAO<PerdidaCaducidad, Integer> {

    private static final String CONTAR_TOKEN =
            "SELECT COUNT(p) FROM PerdidaCaducidad p WHERE p.tokenOperacion = :token";

    public PerdidaCaducidadDAO() {
        super(PerdidaCaducidad.class);
    }

    /** ¿Ya se registró una pérdida con este token de confirmación? (dentro de la transacción) */
    public boolean existeToken(String tokenOperacion, Transaccion tx) {
        return contarToken(tx.em(), tokenOperacion) > 0;
    }

    /** ¿Ya se registró una pérdida con este token de confirmación? (fuera de una transacción) */
    public boolean existeToken(String tokenOperacion) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return contarToken(em, tokenOperacion) > 0;
        } finally {
            em.close();
        }
    }

    /** Historial de pérdidas, de la más reciente a la más antigua (regla 11). */
    public List<PerdidaCaducidad> listarRecientes() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM PerdidaCaducidad p ORDER BY p.fechaRegistro DESC, p.id DESC",
                    PerdidaCaducidad.class).getResultList();
        } finally {
            em.close();
        }
    }

    private static long contarToken(EntityManager em, String tokenOperacion) {
        return em.createQuery(CONTAR_TOKEN, Long.class).setParameter("token", tokenOperacion).getSingleResult();
    }
}
```

Commit `HU20: DAO de pérdidas y token de confirmación`.

**Vista del historial (normal).** Reemplazar el fragmento. Muestra **fecha del registro, referencia, producto, caducidad, unidades y responsable**:

**`src/main/webapp/vistas/personal/historialPerdidas.jspf`**

```jsp
<%@ page pageEncoding="UTF-8" %>
<%-- HU20 (criterio 20.3): historial de pérdidas, de la más reciente a la más antigua. --%>
<section class="tarjeta" aria-labelledby="titulo-historial">
    <h2 id="titulo-historial">Historial de pérdidas</h2>
    <c:choose>
        <c:when test="${empty historial}">
            <div class="vacio"><p>Todavía no hay pérdidas registradas.</p></div>
        </c:when>
        <c:otherwise>
            <div class="tabla-contenedor">
                <table class="tabla">
                    <thead>
                        <tr>
                            <th scope="col">Registrada</th>
                            <th scope="col">Referencia</th>
                            <th scope="col">Producto</th>
                            <th scope="col">Caducidad</th>
                            <th scope="col" class="numero">Unidades</th>
                            <th scope="col">Responsable</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="perdida" items="${historial}">
                            <tr>
                                <td>${perdida.fechaRegistroTexto}</td>
                                <td>${perdida.referencia}</td>
                                <th scope="row"><c:out value="${perdida.nombreProducto}"/></th>
                                <td>${perdida.caducidadTexto}</td>
                                <td class="numero">${perdida.unidades}</td>
                                <td><c:out value="${perdida.responsable.nombres} ${perdida.responsable.apellidos}"/></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>
```

Commit `HU20: vista del historial de pérdidas`. Push y PR.

### 6.5 Mauricio — servlet, vista y R2

Rama: `trabajo/hu20-mauricio`

**Servlet, vista y prueba de soporte (normal).** El responsable es el `PersonalComedor` de la sesión (`"personal"`). El token de cada formulario evita registrar dos veces la misma confirmación, y el botón se desactiva al enviar:

**`src/main/java/ec/edu/epn/polidinner/controlador/PerdidasCaducidadServlet.java`**

```java
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
```

**`src/main/webapp/vistas/personal/perdidasCaducidad.jsp`**

```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- HU20 Registrar pérdidas por caducidad: descarte de unidades de existencias caducadas y su historial. --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Pérdidas por caducidad" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Pérdidas por caducidad
    </nav>
    <div class="titulo-pagina">
        <h1>Pérdidas por caducidad</h1>
        <p>Registre las unidades que retiró físicamente de una existencia caducada. Se descuentan de su stock.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <section class="tarjeta" aria-labelledby="titulo-registro">
        <h2 id="titulo-registro">Registrar pérdida</h2>
        <c:choose>
            <c:when test="${empty descartables}">
                <div class="vacio"><p>No hay existencias caducadas con stock para descartar.</p></div>
            </c:when>
            <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/personal/perdidas"
                      onsubmit="this.querySelector('button[type=submit]').disabled = true;">
                    <input type="hidden" name="token" value="${token}">
                    <div class="campo">
                        <label for="referencia">Existencia caducada <span class="requerido" aria-hidden="true">*</span></label>
                        <select class="entrada" id="referencia" name="referencia" required>
                            <c:forEach var="producto" items="${descartables}">
                                <option value="${producto.id}">
                                    ${producto.id} - <c:out value="${producto.nombre}"/> - ${producto.stockActual} unidades -
                                    caducó el <fmt:formatDate value="${producto.fechaCaducidad}" pattern="dd/MM/yyyy"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="campo">
                        <label for="unidades">Unidades descartadas <span class="requerido" aria-hidden="true">*</span></label>
                        <input class="entrada" type="number" id="unidades" name="unidades" min="1" step="1" required>
                    </div>
                    <button type="submit" class="btn btn-primario">Registrar pérdida</button>
                </form>
            </c:otherwise>
        </c:choose>
    </section>

    <%@ include file="historialPerdidas.jspf" %>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
```

**`src/test/java/ec/edu/epn/polidinner/controlador/PerdidasCaducidadServletTest.java`**

```java
package ec.edu.epn.polidinner.controlador;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.modelo.ValidadorDescarte;
import ec.edu.epn.polidinner.persistencia.PerdidaCaducidadDAO;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;
import ec.edu.epn.polidinner.servicio.ServicioPerdidas;

/**
 * Prueba de soporte del formulario de HU20 (no es una de las 12 pruebas TDD). Protege la refactorización R2:
 * debe seguir en verde antes y después de extraer SolicitudDescarte.
 */
class PerdidasCaducidadServletTest {

    private final ServicioPerdidas servicio = Mockito.mock(ServicioPerdidas.class);
    private final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    private final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    private final HttpSession sesion = Mockito.mock(HttpSession.class);
    private final PersonalComedor maria =
            new PersonalComedor("1700000001", "María", "Pérez", "Administradora", "comedor123");
    private final PerdidasCaducidadServlet servlet = new PerdidasCaducidadServlet(servicio,
            Mockito.mock(ProductoDAO.class), Mockito.mock(PerdidaCaducidadDAO.class), new ValidadorDescarte());

    @BeforeEach
    void preparar() {
        Mockito.when(request.getSession()).thenReturn(sesion);
        Mockito.when(request.getContextPath()).thenReturn("/polidinner");
        Mockito.when(sesion.getAttribute("personal")).thenReturn(maria);
    }

    @Test
    void registraLaPerdidaConLosDatosDelFormularioYElPersonalDeLaSesion() throws Exception {
        Mockito.when(request.getParameter("referencia")).thenReturn("202");
        Mockito.when(request.getParameter("unidades")).thenReturn("6");
        Mockito.when(request.getParameter("token")).thenReturn("op-1");
        Mockito.when(servicio.registrar(ArgumentMatchers.eq(202), ArgumentMatchers.eq(6), ArgumentMatchers.same(maria),
                ArgumentMatchers.eq("op-1"), ArgumentMatchers.any(LocalDateTime.class))).thenReturn(true);

        servlet.doPost(request, response);

        Mockito.verify(sesion).setAttribute("flashExito", PerdidasCaducidadServlet.REGISTRADA);
        Mockito.verify(response).sendRedirect("/polidinner/personal/perdidas");
    }

    @Test
    void rechazaUnidadesNoNumericasSinRegistrar() throws Exception {
        Mockito.when(request.getParameter("referencia")).thenReturn("202");
        Mockito.when(request.getParameter("unidades")).thenReturn("seis");
        Mockito.when(request.getParameter("token")).thenReturn("op-1");

        servlet.doPost(request, response);

        Mockito.verify(servicio, Mockito.never()).registrar(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(),
                ArgumentMatchers.any(), ArgumentMatchers.anyString(), ArgumentMatchers.any());
        Mockito.verify(sesion).setAttribute("flashError", "Las unidades descartadas deben ser un número entero.");
        Mockito.verify(response).sendRedirect("/polidinner/personal/perdidas");
    }
}
```

`./mvnw test` en verde. Commit `HU20: servlet y vista de pérdidas por caducidad`.

**R2 Extract Class.** *Motivo:* `doPost()` mezcla la lectura y conversión del formulario con el registro. La lectura pasa a `SolicitudDescarte` (mismo patrón que `FormularioItemMenu`).

1. 📸 `R2-1-ANTES` (`doPost` actual) y `PerdidasCaducidadServletTest` en verde.
2. Crear `SolicitudDescarte` y reemplazar el servlet:

**`src/main/java/ec/edu/epn/polidinner/controlador/SolicitudDescarte.java`**

```java
package ec.edu.epn.polidinner.controlador;

import javax.servlet.http.HttpServletRequest;

import ec.edu.epn.polidinner.modelo.DescarteRechazadoException;

/**
 * Soporte técnico NO modelado - Refactorización: Extract Class (desde PerdidasCaducidadServlet.doPost()).
 * Lee y convierte los datos del formulario de pérdidas; si faltan o no son números, lanza
 * DescarteRechazadoException con el mismo mensaje que mostraba el servlet.
 */
final class SolicitudDescarte {

    private final int referencia;
    private final int unidades;
    private final String token;

    private SolicitudDescarte(int referencia, int unidades, String token) {
        this.referencia = referencia;
        this.unidades = unidades;
        this.token = token;
    }

    static SolicitudDescarte desde(HttpServletRequest request) {
        String referenciaTexto = request.getParameter("referencia");
        String unidadesTexto = request.getParameter("unidades");
        if (referenciaTexto == null || referenciaTexto.trim().isEmpty()
                || unidadesTexto == null || unidadesTexto.trim().isEmpty()) {
            throw new DescarteRechazadoException("Seleccione la existencia e indique las unidades descartadas.");
        }
        try {
            return new SolicitudDescarte(Integer.parseInt(referenciaTexto.trim()),
                    Integer.parseInt(unidadesTexto.trim()), request.getParameter("token"));
        } catch (NumberFormatException e) {
            throw new DescarteRechazadoException("Las unidades descartadas deben ser un número entero.");
        }
    }

    int getReferencia() {
        return referencia;
    }

    int getUnidades() {
        return unidades;
    }

    String getToken() {
        return token;
    }
}
```

**`src/main/java/ec/edu/epn/polidinner/controlador/PerdidasCaducidadServlet.java`**

```java
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
        try {
            SolicitudDescarte solicitud = SolicitudDescarte.desde(request);
            PersonalComedor personal = (PersonalComedor) sesion.getAttribute("personal");
            boolean registrada = servicio.registrar(solicitud.getReferencia(), solicitud.getUnidades(), personal,
                    solicitud.getToken(), RelojAplicacion.ahora());
            sesion.setAttribute("flashExito", registrada ? REGISTRADA : REPETIDA);
        } catch (DescarteRechazadoException e) {
            sesion.setAttribute("flashError", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/personal/perdidas");
    }
}
```

3. 📸 `R2-2-DESPUES` y `R2-3-DIFF`. 4. `./mvnw test`: 📸 `R2-4-VERDE`.
4. Commit `HU20/R2: Extract Class - SolicitudDescarte`. 📸 `R2-5-COMMIT`. Push y PR.

### 6.6 Oscar — datos y acceso

Rama: `trabajo/hu20-oscar`

**Datos de demostración (normal).** Crear `src/main/resources/demo_hu20_perdidas_h2.sql`:

**`src/main/resources/demo_hu20_perdidas_h2.sql`**

```sql
-- PoliDinner - HU20 Registrar pérdidas por caducidad: datos de la demostración (base H2 del contenedor).
-- Se carga después de demo_hu18_ventas_h2.sql (usa la referencia 101 de ese script como existencia vigente).
-- Referencia 202: Yogur 150 g con 6 unidades que caducaron ayer, habilitado y sin pérdidas registradas.

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 202, 'Yogur 150 g', 0.90, 'Disponible', 'Vaso', 6, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 202);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 202, DATEADD('DAY', -1, CURRENT_DATE), TRUE, 'demo-hu20-202' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 202);
```

Commit `HU20: datos de demostración para H2`.

**Acceso del panel (normal).**

**`src/main/webapp/vistas/personal/accesos/perdidasCaducidad.jspf`**

```jsp
<%@ page pageEncoding="UTF-8" %>
                <li>
                    <a class="accion-fila accion-fila--peligro" href="${pageContext.request.contextPath}/personal/perdidas">
                        <svg class="icono" aria-hidden="true"><use href="#i-retirar"/></svg>
                        <span class="accion-fila__texto">
                            <strong>Pérdidas por caducidad</strong>
                            <span>Registre las unidades descartadas de existencias caducadas y revise el historial.</span>
                        </span>
                        <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                    </a>
                </li>
```

Commit `HU20: acceso a pérdidas por caducidad en el panel`. Push y PR.

---

## 7. HU19 — Consultar productos por franja de caducidad

Rama compartida: `feature/hu19-franjas-caducidad`.

### 7.1 Jorge — PP01 (TDD parametrizada)

Rama: `trabajo/hu19-jorge`

**RED.** Crear `src/test/java/ec/edu/epn/polidinner/modelo/ClasificadorCaducidadTest.java`:

**`src/test/java/ec/edu/epn/polidinner/modelo/ClasificadorCaducidadTest.java`**

```java
package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** HU19 Consultar productos por franja de caducidad - TDD de ClasificadorCaducidad (PP01). */
class ClasificadorCaducidadTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);

    private final ClasificadorCaducidad clasificador = new ClasificadorCaducidad();

    /** PP01 - Tabla de franjas (escenarios 19.1.1, 19.1.2 y 19.2.1): límites de cada franja. */
    @ParameterizedTest(name = "caduca a {0} días de hoy -> {1}")
    @CsvSource({
            "-1, CADUCADOS",
            "0,  CADUCAN_HOY",
            "1,  PROXIMOS_3_DIAS",
            "3,  PROXIMOS_3_DIAS",
            "4,  MAS_ADELANTE"
    })
    void ubicaCadaFechaDeCaducidadEnSuFranja(int diasDesdeHoy, FranjaCaducidad franjaEsperada) {
        assertEquals(franjaEsperada, clasificador.clasificar(HOY.plusDays(diasDesdeHoy), HOY));
    }
}
```

Los cinco casos fallan (`expected: <CADUCADOS> but was: <null>`, etc.). 📸 `PP01-1-RED`. Commit `HU19/PP01: RED - límites de las franjas`.

**GREEN.**

**`src/main/java/ec/edu/epn/polidinner/modelo/ClasificadorCaducidad.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/**
 * HU19 (tabla de franjas): ubica cada fecha de caducidad en su franja. Se conserva la convención de
 * Producto.estaVigente(): el producto sigue vigente durante su fecha de caducidad.
 * Origen TDD: ClasificadorCaducidadTest (PP01).
 */
public class ClasificadorCaducidad implements ClasificadorFranja {

    @Override
    public FranjaCaducidad clasificar(LocalDate caducidad, LocalDate hoy) {
        if (caducidad.isBefore(hoy)) {
            return FranjaCaducidad.CADUCADOS;
        }
        if (caducidad.isEqual(hoy)) {
            return FranjaCaducidad.CADUCAN_HOY;
        }
        if (!caducidad.isAfter(hoy.plusDays(3))) {
            return FranjaCaducidad.PROXIMOS_3_DIAS;
        }
        return FranjaCaducidad.MAS_ADELANTE;
    }
}
```

Pasan los cinco. 📸 `PP01-2-GREEN`. Commit `HU19/PP01: GREEN - clasifica cada fecha en su franja`.

**PP01 — REFACTOR.** **Técnica: Introduce Explaining Variable.** Introducir nombres para el límite y su comparación explica el significado del tercer caso. El cálculo permanece después de las dos primeras guardas; no se adelanta la evaluación.

Con las pruebas verdes, capture el código GREEN antes de modificarlo. Aplique este cambio sin alterar las condiciones ni los resultados de la prueba:

**`src/main/java/ec/edu/epn/polidinner/modelo/ClasificadorCaducidad.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/**
 * HU19 (tabla de franjas): ubica cada fecha de caducidad en su franja. Se conserva la convención de
 * Producto.estaVigente(): el producto sigue vigente durante su fecha de caducidad.
 * Origen TDD: ClasificadorCaducidadTest (PP01).
 */
public class ClasificadorCaducidad implements ClasificadorFranja {

    @Override
    public FranjaCaducidad clasificar(LocalDate caducidad, LocalDate hoy) {
        if (caducidad.isBefore(hoy)) {
            return FranjaCaducidad.CADUCADOS;
        }
        if (caducidad.isEqual(hoy)) {
            return FranjaCaducidad.CADUCAN_HOY;
        }
        LocalDate ultimoDiaDeLosProximosTres = hoy.plusDays(3);
        boolean caducaEnLosProximosTresDias = !caducidad.isAfter(ultimoDiaDeLosProximosTres);
        if (caducaEnLosProximosTresDias) {
            return FranjaCaducidad.PROXIMOS_3_DIAS;
        }
        return FranjaCaducidad.MAS_ADELANTE;
    }
}
```

Ejecute la prueba de `ClasificadorCaducidadTest` y después `./mvnw test`. Capture el diff y el resultado verde como `PP01-3-REFACTOR`. Commit **`HU19/PP01: REFACTOR - Introduce Explaining Variable: explica el límite de los próximos días`**. Este cambio pertenece al ciclo PP01; **no cuenta entre R1–R4**.

Con el ciclo cerrado y la suite verde: push y PR de Jorge a `feature/hu19-franjas-caducidad`.

### 7.2 Jean — PU05 y PU06 (TDD)

Rama: `trabajo/hu19-jean`

**PU05 — RED.** Crear `src/test/java/ec/edu/epn/polidinner/modelo/FiltroCaducidadTest.java`. Usa un clasificador escrito a mano, así que **no espera** a Jorge:

**`src/test/java/ec/edu/epn/polidinner/modelo/FiltroCaducidadTest.java`**

```java
package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/** HU19 Consultar productos por franja de caducidad - TDD de FiltroCaducidad (PU05 y PU06). */
class FiltroCaducidadTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);

    /** Doble de prueba escrito a mano: hasta hoy+3 es «Próximos 3 días» y después «Más adelante». */
    private final ClasificadorFranja clasificadorDePrueba = (caducidad, hoy) ->
            caducidad.isAfter(hoy.plusDays(3)) ? FranjaCaducidad.MAS_ADELANTE : FranjaCaducidad.PROXIMOS_3_DIAS;

    private final FiltroCaducidad filtro = new FiltroCaducidad(clasificadorDePrueba);

    private static List<Integer> referencias(List<ExistenciaProducto> existencias) {
        return existencias.stream().map(ExistenciaProducto::getReferencia).toList();
    }

    /** PU05 - Escenarios 19.1.2 y 19.2.2: solo la franja elegida, por caducidad y luego por referencia. */
    @Test
    void muestraSoloLaFranjaElegidaOrdenadaPorCaducidadYReferencia() {
        List<ExistenciaProducto> existencias = List.of(
                new ExistenciaProducto(201, "Yogur 150 g", 4, HOY.plusDays(2), true),
                new ExistenciaProducto(105, "Agua 500 ml", 4, HOY.plusDays(2), true),
                new ExistenciaProducto(102, "Agua 500 ml", 7, HOY.plusDays(4), true),
                new ExistenciaProducto(101, "Agua 500 ml", 3, HOY.plusDays(1), true));

        List<ExistenciaProducto> resultado = filtro.filtrar(existencias, FranjaCaducidad.PROXIMOS_3_DIAS, HOY);

        assertEquals(List.of(101, 105, 201), referencias(resultado));
    }
}
```

Falla con `expected: <[101, 105, 201]> but was: <[]>`. 📸 `PU05-1-RED`. Commit `HU19/PU05: RED - franja elegida y orden`.

**PU05 — GREEN.**

**`src/main/java/ec/edu/epn/polidinner/modelo/FiltroCaducidad.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * HU19 (criterios 19.1 a 19.3): existencias de la franja elegida, por caducidad más próxima y referencia.
 * Origen TDD: FiltroCaducidadTest (PU05 y PU06).
 */
public class FiltroCaducidad {

    private final ClasificadorFranja clasificador;

    public FiltroCaducidad(ClasificadorFranja clasificador) {
        this.clasificador = clasificador;
    }

    public List<ExistenciaProducto> filtrar(List<ExistenciaProducto> existencias, FranjaCaducidad franja,
                                            LocalDate hoy) {
        return existencias.stream()
                .filter(existencia -> existencia.getCaducidad() != null)
                .filter(existencia -> clasificador.clasificar(existencia.getCaducidad(), hoy) == franja)
                .sorted(Comparator.comparing(ExistenciaProducto::getCaducidad)
                        .thenComparingInt(ExistenciaProducto::getReferencia))
                .toList();
    }
}
```

Pasa. 📸 `PU05-2-GREEN`. Commit `HU19/PU05: GREEN - filtra y ordena por caducidad`.

**PU05 — REFACTOR.** **Técnica: Extract Method.** Extraer el predicado de pertenencia separa la regla de clasificación del recorrido y de la ordenación. Se conserva la guarda para caducidad ausente y la misma llamada al clasificador.

Con las pruebas verdes, capture el código GREEN antes de modificarlo. Aplique este cambio sin alterar las condiciones ni los resultados de la prueba:

**`src/main/java/ec/edu/epn/polidinner/modelo/FiltroCaducidad.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * HU19 (criterios 19.1 a 19.3): existencias de la franja elegida, por caducidad más próxima y referencia.
 * Origen TDD: FiltroCaducidadTest (PU05 y PU06).
 */
public class FiltroCaducidad {

    private final ClasificadorFranja clasificador;

    public FiltroCaducidad(ClasificadorFranja clasificador) {
        this.clasificador = clasificador;
    }

    public List<ExistenciaProducto> filtrar(List<ExistenciaProducto> existencias, FranjaCaducidad franja,
                                            LocalDate hoy) {
        return existencias.stream()
                .filter(existencia -> perteneceALaFranja(existencia, franja, hoy))
                .sorted(Comparator.comparing(ExistenciaProducto::getCaducidad)
                        .thenComparingInt(ExistenciaProducto::getReferencia))
                .toList();
    }

    private boolean perteneceALaFranja(ExistenciaProducto existencia, FranjaCaducidad franja, LocalDate hoy) {
        return existencia.getCaducidad() != null
                && clasificador.clasificar(existencia.getCaducidad(), hoy) == franja;
    }
}
```

Ejecute la prueba de `FiltroCaducidadTest` y después `./mvnw test`. Capture el diff y el resultado verde como `PU05-3-REFACTOR`. Commit **`HU19/PU05: REFACTOR - Extract Method: nombra la pertenencia a la franja`**. Este cambio pertenece al ciclo PU05; **no cuenta entre R1–R4**.

**PU06 — RED.** Agregar el segundo método; la clase queda así:

**`src/test/java/ec/edu/epn/polidinner/modelo/FiltroCaducidadTest.java`**

```java
package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/** HU19 Consultar productos por franja de caducidad - TDD de FiltroCaducidad (PU05 y PU06). */
class FiltroCaducidadTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);

    /** Doble de prueba escrito a mano: hasta hoy+3 es «Próximos 3 días» y después «Más adelante». */
    private final ClasificadorFranja clasificadorDePrueba = (caducidad, hoy) ->
            caducidad.isAfter(hoy.plusDays(3)) ? FranjaCaducidad.MAS_ADELANTE : FranjaCaducidad.PROXIMOS_3_DIAS;

    private final FiltroCaducidad filtro = new FiltroCaducidad(clasificadorDePrueba);

    private static List<Integer> referencias(List<ExistenciaProducto> existencias) {
        return existencias.stream().map(ExistenciaProducto::getReferencia).toList();
    }

    /** PU05 - Escenarios 19.1.2 y 19.2.2: solo la franja elegida, por caducidad y luego por referencia. */
    @Test
    void muestraSoloLaFranjaElegidaOrdenadaPorCaducidadYReferencia() {
        List<ExistenciaProducto> existencias = List.of(
                new ExistenciaProducto(201, "Yogur 150 g", 4, HOY.plusDays(2), true),
                new ExistenciaProducto(105, "Agua 500 ml", 4, HOY.plusDays(2), true),
                new ExistenciaProducto(102, "Agua 500 ml", 7, HOY.plusDays(4), true),
                new ExistenciaProducto(101, "Agua 500 ml", 3, HOY.plusDays(1), true));

        List<ExistenciaProducto> resultado = filtro.filtrar(existencias, FranjaCaducidad.PROXIMOS_3_DIAS, HOY);

        assertEquals(List.of(101, 105, 201), referencias(resultado));
    }

    /** PU06 - Escenario 19.3.1: excluye registros agotados e históricos no habilitados. */
    @Test
    void excluyeExistenciasSinStockYRegistrosNoHabilitados() {
        List<ExistenciaProducto> existencias = List.of(
                new ExistenciaProducto(103, "Galletas 100 g", 0, HOY.plusDays(1), true),
                new ExistenciaProducto(401, "Jugo 250 ml", 5, HOY.plusDays(1), false),
                new ExistenciaProducto(101, "Agua 500 ml", 3, HOY.plusDays(1), true));

        List<ExistenciaProducto> resultado = filtro.filtrar(existencias, FranjaCaducidad.PROXIMOS_3_DIAS, HOY);

        assertEquals(List.of(101), referencias(resultado));
    }
}
```

Falla con `expected: <[101]> but was: <[101, 103, 401]>`. 📸 `PU06-1-RED`. Commit `HU19/PU06: RED - excluye agotados e históricos`.

**PU06 — GREEN.**

**`src/main/java/ec/edu/epn/polidinner/modelo/FiltroCaducidad.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * HU19 (criterios 19.1 a 19.3): existencias de la franja elegida, por caducidad más próxima y referencia.
 * Origen TDD: FiltroCaducidadTest (PU05 y PU06).
 */
public class FiltroCaducidad {

    private final ClasificadorFranja clasificador;

    public FiltroCaducidad(ClasificadorFranja clasificador) {
        this.clasificador = clasificador;
    }

    public List<ExistenciaProducto> filtrar(List<ExistenciaProducto> existencias, FranjaCaducidad franja,
                                            LocalDate hoy) {
        return existencias.stream()
                .filter(existencia -> existencia.isHabilitado() && existencia.getStock() > 0)
                .filter(existencia -> perteneceALaFranja(existencia, franja, hoy))
                .sorted(Comparator.comparing(ExistenciaProducto::getCaducidad)
                        .thenComparingInt(ExistenciaProducto::getReferencia))
                .toList();
    }

    private boolean perteneceALaFranja(ExistenciaProducto existencia, FranjaCaducidad franja, LocalDate hoy) {
        return existencia.getCaducidad() != null
                && clasificador.clasificar(existencia.getCaducidad(), hoy) == franja;
    }
}
```

Pasan las dos. 📸 `PU06-2-GREEN`. Commit `HU19/PU06: GREEN - solo habilitados con stock`.

**PU06 — REFACTOR.** **Técnica: Extract Method.** Extraer la disponibilidad deja visibles las dos reglas independientes de la consulta: que la existencia pueda mostrarse y que pertenezca a la franja. No cambia el filtro.

Con las pruebas verdes, capture el código GREEN antes de modificarlo. Aplique este cambio sin alterar las condiciones ni los resultados de la prueba:

**`src/main/java/ec/edu/epn/polidinner/modelo/FiltroCaducidad.java`**

```java
package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * HU19 (criterios 19.1 a 19.3): existencias de la franja elegida, por caducidad más próxima y referencia.
 * Origen TDD: FiltroCaducidadTest (PU05 y PU06).
 */
public class FiltroCaducidad {

    private final ClasificadorFranja clasificador;

    public FiltroCaducidad(ClasificadorFranja clasificador) {
        this.clasificador = clasificador;
    }

    public List<ExistenciaProducto> filtrar(List<ExistenciaProducto> existencias, FranjaCaducidad franja,
                                            LocalDate hoy) {
        return existencias.stream()
                .filter(this::estaDisponibleParaConsulta)
                .filter(existencia -> perteneceALaFranja(existencia, franja, hoy))
                .sorted(Comparator.comparing(ExistenciaProducto::getCaducidad)
                        .thenComparingInt(ExistenciaProducto::getReferencia))
                .toList();
    }

    private boolean perteneceALaFranja(ExistenciaProducto existencia, FranjaCaducidad franja, LocalDate hoy) {
        return existencia.getCaducidad() != null
                && clasificador.clasificar(existencia.getCaducidad(), hoy) == franja;
    }

    private boolean estaDisponibleParaConsulta(ExistenciaProducto existencia) {
        return existencia.isHabilitado() && existencia.getStock() > 0;
    }
}
```

Ejecute la prueba de `FiltroCaducidadTest` y después `./mvnw test`. Capture el diff y el resultado verde como `PU06-3-REFACTOR`. Commit **`HU19/PU06: REFACTOR - Extract Method: nombra la disponibilidad para consulta`**. Este cambio pertenece al ciclo PU06; **no cuenta entre R1–R4**.

Push y PR.

### 7.3 Christian — servicio y comprobación

Rama: `trabajo/hu19-christian`

**Servicio (normal).** En `ServicioCaducidad` se implementa `consultar`:

**`src/main/java/ec/edu/epn/polidinner/servicio/ServicioCaducidad.java`**

```java
package ec.edu.epn.polidinner.servicio;

import java.time.LocalDate;
import java.util.List;

import ec.edu.epn.polidinner.modelo.ClasificadorCaducidad;
import ec.edu.epn.polidinner.modelo.ExistenciaProducto;
import ec.edu.epn.polidinner.modelo.FiltroCaducidad;
import ec.edu.epn.polidinner.modelo.FranjaCaducidad;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;

/**
 * Soporte técnico NO modelado - HU19: existencias de la franja elegida a partir de los registros de
 * Producto (habilitados o no; el filtro decide).
 */
public class ServicioCaducidad {

    private final ProductoDAO productoDAO;
    private final FiltroCaducidad filtro;

    public ServicioCaducidad() {
        this(new ProductoDAO(), new FiltroCaducidad(new ClasificadorCaducidad()));
    }

    ServicioCaducidad(ProductoDAO productoDAO, FiltroCaducidad filtro) {
        this.productoDAO = productoDAO;
        this.filtro = filtro;
    }

    public List<ExistenciaProducto> consultar(FranjaCaducidad franja, LocalDate hoy) {
        List<ExistenciaProducto> existencias = productoDAO.listarTodos().stream()
                .map(ExistenciaProducto::desde)
                .toList();
        return filtro.filtrar(existencias, franja, hoy);
    }
}
```

Commit `HU19: servicio de consulta por franja`.

**Comprobación con H2 (normal, espera obligatoria).** Cuando PP01 de Jorge y PU05–PU06 de Jean estén en la `feature`, hacer `git merge origin/feature/hu19-franjas-caducidad` y crear `src/test/java/ec/edu/epn/polidinner/servicio/ConsultaCaducidadTest.java`:

**`src/test/java/ec/edu/epn/polidinner/servicio/ConsultaCaducidadTest.java`**

```java
package ec.edu.epn.polidinner.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ec.edu.epn.polidinner.modelo.ExistenciaProducto;
import ec.edu.epn.polidinner.modelo.FranjaCaducidad;
import ec.edu.epn.polidinner.modelo.Producto;
import ec.edu.epn.polidinner.persistencia.JPAUtil;

/**
 * Comprobación de soporte de HU19 (no es una de las 12 pruebas TDD), con H2 y Hibernate reales: los
 * registros de Producto guardados se leen, se clasifican con ClasificadorCaducidad y se filtran con
 * FiltroCaducidad. Usa el código de Jorge (PP01) y de Jean (PU05 y PU06).
 */
class ConsultaCaducidadTest extends BaseDatosPrueba {

    private final ServicioCaducidad consulta = new ServicioCaducidad();

    @BeforeEach
    void preparar() {
        prepararBase();
    }

    @AfterEach
    void cerrar() {
        cerrarBase();
    }

    @Test
    void consultaCadaFranjaConLosRegistrosGuardados() {
        Producto pastel = registrar("Pastel porción", 2, LUNES, true);
        Producto agua = registrar("Agua 500 ml", 3, LUNES.plusDays(1), true);
        Producto empanada = registrar("Empanada unidad", 2, LUNES.plusDays(3), true);
        registrar("Agua 500 ml", 7, LUNES.plusDays(4), true);
        registrar("Galletas 100 g", 0, LUNES.plusDays(1), true);
        registrar("Jugo 250 ml", 5, LUNES.plusDays(1), false);
        Producto yogur = registrar("Yogur 150 g", 6, LUNES.minusDays(1), true);

        assertEquals(List.of(pastel.getId()), referencias(FranjaCaducidad.CADUCAN_HOY));
        assertEquals(List.of(agua.getId(), empanada.getId()), referencias(FranjaCaducidad.PROXIMOS_3_DIAS));
        assertEquals(List.of(yogur.getId()), referencias(FranjaCaducidad.CADUCADOS));
    }

    private List<Integer> referencias(FranjaCaducidad franja) {
        return consulta.consultar(franja, LUNES).stream().map(ExistenciaProducto::getReferencia)
                .filter(id -> id != jugo.getId()).toList();
    }

    private Producto registrar(String nombre, int stock, LocalDate caducidad, boolean habilitado) {
        java.sql.Date fecha = java.sql.Date.valueOf(caducidad);
        Producto producto = habilitado
                ? Producto.registrar(nombre, 1.00, nombre, stock, fecha)
                : new Producto(nombre, 1.00, "Disponible", nombre, stock, fecha);
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(producto);
            em.getTransaction().commit();
            return producto;
        } finally {
            em.close();
        }
    }
}
```

`./mvnw test` en verde. Commit `HU19: comprobación de franjas con H2`.

**Refactorización adicional R3 — Christian: Replace Temp with Query.**

Partir del servicio completo y de `ConsultaCaducidadTest` en verde. La variable temporal `existencias` contiene el resultado de leer y transformar los productos. Se convierte esa expresión en una consulta con nombre y se sustituye el temporal por su llamada.

1. Capturar el método anterior con `List<ExistenciaProducto> existencias`.
2. Extraer la expresión a `consultarExistenciasGuardadas()` y eliminar el temporal. La consulta se ejecuta **una sola vez**, igual que antes; no se modifica la selección ni la ordenación.

**`src/main/java/ec/edu/epn/polidinner/servicio/ServicioCaducidad.java`**

```java
package ec.edu.epn.polidinner.servicio;

import java.time.LocalDate;
import java.util.List;

import ec.edu.epn.polidinner.modelo.ClasificadorCaducidad;
import ec.edu.epn.polidinner.modelo.ExistenciaProducto;
import ec.edu.epn.polidinner.modelo.FiltroCaducidad;
import ec.edu.epn.polidinner.modelo.FranjaCaducidad;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;

/**
 * Soporte técnico NO modelado - HU19: existencias de la franja elegida a partir de los registros de
 * Producto (habilitados o no; el filtro decide).
 */
public class ServicioCaducidad {

    private final ProductoDAO productoDAO;
    private final FiltroCaducidad filtro;

    public ServicioCaducidad() {
        this(new ProductoDAO(), new FiltroCaducidad(new ClasificadorCaducidad()));
    }

    ServicioCaducidad(ProductoDAO productoDAO, FiltroCaducidad filtro) {
        this.productoDAO = productoDAO;
        this.filtro = filtro;
    }

    public List<ExistenciaProducto> consultar(FranjaCaducidad franja, LocalDate hoy) {
        return filtro.filtrar(consultarExistenciasGuardadas(), franja, hoy);
    }

    private List<ExistenciaProducto> consultarExistenciasGuardadas() {
        return productoDAO.listarTodos().stream().map(ExistenciaProducto::desde).toList();
    }
}
```

3. Ejecutar `./mvnw test`; capturar después, diff y suite verde.
4. Commit exclusivo `HU19/R3: Replace Temp with Query - consulta las existencias guardadas`. No agregar funcionalidad ni volver a contar el cambio de PU05/PU06.

Push y PR.

### 7.4 Mauricio — servlet y vista

Rama: `trabajo/hu19-mauricio`

**`src/main/java/ec/edu/epn/polidinner/controlador/CaducidadProductosServlet.java`**

```java
package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ec.edu.epn.polidinner.modelo.ExistenciaProducto;
import ec.edu.epn.polidinner.modelo.FranjaCaducidad;
import ec.edu.epn.polidinner.servicio.ServicioCaducidad;

/**
 * HU19 Consultar productos por franja de caducidad - pantalla «Caducidad de productos» del personal
 * (protegida por FiltroPersonal en /personal/*). Parámetro "franja": un valor de FranjaCaducidad.
 */
@WebServlet("/personal/caducidad-productos")
public class CaducidadProductosServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/caducidadProductos.jsp";

    private final ServicioCaducidad servicio = new ServicioCaducidad();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("franjas", FranjaCaducidad.values());
        String valor = request.getParameter("franja");
        if (valor != null) {
            try {
                FranjaCaducidad franja = FranjaCaducidad.valueOf(valor);
                List<ExistenciaProducto> existencias = servicio.consultar(franja, RelojAplicacion.fechaActual());
                request.setAttribute("franjaSeleccionada", franja);
                request.setAttribute("existencias", existencias);
                request.setAttribute("consultado", true);
            } catch (IllegalArgumentException e) {
                request.setAttribute("error", "Seleccione una franja válida.");
            } catch (RuntimeException e) {
                log("No se pudo consultar la caducidad de productos", e);
                request.setAttribute("error", "No se pudo consultar la caducidad. Intente de nuevo en unos minutos.");
            }
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}
```

Commit `HU19: servlet de caducidad de productos`.

**`src/main/webapp/vistas/personal/caducidadProductos.jsp`**

```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- HU19 Consultar productos por franja de caducidad: registros habilitados con stock de la franja elegida. --%>
<c:set var="titulo" value="Caducidad de productos" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Caducidad de productos
    </nav>
    <div class="titulo-pagina">
        <h1>Caducidad de productos</h1>
        <p>Existencias habilitadas con stock según su fecha de caducidad. Un producto se vende hasta su fecha de caducidad inclusive.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <section class="tarjeta" aria-labelledby="titulo-franja">
        <h2 id="titulo-franja">Franja</h2>
        <form method="get" action="${pageContext.request.contextPath}/personal/caducidad-productos">
            <div class="campo">
                <label for="franja">Seleccione la franja</label>
                <select class="entrada" id="franja" name="franja">
                    <c:forEach var="f" items="${franjas}">
                        <option value="${f}" <c:if test="${f == franjaSeleccionada}">selected</c:if>>${f.etiqueta}</option>
                    </c:forEach>
                </select>
            </div>
            <button type="submit" class="btn btn-primario">Consultar</button>
        </form>
    </section>

    <c:if test="${consultado}">
        <section class="tarjeta" aria-labelledby="titulo-resultado">
            <h2 id="titulo-resultado">${franjaSeleccionada.etiqueta}</h2>
            <c:choose>
                <c:when test="${empty existencias}">
                    <div class="vacio"><p>No hay productos con stock en la franja seleccionada</p></div>
                </c:when>
                <c:otherwise>
                    <div class="tabla-contenedor">
                        <table class="tabla">
                            <thead>
                                <tr>
                                    <th scope="col">Referencia</th>
                                    <th scope="col">Producto</th>
                                    <th scope="col" class="numero">Unidades</th>
                                    <th scope="col">Caduca</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="e" items="${existencias}">
                                    <tr>
                                        <td>${e.referencia}</td>
                                        <th scope="row"><c:out value="${e.nombre}"/></th>
                                        <td class="numero">${e.stock}</td>
                                        <td>${e.caducidadTexto}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </c:if>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
```

Commit `HU19: vista de caducidad de productos`. Push y PR.

### 7.5 Oscar — acceso y datos

Rama: `trabajo/hu19-oscar`

**`src/main/webapp/vistas/personal/accesos/caducidadProductos.jspf`**

```jsp
<%@ page pageEncoding="UTF-8" %>
                <li>
                    <a class="accion-fila" href="${pageContext.request.contextPath}/personal/caducidad-productos">
                        <svg class="icono" aria-hidden="true"><use href="#i-reloj"/></svg>
                        <span class="accion-fila__texto">
                            <strong>Caducidad de productos</strong>
                            <span>Vea las existencias que caducan hoy, en los próximos 3 días o que ya caducaron.</span>
                        </span>
                        <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                    </a>
                </li>
```

Commit `HU19: acceso a caducidad de productos en el panel`.

**`src/main/resources/demo_hu19_caducidad_h2.sql`**

```sql
-- PoliDinner - HU19 Consultar productos por franja de caducidad: datos de la demostración (base H2).
-- Se carga después de los scripts de HU18 y HU20 (usa sus referencias 101, 102, 103, 201 y 202).
-- 105 caduca pasado mañana, 301 caduca hoy, 302 dentro de 3 días y 401 es un histórico no habilitado.

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 105, 'Agua 500 ml', 0.50, 'Disponible', 'Botella', 4, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 105);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 105, DATEADD('DAY', 2, CURRENT_DATE), TRUE, 'demo-hu19-105' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 105);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 301, 'Pastel porción', 1.50, 'Disponible', 'Porción', 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 301);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 301, DATEADD('DAY', 0, CURRENT_DATE), TRUE, 'demo-hu19-301' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 301);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 302, 'Empanada unidad', 1.00, 'Disponible', 'Unidad', 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 302);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 302, DATEADD('DAY', 3, CURRENT_DATE), TRUE, 'demo-hu19-302' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 302);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 401, 'Jugo 250 ml', 0.80, 'Disponible', 'Caja', 5, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 401);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 401, DATEADD('DAY', 1, CURRENT_DATE), FALSE, 'demo-hu19-401' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 401);
```

Commit `HU19: datos de demostración para H2`. Push y PR.

## 8. Integraciones y Jenkins (Mauricio)

Orden: **HU18 → HU20 → HU19**. Una integración por HU.

### 8.1 Pasos de cada integración

1. Confirmar que los PR `trabajo/... → feature/...` de la HU estén fusionados (merge commit).
2. Traer el último `develop` a la `feature` (en HU18 no cambia nada; en HU20 y HU19 trae lo ya integrado) y resolver conflictos si aparecen:

```bash
git checkout feature/hu20-perdidas-caducidad && git pull
git merge origin/develop
./mvnw test
git push
```

3. Recorrer los escenarios de la HU en la aplicación: ficha funcional enlazada al inicio y recorridos de la sección 9.1. No limitar la aceptación al recorrido de HU20.
4. Comprobar que **cada integrante tiene al menos dos commits nuevos** en la HU:

```bash
git log --format="%h %an %s" origin/develop..origin/feature/hu18-ventas-productos
```

5. PR `feature/... → develop` (merge commit) y fusionarlo.
6. **Ejecutar Jenkins sobre `develop`.**
7. Capturas:
   - 📸 `INT-HU18-1-COMMITS`: salida del paso 4.
   - 📸 `INT-HU18-2-PR`: PR fusionado.
   - 📸 `INT-HU18-3-JENKINS`: build con el **SHA** del merge, **Test Result** y **BUILD SUCCESS**.
8. Con el build verde, pasar a la siguiente integración.

### 8.2 Pruebas que debe mostrar cada build

| Integración | Pruebas totales | Nuevas en esa HU |
| --- | --- | --- |
| HU18 | 175 | PU01, PU02, PU03, PU04, PM01 y la comprobación del DAO |
| HU20 | 186 | PU07, PU08, 4 casos de PP02, PM02, 2 comprobaciones del registro y 2 del servlet |
| HU19 | 194 | 5 casos de PP01, PU05, PU06 y la comprobación de franjas |

(Si alguien agrega otras pruebas al proyecto mientras tanto, los totales suben.)

## 9. Demostración y cierre de la entrega

### 9.1 Ejecutar la demostración con datos (contenedor H2)

Construir la imagen y arrancar un contenedor **nuevo** con los scripts de las HU ya integradas (un script que no exista impide cargar todos los datos):

```bash
docker build -t polidinner .
docker run -d --name polidinner-demo -p 8080:8080 \
  -e POLIDINNER_DATOS_INICIALES=datos_iniciales_h2.sql,demo_hu18_ventas_h2.sql,demo_hu20_perdidas_h2.sql,demo_hu19_caducidad_h2.sql \
  polidinner
```

- En la integración de HU18 se usan solo los dos primeros scripts; en la de HU20, los tres primeros.
- Ingresar como personal: cédula `1700000001`, clave `comedor123`. El panel muestra los accesos de las HU integradas.
- Las fechas de los datos se calculan el día del arranque: grabar ese mismo día. Para repetir el guion desde cero, borrar el contenedor y crear uno nuevo (`docker rm -f polidinner-demo`).
- Las referencias de los escenarios (101, 202, 301...) son los id fijados por los scripts.

**Recorrido de HU18:** consultar «Hoy» para comprobar suma de ventas por alta, separación por caducidad y exclusión de cancelados; consultar «Últimos 7 días» para comprobar fecha de pago y sus límites; consultar la fecha de hace treinta días, colocando esa misma fecha como inicio y fin del periodo. Por ejemplo, si graban el 8 de noviembre de 2026, consultar el 9 de octubre de 2026. Si graban otro día, recalcular la fecha; cargar los datos de demostración el mismo día de la grabación. Relacionar cada resultado con 18.1.1–18.3.2.

**Recorrido de HU19:** antes de descartar, consultar «Caducan hoy», «Próximos 3 días» y «Caducados» para comprobar fechas, cantidades, separación de altas y exclusión de agotados e históricos; después del descarte completo de HU20, repetir «Caducados» para comprobar la consulta vacía. Relacionar cada resultado con 19.1.1–19.3.2.

**Recorrido de aceptación de HU20 (conservar los ejemplos de la ficha):** intentar ocho de seis; registrar dos con la misma confirmación repetida; comprobar una pérdida y cuatro unidades; salir y volver al historial; iniciar una operación nueva para las cuatro restantes. Deben quedar dos pérdidas (2 y 4), stock cero y la franja «Caducados» vacía si no hay otras existencias. Descartar las seis de una vez no sustituye ese recorrido.

La prueba automatizada de idempotencia con seis unidades comprueba además el reenvío después de agotar el producto. Es una comprobación de soporte, no sustituye los escenarios web de descarte parcial.

### 9.2 Cierre (no cuenta como commits de desarrollo)

| Actividad | Responsable sugerido | Comprobación |
| --- | --- | --- |
| Publicar la imagen de la aplicación en Docker Hub con una etiqueta nueva (comando multiplataforma del `Dockerfile`) | Mauricio | `docker pull` en otro equipo y arranque del contenedor |
| Comprobar el arranque del contenedor | Mauricio | Login del personal, panel con los tres accesos y una consulta de cada HU |
| Actualizar la imagen de Jenkins de evaluación con los tres builds nuevos (mismo procedimiento de `IMAGEN JENKINS DOCKERHUB.md`) | Mauricio | Los tres builds visibles con su SHA y resultado |
| Subir el video a YouTube | El equipo decide | Enlace abre sin iniciar sesión |
| PDF con los **cuatro enlaces**: repositorio de GitHub, imagen de la aplicación, imagen de Jenkins y video de YouTube | El equipo decide | Los cuatro enlaces abren |

### 9.3 Qué mostrar en el video

**Límite de la consigna: máximo 6 minutos.** El guion propuesto dura 5 minutos y 30 segundos.

| Tiempo | Bloque | Contenido |
| --- | --- | --- |
| 0:00–0:40 | Prácticas XP | Evidencias reales de valores, principios y prácticas. |
| 0:40–1:50 | **TDD** | Mostrar un ciclo con cambio real: RED, GREEN y diff del REFACTOR. Resumir las otras 11 pruebas con su evidencia. No presentar R1–R4 como el REFACTOR de ese ciclo. |
| 1:50–2:30 | **Refactorizaciones** | R1–R4: técnica, antes, después y suite verde. |
| 2:30–3:20 | **Integraciones** | Commits por integrante, PR fusionado y build de Jenkins de cada HU. |
| 3:20–5:20 | **Demostración** | Escenarios de aceptación en el contenedor con los datos de la sección 9.1. |
| 5:20–5:30 | Cierre | Los cuatro enlaces. |

## 10. Lista de control

- [ ] **TDD:** 12 ciclos con capturas y commits RED, GREEN y REFACTOR; 12 cambios de diseño concretos, sin contar R1–R4.
- [ ] **Refactorizaciones:** R1, R2, R3 y R4 con antes, después, diff, verde y commit exclusivo.
- [ ] **Commits:** cada integrante con al menos 2 por HU.
- [ ] **Integraciones:** HU18, HU20 y HU19 con tramo de commits, PR y build (175, 186 y 194 pruebas).
- [ ] **Cierre:** imagen de la aplicación, imagen de Jenkins, video y PDF con los cuatro enlaces.
- [ ] **Ficha de HU:** comprobar que el desempate por referencia esté indicado y que HU20 conserve la caducidad en su historial; no duplicar información que la ficha ya contiene.
- [ ] **Aceptación HU18:** suma por alta, caducidades separadas, fecha del pago, límites del periodo, estados válidos y día sin ventas.
- [ ] **Aceptación HU19:** franjas, cantidades y fechas por alta, exclusión de agotados e históricos y consulta vacía después del descarte.
- [ ] **Aceptación HU20:** descarte parcial de dos, repetición, persistencia del historial y nueva operación de cuatro.
- [ ] **Datos:** revisar antes de entregar las fechas de pago y productos del periodo; no presentar datos incompletos como cero ventas.

## 11. Evidencia de valores, principios y prácticas

Este apartado es una guía de qué recoger mientras se trabaja, no una declaración de que ya sucedió.

| Aspecto | Evidencia concreta a conservar |
| --- | --- |
| Comunicación y respeto | Acuerdos de contratos y revisión de PR con comentarios reales del equipo. |
| Retroalimentación y entregas pequeñas | Resultados de aceptación de cada HU y ajustes acordados tras observarlos. |
| Simplicidad y diseño incremental | Refactorizaciones con motivo, diff y pruebas; reutilización de productos y referencias existentes, sin catálogo nuevo. |
| Pruebas y refactorización | Historial RED → GREEN → REFACTOR de las 12 pruebas y cuatro transformaciones adicionales. |
| Integración frecuente | PR de contribuciones cortas a la feature y verificación del resultado combinado; tres hitos Jenkins sobre develop. No dejar ramas aisladas hasta el último día. |
| Colaboración | Registrar trabajo conjunto solo cuando ocurra. Una revisión del asistente no acredita programación en parejas humana. |

## 12. Verificación independiente de esta guía

La revisión se ejecutó el **8 de noviembre de 2026**, sobre una copia aislada del commit `5277373`, con Temurin 24.0.2 y compilación Java 17. No se modificó el proyecto de IntelliJ ni el repositorio remoto.

- Base original: **169 ejecuciones satisfactorias**.
- Se aplicaron, en su secuencia, los bloques RED, GREEN y REFACTOR de los 12 métodos seleccionados. Cada RED falló por el comportamiento pendiente; no hubo errores de compilación o ejecución que se usaran como sustituto del fallo esperado.
- Tras confirmar el catálogo de clase, PU04 se volvió a comprobar desde GREEN hasta REFACTOR con **Introduce Explaining Variable**, sobre el incremento integrado. El RED y la prueba no cambiaron; la sustitución por una constante de la revisión anterior ya no se utiliza como evidencia.
- Después de cada REFACTOR se ejecutó la suite acumulada completa.
- R1, R2, R3 y R4 se comprobaron como transformaciones adicionales, con la suite verde. Se respetó la espera documentada del registro transaccional por el DAO de pérdidas.
- Para R1 se ejecutó también el cargador real anterior (bucle dentro de `cargarSiCorresponde`) y el posterior (método extraído), con cuatro scripts y dos bases H2 nuevas. Los mismos 17 resultados web fueron satisfactorios antes y después. Tras restaurar la versión final se volvió a ejecutar la suite y a generar el WAR; la comprobación no se limita a que la suite no haya fallado.
- Resultado final: **194 ejecuciones, 0 fallos, 0 errores, 0 omitidas**. Son 169 previas + 19 ejecuciones de los 12 métodos TDD (incluidas las filas parametrizadas) + 6 comprobaciones de soporte.
- Se generó `polidinner.war`.
- Se desplegó ese WAR en **Tomcat 9 con una base H2 aislada**. Las comprobaciones HTTP/JSP del apartado siguiente pasaron.

Estos resultados validan la reconstrucción de la guía y los casos ejecutados. No acreditan que los integrantes ya hayan desarrollado con TDD: cada uno debe realizar sus pasos y guardar sus propias evidencias.

### 12.1 Resultado de los ciclos

| Ciclo | RED observado | GREEN | REFACTOR verificado |
| --- | --- | --- | --- |
| PU01 | Fallo esperado por aserción (1 caso(s)); sin errores de ejecución | Pasa | Suite completa sin fallos ni errores |
| PU02 | Fallo esperado por aserción (1 caso(s)); sin errores de ejecución | Pasa | Suite completa sin fallos ni errores |
| PU03 | Fallo esperado por aserción (1 caso(s)); sin errores de ejecución | Pasa | Suite completa sin fallos ni errores |
| PU04 | Fallo esperado por aserción (1 caso(s)); sin errores de ejecución | Pasa | Suite completa sin fallos ni errores |
| PM01 | Fallo esperado por aserción (1 caso(s)); sin errores de ejecución | Pasa | Suite completa sin fallos ni errores |
| PU07 | Fallo esperado por aserción (1 caso(s)); sin errores de ejecución | Pasa | Suite completa sin fallos ni errores |
| PU08 | Fallo esperado por aserción (1 caso(s)); sin errores de ejecución | Pasa | Suite completa sin fallos ni errores |
| PP02 | Fallo esperado por aserción (4 caso(s)); sin errores de ejecución | Pasa | Suite completa sin fallos ni errores |
| PM02 | Fallo esperado por aserción (1 caso(s)); sin errores de ejecución | Pasa | Suite completa sin fallos ni errores |
| PP01 | Fallo esperado por aserción (5 caso(s)); sin errores de ejecución | Pasa | Suite completa sin fallos ni errores |
| PU05 | Fallo esperado por aserción (1 caso(s)); sin errores de ejecución | Pasa | Suite completa sin fallos ni errores |
| PU06 | Fallo esperado por aserción (1 caso(s)); sin errores de ejecución | Pasa | Suite completa sin fallos ni errores |

### 12.2 Comprobaciones de la web

- **Cumple:** Consulta exclusiva del personal autenticado.
- **Cumple:** Panel con los tres accesos.
- **Cumple:** HU18: suma por referencia y separa caducidades.
- **Cumple:** HU18: agotado vendido y cancelados excluidos.
- **Cumple:** HU18: fecha de pago y frontera de siete días.
- **Cumple:** HU18: periodo sin ventas.
- **Cumple:** HU19: caducan hoy.
- **Cumple:** HU19: límite inclusivo y exclusión de agotados e históricos.
- **Cumple:** HU19: altas distintas separadas y ordenadas.
- **Cumple:** HU19: caducado con seis unidades.
- **Cumple:** HU20: ofrece caducado y no vigente.
- **Cumple:** HU20: rechaza ocho sin descontar.
- **Cumple:** HU20: descarte parcial y caducidad en historial.
- **Cumple:** HU20: el mismo envío no duplica pérdida ni descuento.
- **Cumple:** HU20: historial conservado al salir y volver.
- **Cumple:** HU20: operación nueva de cuatro y dos pérdidas distintas.
- **Cumple:** HU19: consulta actualizada después del descarte.

La comprobación web fue mediante solicitudes HTTP y revisión de las páginas JSP generadas. No sustituye la revisión visual del equipo ni su video.

### 12.3 Límites y pendientes de entrega

- **No ejecutados en esta revisión:** Jenkins, construcción/publicación de imágenes Docker, SQL Server, acceso externo a los enlaces y grabación del video. Se mantienen como actividades del equipo, no como resultados aprobados.
- No se han probado exhaustivamente todos los intercalados concurrentes. Hay transacción, versión optimista y unicidad de confirmación; la verificación realizada cubre el reenvío secuencial y los casos descritos, no una certificación de concurrencia.
- Antes de implementar, comprobar la base de `develop`. Si contiene cambios posteriores, adaptar esta guía sin sobrescribirlos y repetir las verificaciones afectadas.
- Los comentarios que nombran reglas numéricas proceden del documento de partida. Para aceptación prevalecen los identificadores HU18/HU19/HU20 y sus criterios/escenarios de la ficha funcional; no atribuir números nuevos a reglas de negocio sin actualizar esa ficha.

### 12.4 Evaluación de cierre

| Dimensión | Estado | Evidencia o límite |
| --- | --- | --- |
| Propósito y alcance | Cumple | Tres HU conservadas; 8 pruebas normales, 2 con mocks, 2 parametrizadas, cuatro refactorizaciones adicionales y tres hitos de integración planificados. |
| Coherencia conceptual | Cumple | Los 12 ciclos incluyen cambios REFACTOR concretos; no se reserva ni se contabiliza dos veces una transformación. Es una adaptación de demostración del equipo, no una obligación universal de TDD. |
| Abstracción y dominio | Cumple | Se conserva la ficha funcional, las referencias de las existencias, la fecha de pago y las franjas de caducidad. Los detalles técnicos quedan en el plan de implementación. |
| Trazabilidad | Cumple | Identificadores de prueba, HU, responsables, código, capturas y commits enlazados; comprobaciones de soporte diferenciadas. |
| Verificación | Cumple parcialmente | Ciclos, suite de 194 ejecuciones, WAR y comprobaciones web satisfactorios. Jenkins, Docker, SQL Server y entrega externa quedan pendientes. |
| Simplicidad y evolución | Cumple | Cambios locales y reutilización de infraestructura; algunas mejoras son pequeñas por la decisión de mostrar cada fase, sin nuevas funcionalidades. |
| Contradicciones | Cumple | Los bloques posteriores conservan los cambios anteriores y coinciden con los archivos finales compilados. Se declara lo pendiente, sin convertirlo en cumplimiento. |

La guía está preparada para implementar y generar evidencias propias. El proyecto entregado solo podrá darse por completo después de las actividades pendientes de la sección 9.

## Ficha funcional

Anexo incluido en esta guía para consultar sus reglas, historias, criterios, escenarios y datos sin depender de otro archivo. Se conserva el contenido funcional de la ficha de partida.

### Objetivo comercial

**Facilitar las decisiones de compra del comedor mediante información de ventas y caducidad de sus productos.**

Al terminar la iteración, el personal podrá revisar qué productos se venden, cuáles están próximos a caducar y cuántas unidades se han descartado. Esta información le permitirá orientar las próximas compras. La reducción de pérdidas se comprobará después, con el uso del sistema.

### Épica: Información para las compras del comedor

**Como** personal del comedor, **quiero** conocer las ventas y pérdidas de los productos, **para** planificar las próximas compras.

| Historia | Qué permite hacer |
| --- | --- |
| HU18: Consultar ventas de productos | Revisar las unidades vendidas durante un periodo. |
| HU19: Consultar productos por franja de caducidad | Revisar qué productos caducaron, caducan hoy o caducarán después. |
| HU20: Registrar pérdidas por caducidad | Registrar las unidades descartadas y consultar las pérdidas guardadas. |

### Reglas de la iteración

- Las tres pantallas son exclusivas del personal del comedor que ha iniciado sesión. En los ejemplos, María Pérez representa a una persona del comedor.
- Si un producto se agregó varias veces, cada alta conserva su cantidad y caducidad. Por ejemplo, el agua que caduca mañana se muestra separada del agua que caduca dentro de cuatro días. No se juntan automáticamente por tener el mismo nombre.
- Las tablas, el historial de pérdidas y la selección del producto a descartar muestran también la referencia que ya tiene cada alta. Así se distinguen dos altas aunque coincidan el nombre y la caducidad. No se crean códigos comerciales nuevos ni se pide al personal que los escriba.
- Las ventas se suman para cada alta de producto. Esta entrega no calcula un único total para todas las altas de un mismo artículo comercial.
- Cuentan las unidades de pedidos Pagados o Entregados. Los pedidos Creados o Cancelados no cuentan. Se utiliza la fecha del pago.
- Los periodos incluyen el día inicial y el final completos, según la hora de Ecuador. «Últimos 7 días» comprende hoy y los seis días anteriores. Si un pedido se cancela después, su venta deja de contar al repetir la consulta.
- La pantalla de ventas muestra referencia, nombre, caducidad y unidades vendidas, de mayor a menor cantidad. También incluye productos agotados que tuvieron ventas. Para empates se ordena por referencia, de menor a mayor.
- La consulta de caducidad muestra referencia, nombre, unidades disponibles y fecha de caducidad, ordenados por la fecha más próxima. No muestra productos agotados ni productos que solo se conservan como históricos.
- Un producto sigue vigente durante su fecha de caducidad. Desde el día siguiente está caducado y no se puede vender. Se conserva esta política del sistema revisado, pendiente de validación con el comedor.
- Solo se pueden registrar pérdidas por caducidad de productos caducados que todavía tengan unidades disponibles. Al guardar la pérdida se descuenta la cantidad indicada.
- El historial conserva referencia del producto descartado, nombre, fecha de caducidad, unidades descartadas, fecha del registro y persona responsable. Volver a consultar o pulsar dos veces sobre el mismo descarte no crea otra pérdida.
- Se permiten descartes parciales. Un descarte posterior de las unidades restantes es una operación nueva. Las cantidades guardadas en el historial no cambian por ventas o cancelaciones posteriores.
- Las pérdidas se expresan en unidades, no en dinero. El historial empieza con esta funcionalidad; no se inventan pérdidas anteriores a su uso.
- Antes de entregar, el equipo completará la revisión de los productos y las fechas de pago del periodo disponible. No se entregarán esas consultas con datos pendientes de identificar ni se tratará la falta de información como cero ventas.
- Las consultas aportan información para decidir. No generan automáticamente compras a proveedores.

#### Franjas de caducidad

| Franja | Productos que muestra |
| --- | --- |
| Caducados | Los que caducaron antes de hoy. |
| Caducan hoy | Los que tienen fecha de caducidad de hoy. |
| Próximos 3 días | Los que caducan desde mañana hasta dentro de tres días, ambos incluidos. |
| Más adelante | Los que caducan después de los próximos tres días. |

Los tres días son un filtro de consulta. No limitan la fecha que se puede registrar al agregar un producto.

### HU18: Consultar ventas de productos

| **Título:** Consultar ventas de productos | **Prioridad:** Alta, propuesta | **Estimación:** Pendiente del equipo |
| --- | --- | --- |
| **Historia de usuario:** **Como** personal del comedor, **quiero** consultar las unidades vendidas por producto durante un periodo, **para** orientar las próximas compras. |  |  |
| **Criterio de aceptación 18.1: Mostrar las unidades vendidas de cada producto** |  |  |
| **Escenario 18.1.1: Sumar las ventas de varios pedidos.**\<br>**Dado** que María está en «Ventas de productos» y hoy se pagaron dos pedidos con seis y cuatro unidades del Agua 500 ml que caduca mañana,\<br>**Cuando** selecciona «Hoy» y pulsa «Consultar»,\<br>**Entonces** la tabla muestra Agua 500 ml, la fecha de mañana y diez unidades vendidas. |  |  |
| **Escenario 18.1.2: Separar productos con distinta caducidad.**\<br>**Dado** que María está en «Ventas de productos» y hoy se vendieron diez aguas de 500 ml que caducan mañana y cuatro que caducan dentro de cuatro días,\<br>**Cuando** selecciona «Hoy» y pulsa «Consultar»,\<br>**Entonces** la tabla muestra dos filas de Agua 500 ml, cada una con su referencia, caducidad y las unidades vendidas correspondientes. |  |  |
| **Criterio de aceptación 18.2: Consultar las ventas según la fecha del pago** |  |  |
| **Escenario 18.2.1: Incluir un pedido pagado dentro del periodo.**\<br>**Dado** que María está en «Ventas de productos» y un pedido Entregado de dos yogures de 150 g se creó hace diez días y se pagó hace dos días, sin otras ventas de ese yogur,\<br>**Cuando** selecciona «Últimos 7 días» y pulsa «Consultar»,\<br>**Entonces** la tabla muestra Yogur 150 g con dos unidades vendidas. |  |  |
| **Escenario 18.2.2: Dejar fuera los pagos anteriores al periodo.**\<br>**Dado** que María está en «Ventas de productos» y las Galletas 200 g tienen una unidad de un pedido Pagado hace seis días y cuatro de otro Pagado hace siete días,\<br>**Cuando** selecciona «Últimos 7 días» y pulsa «Consultar»,\<br>**Entonces** la tabla muestra una unidad vendida de Galletas 200 g, sin sumar las cuatro pagadas antes del periodo. |  |  |
| **Criterio de aceptación 18.3: Mostrar las ventas válidas o informar que no hay resultados** |  |  |
| **Escenario 18.3.1: No sumar los pedidos cancelados.**\<br>**Dado** que María está en «Ventas de productos» y las Galletas 100 g están agotadas, pero hoy tuvieron dos unidades en un pedido Pagado, tres en otro Entregado y cuatro en otro Cancelado,\<br>**Cuando** selecciona «Hoy» y pulsa «Consultar»,\<br>**Entonces** la tabla muestra cinco unidades vendidas de Galletas 100 g, aunque estén agotadas. |  |  |
| **Escenario 18.3.2: Consultar un día sin ventas.**\<br>**Dado** que María está en «Ventas de productos» y no hubo ventas de productos hace treinta días,\<br>**Cuando** elige esa fecha como inicio y fin del periodo y pulsa «Consultar»,\<br>**Entonces** el sistema muestra «No hay ventas de productos en el periodo seleccionado», sin productos en la tabla. |  |  |

#### Checklist INVEST

| Atributo | Justificación |
| --- | --- |
| Independiente | Utiliza los productos y pedidos existentes. Se puede usar sin abrir las otras dos pantallas. |
| Negociable | Se puede ajustar la presentación, conservando las cantidades y fechas acordadas. |
| Valiosa | Permite revisar cuánto se vende para orientar las compras. |
| Estimable | Los filtros y resultados están definidos; faltan las horas propuestas por el equipo. |
| Pequeña | Se limita a consultar ventas. El equipo debe comprobar que cabe en la semana. |
| Testeable | Los seis escenarios se comprueban en la tabla o en el mensaje de consulta vacía. |

### HU19: Consultar productos por franja de caducidad

| **Título:** Consultar productos por franja de caducidad | **Prioridad:** Alta, propuesta | **Estimación:** Pendiente del equipo |
| --- | --- | --- |
| **Historia de usuario:** **Como** personal del comedor, **quiero** consultar los productos por franja de caducidad, **para** prevenir pérdidas por caducidad. |  |  |
| **Criterio de aceptación 19.1: Mostrar los productos de la franja seleccionada** |  |  |
| **Escenario 19.1.1: Consultar lo que caduca hoy.**\<br>**Dado** que María está en «Caducidad de productos», hay dos porciones de Pastel que caducan hoy y tres aguas de 500 ml que caducan mañana,\<br>**Cuando** selecciona «Caducan hoy» y pulsa «Consultar»,\<br>**Entonces** la tabla muestra las dos porciones de Pastel y no muestra el Agua 500 ml. |  |  |
| **Escenario 19.1.2: Consultar lo que caduca en los próximos tres días.**\<br>**Dado** que María está en «Caducidad de productos», hay dos Empanadas que caducan dentro de tres días y siete aguas de 500 ml que caducan dentro de cuatro días,\<br>**Cuando** selecciona «Próximos 3 días» y pulsa «Consultar»,\<br>**Entonces** la tabla muestra las dos Empanadas y no muestra esas siete aguas. |  |  |
| **Criterio de aceptación 19.2: Mostrar la cantidad y caducidad de cada producto** |  |  |
| **Escenario 19.2.1: Consultar productos caducados.**\<br>**Dado** que María está en «Caducidad de productos» y hay seis yogures de 150 g que caducaron ayer,\<br>**Cuando** selecciona «Caducados» y pulsa «Consultar»,\<br>**Entonces** la tabla muestra Yogur 150 g, seis unidades disponibles y la fecha de ayer. |  |  |
| **Escenario 19.2.2: Separar las cantidades que caducan en fechas distintas.**\<br>**Dado** que María está en «Caducidad de productos» y hay tres aguas de 500 ml que caducan mañana y cuatro que caducan pasado mañana,\<br>**Cuando** selecciona «Próximos 3 días» y pulsa «Consultar»,\<br>**Entonces** la tabla muestra dos filas de Agua 500 ml con sus referencias: primero las tres unidades de mañana y después las cuatro de pasado mañana. |  |  |
| **Criterio de aceptación 19.3: Mostrar solo productos disponibles dentro de la franja** |  |  |
| **Escenario 19.3.1: No mostrar productos agotados ni históricos.**\<br>**Dado** que María está en «Caducidad de productos», las Galletas 100 g están agotadas, un Jugo 250 ml está marcado como histórico y hay tres aguas de 500 ml disponibles; los tres productos tienen caducidad de mañana,\<br>**Cuando** selecciona «Próximos 3 días» y pulsa «Consultar»,\<br>**Entonces** la tabla incluye el Agua 500 ml y excluye las Galletas 100 g y el Jugo 250 ml. |  |  |
| **Escenario 19.3.2: Consultar una franja sin productos.**\<br>**Dado** que María está en «Caducidad de productos», ya se registraron como pérdida los seis yogures que caducaron ayer y no quedan otros productos caducados con unidades disponibles,\<br>**Cuando** selecciona «Caducados» y pulsa «Consultar»,\<br>**Entonces** el sistema muestra «No hay productos con stock en la franja seleccionada». |  |  |

#### Checklist INVEST

| Atributo | Justificación |
| --- | --- |
| Independiente | Usa las cantidades y caducidades existentes. La prueba de actualización aprovecha un descarte hecho mediante HU20. |
| Negociable | Las franjas se pueden acordar con el comedor, dejando claras sus fechas. |
| Valiosa | Permite identificar los productos que requieren atención por su caducidad. |
| Estimable | Franjas y resultados definidos; horas pendientes del equipo. |
| Pequeña | Es una consulta. No cambia cantidades ni crea alertas automáticas. |
| Testeable | Los seis escenarios se comprueban con productos preparados para distintas fechas. |

### HU20: Registrar pérdidas por caducidad

| **Título:** Registrar pérdidas por caducidad | **Prioridad:** Alta, propuesta | **Estimación:** Pendiente del equipo |
| --- | --- | --- |
| **Historia de usuario:** **Como** personal del comedor, **quiero** registrar las unidades descartadas por caducidad, **para** ajustar las próximas compras. |  |  |
| **Criterio de aceptación 20.1: Permitir el descarte de productos caducados** |  |  |
| **Escenario 20.1.1: Seleccionar un producto caducado.**\<br>**Dado** que María está en «Pérdidas por caducidad» y hay seis yogures de 150 g que caducaron ayer,\<br>**Cuando** abre la lista de productos para registrar una pérdida,\<br>**Entonces** puede seleccionar ese Yogur 150 g y ve su referencia, seis unidades disponibles y caducidad. |  |  |
| **Escenario 20.1.2: No permitir el descarte por caducidad de un producto vigente.**\<br>**Dado** que María está en «Pérdidas por caducidad» y hay tres aguas de 500 ml que caducan mañana,\<br>**Cuando** abre la lista de productos,\<br>**Entonces** esas aguas no aparecen entre los productos que puede seleccionar para una pérdida por caducidad. |  |  |
| **Criterio de aceptación 20.2: Descontar únicamente las unidades disponibles** |  |  |
| **Escenario 20.2.1: Registrar el descarte de las unidades restantes.**\<br>**Dado** que María está en «Pérdidas por caducidad», ya registró como pérdida dos yogures que caducaron ayer y ahora ha descartado los cuatro restantes,\<br>**Cuando** inicia un registro nuevo, indica cuatro unidades y pulsa «Registrar pérdida»,\<br>**Entonces** ese Yogur 150 g queda sin unidades disponibles y el historial conserva dos pérdidas distintas: una de dos unidades y otra de cuatro. |  |  |
| **Escenario 20.2.2: Intentar descartar más unidades de las disponibles.**\<br>**Dado** que María está en «Pérdidas por caducidad» y seleccionó el Yogur 150 g que caducó ayer, cuando todavía tiene seis unidades disponibles,\<br>**Cuando** indica ocho unidades y pulsa «Registrar pérdida»,\<br>**Entonces** el sistema muestra «La cantidad supera las 6 unidades disponibles», conserva las seis unidades y no agrega una pérdida al historial. |  |  |
| **Criterio de aceptación 20.3: Conservar las pérdidas sin duplicarlas** |  |  |
| **Escenario 20.3.1: Volver a consultar una pérdida guardada.**\<br>**Dado** que María está en «Pérdidas por caducidad», ya registró como pérdida dos yogures que caducaron ayer y quedan cuatro unidades,\<br>**Cuando** sale a otra página y vuelve a «Pérdidas por caducidad»,\<br>**Entonces** el historial sigue mostrando una sola pérdida de dos yogures, con su referencia, caducidad, fecha del registro y persona responsable; quedan cuatro unidades. |  |  |
| **Escenario 20.3.2: Pulsar dos veces para registrar el mismo descarte.**\<br>**Dado** que María está en «Pérdidas por caducidad», tiene seis yogures que caducaron ayer y completó el formulario para registrar el descarte de dos,\<br>**Cuando** pulsa «Registrar pérdida» dos veces seguidas para ese mismo descarte,\<br>**Entonces** el historial muestra una sola pérdida de dos yogures y quedan cuatro unidades disponibles. |  |  |

#### Checklist INVEST

| Atributo | Justificación |
| --- | --- |
| Independiente | Utiliza los productos existentes. Puede funcionar sin abrir las consultas de ventas o caducidad. |
| Negociable | Se puede ajustar el formulario, conservando cantidad, fecha y producto descartado. |
| Valiosa | Permite conocer las unidades perdidas para complementar las decisiones de compra. |
| Estimable | El equipo debe estimar formulario, historial y protección contra descuentos repetidos. |
| Pequeña | Se limita a pérdidas por caducidad en unidades; no incorpora costos ni otras causas. |
| Testeable | Los seis escenarios se observan en la lista de productos, los mensajes y el historial. |

### Productos preparados para el video

Las cantidades siguientes son las que quedan disponibles después de los pedidos preparados. Los productos caducados y los pedidos de días anteriores se preparan en los datos de prueba antes de grabar. No hace falta esperar días ni cambiar la fecha del equipo.

| Producto | Unidades disponibles | Caducidad | Ventas preparadas |
| --- | --- | --- | --- |
| Agua 500 ml | 3 | Mañana | Dos pedidos Pagados hoy: seis y cuatro unidades. |
| Agua 500 ml | 7 | Dentro de cuatro días | Cuatro unidades Pagadas hoy. |
| Agua 500 ml | 4 | Pasado mañana | Sin ventas. |
| Galletas 100 g | 0 | Mañana | Dos unidades en un pedido Pagado, tres en otro Entregado y cuatro en otro Cancelado; pagos de hoy. |
| Galletas 200 g | 2 | Dentro de cuatro días | Una unidad Pagada hace seis días y cuatro Pagadas hace siete días. |
| Yogur 150 g | 4 | Pasado mañana | Dos unidades Entregadas, pagadas hace dos días; pedido creado hace diez días. |
| Yogur 150 g | 6 | Ayer | Sin ventas ni pérdidas registradas. |
| Pastel porción | 2 | Hoy | Sin ventas. |
| Empanada unidad | 2 | Dentro de tres días | Sin ventas. |
| Jugo 250 ml | 5 | Mañana | Producto histórico; sin ventas. |

Todos salvo el Jugo se pueden utilizar en las funciones actuales según su cantidad y caducidad. No hay ventas hace treinta días. Los seis yogures que caducaron ayer son los únicos productos caducados con unidades disponibles.

Para probar las pérdidas sin cambiar los datos entre casos:

1. Intentar descartar ocho yogures y comprobar el rechazo.
2. Registrar dos yogures con doble pulsación: debe quedar una sola pérdida de dos y cuatro unidades disponibles.
3. Salir y volver al historial: deben mantenerse esos valores.
4. Registrar las cuatro unidades restantes como un descarte nuevo.
5. Volver a «Caducados»: la consulta debe quedar vacía.

### Recorrido propuesto para dos minutos

Abrir las pestañas y preparar los filtros antes de grabar. Las consultas y los registros se ejecutan durante el video. Una misma tabla puede demostrar varios escenarios si se señala el resultado de cada uno.

| Tiempo previsto | Qué mostrar | Escenarios |
| --- | --- | --- |
| 0–25 s | Ventas de hoy, de los últimos siete días y del día sin ventas. | 18.1.1–18.3.2. |
| 25–55 s | Caducan hoy, próximos tres días y caducados; señalar cantidades, fechas y productos excluidos. | 19.1.1–19.3.1. |
| 55–105 s | Productos que se pueden descartar; rechazo de ocho yogures; registro de dos con doble pulsación; vuelta al historial y descarte nuevo de los cuatro restantes. | En orden: 20.1.1, 20.1.2, 20.2.2, 20.3.2, 20.3.1 y 20.2.1. |
| 105–115 s | Consulta de caducados vacía después de los descartes. | 19.3.2. |
| 115–120 s | Explicar cómo las ventas, caducidades y seis unidades perdidas ayudan a decidir las próximas compras. | Objetivo comercial. |

La duración debe comprobarse con un ensayo. No se da por verificada antes de implementar la aplicación.
