FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/request-service-1.0-SNAPSHOT.jar app.jar
EXPOSE 8081 9081
ENTRYPOINT ["java", "-jar", "app.jar"]