package kr.it.pullit.modules.notification.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import kr.it.pullit.modules.notification.domain.NotificationChannel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationChannelFactory {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분

  private final ApplicationEventPublisher eventPublisher;

  public NotificationChannel create(Long userId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    return NotificationChannel.create(userId, emitter, eventPublisher);
  }
}
