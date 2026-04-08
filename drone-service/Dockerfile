FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/drone-service-1.0-SNAPSHOT.jar app.jar
EXPOSE 8082 9082
ENTRYPOINT ["java", "-jar", "app.jar"]