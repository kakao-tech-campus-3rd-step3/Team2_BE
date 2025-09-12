package kr.it.pullit.modules.notification.service;

import java.io.IOException;
import kr.it.pullit.modules.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final EmitterRepository emitterRepository;
  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분
  private static final String EVENT_NAME = "notification";

  public SseEmitter subscribe(Long userId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    emitterRepository.save(userId, emitter);

    emitter.onCompletion(() -> emitterRepository.delete(userId));
    emitter.onTimeout(() -> emitterRepository.delete(userId));
    emitter.onError((e) -> emitterRepository.delete(userId));

    sendToClient(userId, "EventStream Created. userId: " + userId);

    return emitter;
  }

  public void sendToClient(Long userId, Object data) {
    emitterRepository
        .findById(userId)
        .ifPresent(
            emitter -> {
              try {
                emitter.send(SseEmitter.event().name(EVENT_NAME).data(data));
              } catch (IOException e) {
                emitterRepository.delete(userId);
                throw new RuntimeException(e);
              }
            });
  }
}
