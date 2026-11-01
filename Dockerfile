FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/Temperature_Converter-1.0-SNAPSHOT.jar app.jar

USER 10001:10001

ENTRYPOINT ["java", "-cp", "/app/app.jar", "org.example.LocalizationApp"]