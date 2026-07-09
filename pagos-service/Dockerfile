FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8086
 ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]