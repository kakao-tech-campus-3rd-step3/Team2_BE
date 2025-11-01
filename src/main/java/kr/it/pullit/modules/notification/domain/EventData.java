package kr.it.pullit.modules.notification.domain;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

public record EventData(Long id, Long userId, String name, Object data) {

  private static final AtomicLong eventIdGenerator = new AtomicLong(0);

  public static EventData of(Long userId, String name, Object data) {
    return new EventData(eventIdGenerator.getAndIncrement(), userId, name, data);
  }

  public static EventData reConnection(Long userId, Long lastEventId) {
    return new EventData(lastEventId, userId, "reconnection", "connection established");
  }

  public static EventData heartbeat(Long userId) {
    return new EventData(null, userId, "heartbeat" + System.currentTimeMillis(), "ping");
  }

  public SseEventBuilder toSseEventBuilder() {
    if (id == null) {
      return SseEmitter.event().name(name).data(data);
    }

    return SseEmitter.event().id(String.valueOf(id)).name(name).data(data);
  }
}
