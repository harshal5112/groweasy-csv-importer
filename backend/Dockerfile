# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/csv-importer.jar app.jar

# Render (and most PaaS) inject $PORT - Spring Boot reads it via application.properties
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Xmx256m -jar app.jar"]
