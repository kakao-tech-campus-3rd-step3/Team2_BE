package kr.it.pullit.modules.notification.service;

import java.io.IOException;
import java.util.Map;
import kr.it.pullit.modules.notification.api.NotificationPublicApi;
import kr.it.pullit.modules.notification.domain.enums.SseEventType;
import kr.it.pullit.modules.notification.repository.EmitterRepository;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetCreationCompleteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationPublicApi {
  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분
  private static final long HEARTBEAT_INTERVAL_MS = 3_000L; // 3초
  private final EmitterRepository emitterRepository;

  @Override
  public SseEmitter subscribe(Long userId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    emitterRepository.save(userId, emitter);
    log.info("New SSE emitter added for user {}: {}", userId, emitter);

    // 연결이 완료되거나, 타임아웃되거나, 에러 발생 시 emitter 제거
    emitter.onCompletion(() -> {
      log.info("SSE emitter completed for user {}", userId);
      emitterRepository.deleteById(userId);
    });
    emitter.onTimeout(() -> {
      log.info("SSE emitter timed out for user {}", userId);
      emitterRepository.deleteById(userId);
    });
    emitter.onError((e) -> {
      log.warn("SSE emitter error for user {}: {}", userId, e.getMessage());
      emitterRepository.deleteById(userId);
    });

    // 연결 직후, 503 Service Unavailable 방지를 위한 더미 데이터 전송
    sendToMember(userId, SseEventType.HAND_SHAKE_COMPLETE.code(),
        "EventStream Created. userId: " + userId);

    return emitter;
  }

  @Override
  public void publishQuestionSetCreationComplete(Long userId,
      QuestionSetCreationCompleteResponse data) {
    if (data == null) {
      return;
    }
    sendToMember(userId, SseEventType.QUESTION_SET_CREATION_COMPLETE.code(), data);
  }

  private void sendToMember(Long userId, String eventName, Object data) {
    emitterRepository.findById(userId).ifPresent(emitter -> {
      SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event().name(eventName).data(data);
      sendEvent(userId, emitter, eventBuilder, eventName);
    });
  }

  // 20초마다 모든 클라이언트에게 heartbeat 메시지 전송
  @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
  public void sendHeartbeat() {
    Map<Long, SseEmitter> emitters = emitterRepository.findAll();
    if (emitters.isEmpty()) {
      return;
    }
    log.info("Sending heartbeat to {} connected SSE clients", emitters.size());
    emitters.forEach((userId, emitter) -> {
      SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event().comment("heartbeat");
      sendEvent(userId, emitter, eventBuilder, "heartbeat");
    });
  }

  /**
   * 지정된 SseEmitter로 이벤트를 전송하고, 실패 시 에러 처리 및 Emitter를 제거
   *
   * @param userId Emitter를 식별하기 위한 사용자 ID
   * @param emitter 이벤트를 전송할 SseEmitter 객체
   * @param eventBuilder 전송할 SSE 이벤트 빌더
   * @param eventDescription 로그에 표시될 이벤트 설명 (예: "heartbeat", "eventName")
   */
  private void sendEvent(Long userId, SseEmitter emitter, SseEmitter.SseEventBuilder eventBuilder,
      String eventDescription) {
    try {
      emitter.send(eventBuilder);
      // 하트비트는 너무 빈번하므로 디버그 로그에서 제외
      if (!"heartbeat".equals(eventDescription)) {
        log.debug("Sent SSE event '{}' to user {}", eventDescription, userId);
      }
    } catch (IOException e) {
      log.warn("Failed to send SSE event '{}' to user {}. Removing emitter. Error: {}",
          eventDescription, userId, e.getMessage());
      // IOEXception은 클라이언트 연결이 끊어진 경우에 주로 발생하므로,
      // 에러 핸들러가 emitter를 제거하도록 completeWithError 호출
      emitter.completeWithError(e);
    }
  }
}
