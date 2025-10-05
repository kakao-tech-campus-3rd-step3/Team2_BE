package kr.it.pullit.modules.questionset.service;

import java.util.List;
import kr.it.pullit.modules.questionset.api.MarkingPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
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
    if (request == null || request.questionIds() == null || request.questionIds().isEmpty()) {
      throw new IllegalArgumentException("request or questionIds is null or empty");
    }

    List<Long> questionIds = request.questionIds();
    for (Long questionId : request.questionIds()) {
      QuestionResponse questionResponse = questionPublicApi.getQuestionById(questionId);
      final String correctAnswer = questionResponse.answer();

      if (isTargetAnswer(request.answer(), correctAnswer, request.isReviewing())) {
        questionIds.add(questionId);
      }
    }

    if (Boolean.TRUE.equals(request.isReviewing())) {
      wrongAnswerPublicApi.markAsCorrectAnswers(request.memberId(), request.questionIds());
    } else {
      wrongAnswerPublicApi.markAsWrongAnswers(request.memberId(), request.questionIds());
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
