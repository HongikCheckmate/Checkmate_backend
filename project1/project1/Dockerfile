FROM bellsoft/liberica-openjdk-alpine:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

LABEL authors="Hongik-Checkmate"

ENTRYPOINT ["java", "-jar", "/app.jar"]