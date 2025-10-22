package kr.it.pullit.modules.projection.learnstats.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.it.pullit.modules.projection.learnstats.events.dto.MemberIdPayload;
import kr.it.pullit.modules.projection.outbox.domain.OutboxEvent;
import kr.it.pullit.modules.projection.outbox.publisher.OutboxEventPublisher;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class WeeklyResetEventPublisherService {

  private final OutboxEventPublisher publisher;
  private final ObjectMapper objectMapper;

  public WeeklyResetEventPublisherService(
      OutboxEventPublisher publisher, ObjectMapper objectMapper) {
    this.publisher = publisher;
    this.objectMapper = objectMapper;
  }

  @SneakyThrows
  public void publishWeeklyReset(Long memberId) {
    MemberIdPayload payload = new MemberIdPayload(memberId);
    String jsonPayload = objectMapper.writeValueAsString(payload);

    publisher.publish(OutboxEvent.of("LEARN_STATS.WEEKLY_RESET", jsonPayload));
  }
}
