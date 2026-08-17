FROM amazoncorretto:21-alpine

RUN apk add --no-cache libstdc++

WORKDIR /app

COPY build/libs/*.jar app.jar
COPY edot-javaagent.jar /opt/edot/edot-javaagent.jar

ENTRYPOINT [
  "java",
  "-javaagent:/opt/edot/edot-javaagent.jar",
  "-jar",
  "/app/app.jar"
]