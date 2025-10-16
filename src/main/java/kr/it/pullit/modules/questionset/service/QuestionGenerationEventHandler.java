package kr.it.pullit.modules.questionset.service;

import java.util.List;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.event.SourceExtractionCompleteEvent;
import kr.it.pullit.modules.learningsource.source.event.SourceExtractionStartEvent;
import kr.it.pullit.modules.learningsource.source.repository.SourceRepository;
import kr.it.pullit.modules.notification.api.NotificationPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionSetResponse;
import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationRequest;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationSpecification;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.entity.ShortAnswerQuestion;
import kr.it.pullit.modules.questionset.domain.entity.TrueFalseQuestion;
import kr.it.pullit.modules.questionset.domain.event.QuestionSetCreatedEvent;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetCreationCompleteResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionGenerationEventHandler {

  private final SourceRepository sourceRepository;
  private final QuestionPublicApi questionPublicApi;
  private final QuestionSetPublicApi questionSetPublicApi;
  private final NotificationPublicApi notificationPublicApi;
  private final SourcePublicApi sourcePublicApi;

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

  @TransactionalEventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleSourceExtractionStart(final SourceExtractionStartEvent event) {
    sourceRepository
        .findById(event.sourceId())
        .ifPresent(
            source -> {
              source.startProcessing();
              log.info("Source[id={}] status updated to PROCESSING.", source.getId());
            });
  }

  @TransactionalEventListener
  public void handleSourceExtractionComplete(final SourceExtractionCompleteEvent event) {
    sourceRepository
        .findById(event.sourceId())
        .ifPresent(
            source -> {
              source.markAsReady();
              log.info("Source[id={}] status updated to READY.", source.getId());
            });
  }

  private void processQuestionGeneration(QuestionSetCreatedEvent event) {
    QuestionGenerationRequest request = createGenerationRequest(event);
    LlmGeneratedQuestionSetResponse response = questionPublicApi.generateQuestions(request);

    questionSetPublicApi.updateTitle(event.questionSetId(), response.title());
    saveQuestions(event.questionSetId(), event.ownerId(), response.questions());

    questionSetPublicApi.markAsComplete(event.questionSetId());
  }

  private QuestionGenerationRequest createGenerationRequest(QuestionSetCreatedEvent event) {
    QuestionSetResponse questionSetResponse =
        fetchQuestionSetMetadata(event.questionSetId(), event.ownerId());

    validateSourcesAreReady(questionSetResponse.getSourceIds(), event.questionSetId());

    QuestionGenerationSpecification specification = createSpecificationFrom(questionSetResponse);
    return new QuestionGenerationRequest(
        event.ownerId(), event.questionSetId(), questionSetResponse.getSourceIds(), specification);
  }

  // TODO: 과연 서비스에 있을 로직이 맞는지? 리팩토링 대상.
  private void validateSourcesAreReady(List<Long> sourceIds, Long questionSetId) {
    if (sourceIds.isEmpty()) {
      return;
    }

    List<Source> sources = sourcePublicApi.findByIdIn(sourceIds);
    if (sources.size() != sourceIds.size()) {
      log.warn("요청된 소스 ID 중 일부를 DB에서 찾을 수 없습니다. QuestionSet ID: {}", questionSetId);
    }

    List<Source> notReadySources =
        sources.stream().filter(source -> source.getStatus() != SourceStatus.READY).toList();

    if (!notReadySources.isEmpty()) {
      for (Source source : notReadySources) {
        log.error(
            "소스 파일이 준비되지 않았습니다. Source ID: {}, Status: {}", source.getId(), source.getStatus());
      }
      throw new IllegalStateException(
          "모든 소스 파일이 준비되지 않아 문제 생성을 시작할 수 없습니다. QuestionSet ID: " + questionSetId);
    }
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

    return switch (questionSet.getType()) {
      case MULTIPLE_CHOICE -> MultipleChoiceQuestion.createFromLlm(questionSet, questionDto);
      case TRUE_FALSE -> TrueFalseQuestion.createFromLlm(questionSet, questionDto);
      case SHORT_ANSWER -> ShortAnswerQuestion.createFromLlm(questionSet, questionDto);
      // TODO: 나중에 예외처리 추가.
      default ->
          throw new IllegalStateException("Unsupported question type: " + questionSet.getType());
    };
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
    notificationPublicApi.publishQuestionSetCreationComplete(ownerId, responseDto);
  }

  private void logSuccess(Long questionSetId) {
    log.info("AI 문제 생성이 완료되었습니다. QuestionSet ID: {}", questionSetId);
  }

  private void handleFailure(QuestionSetCreatedEvent event, Exception e) {
    log.error("문제 생성 중 오류 발생. QuestionSet ID: {}", event.questionSetId(), e);
    questionSetPublicApi.markAsFailed(event.questionSetId());
  }
}
