FROM eclipse-temurin:25-jre
LABEL authors="juanjo"

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "application.jar"]