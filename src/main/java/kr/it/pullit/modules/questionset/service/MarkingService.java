package kr.it.pullit.modules.questionset.service;

import java.util.List;
import java.util.stream.Collectors;
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
  public void markQuestionsAsWrong(MarkingServiceRequest request) {
    if (request == null || request.questionIds() == null || request.questionIds().isEmpty()) {
      throw new IllegalArgumentException("request or questionIds is null or empty");
    }
    wrongAnswerPublicApi.markAsWrongAnswers(request.memberId(), request.questionIds());
  }
  @Override
  public void markQuestionsAsCorrect(MarkingServiceRequest request) {
    if (request == null || request.questionIds() == null || request.questionIds().isEmpty()) {
      throw new IllegalArgumentException("request or questionIds is null or empty");
    }
    wrongAnswerPublicApi.markAsCorrectAnswers(request.memberId(), request.questionIds());
  }
}
