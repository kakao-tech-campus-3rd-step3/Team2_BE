package kr.it.pullit.modules.notification.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import kr.it.pullit.modules.notification.api.NotificationPublicApi;
import kr.it.pullit.modules.notification.domain.EventData;
import kr.it.pullit.modules.notification.domain.NotificationChannel;
import kr.it.pullit.modules.notification.domain.enums.SseEventType;
import kr.it.pullit.modules.notification.repository.NotificationChannelRepository;
import kr.it.pullit.modules.notification.repository.SseEventCache;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetCreationCompleteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 알림을 생성하고 채널을 통해 전송하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationPublicApi {
  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분
  private static final long HEARTBEAT_INTERVAL_MS = 3_000L; // 3초

  private final NotificationChannelRepository notificationChannelRepository;
  private final SseEventCache sseEventCache;
  private final ApplicationEventPublisher eventPublisher;
  private final AtomicLong eventIdGenerator = new AtomicLong(System.currentTimeMillis());

  @Override
  public SseEmitter subscribe(Long userId, String lastEventId) {
    NotificationChannel channel = createAndRegisterChannel(userId);
    handleInitialConnection(channel, lastEventId);
    return channel.emitter();
  }

  @Override
  public void publishQuestionSetCreationComplete(Long userId,
      QuestionSetCreationCompleteResponse data) {
    if (data == null) {
      return;
    }
    publishAndSend(userId, SseEventType.QUESTION_SET_CREATION_COMPLETE, data);
  }

  @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
  public void sendHeartbeat() {
    Map<Long, NotificationChannel> channels = notificationChannelRepository.findAll();
    if (channels.isEmpty()) {
      return;
    }
    log.debug("Sending heartbeat to {} connected SSE clients", channels.size());
    EventData heartbeatEvent =
        new EventData(-1L, "heartbeat", "heartbeat " + System.currentTimeMillis());

    channels.values().forEach(channel -> channel.send(heartbeatEvent));
  }

  private NotificationChannel createAndRegisterChannel(Long userId) {
    SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
    NotificationChannel channel = NotificationChannel.create(userId, sseEmitter, eventPublisher);
    notificationChannelRepository.save(channel);
    log.info("New notification channel created for user {}: {}", userId, channel);
    return channel;
  }

  private void handleInitialConnection(NotificationChannel channel, String lastEventId) {
    try {
      replayMissedEventsIfNecessary(channel.memberId(), lastEventId);
      sendInstantEvent(channel.memberId(), SseEventType.HAND_SHAKE_COMPLETE,
          "EventStream Created. userId: " + channel.memberId());
    } catch (Exception e) {
      channel.completeWithError(e);
    }
  }

  private void publishAndSend(Long userId, SseEventType eventType, Object data) {
    long eventId = eventIdGenerator.getAndIncrement();
    EventData eventData = new EventData(eventId, eventType.code(), data);

    sseEventCache.put(userId, eventData);

    notificationChannelRepository.findById(userId).ifPresent(channel -> channel.send(eventData));
  }

  private void sendInstantEvent(Long userId, SseEventType eventType, Object data) {
    long eventId = eventIdGenerator.getAndIncrement();
    EventData eventData = new EventData(eventId, eventType.code(), data);

    notificationChannelRepository.findById(userId).ifPresent(channel -> channel.send(eventData));
  }

  private void replayMissedEventsIfNecessary(Long userId, String lastEventId) {
    if (lastEventId == null || lastEventId.isEmpty()) {
      return;
    }

    log.info("Reconnecting user {} with lastEventId: {}", userId, lastEventId);
    try {
      long lastId = Long.parseLong(lastEventId);
      List<EventData> missedEvents = sseEventCache.findAllByUserIdAfter(userId, lastId);

      if (!missedEvents.isEmpty()) {
        log.info("Replaying {} missed events for user {}", missedEvents.size(), userId);
        notificationChannelRepository.findById(userId)
            .ifPresent(channel -> missedEvents.forEach(event -> {
              channel.send(event);
              log.debug("Replayed event ID {} for user {}", event.id(), userId);
            }));
      }
    } catch (NumberFormatException e) {
      log.warn("Invalid lastEventId format: {} for user {}", lastEventId, userId);
    }
  }
}
