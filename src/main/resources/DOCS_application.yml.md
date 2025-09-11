## `application.yml` 파일 설명

`application.yml` 파일은 Spring Boot 애플리케이션의 모든 설정을 계층적인 구조로 관리하는 파일입니다. 이 파일을 통해 데이터베이스 연결, 서버 포트, 로깅 레벨 등 애플리케이션의 동작 방식을
손쉽게 제어할 수 있습니다.

---

### 전체 설정 내용

```yaml
# Spring Boot 애플리케이션 관련 기본 설정
spring:
  application:
    name: pullit # 애플리케이션의 고유한 이름

  # 데이터베이스 연결 설정
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    # DB_URL 환경 변수가 있으면 그 값을 사용하고, 없으면 로컬 MariaDB 주소를 기본값으로 사용합니다.
    url: ${DB_URL:jdbc:mariadb://localhost:3306/pullit_local?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8}
    # DB_USERNAME 환경 변수가 있으면 그 값을 사용하고, 없으면 'root'를 기본값으로 사용합니다.
    username: ${DB_USERNAME:root}
    # DB_PASSWORD 환경 변수가 있으면 그 값을 사용하고, 없으면 'root'를 기본값으로 사용합니다.
    password: ${DB_PASSWORD:root}

  # JPA (Java Persistence API) 및 Hibernate 관련 설정
  jpa:
    hibernate:
      # ddl-auto: 애플리케이션 실행 시 JPA 엔티티와 데이터베이스 스키마를 어떻게 처리할지 결정합니다.
      # - update: 엔티티의 변경사항을 감지하여 데이터베이스 스키마에 자동으로 반영합니다. (로컬 개발 시 유용)
      # - validate: 엔티티와 스키마가 일치하는지 검사만 합니다. (운영 환경에서 안전)
      # - create: 실행 시마다 스키마를 새로 생성합니다. (기존 데이터 삭제됨)
      # - none: 아무 작업도 하지 않습니다.
      ddl-auto: update

  # Liquibase 설정 (데이터베이스 마이그레이션 도구)
  liquibase:
    # enabled: Liquibase를 활성화할지 여부를 결정합니다.
    # - false: Liquibase를 비활성화하고 Flyway만 사용합니다. (현재 설정)
    # - true: Liquibase를 활성화하여 changelog 파일을 기반으로 DB 마이그레이션 수행
    enabled: false

  # Spring Boot DevTools 설정 (개발 시 자동 재시작)
  devtools:
    restart:
      # enabled: 코드 변경 시 자동 재시작 기능을 활성화합니다.
      enabled: true
      # additional-paths: 재시작을 트리거할 추가 경로들을 지정합니다.
      additional-paths:
        - "src/main/java"
      # additional-exclude: 재시작에서 제외할 경로들을 지정합니다.
      additional-exclude: "static/**,public/**"
      # poll-interval: 파일 변경을 감지하는 간격을 설정합니다. (기본값: 1초)
      # 값이 작을수록 더 빠르게 변경을 감지하지만 CPU 사용량이 증가합니다.
      poll-interval: 1000ms
      # quiet-period: 재시작을 트리거하기 전에 대기하는 시간을 설정합니다.
      # 파일 변경이 빈번한 경우 불필요한 재시작을 방지합니다.
      quiet-period: 400ms
      # trigger-file: 특정 파일이 변경될 때만 재시작을 트리거합니다.
      # 이 파일을 터치하면 강제로 재시작됩니다.
      trigger-file: ".reloadtrigger"
    livereload:
      # enabled: 브라우저 자동 새로고침 기능을 활성화합니다.
      enabled: true
      # port: 라이브 리로드 서버가 사용할 포트를 지정합니다.
      port: 35729

# 서버 관련 설정
server:
  error:
    # API 오류 발생 시, Spring Boot의 기본 HTML 오류 페이지(Whitelabel Page)를 비활성화합니다.
    # 이를 통해 일관된 JSON 형태의 오류 응답을 반환할 수 있습니다.
    whitelabel:
      enabled: false

    # 개발 환경에서 디버깅을 쉽게 하기 위해, 오류 응답에 예외(Exception) 클래스 이름을 포함시킵니다.
    include-exception: true

    # 오류 응답에 전체 호출 스택(Stacktrace)을 포함시켜 문제의 원인을 빠르게 파악할 수 있도록 합니다.
    # ON_TRACE_PARAM: `trace=true` 파라미터가 있을 때만 포함 (운영에서 고려)
    # ALWAYS: 항상 포함 (개발 시 유용)
    # NEVER: 절대 포함하지 않음

    include-stacktrace: ALWAYS

# Actuator의 /info 엔드포인트에 노출될 추가 정보
info:
  app:
    name: Pullit Local Development
    description: Pullit 스터디 플랫폼 로컬 개발 환경

# CORS(Cross-Origin Resource Sharing) 설정
# 다른 출처(도메인)의 프론트엔드에서 백엔드 API를 호출할 수 있도록 허용하는 정책입니다.
cors:
  # 프론트엔드 개발 서버의 주소를 지정합니다. ACCESS_CONTROL_ALLOWED_ORIGINS 환경 변수로 변경 가능합니다.
  allowed-origins: ${ACCESS_CONTROL_ALLOWED_ORIGINS:http://localhost:3000}

# 로깅 레벨 설정
# 특정 라이브러리나 패키지에서 발생하는 로그의 상세 수준을 조절합니다.
logging:
  level:
    # Spring Security 관련 로그는 ERROR 레벨 이상의 심각한 문제만 출력하여 로그 가독성을 높입니다.
    org.springframework.security: error

# Spring Boot Actuator 설정 (애플리케이션 모니터링)
management:
  endpoints:
    web:
      # exposure.include: 노출할 Actuator 엔드포인트들을 지정합니다.
      # - "*": 모든 엔드포인트를 노출합니다. (개발 환경용)
      # - "health,info,metrics": 특정 엔드포인트만 노출 (운영 환경용)
      exposure:
        include: "*"  # 모든 엔드포인트 노출
  endpoint:
    health:
      # show-details: 헬스 체크 엔드포인트에서 상세 정보를 표시할지 여부를 결정합니다.
      # - always: 항상 상세 정보를 표시합니다. (개발 환경용)
      # - when-authorized: 인증된 사용자에게만 표시
      # - never: 상세 정보를 표시하지 않습니다.
      show-details: always  # 헬스 체크 상세 정보 표시
```
