package kr.it.pullit.modules.questionset.scheduler;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.shared.retry.RetryOnOptimisticLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionSetCleanupScheduler {

  private static final long STALE_THRESHOLD_MINUTES = 60;

  private final QuestionSetPublicApi questionSetPublicApi;
  private final Clock clock;

  @Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul") // 매 10분마다 실행
  @SchedulerLock(
      name = "cleanupStalePendingQuestionSets",
      lockAtMostFor = "9m",
      lockAtLeastFor = "1m")
  @Transactional
  @RetryOnOptimisticLock(backoff = @Backoff(delay = 1000))
  public void cleanupStalePendingQuestionSets() {
    log.info("오래된 '생성중' 상태의 문제집 정리 작업을 시작합니다.");
    LocalDateTime threshold = LocalDateTime.now(clock).minusMinutes(STALE_THRESHOLD_MINUTES);

    List<QuestionSet> staleQuestionSets = questionSetPublicApi.findStalePending(threshold);

    if (staleQuestionSets.isEmpty()) {
      log.info("정리할 오래된 '생성중' 상태의 문제집이 없습니다.");
      return;
    }

    for (QuestionSet questionSet : staleQuestionSets) {
      log.warn(
          "문제집 ID {}가 '생성중' 상태로 {}분 이상 방치되어 '실패' 처리합니다.",
          questionSet.getId(),
          STALE_THRESHOLD_MINUTES);
      questionSetPublicApi.markAsFailed(questionSet.getId());
    }
    log.info("총 {}개의 오래된 문제집을 '실패' 처리했습니다.", staleQuestionSets.size());
  }
}
