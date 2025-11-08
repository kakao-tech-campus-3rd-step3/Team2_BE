package kr.it.pullit.modules.learningsource.source.scheduler;

import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.shared.retry.RetryOnOptimisticLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SourceSyncScheduler {

  private final SourcePublicApi sourcePublicApi;

  /** S3와 DB의 Source 데이터 정합성을 맞추기 위해 15분마다 스케줄러를 실행한다. */
  @Scheduled(cron = "0 0/15 * * * ?", zone = "Asia/Seoul")
  @SchedulerLock(name = "synchronizeS3SourceFiles", lockAtMostFor = "14m", lockAtLeastFor = "10m")
  @RetryOnOptimisticLock(backoff = @Backoff(delay = 5000))
  public void runSourceS3Sync() {
    log.info("S3와 DB의 Source 데이터 정합성 동기화 스케줄러를 시작합니다.");
    try {
      sourcePublicApi.synchronizeS3Files();
    } catch (Exception e) {
      log.error("Source 데이터 동기화 스케줄러 실행 중 오류가 발생했습니다.", e);
    }
    log.info("S3와 DB의 Source 데이터 정합성 동기화 스케줄러를 종료합니다.");
  }
}
