FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/api-gateway-1.0-SNAPSHOT.jar app.jar
COPY src/main/resources/webroot ./webroot
EXPOSE 8080 9080
ENTRYPOINT ["java", "-jar", "app.jar"]