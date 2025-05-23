# 1. Build stage: builder (Gradle로 빌드함)
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
# TODO: 추후 CI 중 테스트 확인
RUN ./gradlew clean build -x test

# 2. Run stage: JDK 21 경량 이미지
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
