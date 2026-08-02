FROM amazoncorretto:21-alpine
RUN apk add --no-cache libstdc++
WORKDIR /app
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]