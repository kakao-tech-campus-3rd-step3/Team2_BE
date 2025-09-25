package kr.it.pullit.modules.notification.web;

import kr.it.pullit.modules.notification.api.NotificationPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController()
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationPublicApi notificationPublicApi;

  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(
      @RequestHeader(value = "Last-Event-ID", required = false) String headerLastEventId,
      @RequestParam(value = "lastEventId", required = false) String paramLastEventId) {
    final Long memberId = 1L; // TODO: 인증된 회원 ID로 변경

    String lastEventId = headerLastEventId != null ? headerLastEventId : paramLastEventId;

    return notificationPublicApi.subscribe(memberId, lastEventId);
  }
}
