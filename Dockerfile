FROM maven:3.9.11-eclipse-temurin-25 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN addgroup --system kedada && adduser --system --ingroup kedada kedada

COPY --from=build /workspace/target/kedada-api-0.0.1-SNAPSHOT.jar /app/kedada-api.jar

USER kedada

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/kedada-api.jar"]
