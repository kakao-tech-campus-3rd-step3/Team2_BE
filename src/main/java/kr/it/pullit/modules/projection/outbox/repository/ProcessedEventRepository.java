package kr.it.pullit.modules.projection.outbox.repository;

import kr.it.pullit.modules.projection.outbox.domain.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
  boolean existsByEventId(String eventId);
}
