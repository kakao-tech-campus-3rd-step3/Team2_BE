package kr.it.pullit.configuration;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class TaskExecutorConfig {

  private final int corePoolSize;
  private final int maxPoolSize;
  private final int queueCapacity;
  private final String prefix;

  public TaskExecutorConfig(
      @Value("${spring.async.executor.application-task-executor.core-pool-size}") int corePoolSize,
      @Value("${spring.async.executor.application-task-executor.max-pool-size}") int maxPoolSize,
      @Value("${spring.async.executor.application-task-executor.queue-capacity}") int queueCapacity,
      @Value("${spring.async.executor.application-task-executor.prefix}") String prefix) {
    this.corePoolSize = corePoolSize;
    this.maxPoolSize = maxPoolSize;
    this.queueCapacity = queueCapacity;
    this.prefix = prefix;
  }

  @Bean(name = "applicationTaskExecutor")
  public Executor asyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.setThreadNamePrefix(prefix);
    executor.initialize();
    return executor;
  }
}
