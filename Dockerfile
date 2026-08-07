# ==========================================
# Build Stage
# ==========================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy dependency definition to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source files and build the executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Run Stage
# ==========================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/echovault-*.jar app.jar

EXPOSE 8080

# Tune JVM heap memory relative to container RAM limits
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
