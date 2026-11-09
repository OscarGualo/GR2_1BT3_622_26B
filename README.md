# PoliDinner

Aplicación web del comedor de la Escuela Politécnica Nacional. Permite a los estudiantes comprar con PoliCuenta, programar pedidos y reservar mesas. El personal del comedor puede gestionar el menú, autorizar entregas y consultar las ventas, la caducidad y las pérdidas de productos.

**Tecnologías** Java 17, Servlets/JSP, Hibernate 5.6 y Tomcat 9.

## Despliegue con Docker — Proyecto [1B]

Inicie Docker Desktop o Docker Engine antes de ejecutar los comandos. Las imágenes de la entrega están publicadas para contenedores Linux de 64 bits (`linux/amd64`). En Windows, seleccione el modo de contenedores Linux. [Instalación de Docker Desktop](https://docs.docker.com/desktop/) · [Instalación de Docker Engine](https://docs.docker.com/engine/install/).

### Aplicación web

Ejecute cada comando por separado en PowerShell o en una terminal.

```bash
docker pull m4urici0/polidinner:proyecto-1b
docker run -d --name polidinner-proyecto -p 127.0.0.1:18097:8080 m4urici0/polidinner:proyecto-1b
```

Abra [PoliDinner en su equipo](http://localhost:18097). En el primer arranque se prepara automáticamente la base H2 y se cargan los datos de demostración.

Si la página todavía no responde, ejecute `docker logs -f polidinner-proyecto` y espere a que aparezca `Server startup`. Pulse **Ctrl+C** para salir de los registros. La aplicación seguirá ejecutándose.

### Cuentas de demostración

En la portada, elija **Soy estudiante** o **Personal del comedor** según la cuenta que vaya a utilizar.

| Perfil | Nombre | Cédula | Contraseña | Saldo inicial de PoliCuenta |
| --- | --- | --- | --- | --- |
| Personal del comedor | María Pérez | `1700000001` | `comedor123` | No aplica |
| Estudiante | Andrea Gómez | `1750000001` | `cliente123` | $10,00 |
| Estudiante | Luis Torres | `1750000002` | `cliente123` | $8,00 |
| Estudiante | Carla Ruiz | `1750000003` | `cliente123` | $5,00 |

Para utilizar ambos perfiles al mismo tiempo, abra uno en una ventana normal y el otro en una ventana privada.

### Qué puede probar

- **Estudiante.** Consulte **Menú de hoy**, agregue un plato al carrito y pague con PoliCuenta. El comprobante muestra el código de retiro. También puede programar un pedido y consultar **Pedidos programados**, cancelar un pedido pendiente desde **Mis pedidos** o reservar una mesa. Para probar franjas fuera del horario real, abra **Modo demostración** desde **Reservar mesa**, fije las **11:00** y vuelva a **Usar hora real** al terminar.
- **Personal del comedor.** Administre el menú y los productos, consulte los pedidos programados y la ocupación de mesas, y autorice el despacho introduciendo un código de retiro en **Habilitar entrega**. Utilice **Cerrar venta del día** después de probar los pagos.

### Historias de la iteración

Para reproducir las cifras de los escenarios, use los datos iniciales antes de registrar compras o descartes adicionales.

1. **HU18 — Ventas de productos.** Consulte **Hoy** y **Últimos 7 días**. Para comprobar una consulta vacía, seleccione la fecha de hace 30 días como inicio y fin.
2. **HU19 — Caducidad de productos.** Consulte **Caducan hoy**, **Próximos 3 días** y **Caducados**. Al inicio, la franja **Caducados** muestra seis yogures.
3. **HU20 — Pérdidas por caducidad.** Intente descartar ocho yogures y compruebe el rechazo. Registre dos y verifique que quedan cuatro. Salga y vuelva para consultar el historial. Registre las cuatro unidades restantes como una operación nueva y compruebe que la franja **Caducados** queda vacía.

### Volver a abrir o reiniciar la demostración

Para volver a abrir el contenedor conservando sus datos, ejecute `docker start polidinner-proyecto`.

Para repetir los escenarios con datos iniciales, ejecute los comandos siguientes. Al eliminar el contenedor se borran los datos generados durante esa sesión de prueba.

```bash
docker stop polidinner-proyecto
docker rm polidinner-proyecto
docker pull m4urici0/polidinner:proyecto-1b
docker run -d --name polidinner-proyecto -p 127.0.0.1:18097:8080 m4urici0/polidinner:proyecto-1b
```

## Jenkins y evidencias de integración continua

La imagen ocupa aproximadamente **341 MB de descarga y 755 MB instalada**, más el espacio del volumen de Jenkins. Conserva los siete builds originales de `PoliDinner-CI`.

```bash
docker pull m4urici0/polidinner-jenkins:proyecto-1b
docker run -d --name jenkins-polidinner-proyecto -p 127.0.0.1:18129:8080 -e POLIDINNER_JENKINS_URL=http://localhost:18129/ -v jenkins-polidinner-proyecto-datos:/var/jenkins_home m4urici0/polidinner-jenkins:proyecto-1b
```

Abra [Jenkins en su equipo](http://localhost:18129) e ingrese con el usuario **`docente`** y la contraseña **`PoliDinner2026!`**. Si Jenkins todavía se está iniciando, ejecute `docker logs -f jenkins-polidinner-proyecto` y espere el mensaje `Jenkins is fully up and running`.

Seleccione **PoliDinner-CI** y abra el historial. Los builds **#1 a #4** corresponden a la prueba anterior. Las integraciones del proyecto son las siguientes.

| Build | Historia integrada | Commit | Pruebas aprobadas |
| --- | --- | --- | ---: |
| **#5** | HU18 — Consultar ventas de productos | `0042d88d` | 175 |
| **#6** | HU20 — Registrar pérdidas por caducidad | `6fe27ea3` | 186 |
| **#7** | HU19 — Consultar productos por franja de caducidad | `f1b7b2d3` | 194 |

Cada build permite consultar **Console Output**, **Test Result** y **Changes**, y descargar `polidinner.war` desde **Build Artifacts**.

Para volver a abrir Jenkins después de detenerlo, ejecute `docker start jenkins-polidinner-proyecto`.

## Desarrollo local con SQL Server

Al ejecutar la aplicación fuera de Docker, si no se define `POLIDINNER_DB_URL`, se utiliza la conexión a SQL Server configurada en `persistence.xml`.

Las credenciales de SQL Server se pueden configurar mediante las variables de entorno `POLIDINNER_DB_USER` y `POLIDINNER_DB_PASSWORD`. Para preparar los datos de SQL Server se utilizan `datos_iniciales.sql` y las migraciones correspondientes.

La imagen publicada `m4urici0/polidinner:proyecto-1b` utiliza H2 y carga automáticamente los datos iniciales y los datos de demostración de HU18, HU19 y HU20.

## Enlaces de la entrega

- [Repositorio de PoliDinner en GitHub](https://github.com/OscarGualo/GR2_1BT3_622_26B)
- [Imagen web en Docker Hub — `proyecto-1b`](https://hub.docker.com/r/m4urici0/polidinner/tags?name=proyecto-1b)
- [Imagen de Jenkins en Docker Hub — `proyecto-1b`](https://hub.docker.com/r/m4urici0/polidinner-jenkins/tags?name=proyecto-1b)
- [Video del Proyecto [1B]](https://youtu.be/w0ASWL59OPU)
