package kr.it.pullit.modules.projection.outbox.relay;

import java.util.List;
import kr.it.pullit.modules.projection.learnstats.events.handler.LearnStatsEventDispatcher;
import kr.it.pullit.modules.projection.outbox.domain.OutboxEvent;
import kr.it.pullit.modules.projection.outbox.domain.ProcessedEvent;
import kr.it.pullit.modules.projection.outbox.repository.OutboxEventJpaRepository;
import kr.it.pullit.modules.projection.outbox.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventRelay {

  private final OutboxEventJpaRepository outboxEventRepository;
  private final ProcessedEventRepository processedEventRepository;
  private final LearnStatsEventDispatcher dispatcher;
  private final TransactionTemplate transactionTemplate;

  @Scheduled(fixedDelay = 1000)
  public void relayOutboxEvents() {
    List<OutboxEvent> events = outboxEventRepository.findTop100ByOrderByCreatedAtAsc();

    for (OutboxEvent event : events) {
      log.info("아웃박스 이벤트 처리 시도: {}", event.getEventType());

      transactionTemplate.executeWithoutResult(
          status -> {
            // 1. 멱등성 체크: 이미 처리된 이벤트인지 확인
            if (processedEventRepository.existsById(event.getId().toString())) {
              log.warn("이미 처리된 이벤트입니다. Skipping: {}", event.getId());
              outboxEventRepository.delete(event); // 중복이므로 Outbox에서 바로 삭제
              return;
            }

            // 2. 이벤트 처리
            boolean dispatched = dispatcher.dispatch(event);

            // 3. 처리 완료 기록
            if (dispatched) {
              log.info("아웃박스 이벤트 처리 성공 및 삭제: {}", event.getEventType());
              processedEventRepository.save(ProcessedEvent.of(event.getId().toString()));
              outboxEventRepository.delete(event);
            } else {
              log.warn("처리할 수 없는 아웃박스 이벤트 타입입니다: {}", event.getEventType());
            }
          });
    }
  }
}
