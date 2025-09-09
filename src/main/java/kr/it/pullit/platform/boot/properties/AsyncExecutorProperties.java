package kr.it.pullit.platform.boot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

/**
 * I/O Bound 비동기 작업을 위한 ThreadPoolTaskExecutor 설정 프로퍼티.
 * <p>
 * 이 설정 클래스는 'pullit.async.io' prefix를 사용하여 application.yml에서 스레드 풀 설정을 바인딩합니다. 설정하지 않을 경우 자동 계산된
 * 기본값을 사용합니다.
 * </p>
 *
 * <h3>설정 예시:</h3>
 *
 * <pre>
 * pullit:
 *   async:
 *     io:
 *       target-cpu-utilization: 0.3  # CPU 사용률 30%
 *       blocking-coefficient: 0.9     # I/O 대기 시간 비율 90%
 *       core-pool-size: 16            # 직접 지정 (공식 대신 사용)
 * </pre>
 *
 * @author Pullit Development Team
 * @since 1.0.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "pullit.async.io")
public class AsyncExecutorProperties {

  /**
   * 목표 CPU 사용률 (0.0 ~ 1.0).
   * <p>
   * corePoolSize 자동 계산 시 사용되는 값입니다. 기본값: 0.3 (30%)
   * </p>
   */
  private Float targetCpuUtilization = 0.3f;

  /**
   * I/O 대기 시간 계수 (Blocking Coefficient).
   * <p>
   * I/O 대기 시간 / CPU 서비스 시간의 비율입니다. 값이 클수록 I/O 대기가 많다는 의미이므로 더 많은 스레드가 필요합니다. 기본값: 0.9 (90% I/O 대기)
   * </p>
   */
  private Float blockingCoefficient = 0.9f;

  /**
   * 코어 스레드 풀 크기 직접 지정.
   * <p>
   * null인 경우 공식을 사용한 자동 계산값을 사용합니다: corePoolSize = CPU코어수 × targetCpuUtilization × (1 +
   * blockingCoefficient)
   * </p>
   */
  private Integer corePoolSize;

  /**
   * 최대 스레드 풀 크기.
   * <p>
   * null인 경우 Spring 기본값(Integer.MAX_VALUE)을 사용합니다. 설정 시 DB 커넥션 풀 등 의존 리소스의 한계를 고려해야 합니다.
   * </p>
   */
  private Integer maxPoolSize;

  /**
   * 작업 대기 큐 용량.
   * <p>
   * null인 경우 Spring 기본값(Integer.MAX_VALUE)을 사용합니다. unbounded queue가 일반적으로 더 좋은 성능을 제공합니다.
   * </p>
   */
  private Integer queueCapacity;
}
