package kr.it.pullit.modules.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kr.it.pullit.modules.notification.domain.EventData;
import kr.it.pullit.modules.notification.domain.NotificationChannel;
import kr.it.pullit.modules.notification.repository.NotificationChannelRepository;
import kr.it.pullit.modules.notification.repository.SseEventCache;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetCreationCompleteResponse;
import kr.it.pullit.support.annotation.SpringUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@SpringUnitTest
@ContextConfiguration(classes = NotificationEventService.class)
@DisplayName("NotificationEventService 단위 테스트")
class NotificationEventServiceTest {

  @Autowired private NotificationEventService notificationEventService;

  @MockitoBean private NotificationChannelRepository notificationChannelRepository;
  @MockitoBean private SseEventCache sseEventCache;
  @MockitoBean private NotificationChannelFactory notificationChannelFactory;

  private NotificationChannel mockChannel;
  private SseEmitter mockEmitter;

  private final Long userId = 1L;

  @BeforeEach
  void setUp() {
    mockChannel = mock(NotificationChannel.class);
    mockEmitter = mock(SseEmitter.class);
  }

  @Nested
  @DisplayName("구독 (subscribe)")
  class Subscribe {

    @Test
    @DisplayName("신규 사용자가 구독하면, 채널을 생성하고 핸드셰이크 이벤트를 전송한다")
    void givenNewUserCreatesChannelAndSendsHandshake() {
      // given
      given(notificationChannelFactory.create(userId)).willReturn(mockChannel);
      given(mockChannel.emitter()).willReturn(mockEmitter);
      given(mockChannel.memberId()).willReturn(userId);
      given(notificationChannelRepository.findById(userId)).willReturn(Optional.of(mockChannel));

      // when
      SseEmitter result = notificationEventService.subscribe(userId, null);

      // then
      assertThat(result).isEqualTo(mockEmitter);
      verify(notificationChannelRepository).save(mockChannel);
      verify(mockChannel, times(1)).send(any(EventData.class)); // 핸드셰이크
      verify(sseEventCache, never()).pollAllByUserIdAfter(anyLong(), anyLong());
    }

    @Test
    @DisplayName("기존 사용자가 재연결하면, 채널 생성 후 놓친 이벤트를 재전송한다")
    void givenReconnectingUserReplaysMissedEvents() {
      // given
      long lastEventId = 10L;
      EventData missedEvent = EventData.of(userId, "missed-event", "data");

      given(notificationChannelFactory.create(userId)).willReturn(mockChannel);
      given(mockChannel.emitter()).willReturn(mockEmitter);
      given(mockChannel.memberId()).willReturn(userId);
      given(notificationChannelRepository.findById(userId)).willReturn(Optional.of(mockChannel));
      given(sseEventCache.pollAllByUserIdAfter(userId, lastEventId))
          .willReturn(List.of(missedEvent));

      // when
      notificationEventService.subscribe(userId, lastEventId);

      // then
      verify(mockChannel, times(2)).send(any(EventData.class)); // 핸드셰이크 + 놓친 이벤트
      verify(sseEventCache).pollAllByUserIdAfter(userId, lastEventId);
    }
  }

  @Nested
  @DisplayName("이벤트 발행 (publish)")
  class Publish {
    @Test
    @DisplayName("사용자 채널이 존재하면, 이벤트를 캐시에 저장하고 채널로 전송한다")
    void givenChannelExistsCachesAndSendsEvent() {
      // given
      QuestionSetCreationCompleteResponse data =
          QuestionSetCreationCompleteResponse.of(true, 1L, "message");
      given(notificationChannelRepository.findById(userId)).willReturn(Optional.of(mockChannel));

      // when
      notificationEventService.publishQuestionSetCreationComplete(userId, data);

      // then
      verify(sseEventCache).put(any(EventData.class));
      verify(mockChannel).send(any(EventData.class));
    }

    @Test
    @DisplayName("사용자 채널이 없으면, 이벤트를 캐시에만 저장한다")
    void givenChannelNotExistsCachesEventOnly() {
      // given
      QuestionSetCreationCompleteResponse data =
          QuestionSetCreationCompleteResponse.of(true, 1L, "message");
      given(notificationChannelRepository.findById(userId)).willReturn(Optional.empty());

      // when
      notificationEventService.publishQuestionSetCreationComplete(userId, data);

      // then
      verify(sseEventCache).put(any(EventData.class));
      verify(mockChannel, never()).send(any(EventData.class));
    }
  }

  @Nested
  @DisplayName("하트비트 전송 (sendHeartbeat)")
  class SendHeartbeat {
    @Test
    @DisplayName("연결된 모든 채널에 하트비트를 전송한다")
    void givenConnectedChannelsSendHeartbeatToAllChannels() {
      // given
      given(notificationChannelRepository.findAll()).willReturn(Map.of(userId, mockChannel));
      given(mockChannel.memberId()).willReturn(userId);

      // when
      notificationEventService.sendHeartbeat();

      // then
      verify(mockChannel).send(any(EventData.class));
    }

    @Test
    @DisplayName("연결된 채널이 없으면 아무것도 하지 않는다")
    void givenNoChannelsDoNothing() {
      // given
      given(notificationChannelRepository.findAll()).willReturn(Collections.emptyMap());

      // when
      notificationEventService.sendHeartbeat();

      // then
      verify(mockChannel, never()).send(any(EventData.class));
    }
  }
}
