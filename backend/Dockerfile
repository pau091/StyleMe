# IMAGEN DEL BACKEND DE STYLEME
# -----------------------------
# Dos etapas:
#   1. "compilar": usa Maven + Java 21 para crear el .jar (sin correr las pruebas: necesitan la base y Gemini).
#   2. "ejecutar": solo Java 21 (JRE) y el .jar. Así la imagen final es mucho más liviana.
# La construye docker compose (servicio "backend"); no hace falta Java instalado en el computador.

FROM maven:3.9.6-eclipse-temurin-21 AS compilar
WORKDIR /app
# Primero solo el pom.xml: Docker guarda en caché las dependencias y no las vuelve a bajar si el código cambia
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
# Usuario sin privilegios (buena práctica de seguridad: el backend no corre como administrador)
RUN useradd --system --create-home styleme
COPY --from=compilar /app/target/*.jar app.jar
USER styleme
EXPOSE 8090
# Docker pregunta cada 15 s si el backend está sano (/actuator/health también revisa la base de datos)
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -fs http://localhost:8090/actuator/health/readiness || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
