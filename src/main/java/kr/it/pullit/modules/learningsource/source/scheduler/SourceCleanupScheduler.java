package kr.it.pullit.modules.learningsource.source.scheduler;

import kr.it.pullit.shared.retry.RetryOnOptimisticLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SourceCleanupScheduler {

  private final SourceCleanupService sourceCleanupService;

  /** 유저 보안 정책에 따라 S3와 DB에서 삭제 처리된 Source를 완전히 제거하기 위해 매일 새벽 4시에 스케줄러를 실행한다. */
  @Scheduled(cron = "0 0 4 * * ?", zone = "Asia/Seoul")
  @SchedulerLock(name = "runSourceCleanup", lockAtMostFor = "50m", lockAtLeastFor = "10m")
  @RetryOnOptimisticLock(backoff = @Backoff(delay = 1000))
  public void runCleanup() {
    log.info("Source 자동 정리 스케줄러를 실행합니다.");
    sourceCleanupService.cleanupDeletedSources();
  }
}
