package kr.it.pullit.modules.questionset.service;

import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.QuestionSetStatus;
import kr.it.pullit.modules.questionset.exception.QuestionSetFailedException;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotFoundException;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotReadyException;
import kr.it.pullit.modules.questionset.exception.QuestionSetUnprocessableException;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.wronganswer.exception.WrongAnswerNotFoundException;
import kr.it.pullit.shared.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionSetExceptionHandler {

  private final QuestionSetRepository questionSetRepository;

  public RuntimeException handleReviewSetNotFound(Long id, Long memberId) {
    QuestionSet qs =
        questionSetRepository
            .findByIdAndMemberId(id, memberId)
            .orElseThrow(() -> QuestionSetNotFoundException.byId(id));

    if (qs.getStatus() != QuestionSetStatus.COMPLETE) {
      return handleQuestionSetStatusException(qs);
    }

    return WrongAnswerNotFoundException.noWrongAnswersToReview();
  }

  public RuntimeException handleQuestionSetNotFound(Long id, Long memberId) {
    return questionSetRepository
        .findByIdAndMemberId(id, memberId)
        .map(this::handleQuestionSetStatusException)
        .orElse(QuestionSetNotFoundException.byId(id));
  }

  private BusinessException handleQuestionSetStatusException(QuestionSet qs) {
    return switch (qs.getStatus()) {
      case PENDING -> QuestionSetNotReadyException.byId(qs.getId());
      case FAILED -> QuestionSetFailedException.byId(qs.getId());
      case UNPROCESSABLE -> QuestionSetUnprocessableException.byId(qs.getId());
      default -> QuestionSetNotFoundException.byId(qs.getId());
    };
  }
}
