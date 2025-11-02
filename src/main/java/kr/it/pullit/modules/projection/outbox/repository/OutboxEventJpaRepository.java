package kr.it.pullit.modules.projection.outbox.repository;

import java.util.List;
import kr.it.pullit.modules.projection.outbox.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, Long> {

  // claim 쿼리: PENDING -> SENDING (UPDATE ... ORDER BY id LIMIT N)
  @Modifying
  @Query(
      value =
          """
      UPDATE outbox_event
      SET status='SENDING', worker_id=:workerId, picked_at=NOW()
      WHERE status='PENDING'
      ORDER BY id
      LIMIT :limit
      """,
      nativeQuery = true)
  int claimPendingBatch(@Param("workerId") String workerId, @Param("limit") int limit);

  // 내가 집어간 것만 읽기
  @Query(
      """
      SELECT e
      FROM OutboxEvent e
      WHERE e.status = 'SENDING'
      AND e.workerId = :workerId
      ORDER BY e.id ASC
      """)
  List<OutboxEvent> findSendingByWorker(@Param("workerId") String workerId);

  List<OutboxEvent> findTop100ByOrderByCreatedAtAsc();
}
