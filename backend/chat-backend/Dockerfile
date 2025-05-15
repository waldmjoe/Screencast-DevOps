FROM gradle:8.8-jdk21 AS builder
WORKDIR /app
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

RUN ./gradlew build --no-daemon -x test || ./gradlew dependencies --no-daemon || exit 0
COPY src ./src
RUN ./gradlew build --no-daemon -x test

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENV OLLAMA_API_URL="http://host.docker.internal:11434"
ENV OLLAMA_MODEL="phi3"

ENTRYPOINT ["java", "-jar", "app.jar", "--ollama.api.url=${OLLAMA_API_URL}", "--ollama.model=${OLLAMA_MODEL}"]