package kr.it.pullit.modules.notification.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.function.Consumer;
import kr.it.pullit.modules.notification.domain.events.NotificationChannelClosedEvent;
import kr.it.pullit.shared.event.EventPublisher;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@MockitoUnitTest
@DisplayName("NotificationChannel 단위 테스트")
class NotificationChannelTest {

  private NotificationChannel notificationChannel;
  private final Long memberId = 1L;

  @Mock private SseEmitter mockEmitter;
  @Mock private EventPublisher mockEventPublisher;

  @BeforeEach
  void setUp() {
    notificationChannel = new NotificationChannel(memberId, mockEmitter, mockEventPublisher);
  }

  @Nested
  @DisplayName("이벤트 전송 (send)")
  class Send {
    @Test
    @DisplayName("유효한 이벤트를 전송한다")
    void givenValidEventSendsEvent() throws IOException {
      // given
      EventData event = EventData.of(memberId, "test-event", "test-data");

      // when
      notificationChannel.send(event);

      // then
      verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("이벤트가 null이면 아무것도 하지 않는다")
    void givenNullEventDoesNothing() throws IOException {
      // when
      notificationChannel.send(null);

      // then
      verify(mockEmitter, never()).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("전송 중 IOException 발생 시 에러와 함께 complete한다")
    void givenIoExceptionOccursCompletesWithError() throws IOException {
      // given
      EventData event = EventData.of(memberId, "test-event", "test-data");
      doThrow(IOException.class).when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

      // when
      notificationChannel.send(event);

      // then
      verify(mockEmitter).completeWithError(any(IOException.class));
    }
  }

  @Nested
  @DisplayName("SSE Emitter 콜백")
  class EmitterCallbacks {
    @Test
    @DisplayName("onCompletion 시 채널 종료 이벤트를 발행한다")
    void givenOnCompletionPublishesClosedEvent() {
      // given
      ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
      verify(mockEmitter).onCompletion(captor.capture());
      Runnable onCompletionCallback = captor.getValue();

      // when
      onCompletionCallback.run();

      // then
      verify(mockEventPublisher).publish(NotificationChannelClosedEvent.of(memberId));
    }

    @Test
    @DisplayName("onTimeout 시 채널 종료 이벤트를 발행한다")
    void givenOnTimeoutPublishesClosedEvent() {
      // given
      ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
      verify(mockEmitter).onTimeout(captor.capture());
      Runnable onTimeoutCallback = captor.getValue();

      // when
      onTimeoutCallback.run();

      // then
      verify(mockEventPublisher).publish(NotificationChannelClosedEvent.of(memberId));
    }

    @Test
    @DisplayName("onError 시 채널 종료 이벤트를 발행한다")
    void givenOnErrorPublishesClosedEvent() {
      // given
      ArgumentCaptor<Consumer<Throwable>> captor = ArgumentCaptor.forClass(Consumer.class);
      verify(mockEmitter).onError(captor.capture());
      Consumer<Throwable> onErrorCallback = captor.getValue();

      // when
      onErrorCallback.accept(new RuntimeException("test error"));

      // then
      verify(mockEventPublisher).publish(NotificationChannelClosedEvent.of(memberId));
    }
  }
}
