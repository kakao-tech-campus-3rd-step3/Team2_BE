package kr.it.pullit.modules.notification.web;

import kr.it.pullit.modules.notification.api.NotificationPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController()
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationPublicApi notificationPublicApi;

  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe() {
    final Long memberId = 1L; // TODO: 인증된 회원 ID로 변경
    return notificationPublicApi.subscribe(memberId);
  }
}
