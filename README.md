# PoliDinner

Aplicación web del PoliComedor de la EPN: compra de comida con PoliCuenta y reserva de mesas.
Java 17, Servlets/JSP, Hibernate 5.6, Tomcat 9.

## Despliegue con Docker

Requisito único: Docker Desktop (Windows / macOS) o Docker Engine (Linux).
No hace falta instalar Java, Maven, Tomcat ni una base de datos.

### Opción 1: imagen publicada en Docker Hub (recomendada)

```bash
docker run -d -p 8080:8080 --name polidinner TU_USUARIO/polidinner:1.0
```

Abrir http://localhost:8080

### Opción 2: construir desde el código

```bash
git clone https://github.com/OscarGualo/GR2_1BT3_622_26B.git
cd GR2_1BT3_622_26B
docker build -t polidinner .
docker run -d -p 8080:8080 --name polidinner polidinner
```

### Qué hace el contenedor al iniciar

- Arranca Tomcat 9 con la aplicación en la raíz (`/`).
- Crea la base H2 embebida en `/data` y sus tablas (Hibernate).
- Carga los datos de prueba automáticamente (`datos_iniciales_h2.sql`); no se ejecuta ningún script a mano.
- Usa la hora de Ecuador (America/Guayaquil).

### Usuarios de prueba

| Rol | Cédula | Clave |
|---|---|---|
| Personal del comedor | 1700000001 | comedor123 |
| Cliente (Andrea Gómez, saldo $10.00) | 1750000001 | cliente123 |
| Cliente (Luis Torres, saldo $8.00) | 1750000002 | cliente123 |
| Cliente (Carla Ruiz, saldo $5.00) | 1750000003 | cliente123 |

Hay 3 mesas y un menú del día publicado (Arroz con pollo y Jugo de naranja).

### Comandos útiles

```bash
docker logs -f polidinner        # ver el arranque (Ctrl + C para salir)
docker stop polidinner           # detener (los datos se conservan)
docker start polidinner          # volver a iniciar
docker rm -f polidinner          # eliminar el contenedor (borra los datos)
```

Para conservar los datos aunque se elimine el contenedor:

```bash
docker run -d -p 8080:8080 -v polidinner-datos:/data --name polidinner TU_USUARIO/polidinner:1.0
```

Si el puerto 8080 está ocupado, usar otro a la izquierda: `-p 8081:8080` y abrir http://localhost:8081

## Desarrollo local (IntelliJ + SQL Server)

Sin la variable `POLIDINNER_DB_URL`, la aplicación usa SQL Server según `persistence.xml`
(usuario y clave en `POLIDINNER_DB_USER` y `POLIDINNER_DB_PASSWORD`).
Los scripts de `src/main/resources/*.sql` son para esa base.

## Enlaces

- Imagen de la aplicación: https://hub.docker.com/r/TU_USUARIO/polidinner
- Imagen de Jenkins: https://hub.docker.com/r/TU_USUARIO/polidinner-jenkins
