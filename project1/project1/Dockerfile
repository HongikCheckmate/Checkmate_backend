FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

LABEL authors="Hongik-Checkmate"

ENTRYPOINT ["java", "-jar", "/app.jar"]