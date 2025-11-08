package kr.it.pullit.modules.projection.learnstats.scheduler;

import kr.it.pullit.modules.projection.learnstats.service.LearnStatsValidationService;
import kr.it.pullit.shared.retry.RetryOnOptimisticLock;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LearnStatsScheduler {

  private final LearnStatsValidationService learnStatsValidationService;

  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  @SchedulerLock(
      name = "validateConsecutiveLearning",
      lockAtMostFor = "23h",
      lockAtLeastFor = "10m")
  @RetryOnOptimisticLock(backoff = @Backoff(delay = 5000))
  public void validateConsecutiveLearning() {
    learnStatsValidationService.validateAllMembersConsecutiveLearning();
  }
}
