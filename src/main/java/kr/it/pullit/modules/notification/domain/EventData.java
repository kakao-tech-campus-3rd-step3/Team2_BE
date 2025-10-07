package kr.it.pullit.modules.notification.domain;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public record EventData(long id, String name, Object data) {

  public SseEmitter.SseEventBuilder toSseEventBuilder() {
    return SseEmitter.event().id(String.valueOf(id)).name(name).data(data);
  }
}
