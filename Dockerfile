# 1. Build stage: builder (Gradle로 빌드함)
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Gradle 래퍼와 의존성 관련 파일 복사
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src ./src
RUN ./gradlew clean build -x test --no-daemon

# 2. Run stage: JDK 21 경량 이미지
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 비루트 사용자 생성 및 설정
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
