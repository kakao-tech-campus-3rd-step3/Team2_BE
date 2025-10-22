package kr.it.pullit.modules.wronganswer.service;

import java.util.List;
import kr.it.pullit.modules.questionset.event.MarkingCompletedEvent;
import kr.it.pullit.modules.questionset.web.dto.response.MarkingResult;
import kr.it.pullit.modules.wronganswer.api.WrongAnswerPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WrongAnswerEventListener {

  private final WrongAnswerPublicApi wrongAnswerPublicApi;

  @EventListener
  @Transactional
  public void handleMarkingCompletedEvent(MarkingCompletedEvent event) {
    List<Long> targetQuestionIds =
        event.results().stream()
            .filter(result -> isTargetForWrongAnswerUpdate(result.isCorrect(), event.isReviewing()))
            .map(MarkingResult::questionId)
            .toList();

    processMarking(event.memberId(), targetQuestionIds, event.isReviewing());
  }

  private boolean isTargetForWrongAnswerUpdate(boolean isCorrect, boolean isReviewing) {
    return isCorrect == isReviewing;
  }

  private void processMarking(Long memberId, List<Long> targetQuestionIds, boolean isReviewing) {
    if (isReviewing) {
      wrongAnswerPublicApi.markAsCorrectAnswers(memberId, targetQuestionIds);
    } else {
      wrongAnswerPublicApi.markAsWrongAnswers(memberId, targetQuestionIds);
    }
  }
}
