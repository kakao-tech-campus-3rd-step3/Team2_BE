package kr.it.pullit.modules.projection.learnstats.event.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsEventPublicApi;
import kr.it.pullit.modules.projection.learnstats.event.LearnStatsEventType;
import kr.it.pullit.modules.projection.learnstats.event.dto.MemberIdPayload;
import kr.it.pullit.modules.projection.learnstats.event.dto.QuestionSetSolvedPayload;
import kr.it.pullit.modules.projection.outbox.domain.OutboxEvent;
import kr.it.pullit.modules.projection.outbox.publisher.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LearnStatsEventPublisher implements LearnStatsEventPublicApi {

  private final OutboxEventPublisher outboxPublisher;
  private final ObjectMapper objectMapper;

  @SneakyThrows
  @Override
  public void publishWeeklyReset(Long memberId) {
    MemberIdPayload payload = new MemberIdPayload(memberId);
    String jsonPayload = objectMapper.writeValueAsString(payload);

    outboxPublisher.publish(
        OutboxEvent.of(LearnStatsEventType.WEEKLY_RESET.getEventType(), jsonPayload));
  }

  @Override
  @SneakyThrows
  public void publishQuestionSetSolved(Long memberId, int solvedQuestionCount) {
    QuestionSetSolvedPayload payload = new QuestionSetSolvedPayload(memberId, solvedQuestionCount);
    String jsonPayload = objectMapper.writeValueAsString(payload);
    OutboxEvent event =
        OutboxEvent.of(LearnStatsEventType.QUESTION_SET_SOLVED.getEventType(), jsonPayload);
    outboxPublisher.publish(event);
  }
}
