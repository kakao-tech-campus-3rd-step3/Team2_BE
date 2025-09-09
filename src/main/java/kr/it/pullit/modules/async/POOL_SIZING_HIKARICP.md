# ThreadPool & HikariCP 사이징 원리와 설정 근거

본 문서는 Pullit 서비스에서 비동기 ThreadPool과 HikariCP 커넥션 풀을 어떻게, 왜 그렇게 설정하는지에 대한 실전 가이드입니다. 운영 중 튜닝의 기준, 모니터링 지표, 그리고 공식 문서 근거를 포함합니다.

## 1) 용어와 역할 구분
- 톰캣 스레드 풀: HTTP 요청을 받는 웹 서버 스레드(창구). 가능한 빨리 응답 반환.
- `@Async` ThreadPool: 시간이 오래 걸리는 I/O 작업을 처리하는 백오피스 스레드.
- HikariCP 풀: DB 연결(Connection)의 재사용/관리 풀. DB는 가장 빈번한 I/O 병목 지점.

각 풀은 목적과 자원 한계가 다르므로 독립적으로 튜닝해야 합니다.

## 2) ThreadPool 사이징 원리 (I/O-Bound 기준)

### 2.1 코어 풀 사이즈(corePoolSize) 계산 공식
- 공식: 코어 스레드 수 = CPU코어수 × 목표CPU사용률 × (1 + I/O대기계수)
  - 목표CPU사용률 (예: 0.3)
  - I/O대기계수 = (I/O 대기시간 / CPU 처리시간) (예: 0.9)
- 의미: I/O 대기 동안 CPU가 놀지 않도록, 다른 스레드가 CPU를 활용하게 하는 합리적 시작점.
- 운영 가이드: core 중심 튜닝 → 모니터링(활성 스레드, 큐 길이)을 통해 점진 조정

### 2.2 큐/맥스는 기본 "unbounded" 유지 권장
- 이유: bounded queue는 급격한 스레드 증가와 컨텍스트 스위칭 비용을 초래할 수 있음.
- 전략: queue/max는 필요할 때만 제한값을 지정. 기본은 Spring 디폴트(Integer.MAX_VALUE).
- 과부하 보호: RejectedExecutionHandler=CallerRunsPolicy로 자연 감속.

## 3) HikariCP 사이징 원리

### 3.1 공식 가이드(About Pool Sizing)
- 공식: `connections = ((core_count * 2) + effective_spindle_count)`
  - `core_count`: **DB 서버의** 물리적 CPU 코어 수
  - `effective_spindle_count`: 디스크 동시 I/O 가능 수(SSD는 보통 1)
- 의미: CPU 대비 I/O 대기/디스크 처리 병목을 고려한 실전적 상한선 제시.

### 3.2 왜 기본값은 10인가? (핵심 원리)
"왜 CPU 코어 수와 상관없이 10인가?" 라는 질문은 매우 중요합니다. 이는 `10`이 대부분의 환경에서 **성능 저하 없이 안전하게 시작할 수 있는 가장 합리적인 기본값(Safe Harbor)** 이기 때문입니다.

1.  **DB 커넥션은 비싼 자원이다**: `@Async` 스레드와 달리, DB 커넥션은 원격 DB 서버의 메모리와 CPU를 직접 점유하는 비싼 리소스입니다.

2.  **많다고 빨라지지 않는다 (수확 체감의 법칙)**: 커넥션 풀 크기를 늘릴수록 성능(TPS)은 특정 지점까지만 증가하고, 그 이상부터는 오히려 DB 서버의 리소스 경합으로 인해 성능이 감소합니다. `10`은 대부분의 시스템에서 이 최적 성능 구간의 초입에 해당합니다.

3.  **병목은 DB 서버에 있다**: 커넥션 풀의 실제 성능은 우리 애플리케이션 서버가 아닌 **DB 서버의 CPU 코어 수와 디스크 I/O 속도**에 의해 결정됩니다. 애플리케이션 서버가 아무리 빨라도 DB가 처리하지 못하면 소용이 없습니다.

이 때문에 HikariCP는 불확실한 외부 요인(앱 서버 CPU 등)에 의존하는 대신, 어떤 환경에서든 DB 서버를 보호하며 안정적으로 시작할 수 있는 보수적인 값 `10`을 기본값으로 채택했습니다.

### 3.3 HikariCP 기본값 및 권장 설정
- **maximumPoolSize: 10 (HikariCP 기본값)**
  - `ThreadPoolTaskExecutor`의 기본값(`Integer.MAX_VALUE`)과 달리, DB 커넥션은 매우 비싼 리소스이므로 보수적인 기본값을 가집니다.
  - 프로덕션 환경에서는 반드시 위 공식(**DB 서버 기준**)과 부하 테스트를 통해 튜닝해야 합니다.
- **minimumIdle = maximumPoolSize**
  - HikariCP 공식 문서에서 강력히 권장하는 설정으로, **고정 크기(Fixed-size) 커넥션 풀**을 만들어 일관된 성능을 보장합니다. 아래 '3.4'에서 상세히 설명합니다.
- **connectionTimeout=30s, idleTimeout=10m, maxLifetime=30m**
  - 모두 HikariCP의 기본값이며, 대부분의 환경에서 안정적으로 동작합니다. DB 서버의 `wait_timeout` 설정 등과 연계하여 필요시 조정합니다.

### 3.4 왜 `minimum-idle`을 `maximum-pool-size`와 동일하게 설정하는가?

HikariCP 공식 문서에서 `minimum-idle`을 `maximum-pool-size`와 동일하게 설정하는 것을 강력히 권장합니다. 이는 **고정 크기(Fixed-size) 커넥션 풀**을 만들기 위함이며, 다음과 같은 중요한 이점을 제공합니다.

1.  **최상의 성능과 예측 가능성**: 트래픽이 적을 때 커넥션을 닫고, 트래픽이 많아질 때 다시 커넥션을 여는 과정에는 상당한 오버헤드(시간 지연)가 발생합니다. 풀 크기를 고정하면 이러한 '커넥션 요동(Churn)'이 사라집니다. 갑작스러운 트래픽 증가(Spike) 시에도 풀은 이미 모든 커넥션을 준비하고 있으므로, 지연 없이 즉시 요청을 처리할 수 있어 **일관되고 예측 가능한 응답 시간**을 보장합니다.

2.  **'커넥션 폭풍(Connection Storm)' 방지**: 유휴 상태에서 갑자기 많은 요청이 들어오면, 풀은 `maximum-pool-size`에 도달하기 위해 동시에 여러 개의 커넥션을 생성하려고 시도합니다. 이 순간 DB 서버에 짧은 시간 동안 과도한 부하가 걸리는 '커넥션 폭풍'이 발생할 수 있습니다. 고정 풀은 이러한 위험을 원천적으로 방지합니다.

#### 단점은 없는가?

유일한 단점은 트래픽이 거의 없는 유휴 시간에도 최대치의 커넥션을 유지하므로 DB 리소스를 더 많이 점유한다는 것입니다. 하지만 대부분의 서버 애플리케이션 환경에서 이 단점보다는, **일관된 성능과 안정성이라는 장점이 훨씬 크기 때문에** 고정 크기 풀이 표준적인 모범 사례(Best Practice)로 여겨집니다.

## 4) 설정 근거 요약
- 비동기 ThreadPool: core만 튜닝(공식 적용), queue/max는 default → 지연 증가/컨텍스트 스위칭 비용 최소화
- HikariCP: 공식 공식을 기준으로 산정 후 부하 테스트로 보정, minimumIdle=maximumPoolSize로 스파이크에 안정적

## 5) 운영 시 모니터링 지표
- executor.active.threads: 활성 스레드 수 → core 포화 여부 판단
- executor.queue.size: 큐 적체 → core 확대 고려(우선), 불가 시 queue 제한 도입
- DB: 커넥션 사용률, 대기시간, lock 대기, slow query 비율

## 6) 단계별 튜닝 절차
1. 초기 배포: corePoolSize=공식값, HikariCP=기본값(또는 공식) 적용
2. 모니터링: Actuator/DB 메트릭 관찰(피크 시간대 포함)
3. 조정: core → Hikari → queue 순서로 보수적 조정
4. 재검증: 부하 테스트(k6 등)로 SLA 충족 확인

## 7) 공식 문서 링크 (HikariCP)
- HikariCP GitHub (공식): https://github.com/brettwooldridge/HikariCP
- HikariCP Documentation (Wiki): https://github.com/brettwooldridge/HikariCP/wiki
- About Pool Sizing (Wiki 섹션): https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing

위 링크는 HikariCP 프로젝트의 공식 저장소 및 공식 위키 문서입니다. "About Pool Sizing" 페이지에 커넥션 수 산정 공식과 배경 설명이 포함되어 있습니다.

## 8) Pullit 적용 스냅샷
- AsyncConfig: core 자동계산(공식), max/queue는 명시적 설정시에만 지정
- application.yml(HikariCP): 공식 기반 코멘트/기본값, minimumIdle=maximumPoolSize

---
이 문서는 운영 중 지속 업데이트되며, 변경 시점의 부하 테스트 리포트 링크를 함께 기록합니다.
