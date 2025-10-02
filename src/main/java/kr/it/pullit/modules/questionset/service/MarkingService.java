package kr.it.pullit.modules.questionset.service;

import kr.it.pullit.modules.questionset.api.MarkingPublicApi;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import kr.it.pullit.modules.wronganswer.api.WrongAnswerPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarkingService implements MarkingPublicApi {

  private final WrongAnswerPublicApi wrongAnswerPublicApi;

  @Override
  public void markQuestions(MarkingServiceRequest request) {
    if (request == null || request.questionIds() == null || request.questionIds().isEmpty()) {
      throw new IllegalArgumentException("request or questionIds is null or empty");
    }

    if (Boolean.TRUE.equals(request.isReviewing())) {
      wrongAnswerPublicApi.markAsCorrectAnswers(request.memberId(), request.questionIds());
    } else {
      wrongAnswerPublicApi.markAsWrongAnswers(request.memberId(), request.questionIds());
    }
  }
}
