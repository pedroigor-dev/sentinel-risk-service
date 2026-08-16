FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src/ src/
COPY config/ config/
RUN ./mvnw --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21-jre-jammy

RUN groupadd --system sentinel \
    && useradd --system --gid sentinel --home-dir /app sentinel

WORKDIR /app
COPY --from=build --chown=sentinel:sentinel \
    /workspace/target/sentinel-risk-service-1.0.0-SNAPSHOT.jar app.jar

USER sentinel
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
