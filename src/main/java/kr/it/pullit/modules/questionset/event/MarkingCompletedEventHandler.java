package kr.it.pullit.modules.questionset.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsDailyPublicApi;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsEventPublicApi;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MarkingCompletedEventHandler {

  private final LearnStatsEventPublicApi learnStatsEventPublicApi;
  private final LearnStatsDailyPublicApi learnStatsDailyPublicApi;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleMarkingCompletedEvent(MarkingCompletedEvent event) {

    if (event.results().isEmpty()) {
      return;
    }

    learnStatsEventPublicApi.publishQuestionSetSolved(event.memberId(), event.results().size());
    learnStatsDailyPublicApi.addDailyStats(event.memberId(), event.results().size(), 1);
  }
}
