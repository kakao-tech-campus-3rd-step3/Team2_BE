package kr.it.pullit.modules.questionset.service;

import java.util.List;
import kr.it.pullit.modules.notification.api.NotificationPublicApi;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationRequest;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationSpecification;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.enums.QuestionSetStatus;
import kr.it.pullit.modules.questionset.domain.event.QuestionSetCreatedEvent;
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
  private final NotificationPublicApi notificationPublicApi;
  private final LlmClient llmClient;

  @Async("llmGeneratorAsyncExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleQuestionSetCreatedEvent(QuestionSetCreatedEvent event) {
    log.info("AI 문제 생성을 시작합니다. QuestionSet ID: {}", event.questionSetId());
    try {
      QuestionSetResponse questionSetResponse =
          questionSetPublicApi.getQuestionSetById(event.questionSetId());

      QuestionSetCreationCompleteResponse responseDto =
          new QuestionSetCreationCompleteResponse(true, questionSetResponse.getId(), "문제집 생성 완료");

      QuestionGenerationRequest questionGenerationRequest =
          new QuestionGenerationRequest(
              event.questionSetId(),
              event.ownerId(),
              questionSetResponse.getSourceIds(),
              new QuestionGenerationSpecification(
                  questionSetResponse.getDifficulty(),
                  questionSetResponse.getType(),
                  questionSetResponse.getQuestionLength()));

      List<LlmGeneratedQuestionDto> questionDtos =
          questionPublicApi.generateQuestions(questionGenerationRequest);

      QuestionSet questionSet =
          questionSetPublicApi
              .findEntityById(event.questionSetId())
              .orElseThrow(
                  () -> {
                    return new IllegalArgumentException(
                        "QuestionSet not found with id: " + event.questionSetId());
                  });

      for (LlmGeneratedQuestionDto questionDto : questionDtos) {
        log.info("Generated Question: {}", questionDto.questionText());
        Question question =
            new Question(
                questionSet,
                questionDto.questionText(),
                questionDto.options(),
                questionDto.answer(),
                questionDto.explanation());
        questionPublicApi.saveQuestion(question);
      }

      questionSetPublicApi.updateStatus(event.questionSetId(), QuestionSetStatus.COMPLETE);
      notificationPublicApi.publishQuestionSetCreationComplete(event.ownerId(), responseDto);
      log.info("AI 문제 생성이 완료되었습니다. QuestionSet ID: {}", event.questionSetId());
    } catch (Exception e) {
      log.error("문제 생성 중 오류 발생. QuestionSet ID: {}", event.questionSetId(), e);
      questionSetPublicApi.updateStatus(event.questionSetId(), QuestionSetStatus.FAILED);
    }
  }
}
