package kr.it.pullit.modules.notification.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.it.pullit.modules.notification.api.NotificationEventPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Notification API", description = "알림 관련 API (SSE)")
@RestController()
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationEventPublicApi notificationEventPublicApi;

  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(
      @AuthenticationPrincipal Long memberId,
      @RequestHeader(value = "Last-Event-ID", required = false) String headerLastEventId,
      @RequestParam(value = "lastEventId", required = false) String paramLastEventId) {

    String lastEventId = headerLastEventId != null ? headerLastEventId : paramLastEventId;

    return notificationEventPublicApi.subscribe(memberId, lastEventId);
  }
}
