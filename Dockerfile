FROM eclipse-temurin:21-jdk

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
