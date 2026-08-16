FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml ./
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src/ src/
COPY config/ config/
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21-jre-jammy

RUN groupadd --system sentinel \
    && useradd --system --gid sentinel --home-dir /app sentinel

WORKDIR /app
COPY --from=build --chown=sentinel:sentinel \
    /workspace/target/sentinel-risk-service-1.0.0-SNAPSHOT.jar app.jar

USER sentinel
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
