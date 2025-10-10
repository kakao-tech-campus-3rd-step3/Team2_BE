package kr.it.pullit.modules.questionset.service;

import java.util.ArrayList;
import java.util.List;
import kr.it.pullit.modules.questionset.api.MarkingPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.exception.QuestionNotFoundException;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingRequest;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import kr.it.pullit.modules.questionset.web.dto.response.MarkQuestionsResponse;
import kr.it.pullit.modules.wronganswer.api.WrongAnswerPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarkingService implements MarkingPublicApi {

  private final WrongAnswerPublicApi wrongAnswerPublicApi;
  private final QuestionPublicApi questionPublicApi;

  @Override
  public MarkQuestionsResponse markQuestions(MarkingServiceRequest request) {
    if (request == null
        || request.markingRequests() == null
        || request.markingRequests().isEmpty()) {
      throw new IllegalArgumentException("request or questionIds is null or empty");
    }

    List<Long> targetQuestionIds = new ArrayList<>();
    for (MarkingRequest markingRequest : request.markingRequests()) {
      Question question =
          questionPublicApi
              .findEntityById(markingRequest.questionId())
              .orElseThrow(() -> QuestionNotFoundException.byId(markingRequest.questionId()));

      if (isTargetAnswer(question.isCorrect(markingRequest.answer()), request.isReviewing())) {
        targetQuestionIds.add(markingRequest.questionId());
      }
    }

    if (Boolean.TRUE.equals(request.isReviewing())) {
      wrongAnswerPublicApi.markAsCorrectAnswers(request.memberId(), targetQuestionIds);
    } else {
      wrongAnswerPublicApi.markAsWrongAnswers(request.memberId(), targetQuestionIds);
    }

    return new MarkQuestionsResponse(targetQuestionIds.size());
  }

  private boolean isTargetAnswer(boolean isCorrect, Boolean isReviewing) {
    return isCorrect == isReviewing;
  }
}
