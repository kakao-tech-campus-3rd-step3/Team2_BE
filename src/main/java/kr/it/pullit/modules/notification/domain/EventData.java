package kr.it.pullit.modules.notification.domain;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

public record EventData(long id, Long userId, String name, Object data) {

  private static final AtomicLong eventIdGenerator = new AtomicLong(System.currentTimeMillis());

  public static EventData of(Long userId, String name, Object data) {
    return new EventData(0, userId, name, data);
  }

  public SseEventBuilder toSseEventBuilder() {
    return SseEmitter.event().id(String.valueOf(id)).name(name).data(data);
  }

  public EventData withId(long id) {
    return new EventData(id, this.userId, this.name, this.data);
  }
}
