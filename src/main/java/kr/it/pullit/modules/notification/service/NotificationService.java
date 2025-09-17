package kr.it.pullit.modules.notification.service;

import java.io.IOException;
import kr.it.pullit.modules.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분
  private static final String EVENT_NAME = "notification";
  private final EmitterRepository emitterRepository;

  public SseEmitter subscribe(Long userId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    emitterRepository.save(userId, emitter);

    emitter.onCompletion(() -> emitterRepository.deleteById(userId));
    emitter.onTimeout(() -> emitterRepository.deleteById(userId));
    emitter.onError((e) -> emitterRepository.deleteById(userId));

    sendToMember(userId, "EventStream Created. userId: " + userId);

    return emitter;
  }

  public void sendToMember(Long userId, Object data) {
    emitterRepository
        .findById(userId)
        .ifPresent(
            emitter -> {
              try {
                emitter.send(SseEmitter.event().name(EVENT_NAME).data(data));
              } catch (IOException e) {
                emitterRepository.deleteById(userId);
                throw new RuntimeException(e);
              }
            });
  }
}
