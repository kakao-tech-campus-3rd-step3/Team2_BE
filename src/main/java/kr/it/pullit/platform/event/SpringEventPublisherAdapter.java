package kr.it.pullit.platform.event;

import kr.it.pullit.shared.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringEventPublisherAdapter implements EventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void publish(Object event) {
    applicationEventPublisher.publishEvent(event);
  }
}
