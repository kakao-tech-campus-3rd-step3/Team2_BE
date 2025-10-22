package kr.it.pullit.modules.projection.outbox.repository;

import kr.it.pullit.modules.projection.outbox.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, Long> {}
