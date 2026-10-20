# syntax=docker/dockerfile:1

# ---------- Etapa 1: build ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# Primero solo el pom: las dependencias quedan cacheadas mientras no cambie
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

# Separa el jar en capas (dependencias / loader / app) para aprovechar la caché de Docker
RUN java -Djarmode=tools -jar target/daas-proyecto-final.jar extract --layers --launcher --destination extracted

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
