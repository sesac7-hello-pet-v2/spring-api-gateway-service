# Hello Pet v2 - Spring API Gateway Service

Spring Cloud Gateway MVC 기반의 마이크로서비스 API 게이트웨이입니다.

## 📌 개요

Hello Pet v2의 API Gateway는 모든 마이크로서비스에 대한 단일 진입점을 제공합니다. Spring Cloud Gateway MVC를 사용하여 라우팅, 인증, CORS 처리 등의 횡단 관심사를 중앙에서 관리합니다.

## 🚀 기술 스택

### Core Framework
- **Java**: 17
- **Spring Boot**: 3.5.6
- **Spring Cloud**: 2025.0.0
- **Spring Cloud Gateway**: MVC (WebMVC 기반)
- **Build Tool**: Gradle 8.5

### Dependencies
- **인증**: JWT (jjwt 0.12.6)
- **모니터링**: Spring Boot Actuator
- **메트릭**: Micrometer + Prometheus
- **트레이싱**: OpenTelemetry
- **유틸리티**: Lombok

## 🏗️ 아키텍처

```
                    ┌──────────────┐
                    │   Client     │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │  API Gateway │
                    │   (Port 8080) │
                    └──────┬───────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼─────┐     ┌─────▼──────┐    ┌─────▼──────┐
   │Auth      │     │User        │    │Pet         │
   │Service   │     │Service     │    │Service     │
   │(8081)    │     │(8082)      │    │(8085)      │
   └──────────┘     └────────────┘    └────────────┘
```

## 📁 프로젝트 구조

```
spring-api-gateway-service/
├── src/
│   ├── main/
│   │   ├── java/hello/pet/springapigatewayservice/
│   │   │   ├── SpringApiGatewayServiceApplication.java  # 메인 클래스
│   │   │   ├── CorsConfig.java                         # CORS 설정
│   │   │   ├── JwtAuthenticationFilter.java            # JWT 인증 필터
│   │   │   ├── JwtProvider.java                        # JWT 유틸리티
│   │   │   └── PublicPathMatcher.java                  # 공개 경로 매처
│   │   └── resources/
│   │       └── application.yaml                        # 애플리케이션 설정
│   └── test/
├── gradle/                                              # Gradle 래퍼
├── build.gradle                                         # 빌드 설정
├── Dockerfile                                           # Docker 이미지 정의
└── settings.gradle                                      # 프로젝트 설정
```

## 🔧 주요 기능

### 1. 라우팅
- **동적 라우팅**: 마이크로서비스로의 요청 전달
- **경로 재작성**: StripPrefix 필터를 통한 경로 변환
- **로드 밸런싱**: 서비스 인스턴스 간 부하 분산

### 2. 인증 및 인가
- **JWT 검증**: 모든 요청에 대한 토큰 검증
- **공개 경로 관리**: 인증이 필요없는 엔드포인트 정의
- **사용자 정보 전달**: X-User-Id, X-User-Role 헤더 추가

### 3. CORS 처리
- **허용 도메인**: localhost:3000, hello-pet.my
- **허용 메서드**: GET, POST, PUT, DELETE, PATCH, OPTIONS
- **자격 증명**: 쿠키 및 인증 헤더 지원

### 4. 모니터링
- **헬스 체크**: /actuator/health
- **메트릭 수집**: Prometheus 형식
- **분산 트레이싱**: OpenTelemetry

## 📋 라우팅 규칙

| 서비스 | 경로 패턴 | 대상 서비스 | 포트 |
|--------|----------|------------|------|
| 인증 서비스 | `/api/v1/auth/**` | auth-service | 8081 |
| 사용자 서비스 | `/api/v1/users/**` | user-service | 8082 |
| 신청 서비스 | `/api/v1/applications/**` | application-service | 8089 |
| 공고 서비스 | `/api/v1/announcements/**` | announcement-service | 8084 |
| 반려동물 서비스 | `/api/v1/pets/**` | pet-service | 8085 |

모든 경로는 `/api` 프리픽스를 제거하고 백엔드 서비스로 전달됩니다.

## 🚦 시작하기

### 사전 요구사항
- JDK 17 이상
- Gradle 8.5 이상

### 로컬 개발 환경

1. **프로젝트 클론**
```bash
git clone <repository-url>
cd spring-api-gateway-service
```

2. **빌드**
```bash
./gradlew clean build
```

3. **실행**
```bash
./gradlew bootRun
```

4. **또는 JAR 실행**
```bash
java -jar build/libs/spring-api-gateway-service-0.0.1-SNAPSHOT.jar
```

### 환경 변수

```yaml
# JWT 설정
JWT_SECRET: your-secret-key (최소 256비트)

# 서비스 URL (Kubernetes 환경)
AUTH_SERVICE_URL: http://auth-service:8081
USER_SERVICE_URL: http://user-service:8082
PET_SERVICE_URL: http://pet-service:8085
```

## 🐳 Docker

### 이미지 빌드
```bash
docker build -t hello-pet-gateway .
```

### 컨테이너 실행
```bash
docker run -p 8080:8080 \
  -e JWT_SECRET=your-secret-key \
  hello-pet-gateway
```

### Multi-stage Build
- **Stage 1**: Gradle 빌드 환경
  - gradle:8.5-jdk17-alpine
  - 소스 코드 컴파일 및 JAR 생성
- **Stage 2**: 실행 환경
  - eclipse-temurin:17-jre-alpine
  - 최소화된 런타임 이미지

## 🔒 보안

### JWT 인증
- **토큰 검증**: 모든 보호된 엔드포인트에 대한 JWT 검증
- **토큰 추출**: Authorization 헤더 또는 쿠키에서 토큰 추출
- **클레임 전달**: userId, role 정보를 헤더로 전달

### 공개 경로
인증이 필요없는 엔드포인트:
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/health`
- `GET /actuator/**`

## 📊 모니터링

### Actuator 엔드포인트
```bash
# 헬스 체크
GET http://localhost:8080/actuator/health

# Prometheus 메트릭
GET http://localhost:8080/actuator/prometheus

# 애플리케이션 정보
GET http://localhost:8080/actuator/info
```

### 메트릭 수집
- JVM 메모리 사용량
- HTTP 요청 메트릭
- 게이트웨이 라우팅 메트릭
- 커스텀 비즈니스 메트릭

## 🔧 설정 커스터마이징

### 라우트 추가
```yaml
spring:
  cloud:
    gateway:
      mvc:
        routes:
          - id: new-service
            uri: http://new-service:port
            predicates:
              - Path=/api/v1/new/**
            filters:
              - StripPrefix=1
```

### CORS 설정 변경
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://your-domain.com")
                .allowedMethods("*")
                .allowCredentials(true);
    }
}
```

## 🐛 문제 해결

### 서비스 연결 실패
```bash
# 서비스 도메인 확인
nslookup auth-service

# 네트워크 연결 테스트
curl http://auth-service:8081/actuator/health
```

### JWT 검증 실패
```bash
# 토큰 디코딩 테스트
echo $JWT_TOKEN | base64 -d

# 시크릿 키 확인
echo $JWT_SECRET | wc -c  # 최소 32자 이상
```

### CORS 오류
- 브라우저 개발자 도구에서 Origin 헤더 확인
- 허용된 도메인 목록 확인
- Preflight 요청 (OPTIONS) 처리 확인

## 📈 성능 최적화

- **연결 풀링**: HTTP 클라이언트 연결 재사용
- **타임아웃 설정**: 적절한 읽기/쓰기 타임아웃
- **서킷 브레이커**: 장애 전파 방지 (추가 예정)
- **캐싱**: 자주 사용되는 데이터 캐싱 (추가 예정)

## 🔄 업그레이드 가이드

### Spring Boot 업그레이드
```gradle
plugins {
    id 'org.springframework.boot' version '3.x.x'
}
```

### Spring Cloud 업그레이드
```gradle
ext {
    set('springCloudVersion', "202x.x.x")
}
```

## 📝 API 문서

게이트웨이를 통한 서비스 API 접근:
- Auth API: `http://gateway:8080/api/v1/auth`
- User API: `http://gateway:8080/api/v1/users`
- Pet API: `http://gateway:8080/api/v1/pets`
