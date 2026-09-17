# =============================================================================
# Stage 1: Build Stage
# =============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and POM for dependency layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Normalize line endings (CRLF -> LF) and grant execution permissions
RUN tr -d '\r' < mvnw > mvnw_unix && mv mvnw_unix mvnw && chmod +x mvnw

# Download dependencies in batch mode for efficient Docker layer caching
RUN ./mvnw dependency:go-offline -B

# Copy project source code and build the executable JAR
COPY src/ src/
RUN ./mvnw clean package -DskipTests

# =============================================================================
# Stage 2: Production Runtime Stage
# =============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as an unprivileged non-root user for container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the built artifact from the builder stage
COPY --from=builder --chown=appuser:appgroup /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Expose Spring Boot server port
EXPOSE 8080

# Production JVM entrypoint with container-aware memory limits and G1GC
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
