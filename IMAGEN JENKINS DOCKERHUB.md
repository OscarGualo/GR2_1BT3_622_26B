# Jenkins de PoliDinner — Integración continua

Imagen Docker de evaluación que incluye el proyecto **PoliDinner-CI** y sus cuatro construcciones originales, con registros, commits, resultados de pruebas y archivos WAR.

## 1. Enlaces

- **Imagen de Jenkins:** https://hub.docker.com/r/m4urici0/polidinner-jenkins
- **Repositorio del proyecto:** https://github.com/OscarGualo/GR2_1BT3_622_26B
- **Imagen de la aplicación web:** https://hub.docker.com/r/m4urici0/polidinner

**La etiqueta de Jenkins que contiene el historial es `2.0-evaluacion`. La etiqueta anterior `1.0` no incluye estas evidencias.**

## 2. Requisitos

- Docker Desktop instalado y ejecutándose, utilizando contenedores Linux; en Linux puede utilizarse Docker Engine.
- Equipo Intel/AMD de 64 bits. La imagen fue construida y comprobada para `linux/amd64`.
- Puerto **8082** disponible.
- Conexión a Internet para descargar inicialmente la imagen.

No es necesario instalar Java, Maven o Jenkins por separado.

Para comprobar que Docker está funcionando:

```bash
docker version
```

La salida debe mostrar información tanto del cliente como del servidor de Docker.

## 3. Descargar la imagen

Abrir PowerShell o una terminal y ejecutar:

```bash
docker pull m4urici0/polidinner-jenkins:2.0-evaluacion
```

La descarga comprimida ocupa aproximadamente **362 MB**. El espacio utilizado localmente y por los datos de Jenkins será mayor.

## 4. Crear e iniciar el contenedor

Ejecutar:

```bash
docker run -d --name jenkins-evaluacion -p 127.0.0.1:8082:8080 -v jenkins-evaluacion-datos-v2:/var/jenkins_home m4urici0/polidinner-jenkins:2.0-evaluacion
```

El comando:

- Inicia Jenkins en segundo plano.
- Permite acceder desde la computadora mediante el puerto **8082**.
- Crea automáticamente el volumen `jenkins-evaluacion-datos-v2`.
- Restaura el proyecto y los cuatro builds incluidos en la imagen.

**Se debe utilizar un volumen nuevo como el indicado. No reutilizar el volumen de otra instalación de Jenkins.**

Consultar el arranque:

```bash
docker logs -f jenkins-evaluacion
```

Esperar hasta que aparezca:

```text
Jenkins is fully up and running
```

Presionar **Ctrl+C** para salir de los registros. Jenkins continuará ejecutándose.

## 5. Ingresar a Jenkins

Abrir en el navegador:

**http://localhost:8082**

Ingresar con la cuenta de evaluación:

| Campo | Valor |
|---|---|
| Usuario | `docente` |
| Contraseña | `PoliDinner2026!` |

La cuenta permite consultar el proyecto, descargar sus artefactos y ejecutar nuevas construcciones. No permite administrar Jenkins ni modificar o borrar la configuración del proyecto.

## 6. Abrir el proyecto y revisar las integraciones

Seleccionar **PoliDinner-CI** en el panel principal.

Acceso directo:

**http://localhost:8082/job/PoliDinner-CI/**

El historial incluye:

| Build | Alcance | Commit construido | Pruebas | Resultado |
|---|---|---|---:|---|
| #1 | Integración HU-01 | `52abdea` | 25 | SUCCESS |
| #2 | Integración HU-02 | `d04527b` | 25 | SUCCESS |
| #3 | Integración HU-03 | `522c10e` | 27 | SUCCESS |
| #4 | Verificación posterior del incremento | `0fad3f5` | 33 | SUCCESS |

Los builds conservan sus fechas, commits, resultados y artefactos originales.

### Revisar un build

Seleccionar su número en el historial y consultar:

1. **Salida de consola / Console Output:** muestra la revisión descargada, la ejecución de Maven, las pruebas y el resultado final.
2. **Resultados de pruebas / Test Result:** permite consultar las clases de prueba y sus resultados.
3. **Cambios / Changes:** muestra los cambios registrados para la construcción.
4. **Artefactos / Build Artifacts:** permite descargar el archivo `target/polidinner.war`.

En una construcción satisfactoria, la consola muestra:

```text
BUILD SUCCESS
Finished: SUCCESS
```

### Accesos directos

- **Integración HU-01:** http://localhost:8082/job/PoliDinner-CI/1/
- **Integración HU-02:** http://localhost:8082/job/PoliDinner-CI/2/
- **Integración HU-03:** http://localhost:8082/job/PoliDinner-CI/3/
- **Verificación posterior:** http://localhost:8082/job/PoliDinner-CI/4/

## 7. Ejecutar una construcción nueva

Este paso es opcional y requiere Internet.

1. Abrir **PoliDinner-CI**.
2. Pulsar **Construir ahora**.
3. Esperar a que aparezca la nueva ejecución en el historial.
4. Abrir su **Salida de consola**.
5. Comprobar el resultado de la compilación, las pruebas y el empaquetado.

El proyecto descarga el código de GitHub y ejecuta:

```bash
sh mvnw -B clean package
```

La nueva construcción tendrá el número **#5**, o el siguiente disponible. Utilizará el código vigente de la rama configurada, por lo que sus resultados pueden diferir de los históricos.

Los builds **#1 a #4** son las evidencias originales incluidas en la imagen.

## 8. Detener y volver a iniciar

Detener Jenkins:

```bash
docker stop jenkins-evaluacion
```

Volver a iniciarlo conservando los datos:

```bash
docker start jenkins-evaluacion
```

Comprobar su estado:

```bash
docker ps -a
```

Consultar los registros:

```bash
docker logs -f jenkins-evaluacion
```

**Si el contenedor ya existe, utilizar `docker start` en lugar de repetir `docker run`.**

## 9. Si el puerto 8082 está ocupado

Al crear un contenedor nuevo, utilizar otro puerto, por ejemplo **8083**:

```bash
docker run -d --name jenkins-evaluacion -p 127.0.0.1:8083:8080 -e POLIDINNER_JENKINS_URL=http://localhost:8083/ -v jenkins-evaluacion-datos-v2:/var/jenkins_home m4urici0/polidinner-jenkins:2.0-evaluacion
```

Abrir:

**http://localhost:8083**

Esta es una alternativa al comando del apartado 4. No deben ejecutarse ambos comandos con el mismo nombre de contenedor.

## 10. Consultar el historial sin Internet

Después de descargar la imagen, los cuatro builds históricos, sus registros, resultados y WAR pueden consultarse sin Internet.

Para volver a iniciar un contenedor existente:

```bash
docker start jenkins-evaluacion
```

**Ejecutar construcciones nuevas sí requiere Internet**, porque Jenkins consulta GitHub y descarga las dependencias necesarias.

Sin conexión pueden aparecer avisos relacionados con la comprobación de actualizaciones de Jenkins. Estos avisos no impiden consultar el historial almacenado.

## 11. Restaurar la copia inicial de evaluación

Este procedimiento elimina las construcciones nuevas y los cambios realizados en esta instalación de evaluación.

```bash
docker rm -f jenkins-evaluacion
docker volume rm jenkins-evaluacion-datos-v2
```

Después, repetir el comando del apartado 4.

Se restaurarán los cuatro builds originales incluidos en la imagen.

## 12. Solución de problemas

| Situación | Acción |
|---|---|
| Docker no responde | Abrir Docker Desktop y esperar a que el motor esté disponible. |
| El nombre del contenedor ya existe | Ejecutar `docker start jenkins-evaluacion`. |
| La página no abre | Revisar `docker ps -a` y `docker logs jenkins-evaluacion`. |
| El puerto está ocupado | Utilizar la alternativa del apartado 9. |
| No aparece `PoliDinner-CI` | Comprobar que se utiliza la etiqueta `2.0-evaluacion` y un volumen nuevo. |
| No aparecen los cuatro builds | Comprobar que no se está reutilizando el volumen de otra instalación. |
| Una construcción nueva falla al descargar archivos | Comprobar la conexión a GitHub y Maven Central; revisar la salida de consola. |
| No aparece la opción de administrar Jenkins | Es el comportamiento esperado de la cuenta de evaluación. |

## 13. Alcance de la entrega

La imagen incluye únicamente el proyecto **PoliDinner-CI**, su historial y los plugins necesarios para consultarlo y ejecutarlo.

No se copiaron los usuarios ni los archivos de secretos del Jenkins personal.

Los cuatro WAR restaurados fueron verificados mediante SHA-256 contra sus archivos originales.

Este contenedor permite revisar la integración continua. La aplicación web PoliDinner se ejecuta por separado mediante su propia imagen Docker.
