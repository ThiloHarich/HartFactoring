# Dockerfile
FROM eclipse-temurin:24-jdk-noble AS build
WORKDIR /app

# Kopiere Gradle Wrapper und Build-Dateien
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Kopiere Source Code
COPY src src

# Baue die Anwendung
RUN ./gradlew build -x test

# Runtime Stage
FROM eclipse-temurin:24-jre-noble
WORKDIR /app

# Kopiere das gebaute Jar aus der Build-Stage
COPY --from=build /app/build/libs/HartFactoring-1.0-SNAPSHOT.jar app.jar

# Port für Spring Boot (Standard)
EXPOSE 8080

# Starte die Anwendung
ENTRYPOINT ["java", "-jar", "app.jar"]
