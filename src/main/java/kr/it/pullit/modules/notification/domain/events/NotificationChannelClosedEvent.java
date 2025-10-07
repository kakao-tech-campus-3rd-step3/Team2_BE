package kr.it.pullit.modules.notification.domain.events;

public record NotificationChannelClosedEvent(Long memberId) {
  public static NotificationChannelClosedEvent of(Long memberId) {
    return new NotificationChannelClosedEvent(memberId);
  }
}
