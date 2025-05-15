# Stage 1: Build the application
# Use a Gradle image that includes JDK 21.
# You can check Docker Hub for specific gradle:tag-jdk21 tags.
# gradle:8.8-jdk21 or gradle:jdk21 (for latest Gradle supporting JDK21) are good choices.
FROM gradle:8.8-jdk21 AS builder
WORKDIR /app

# Copy only necessary files for dependency resolution first to leverage Docker cache
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Download dependencies.
# RUN ./gradlew dependencies --no-daemon
# A full build -x test is often more reliable here if `dependencies` task doesn't fetch all it needs
# or if you want to ensure it can build before copying source.
# Let's try a build to ensure everything is fetched and compiled.
# The || exit 0 part is a fallback if there are no dependencies to lock, not strictly needed for a build.
RUN ./gradlew build --no-daemon -x test || ./gradlew dependencies --no-daemon || exit 0


# Copy the source code
COPY src ./src

# Build the application, skip tests for faster image build if tests are run elsewhere
# Ensure the JAR is built
RUN ./gradlew build --no-daemon -x test

# Stage 2: Create the runtime image
# Use an official OpenJDK JRE image for Java 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built JAR from the builder stage
# The JAR file is typically in build/libs/ and might have a version in its name.
# Using a wildcard helps, but ensure only one JAR is there or be more specific.
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port the application runs on (as defined in application.properties)
EXPOSE 8080

# Command to run the application
# OLLAMA_API_URL and OLLAMA_MODEL will be passed as environment variables via docker-compose
# or docker run. We provide defaults here just in case, but they should be overridden.
ENV OLLAMA_API_URL="http://host.docker.internal:11434"
ENV OLLAMA_MODEL="phi3"

ENTRYPOINT ["java", "-jar", "app.jar", "--ollama.api.url=${OLLAMA_API_URL}", "--ollama.model=${OLLAMA_MODEL}"]