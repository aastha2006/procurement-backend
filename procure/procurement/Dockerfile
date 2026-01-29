# Stage 1: Build the application
FROM maven:3.9.9-amazoncorretto-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

# Copy the built jar from the build stage
# The jar name matches the artifactId-version in pom.xml (demo-0.0.1-SNAPSHOT.jar)
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose the port
EXPOSE 8080

# Environment variables for configuration (to be overridden by Cloud Run)
ENV SPRING_PROFILES_ACTIVE=prod

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
