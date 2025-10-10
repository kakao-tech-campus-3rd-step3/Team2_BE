package kr.it.pullit.modules.notification.service;

import java.util.List;
import java.util.Map;
import kr.it.pullit.modules.notification.api.NotificationPublicApi;
import kr.it.pullit.modules.notification.domain.EventData;
import kr.it.pullit.modules.notification.domain.NotificationChannel;
import kr.it.pullit.modules.notification.domain.enums.SseEventType;
import kr.it.pullit.modules.notification.repository.NotificationChannelRepository;
import kr.it.pullit.modules.notification.repository.SseEventCache;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetCreationCompleteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 알림을 생성하고 채널을 통해 전송하는 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationPublicApi {
  private static final long HEARTBEAT_INTERVAL_MS = 3_000L; // 3초

  private final NotificationChannelRepository notificationChannelRepository;
  private final SseEventCache sseEventCache;
  private final NotificationChannelFactory notificationChannelFactory;

  @Override
  public SseEmitter subscribe(Long userId, String lastEventId) {
    NotificationChannel channel = createAndRegisterChannel(userId);
    handleInitialConnection(channel, lastEventId);
    return channel.emitter();
  }

  @Override
  public void publishQuestionSetCreationComplete(
      Long userId, QuestionSetCreationCompleteResponse data) {
    if (data == null) {
      return;
    }
    publishAndSend(userId, SseEventType.QUESTION_SET_CREATION_COMPLETE, data);
  }

  @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
  public void sendHeartbeat() {
    Map<Long, NotificationChannel> channels = notificationChannelRepository.findAll();
    if (isChannelsEmpty(channels)) {
      return;
    }
    log.debug("Sending heartbeat to {} connected SSE clients", channels.size());
    EventData heartbeatEvent = EventData.of("heartbeat", "heartbeat " + System.currentTimeMillis());
    channels.values().forEach(channel -> channel.send(heartbeatEvent));
  }

  private static boolean isChannelsEmpty(Map<Long, NotificationChannel> channels) {
    return channels.isEmpty();
  }

  private NotificationChannel createAndRegisterChannel(Long userId) {
    NotificationChannel channel = notificationChannelFactory.create(userId);
    notificationChannelRepository.save(channel);
    log.info("New notification channel created for user {}: {}", userId, channel);
    return channel;
  }

  private void handleInitialConnection(NotificationChannel channel, String lastEventId) {
    try {
      replayMissedEventsIfNecessary(channel.memberId(), lastEventId);
      sendInstantEvent(
          channel.memberId(),
          SseEventType.HAND_SHAKE_COMPLETE,
          "EventStream Created. userId: " + channel.memberId());
    } catch (Exception e) {
      channel.completeWithError(e);
    }
  }

  private void publishAndSend(Long userId, SseEventType eventType, Object data) {
    EventData eventData = EventData.of(eventType.code(), data);
    sseEventCache.put(userId, eventData);
    notificationChannelRepository.findById(userId).ifPresent(channel -> channel.send(eventData));
  }

  private void sendInstantEvent(Long userId, SseEventType eventType, Object data) {
    EventData eventData = EventData.of(eventType.code(), data);
    notificationChannelRepository.findById(userId).ifPresent(channel -> channel.send(eventData));
  }

  private void replayMissedEventsIfNecessary(Long userId, String lastEventId) {
    if (isFirstConnection(lastEventId)) {
      return;
    }
    handleReconnection(userId, lastEventId);
  }

  private static boolean isFirstConnection(String lastEventId) {
    return lastEventId == null || lastEventId.isEmpty();
  }

  private void handleReconnection(Long userId, String lastEventId) {
    log.info("Reconnecting user {} with lastEventId: {}", userId, lastEventId);
    doReplayMissedEvents(userId, lastEventId);
  }

  private void doReplayMissedEvents(Long userId, String lastEventId) {
    try {
      long lastId = Long.parseLong(lastEventId);
      List<EventData> missedEvents = sseEventCache.findAllByUserIdAfter(userId, lastId);
      replayFoundEvents(userId, missedEvents);
    } catch (NumberFormatException e) {
      log.warn("Invalid lastEventId format: {} for user {}", lastEventId, userId);
    }
  }

  private void replayFoundEvents(Long userId, List<EventData> missedEvents) {
    if (isMissedEventsEmpty(missedEvents)) {
      return;
    }
    logAndSendMissedEvents(userId, missedEvents);
  }

  private static boolean isMissedEventsEmpty(List<EventData> missedEvents) {
    return missedEvents.isEmpty();
  }

  private void logAndSendMissedEvents(Long userId, List<EventData> missedEvents) {
    log.info("Replaying {} missed events for user {}", missedEvents.size(), userId);
    notificationChannelRepository
        .findById(userId)
        .ifPresent(channel -> sendAllEventsToChannel(channel, missedEvents));
  }

  private void sendAllEventsToChannel(NotificationChannel channel, List<EventData> events) {
    events.forEach(
        event -> {
          channel.send(event);
          log.debug("Replayed event ID {} for user {}", event.id(), channel.memberId());
        });
  }
}
