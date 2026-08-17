FROM amazoncorretto:21-alpine

RUN apk add --no-cache wget

WORKDIR /app

# Скачиваем агент при сборке образа
RUN wget -O /opt/edot/edot-javaagent.jar \
    https://github.com/elastic/elastic-distribution-opentelemetry/releases/latest/download/edot-javaagent.jar || \
    echo "Warning: Failed to download EDOT agent"

COPY build/libs/*.jar app.jar

ENTRYPOINT [
  "java",
  "-javaagent:/opt/edot/edot-javaagent.jar",
  "-jar",
  "/app/app.jar"
]