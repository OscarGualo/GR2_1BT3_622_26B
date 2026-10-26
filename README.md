# PoliDinner

Aplicación web del PoliComedor de la EPN: compra de comida con PoliCuenta y reserva de mesas.

Tecnologías: Java 17, Servlets/JSP, Hibernate 5.6 y Tomcat 9.

## Despliegue con Docker

Se requiere Docker Desktop instalado y ejecutándose en Windows/macOS, o Docker Engine en Linux, utilizando contenedores Linux.

No hace falta instalar Java, Maven, Tomcat ni una base de datos por separado.

La descarga inicial requiere Internet. Una vez descargada la imagen, la aplicación puede ejecutarse sin conexión.

### Opción 1: utilizar la imagen publicada

Descargar la imagen:

```bash
docker pull m4urici0/polidinner:1.0
```

Iniciar la aplicación:

```bash
docker run -d -p 127.0.0.1:8081:8080 -v polidinner-datos:/data -e POLIDINNER_DEMO=true --name polidinner m4urici0/polidinner:1.0
```

Consultar el arranque:

```bash
docker logs -f polidinner
```

Esperar a que aparezca `Server startup in [...] milliseconds`. Presionar **Ctrl+C** para salir de los registros; la aplicación continuará ejecutándose.

Abrir **http://localhost:8081**.

El volumen `polidinner-datos` conserva la base de datos aunque se elimine el contenedor.

### Opción 2: construir desde el código

Esta alternativa requiere Git y conexión a Internet para descargar el proyecto, las imágenes base y las dependencias.

```bash
git clone https://github.com/OscarGualo/GR2_1BT3_622_26B.git
cd GR2_1BT3_622_26B
docker build -t polidinner .
docker run -d -p 127.0.0.1:8081:8080 -v polidinner-datos:/data -e POLIDINNER_DEMO=true --name polidinner polidinner
```

Abrir **http://localhost:8081**.

Las dos opciones son alternativas: no deben ejecutarse simultáneamente con el mismo nombre y puerto.

### Qué hace el contenedor al iniciar

- Arranca Tomcat 9 con la aplicación en la raíz `/`.
- Crea la base H2 embebida en `/data` y sus tablas mediante Hibernate.
- Carga los datos de demostración mediante `datos_iniciales_h2.sql`.
- Utiliza la zona horaria `America/Guayaquil`.

No es necesario ejecutar scripts manualmente para esta imagen.

### Usuarios de prueba

| Perfil | Cédula | Contraseña | Saldo inicial |
|---|---|---|---|
| María Pérez — Personal del comedor | `1700000001` | `comedor123` | No aplica |
| Andrea Gómez — Cliente | `1750000001` | `cliente123` | $10.00 |
| Luis Torres — Cliente | `1750000002` | `cliente123` | $8.00 |
| Carla Ruiz — Cliente | `1750000003` | `cliente123` | $5.00 |

Una base nueva incluye tres mesas y un menú publicado con Arroz con pollo y Jugo de naranja.

Los saldos y las existencias cambian al utilizar la aplicación.

### Comprobar el incremento de reservas

- **HU-01:** ingresar como cliente y abrir **Reservar mesa**.
- **HU-02:** abrir **Mi reserva** y cancelar una reserva activa.
- **HU-03:** ingresar como personal del comedor y abrir **Ocupación de mesas**.

Para utilizar varios usuarios simultáneamente, emplear navegadores o perfiles separados.

### Modo demostración

Las pantallas **Reservar mesa** y **Mi reserva** permiten fijar la hora del comedor:

| Hora | Comprobación |
|---|---|
| 11:00 | Reservar franjas futuras. |
| 12:00 | Cancelar anticipadamente una reserva de las 12:30. |
| 12:15 | Cancelar en el límite exacto de 15 minutos. |
| 12:20 | Cancelar fuera de plazo una reserva de las 12:30. |
| 13:40 | Comprobar que no quedan franjas futuras. |

Para establecer una hora manualmente, escribirla y pulsar **Fijar hora**.

La hora simulada afecta a todos los usuarios de la aplicación. **Usar hora real** elimina la hora fijada desde el panel.

### Comandos útiles

Ver los contenedores:

```bash
docker ps -a
```

Detener la aplicación:

```bash
docker stop polidinner
```

Volver a iniciarla conservando los datos:

```bash
docker start polidinner
```

Consultar los registros:

```bash
docker logs -f polidinner
```

Si el contenedor ya existe, utilizar `docker start polidinner` en lugar de repetir `docker run`.

Si el puerto 8081 está ocupado, utilizar `-p 127.0.0.1:8082:8080` al crear el contenedor y abrir **http://localhost:8082**.

### Restablecer los datos de demostración

**Estos comandos eliminan los datos guardados en el volumen de esta instalación.**

```bash
docker rm -f polidinner
docker volume rm polidinner-datos
```

Después, repetir el comando de inicio de la opción elegida. Se creará una base nueva con los datos iniciales.

## Desarrollo local con SQL Server

Al ejecutar la aplicación fuera de Docker, si no se define `POLIDINNER_DB_URL`, se utiliza la conexión a SQL Server configurada en `persistence.xml`.

Las credenciales pueden proporcionarse mediante:

- `POLIDINNER_DB_USER`
- `POLIDINNER_DB_PASSWORD`

Para SQL Server se utilizan `datos_iniciales.sql` y las migraciones correspondientes.

La imagen Docker configura `POLIDINNER_DB_URL` para utilizar H2 y carga `datos_iniciales_h2.sql`. Para probar esa imagen no es necesario instalar SQL Server.

## Enlaces

- Proyecto: https://github.com/OscarGualo/GR2_1BT3_622_26B
- Imagen de la aplicación: https://hub.docker.com/r/m4urici0/polidinner
- Imagen de Jenkins: https://hub.docker.com/r/m4urici0/polidinner-jenkins
