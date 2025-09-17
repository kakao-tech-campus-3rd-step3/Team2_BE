package kr.it.pullit.modules.notification.web;

import kr.it.pullit.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController()
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping(value = "/sub", produces = "text/event-stream")
  public SseEmitter subscribe() {
    final Long memberId = 1L; // TODO: 인증된 회원 ID로 변경
    return notificationService.subscribe(memberId);
  }
}
