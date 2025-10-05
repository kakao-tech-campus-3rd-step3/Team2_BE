package kr.it.pullit.modules.questionset.service;

import java.util.ArrayList;
import java.util.List;
import kr.it.pullit.modules.questionset.api.MarkingPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingRequest;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionResponse;
import kr.it.pullit.modules.wronganswer.api.WrongAnswerPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarkingService implements MarkingPublicApi {

  private final WrongAnswerPublicApi wrongAnswerPublicApi;
  private final QuestionPublicApi questionPublicApi;

  @Override
  public void markQuestions(MarkingServiceRequest request) {
    if (request == null
        || request.markingRequests() == null
        || request.markingRequests().isEmpty()) {
      throw new IllegalArgumentException("request or questionIds is null or empty");
    }

    List<Long> questionIds = new ArrayList<>();
    for (MarkingRequest markingRequest : request.markingRequests()) {
      QuestionResponse questionResponse =
          questionPublicApi.getQuestionById(markingRequest.questionId());
      final String correctAnswer = questionResponse.answer();

      if (isTargetAnswer(markingRequest.answer(), correctAnswer, request.isReviewing())) {
        questionIds.add(markingRequest.questionId());
      }
    }

    if (Boolean.TRUE.equals(request.isReviewing())) {
      wrongAnswerPublicApi.markAsCorrectAnswers(request.memberId(), questionIds);
    } else {
      wrongAnswerPublicApi.markAsWrongAnswers(request.memberId(), questionIds);
    }
  }

  private boolean isTargetAnswer(String userAnswer, String correctAnswer, Boolean isReviewing) {
    return isCorrectAnswer(userAnswer, correctAnswer) == isReviewing;
  }

  private boolean isCorrectAnswer(String userAnswer, String correctAnswer) {
    if (userAnswer == null || correctAnswer == null) {
      return false;
    }
    return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
  }
}
