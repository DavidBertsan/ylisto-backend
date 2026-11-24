# Stage 1: build the jar with the full JDK and the Maven wrapper.
# Tests are not run here: CI runs them, and Testcontainers needs Docker, which a build stage doesn't have.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Dependencies first, so this layer is cached until pom.xml changes
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -q dependency:go-offline

COPY src src
RUN ./mvnw -q package -DskipTests

# Stage 2: run it on a JRE only, as a non-root user. The JDK and Maven never reach this image.
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 1001 ylisto
COPY --from=build /app/target/*.jar app.jar
USER ylisto

# Size the heap from the container's memory limit, not the host's
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
