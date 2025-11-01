package kr.it.pullit.modules.questionset.event;

import java.util.List;
import kr.it.pullit.modules.notification.api.NotificationEventPublicApi;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsEventPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionSetResponse;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationRequest;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationSpecification;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.service.SourceValidator;
import kr.it.pullit.modules.questionset.service.creationstrategy.QuestionCreationStrategyFactory;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetCreationCompleteResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionGenerationEventHandler {

  private final QuestionPublicApi questionPublicApi;
  private final QuestionSetPublicApi questionSetPublicApi;
  private final NotificationEventPublicApi notificationEventPublicApi;
  private final SourceValidator sourceValidator;
  private final QuestionCreationStrategyFactory questionCreationStrategyFactory;
  private final LearnStatsEventPublicApi learnStatsEventPublicApi;

  @Async("applicationTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleQuestionSetCreatedEvent(QuestionSetCreatedEvent event) {
    log.info("AI 문제 생성을 시작합니다. QuestionSet ID: {}", event.questionSetId());

    try {
      processQuestionGeneration(event);
      handleSuccess(event);
    } catch (Exception e) {
      handleFailure(event, e);
    }
  }

  private void processQuestionGeneration(QuestionSetCreatedEvent event) {
    QuestionGenerationRequest request = createGenerationRequest(event);
    LlmGeneratedQuestionSetResponse response = questionPublicApi.generateQuestions(request);

    questionSetPublicApi.update(
        event.questionSetId(), toQuestionSetUpdateRequestDto(response), event.ownerId());
    saveQuestions(event.questionSetId(), event.ownerId(), response.questions());

    questionSetPublicApi.markAsComplete(event.questionSetId());
  }

  private static QuestionSetUpdateRequestDto toQuestionSetUpdateRequestDto(
      LlmGeneratedQuestionSetResponse response) {
    return QuestionSetUpdateRequestDto.builder().title(response.title()).build();
  }

  private QuestionGenerationRequest createGenerationRequest(QuestionSetCreatedEvent event) {
    QuestionSetResponse questionSetResponse =
        fetchQuestionSetMetadata(event.questionSetId(), event.ownerId());

    sourceValidator.validateSourcesAreReady(
        questionSetResponse.getSourceIds(), event.questionSetId());

    QuestionGenerationSpecification specification = createSpecificationFrom(questionSetResponse);
    return new QuestionGenerationRequest(
        event.ownerId(), event.questionSetId(), questionSetResponse.getSourceIds(), specification);
  }

  private QuestionSetResponse fetchQuestionSetMetadata(Long questionSetId, Long ownerId) {
    return questionSetPublicApi.getQuestionSetWhenHaveNoQuestionsYet(questionSetId, ownerId);
  }

  private QuestionGenerationSpecification createSpecificationFrom(
      QuestionSetResponse questionSetResponse) {
    return new QuestionGenerationSpecification(
        questionSetResponse.getDifficulty(),
        questionSetResponse.getType(),
        questionSetResponse.getQuestionLength());
  }

  private void saveQuestions(
      Long questionSetId, Long memberId, List<LlmGeneratedQuestionResponse> questionDtos) {
    QuestionSet questionSet = findQuestionSetById(questionSetId, memberId);
    questionDtos.forEach(dto -> saveSingleQuestion(questionSet, dto));
  }

  private void saveSingleQuestion(
      QuestionSet questionSet, LlmGeneratedQuestionResponse questionDto) {
    log.info("Generated Question: {}", questionDto.questionText());
    Question question = createQuestion(questionSet, questionDto);
    questionPublicApi.saveQuestion(question);
  }

  private QuestionSet findQuestionSetById(Long questionSetId, Long memberId) {
    return questionSetPublicApi
        .findEntityByIdAndMemberId(questionSetId, memberId)
        .orElseThrow(
            () -> new IllegalArgumentException("QuestionSet not found with id: " + questionSetId));
  }

  private Question createQuestion(
      QuestionSet questionSet, LlmGeneratedQuestionResponse questionDto) {

    return questionCreationStrategyFactory
        .getStrategy(questionSet.getType())
        .create(questionSet, questionDto);
  }

  private void handleSuccess(QuestionSetCreatedEvent event) {
    QuestionSetCreationCompleteResponse responseDto = createSuccessResponse(event);
    publishSuccessNotification(event.ownerId(), responseDto);
    logSuccess(event.questionSetId());
  }

  private QuestionSetCreationCompleteResponse createSuccessResponse(QuestionSetCreatedEvent event) {
    QuestionSetResponse questionSetResponse =
        questionSetPublicApi.getQuestionSetForSolving(
            event.questionSetId(), event.ownerId(), false);
    return new QuestionSetCreationCompleteResponse(true, questionSetResponse.getId(), "문제집 생성 완료");
  }

  private void publishSuccessNotification(
      Long ownerId, QuestionSetCreationCompleteResponse responseDto) {
    notificationEventPublicApi.publishQuestionSetCreationComplete(ownerId, responseDto);
  }

  private void logSuccess(Long questionSetId) {
    log.info("AI 문제 생성이 완료되었습니다. QuestionSet ID: {}", questionSetId);
  }

  private void handleFailure(QuestionSetCreatedEvent event, Exception e) {
    log.error("문제 생성 중 오류 발생. QuestionSet ID: {}", event.questionSetId(), e);
    questionSetPublicApi.markAsFailed(event.questionSetId());
  }
}
