FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/delivery-service-1.0-SNAPSHOT.jar app.jar
EXPOSE 8083 9083
ENTRYPOINT ["java", "-jar", "app.jar"]