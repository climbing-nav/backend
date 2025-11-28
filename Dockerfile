# === Build Stage ===
FROM gradle:8-jdk17-alpine AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle/

RUN chmod +x gradlew

COPY src src/

RUN ./gradlew build -x test --no-daemon

# === Runtime Stage ===
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
