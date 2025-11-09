package kr.it.pullit.modules.questionset.event;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import kr.it.pullit.configuration.RabbitMqConfig;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import kr.it.pullit.support.fixture.QuestionSetFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@MockitoUnitTest
@DisplayName("QuestionGenerationEventHandler 단위 테스트")
class QuestionGenerationEventHandlerTest {

  @InjectMocks private QuestionGenerationEventHandler questionGenerationEventHandler;

  @Mock private RabbitTemplate rabbitTemplate;
  @Mock private QuestionSetPublicApi questionSetPublicApi;

  @Test
  @DisplayName("문제집 생성 이벤트 수신 시, RabbitMQ로 메시지를 성공적으로 발행한다")
  void handleQuestionSetCreatedEvent_Success() {
    // given
    var questionSet = QuestionSetFixtures.basic();
    var event = QuestionSetCreatedEvent.from(questionSet);

    // when
    questionGenerationEventHandler.handleQuestionSetCreatedEvent(event);

    // then
    verify(rabbitTemplate)
        .convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.ROUTING_KEY, event);
  }

  @Test
  @DisplayName("메시지 발행 중 예외 발생 시, 문제집 상태를 FAILED로 변경한다")
  void handleQuestionSetCreatedEvent_Failure() {
    // given
    var questionSet = QuestionSetFixtures.basic();
    var event = QuestionSetCreatedEvent.from(questionSet);

    doThrow(new RuntimeException("MQ Broker connection failed"))
        .when(rabbitTemplate)
        .convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.ROUTING_KEY, event);

    // when
    questionGenerationEventHandler.handleQuestionSetCreatedEvent(event);

    // then
    verify(questionSetPublicApi).markAsFailed(event.questionSetId());
  }
}
