package kr.it.pullit.modules.questionset.service;

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
    validateRequest(request);

    List<Long> targetQuestionIds = getTargetQuestionIds(request);

    processMarking(request.memberId(), targetQuestionIds, request.isReviewing());

    return new MarkQuestionsResponse(targetQuestionIds.size());
  }

  private void validateRequest(MarkingServiceRequest request) {
    if (request == null
        || request.markingRequests() == null
        || request.markingRequests().isEmpty()) {
      throw new IllegalArgumentException("request or questionIds is null or empty");
    }
  }

  private List<Long> getTargetQuestionIds(MarkingServiceRequest request) {
    return request.markingRequests().stream()
        .filter(markingRequest -> isTargetAnswerForMarking(markingRequest, request.isReviewing()))
        .map(MarkingRequest::questionId)
        .toList();
  }

  private boolean isTargetAnswerForMarking(MarkingRequest markingRequest, Boolean isReviewing) {
    Question question = findQuestionById(markingRequest.questionId());
    return isTargetAnswer(question.isCorrect(markingRequest.answer()), isReviewing);
  }

  private Question findQuestionById(Long questionId) {
    return questionPublicApi
        .findEntityById(questionId)
        .orElseThrow(() -> QuestionNotFoundException.byId(questionId));
  }

  private boolean isTargetAnswer(boolean isCorrect, Boolean isReviewing) {
    return isCorrect == isReviewing;
  }

  private void processMarking(Long memberId, List<Long> targetQuestionIds, Boolean isReviewing) {
    if (Boolean.TRUE.equals(isReviewing)) {
      wrongAnswerPublicApi.markAsCorrectAnswers(memberId, targetQuestionIds);
    } else {
      wrongAnswerPublicApi.markAsWrongAnswers(memberId, targetQuestionIds);
    }
  }
}
