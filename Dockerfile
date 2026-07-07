# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy gradle configuration files
COPY gradle gradle
COPY gradlew .
COPY build.gradle settings.gradle ./

# Ensure gradlew has executable permissions
RUN chmod +x gradlew

# Download dependencies to cache them
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build bootJar without running tests
RUN ./gradlew bootJar -x test --no-daemon

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
