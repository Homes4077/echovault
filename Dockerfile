# --- Stage 1: Build Stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Step 1: Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Step 2: Copy source code and package the app
COPY src ./src
RUN mvn package -DskipTests

# --- Stage 2: Runtime Stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Step 3: Copy the compiled JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Step 4: Expose application port (adjust if using a port other than 8080)
EXPOSE 8080

# Step 5: Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
