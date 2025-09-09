# Spring @Async 비동기 스레드풀 동작 원리

본 문서는 Spring의 `@Async` 기반 비동기 처리에서 ThreadPoolTaskExecutor가 어떻게 동작하는지, 요청 흐름과 내부 상태 전이를 중심으로 설명합니다. 운영 시 튜닝 포인트와 모니터링 지표도 포함합니다.

## 1) 구성 요소
- 컨트롤러(톰캣 스레드): HTTP 요청을 수신하고 `@Async` 메서드를 호출한 뒤 즉시 반환
- `@Async` 메서드: 별도 스레드에서 실행될 비동기 작업의 진입점
- `TaskExecutor`(ThreadPoolTaskExecutor): 비동기 작업 스케줄링/실행 엔진
- 작업 큐(BlockingQueue): 코어 스레드가 바쁠 때 작업이 대기하는 공간

## 2) 실행 흐름
1. 클라이언트가 API 요청 → 톰캣 스레드가 컨트롤러 진입
2. 컨트롤러가 서비스의 `@Async` 메서드 호출
3. Spring AOP 프록시가 호출을 가로채서 TaskExecutor에 Runnable/Callable 형태로 제출
4. Executor가 정책에 따라 즉시 실행 또는 큐 대기/확장/거부 처리
5. 비동기 작업이 완료되면 후속 로직 로깅/콜백/저장 등을 수행

## 3) ThreadPoolTaskExecutor의 상태 전이
- corePoolSize 이하: 즉시 새 워커 스레드 또는 유휴 스레드로 실행
- corePoolSize 초과: 작업 큐에 대기(기본 unbounded)
- 큐 포화(한정 시): maxPoolSize까지 스레드 확장
- maxPoolSize 및 큐 모두 포화: RejectedExecutionHandler 동작(기본 Abort, 권장 CallerRunsPolicy)

## 4) 주요 파라미터 상호작용
- corePoolSize: 평상시 동시 실행 스레드 수. I/O-bound는 공식 기반 자동 계산 권장
- queueCapacity: 코어 포화 시 대기 버퍼. 대부분 unbounded 유지(성능/일관성), 필요 시에만 제한
- maxPoolSize: 큐가 포화될 때 확장 상한. 외부 리소스(DB/외부 API) 한계를 넘지 않도록 신중히 사용
- keepAliveSeconds: 코어 초과 스레드의 유휴 제거 지연
- RejectedExecutionHandler: 과부하 시 정책. CallerRunsPolicy로 자연 감속 권장

## 5) 예외 처리
- `Future` 반환: `future.get()`에서 예외 전파
- `void` 반환: 호출 스레드로 예외 전파되지 않음 → `AsyncUncaughtExceptionHandler` 구성 필요

## 6) 운영 가이드
- 초기: core 중심 튜닝(공식) → max/queue는 기본 유지
- 모니터링: executor.active.threads, executor.queue.size, 처리 지연, 에러 비율
- 조정 순서: core ↑ → (필요 시) queue 소량 ↑ → (마지막) max 제한 도입
- 과부하 보호: CallerRunsPolicy로 자동 감속, API rate limiting 병행 고려

## 7) 관련 코드 포인트
- `AsyncConfig.ioBoundTaskExecutor()`: 코어 자동 계산, max/queue 조건부 지정, 거부 정책 설정
- `@Async("ioBoundTaskExecutor")`: 지정한 Executor로 라우팅

## 8) 포화 정책(RejectedExecutionHandler) 선택 이유

비동기 스레드풀의 포화 시 어떤 정책으로 대응할지에 따라 시스템의 안정성과 사용자 경험이 크게 달라집니다. 본 프로젝트는 **CallerRunsPolicy**를 채택했습니다.

- 왜 CallerRunsPolicy인가?
  - **자연스러운 역압(Backpressure)**: 풀/큐가 포화되면, 새 작업을 제출한 요청 스레드(대개 톰캣)가 해당 작업을 직접 수행합니다. 이를 통해 **처리 속도가 자동으로 느려져** 시스템이 감당 가능한 수준으로 수렴합니다.
  - **예측 가능한 동작**: 예외를 던지거나(Abort) 조용히 버리는(Discard) 대신, 요청 처리 지연이라는 형태로 드러나므로 운영 관점에서 원인 파악이 쉽습니다.
  - **데이터 손실 방지**: Discard/DiscardOldest는 작업 유실 위험이 있습니다. CallerRuns는 유실시키지 않습니다.
  - **DB/외부 시스템 보호**: 포화 구간에서 무제한 추가 제출을 차단하고, 상류(웹 계층)에서 자연 감속되므로 하류(DB·외부 API)의 폭주를 막습니다.

- 다른 정책과 비교
  - AbortPolicy(기본): 즉시 `RejectedExecutionException` → 피크 시 대량 500/에러 스파이크 위험
  - DiscardPolicy/DiscardOldestPolicy: 작업 유실/순서 역전 위험 → 금융/도메인 이벤트 계열에 부적합

- 트레이드오프
  - **지연 증가**: 포화 시 컨트롤러 스레드가 작업을 직접 수행하므로 응답 지연이 커집니다. 하지만 이는 **의도된 완화 메커니즘**입니다.
  - **관찰 필요**: 포화 빈도가 높다면 core/queue/max 및 상류 Rate Limiting 재점검이 필요합니다.

- 함께 쓰면 좋은 것
  - **서버 측 Rate Limiting/서킷브레이커**: 상류에서 유입량을 제어하여 포화 빈도를 낮춤
  - **메트릭 경보**: `executor.active.threads`, `executor.queue.size`, 응답 시간 P95/99 경보로 조기 감지

## 9) 차트 읽는 법(ASYNC_THREADPOOL.mmd)
- A→B→C: HTTP 요청이 컨트롤러로 들어와 `@Async` 메서드 호출까지의 웹 계층 흐름
- C→D: `@Async` 호출이 프록시를 통해 Executor에 작업 제출됨
- D→E: 코어 여유가 있으면 즉시 워커 스레드 실행(core 경로)
- D→F: 코어가 포화되면 큐에 대기(queue 경로, 기본 unbounded)
- F→G: 큐가 포화이고 max가 지정된 경우에만 스레드 확장
- F→H: 큐도 포화인데 max 미지정(또는 한계) 시 CallerRunsPolicy로 요청 스레드가 직접 수행(역압)
- Params(L/M/N): 코어 계산식, 큐 기본, max 제한 원칙을 정책으로 명시
- Policy(P1/P2/P3): 차트 내 정책 선언부로, 각각 큐 기본값, 거부 정책 선택, 코어 계산 공식을 나타냄

