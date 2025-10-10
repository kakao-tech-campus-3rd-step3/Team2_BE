package kr.it.pullit.modules.notification.listener;

import kr.it.pullit.modules.notification.domain.events.NotificationChannelClosedEvent;
import kr.it.pullit.modules.notification.repository.NotificationChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationChannelRepository notificationChannelRepository;

  @EventListener
  public void handleNotificationChannelClosed(NotificationChannelClosedEvent event) {
    Long memberId = event.memberId();
    log.info(
        "NotificationChannelClosedEvent received for member {}. Removing channel from repository.",
        memberId);
    notificationChannelRepository.deleteById(memberId);
  }
}
