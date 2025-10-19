package kr.it.pullit.modules.questionset.service;

import java.util.ArrayList;
import java.util.List;
import kr.it.pullit.modules.questionset.api.MarkingPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.exception.QuestionNotFoundException;
import kr.it.pullit.modules.questionset.service.event.MarkingCompletedEvent;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import kr.it.pullit.modules.questionset.web.dto.response.MarkQuestionsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.MarkingResult;
import kr.it.pullit.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarkingService implements MarkingPublicApi {

  private final QuestionPublicApi questionPublicApi;
  private final EventPublisher eventPublisher;

  @Override
  public MarkQuestionsResponse markQuestions(MarkingServiceRequest request) {
    validateRequest(request);

    List<MarkingResult> results = new ArrayList<>();

    for (var markingRequest : request.markingRequests()) {
      Question question = findQuestionById(markingRequest.questionId());
      boolean isCorrect = question.isCorrect(markingRequest.answer());
      results.add(MarkingResult.of(question.getId(), isCorrect));
    }

    eventPublisher.publish(
        new MarkingCompletedEvent(request.memberId(), results, request.isReviewing()));

    long correctCount = results.stream().filter(MarkingResult::isCorrect).count();

    return MarkQuestionsResponse.of(results, results.size(), (int) correctCount);
  }

  private void validateRequest(MarkingServiceRequest request) {
    if (request == null
        || request.markingRequests() == null
        || request.markingRequests().isEmpty()) {
      throw new IllegalArgumentException("request or questionIds is null or empty");
    }
  }

  private Question findQuestionById(Long questionId) {
    return questionPublicApi
        .findEntityById(questionId)
        .orElseThrow(() -> QuestionNotFoundException.byId(questionId));
  }
}
