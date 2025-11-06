package kr.it.pullit.modules.questionset.scheduler;

import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.event.QuestionSetCreatedEvent;
import kr.it.pullit.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionSetRetryScheduler {

  private static final int MAX_RETRY_COUNT = 3;

  private final QuestionSetPublicApi questionSetPublicApi;
  private final EventPublisher eventPublisher;

  @Scheduled(cron = "0 */5 * * * *") // 매 5분마다 실행
  @SchedulerLock(
      name = "retryFailedQuestionSetGeneration",
      lockAtMostFor = "4m",
      lockAtLeastFor = "1m")
  @Transactional
  public void retryFailedQuestionSetGeneration() {
    log.info("'생성실패' 상태의 문제집 재시도 작업을 시작합니다.");

    questionSetPublicApi
        .findFirstFailedSetForRetry(MAX_RETRY_COUNT)
        .ifPresent(
            questionSet -> {
              log.info(
                  "문제집 ID {} (재시도 횟수: {}) 재생성을 시작합니다.",
                  questionSet.getId(),
                  questionSet.getRetryCount());
              questionSet.retry();
              eventPublisher.publish(QuestionSetCreatedEvent.from(questionSet));
              log.info("문제집 ID {}에 대한 재생성 이벤트를 발행했습니다.", questionSet.getId());
            });
  }
}
