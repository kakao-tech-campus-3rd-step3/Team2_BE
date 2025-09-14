package kr.it.pullit.modules.notification.service;

import java.io.IOException;
import kr.it.pullit.modules.notification.domain.enums.SseEventType;
import kr.it.pullit.modules.notification.repository.EmitterRepository;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetCreationCompleteResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분
  private final EmitterRepository emitterRepository;

  public SseEmitter subscribe(Long userId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    emitterRepository.save(userId, emitter);

    emitter.onCompletion(() -> emitterRepository.deleteById(userId));
    emitter.onTimeout(() -> emitterRepository.deleteById(userId));
    emitter.onError((e) -> emitterRepository.deleteById(userId));

    publishHandShakeComplete(userId, "EventStream Created. userId: " + userId);

    return emitter;
  }

  public void publishQuestionSetCreationComplete(
      Long userId, QuestionSetCreationCompleteResponseDto data) {
    if (data == null) {
      return;
    }
    sendToMember(userId, SseEventType.QUESTION_SET_CREATION_COMPLETE.code(), data);
  }

  public void publishHandShakeComplete(Long userId, Object data) {
    if (data == null) {
      data = "Handshake Complete";
    }
    sendToMember(userId, SseEventType.HAND_SHAKE_COMPLETE.code(), data);
  }

  private void sendToMember(Long userId, String eventName, Object data) {
    emitterRepository
        .findById(userId)
        .ifPresent(
            emitter -> {
              try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
              } catch (IOException e) {
                emitterRepository.deleteById(userId);
                throw new RuntimeException(e);
              }
            });
  }
}
