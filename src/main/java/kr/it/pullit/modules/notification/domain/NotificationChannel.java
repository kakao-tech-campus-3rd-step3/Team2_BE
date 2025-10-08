package kr.it.pullit.modules.notification.domain;

import java.io.IOException;
import kr.it.pullit.modules.notification.domain.events.NotificationChannelClosedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
public record NotificationChannel(
    Long memberId, SseEmitter emitter, ApplicationEventPublisher eventPublisher) {

  public NotificationChannel(
      Long memberId, SseEmitter emitter, ApplicationEventPublisher eventPublisher) {
    this.memberId = memberId;
    this.emitter = emitter;
    this.eventPublisher = eventPublisher;
    setupCallbacks();
  }

  public static NotificationChannel create(
      Long userId, SseEmitter emitter, ApplicationEventPublisher eventPublisher) {
    return new NotificationChannel(userId, emitter, eventPublisher);
  }

  public void send(EventData event) {
    if (event == null) {
      return;
    }
    executeSend(() -> emitter.send(event.toSseEventBuilder()), event);
  }

  public void completeWithError(Throwable throwable) {
    log.error(
        "SSE connection error for user {}: {}. Completing emitter.",
        memberId,
        throwable.getMessage());
    emitter.completeWithError(throwable);
  }

  @FunctionalInterface
  private interface SendAction {
    void execute() throws IOException;
  }

  private void executeSend(SendAction sendAction, EventData event) {
    try {
      sendAction.execute();
      logOnSuccess(event);
    } catch (IOException | IllegalStateException e) {
      log.warn(
          "Failed to send SSE event '{}' to user {}. Completing emitter. Error: {}",
          event.name(),
          memberId,
          e.getMessage());
      emitter.completeWithError(e);
    }
  }

  private void logOnSuccess(EventData event) {
    if (!isHeartbeat(event)) {
      log.debug("Sent SSE event '{}' to user {}", event.name(), memberId);
    }
  }

  private boolean isHeartbeat(EventData event) {
    return "heartbeat".equals(event.name());
  }

  private void setupCallbacks() {
    emitter.onCompletion(this::publishClosedEvent);
    emitter.onTimeout(this::publishClosedEvent);
    emitter.onError(this::publishErrorEvent);
  }

  private void publishClosedEvent() {
    log.info(
        "SSE emitter for user {} is closing (completed or timed out). Publishing event.", memberId);
    eventPublisher.publishEvent(NotificationChannelClosedEvent.of(memberId));
  }

  private void publishErrorEvent(Throwable e) {
    log.warn("SSE emitter error for user {}: {}. Publishing event.", memberId, e.getMessage());
    eventPublisher.publishEvent(NotificationChannelClosedEvent.of(memberId));
  }
}
