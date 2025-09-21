# Multi-stage build를 위한 Dockerfile
FROM docker.io/amazoncorretto:21-alpine-jdk AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시를 위한 레이어 분리
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x gradlew

# 의존성 다운로드 (소스코드 변경 없이 캐시 활용)
RUN ./gradlew dependencies --no-daemon

# 소스코드 복사
COPY src/ src/

# JAR 파일 빌드
RUN ./gradlew bootJar --no-daemon

# 런타임 이미지
FROM docker.io/amazoncorretto:21-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 타임존 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 헬스체크 의존 도구 설치
RUN apk add --no-cache tzdata wget

# 보안 사용자 생성
RUN addgroup -S spring && adduser -S spring -G spring

# JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 소유권 변경
RUN chown spring:spring app.jar

# 보안 사용자 전환
USER spring

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 포트 노출
EXPOSE 8080

# 실행 명령
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
