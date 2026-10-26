# PoliDinner - imagen de despliegue (Tomcat 9 + Java 17 + base H2 embebida)
# Construir:  docker build -t polidinner .
# Multiplataforma (PC Intel/AMD y Mac M):
#   docker buildx build --platform linux/amd64,linux/arm64 -t USUARIO/polidinner:1.0 --push .
# Ejecutar:   docker run -d -p 8080:8080 --name polidinner polidinner   ->  http://localhost:8080

# ---------- Etapa 1: compilar el .war con Maven (no queda en la imagen final) ----------
# El .war es igual para cualquier procesador: se compila una sola vez en la arquitectura de la máquina
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Las pruebas unitarias ya se ejecutan en el pipeline de Jenkins
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: solo Tomcat 9 con Java 17 (JRE) y el .war ----------
FROM tomcat:9.0-jre17-temurin

# Base H2 embebida dentro del contenedor: Hibernate crea las tablas y la app carga los datos iniciales sola
ENV POLIDINNER_DB_URL="jdbc:h2:file:/data/polidinner" \
    POLIDINNER_DB_USER="polidinner" \
    POLIDINNER_DB_PASSWORD="polidinner" \
    POLIDINNER_DATOS_INICIALES="datos_iniciales_h2.sql" \
    CATALINA_OPTS="-Duser.timezone=America/Guayaquil -Xms128m -Xmx384m"

RUN mkdir -p /data && rm -rf /usr/local/tomcat/webapps/*

# ROOT.war: la aplicación queda en http://localhost:8080/
COPY --from=build /app/target/polidinner.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
