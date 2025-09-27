package kr.it.pullit.modules.notification.api;

import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetCreationCompleteResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationPublicApi {
  SseEmitter subscribe(Long userId);

  void publishQuestionSetCreationComplete(Long userId, QuestionSetCreationCompleteResponse data);
}
