package kr.it.pullit.modules.questionset.service;

import java.util.ArrayList;
import java.util.List;
import kr.it.pullit.modules.questionset.api.MarkingPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.MarkingResult;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.event.MarkingCompletedEvent;
import kr.it.pullit.modules.questionset.exception.QuestionNotFoundException;
import kr.it.pullit.modules.questionset.repository.MarkingResultRepository;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.web.dto.request.MarkingServiceRequest;
import kr.it.pullit.modules.questionset.web.dto.response.MarkQuestionsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.MarkingResultDto;
import kr.it.pullit.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MarkingService implements MarkingPublicApi {

  private final QuestionPublicApi questionPublicApi;
  private final EventPublisher eventPublisher;
  private final MarkingResultRepository markingResultRepository;
  private final QuestionSetRepository questionSetRepository;

  @Override
  public MarkQuestionsResponse markQuestions(MarkingServiceRequest request) {
    validateRequest(request);

    List<MarkingResultDto> results = new ArrayList<>();

    for (var markingRequest : request.markingRequests()) {
      Question question = findQuestionById(markingRequest.questionId());
      boolean isCorrect = question.isCorrect(markingRequest.memberAnswer());
      results.add(MarkingResultDto.of(question.getId(), isCorrect));

      MarkingResult markingResult = MarkingResult.create(request.memberId(), question, isCorrect);
      markingResultRepository.save(markingResult);

      updateQuestionSetLearningStatus(request.memberId(), question.getQuestionSet());
    }

    eventPublisher.publish(
        new MarkingCompletedEvent(request.memberId(), results, request.isReviewing()));

    long correctCount = results.stream().filter(MarkingResultDto::isCorrect).count();

    return MarkQuestionsResponse.of(results, results.size(), (int) correctCount);
  }

  private void updateQuestionSetLearningStatus(Long memberId, QuestionSet questionSet) {
    long correctQuestionCount =
        markingResultRepository.countCorrectByQuestionSetIdAndMemberId(
            questionSet.getId(), memberId);

    if (correctQuestionCount == 0) {
      long attemptedQuestionCount =
          markingResultRepository.countByQuestionSetIdAndMemberId(questionSet.getId(), memberId);
      if (attemptedQuestionCount > 0) {
        questionSet.updateLearningStatus(1);
        questionSetRepository.save(questionSet);
        return;
      }
    }

    questionSet.updateLearningStatus(correctQuestionCount);
    questionSetRepository.save(questionSet);
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
