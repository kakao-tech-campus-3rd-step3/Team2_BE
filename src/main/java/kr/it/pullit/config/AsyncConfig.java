package kr.it.pullit.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import kr.it.pullit.platform.boot.properties.AsyncExecutorProperties;
import kr.it.pullit.platform.boot.properties.DataSourceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 비동기 처리를 위한 ThreadPoolTaskExecutor 설정.
 * <p>
 * I/O-bound 작업에 최적화된 스레드 풀을 구성하며, 설정값은 프로퍼티를 통해 조정 가능합니다. 기본적으로 corePoolSize만 설정하고 maxPoolSize와
 * queueCapacity는 Spring 기본값(unbounded)을 사용합니다.
 * </p>
 *
 * <h3>스레드 풀 동작 원리:</h3>
 * <ol>
 * <li>corePoolSize 만큼 스레드 실행</li>
 * <li>코어 스레드가 모두 사용 중이면 작업을 큐에 대기</li>
 * <li>큐가 가득 차면 maxPoolSize까지 스레드 생성</li>
 * <li>모든 스레드와 큐가 가득 차면 RejectedExecutionHandler 실행</li>
 * </ol>
 *
 * @author Pullit Development Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfig {

  private final DataSourceProperties dataSourceProperties;
  private final AsyncExecutorProperties asyncExecutorProperties;

  /**
   * I/O-bound 작업을 위한 ThreadPoolTaskExecutor 빈을 생성합니다.
   * <p>
   * corePoolSize는 다음 공식으로 자동 계산됩니다: <strong>CPU코어수 × 목표CPU사용률 × (1 + I/O대기시간/CPU처리시간)</strong>
   * </p>
   *
   * @return I/O-bound 작업용 Executor
   */
  @Bean(name = "ioBoundTaskExecutor")
  public Executor ioBoundTaskExecutor() {
    int cpuCores = Runtime.getRuntime().availableProcessors();
    int hikariMax = dataSourceProperties.getMaximumPoolSize();
    if (hikariMax <= 0) {
      hikariMax = 10; // HikariCP 기본값 fallback
    }

    // corePoolSize 계산: 직접 지정 또는 공식 사용
    int corePoolSize = calculateCorePoolSize(cpuCores);

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);

    // maxPoolSize 설정 (필요시에만)
    Integer finalMax = null;
    Integer configuredMax = asyncExecutorProperties.getMaxPoolSize();
    if (configuredMax != null) {
      int maxPoolSize = configuredMax <= 0 ? Math.max(corePoolSize, 1) : configuredMax;
      if (corePoolSize > maxPoolSize) {
        corePoolSize = maxPoolSize;
        executor.setCorePoolSize(corePoolSize);
        log.warn("corePoolSize({})가 maxPoolSize({})보다 커서 조정되었습니다.",
            asyncExecutorProperties.getCorePoolSize(), maxPoolSize);
      }
      executor.setMaxPoolSize(maxPoolSize);
      finalMax = maxPoolSize;
    }

    // queueCapacity 설정 (필요시에만)
    Integer finalQueue = null;
    Integer configuredQueue = asyncExecutorProperties.getQueueCapacity();
    if (configuredQueue != null) {
      int queueCapacity = Math.max(0, configuredQueue);
      executor.setQueueCapacity(queueCapacity);
      finalQueue = queueCapacity;
    }

    executor.setThreadNamePrefix("IoBoundAsync-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();

    log.info("I/O-bound ThreadPoolTaskExecutor 설정 완료");
    log.info("  - CPU Cores: {}", cpuCores);
    log.info("  - HikariCP MaxPoolSize(참고): {}", hikariMax);
    log.info("  - CorePoolSize: {} (계산식: {})", corePoolSize, getCorePoolSizeFormula());
    log.info("  - MaxPoolSize: {}", finalMax == null ? "default(unbounded)" : finalMax);
    log.info("  - QueueCapacity: {}", finalQueue == null ? "default(unbounded)" : finalQueue);

    return executor;
  }

  /**
   * corePoolSize를 계산합니다.
   * <p>
   * 직접 지정된 값이 있으면 사용하고, 없으면 공식을 사용하여 계산합니다: <strong>CPU코어수 × 목표CPU사용률 × (1 + I/O대기계수)</strong>
   * </p>
   *
   * @param cpuCores CPU 코어 수
   * @return 계산된 corePoolSize
   */
  private int calculateCorePoolSize(int cpuCores) {
    Integer configured = asyncExecutorProperties.getCorePoolSize();
    if (configured != null) {
      return Math.max(1, configured);
    }

    // 공식 적용: CPU코어수 × 목표CPU사용률 × (1 + I/O대기계수)
    float targetCpuUtilization = asyncExecutorProperties.getTargetCpuUtilization();
    float blockingCoefficient = asyncExecutorProperties.getBlockingCoefficient();

    int calculated = Math.round(cpuCores * targetCpuUtilization * (1 + blockingCoefficient));
    return Math.max(1, calculated); // 최소 1개 스레드 보장
  }

  /**
   * 현재 설정에 따른 corePoolSize 계산 공식을 문자열로 반환합니다.
   *
   * @return 계산 공식 문자열
   */
  private String getCorePoolSizeFormula() {
    Integer configured = asyncExecutorProperties.getCorePoolSize();
    if (configured != null) {
      return "직접 지정값 " + configured;
    }

    int cpuCores = Runtime.getRuntime().availableProcessors();
    float targetCpu = asyncExecutorProperties.getTargetCpuUtilization();
    float blocking = asyncExecutorProperties.getBlockingCoefficient();

    return String.format("%d × %.1f × (1 + %.1f) = %.1f", cpuCores, targetCpu, blocking,
        cpuCores * targetCpu * (1 + blocking));
  }
}
