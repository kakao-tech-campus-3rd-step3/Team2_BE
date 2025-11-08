package kr.it.pullit.modules.learningsource.source.batch;

import kr.it.pullit.shared.retry.RetryOnOptimisticLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.scheduling.source-cleanup.enabled", havingValue = "true")
public class SourceCleanupScheduler {

  private final SourceCleanupService sourceCleanupService;

  @Scheduled(cron = "${app.scheduling.source-cleanup.cron:0 0 4 * * ?}") // 매일 새벽 4시에 실행
  @SchedulerLock(name = "runSourceCleanup", lockAtMostFor = "50m", lockAtLeastFor = "10m")
  @RetryOnOptimisticLock(backoff = @Backoff(delay = 1000))
  public void runCleanup() {
    log.info("Source 자동 정리 스케줄러를 실행합니다.");
    sourceCleanupService.cleanupDeletedSources();
  }
}
