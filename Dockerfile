# Dockerfile para LabToDo - Spring Boot Application with JSF/PrimeFaces

# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Crear usuario no-root para seguridad
RUN addgroup -g 1001 -S labtodo && adduser -u 1001 -S labtodo -G labtodo

# Instalar curl para health check
RUN apk add --no-cache curl

WORKDIR /app

# Copiar JAR desde la etapa de build
COPY --from=build /app/target/labtodo.jar app.jar

# Cambiar propietario del directorio a usuario labtodo
RUN chown -R labtodo:labtodo /app

# Cambiar a usuario no-root
USER labtodo

# Exponer el puerto de la aplicación
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]