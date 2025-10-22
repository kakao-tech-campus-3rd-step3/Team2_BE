package kr.it.pullit.modules.projection.outbox.publisher;

import kr.it.pullit.modules.projection.outbox.domain.OutboxEvent;
import kr.it.pullit.modules.projection.outbox.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

  private final OutboxEventJpaRepository repository;

  @Transactional
  public void publish(OutboxEvent event) {
    repository.save(event);
  }
}
