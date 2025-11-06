package kr.it.pullit.modules.projection.learnstats.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import kr.it.pullit.modules.projection.learnstats.service.LearnStatsValidationService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
@RequiredArgsConstructor
public class LearnStatsScheduler {

  private final LearnStatsValidationService learnStatsValidationService;

  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  @SchedulerLock(
      name = "validateConsecutiveLearning",
      lockAtMostFor = "23h",
      lockAtLeastFor = "10m")
  public void validateConsecutiveLearning() {
    learnStatsValidationService.validateAllMembersConsecutiveLearning();
  }
}
