# 1. 빌드 환경 (Builder Stage)
FROM gradle:8.5-jdk17-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼 및 빌드 설정 파일 복사
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# 소스 코드 복사
COPY src ./src

# Gradle 실행 권한 부여 및 빌드 수행 (테스트 제외)
RUN chmod +x ./gradlew && ./gradlew build -x test

# 2. 프로덕션 환경 (Production Stage)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 JAR 파일 복사 (plain JAR 제외)
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

# 포트 노출 (Spring Boot 기본 포트)
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
