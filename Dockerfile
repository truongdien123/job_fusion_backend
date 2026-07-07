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

# Tạo user và group non-root để tăng cường bảo mật
RUN addgroup --system javauser && adduser --system --ingroup javauser javauser

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Cấp quyền sở hữu file jar cho user mới
RUN chown javauser:javauser app.jar

# Sử dụng user non-root
USER javauser

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]