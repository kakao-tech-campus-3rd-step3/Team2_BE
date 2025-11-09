package kr.it.pullit.modules.questionset.event;

import java.util.List;
import kr.it.pullit.configuration.RabbitMqConfig;
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
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("worker")
@RequiredArgsConstructor
public class QuestionGenerationWorker {

  private final RabbitTemplate rabbitTemplate;
  private final QuestionPublicApi questionPublicApi;
  private final QuestionSetPublicApi questionSetPublicApi;
  private final SourceValidator sourceValidator;
  private final QuestionCreationStrategyFactory questionCreationStrategyFactory;
  private final QuestionGenerationFailureHandler failureHandler;

  @RabbitListener(queues = RabbitMqConfig.QUEUE_NAME)
  public void handleQuestionGenerationRequest(QuestionSetCreatedEvent event) {
    log.info("AI 문제 생성을 시작합니다. QuestionSet ID: {}", event.questionSetId());

    try {
      processQuestionGeneration(event);
      handleSuccess(event);
    } catch (Exception e) {
      failureHandler.handle(event, e);
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
    log.info("AI 문제 생성이 완료되었습니다. QuestionSet ID: {}", event.questionSetId());
    QuestionSetCompletionEvent completionEvent =
        new QuestionSetCompletionEvent(event.questionSetId(), event.ownerId());
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.COMPLETION_EXCHANGE_NAME,
        RabbitMqConfig.COMPLETION_ROUTING_KEY,
        completionEvent);
  }
}
