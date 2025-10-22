package kr.it.pullit.modules.questionset.service;

import kr.it.pullit.modules.projection.learnstats.api.LearnStatsEventPublicApi;
import kr.it.pullit.modules.questionset.service.event.MarkingCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MarkingCompletedEventHandler {

  private final LearnStatsEventPublicApi learnStatsEventPublicApi;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleMarkingCompletedEvent(MarkingCompletedEvent event) {

    if (event.results().isEmpty()) {
      return;
    }

    learnStatsEventPublicApi.publishQuestionSetSolved(event.memberId(), event.results().size());
  }
}
