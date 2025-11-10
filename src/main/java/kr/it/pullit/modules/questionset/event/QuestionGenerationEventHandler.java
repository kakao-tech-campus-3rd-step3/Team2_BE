package kr.it.pullit.modules.questionset.event;

import kr.it.pullit.configuration.RabbitMqConfig;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionGenerationEventHandler {

  private final RabbitTemplate rabbitTemplate;
  private final QuestionSetPublicApi questionSetPublicApi;

  @Async("applicationTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleQuestionSetCreatedEvent(QuestionSetCreatedEvent event) {
    log.info("QuestionSet ID: {} 에 대한 AI 문제 생성 요청을 메시지 큐에 발행합니다.", event.questionSetId());

    try {
      rabbitTemplate.convertAndSend(
          RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.ROUTING_KEY, event);
    } catch (Exception e) {
      log.error("문제 생성 요청 메시지 발행에 실패했습니다. QuestionSet ID: {}", event.questionSetId(), e);
      questionSetPublicApi.markAsFailed(event.questionSetId());
    }
  }
}
