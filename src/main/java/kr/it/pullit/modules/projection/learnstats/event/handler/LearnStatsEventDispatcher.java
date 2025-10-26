package kr.it.pullit.modules.projection.learnstats.event.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.it.pullit.modules.projection.learnstats.event.LearnStatsEventType;
import kr.it.pullit.modules.projection.learnstats.event.dto.MemberIdPayload;
import kr.it.pullit.modules.projection.learnstats.event.dto.QuestionSetSolvedPayload;
import kr.it.pullit.modules.projection.learnstats.service.LearnStatsService;
import kr.it.pullit.modules.projection.outbox.domain.OutboxEvent;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class LearnStatsEventDispatcher {

  private final LearnStatsService projectionService;
  private final ObjectMapper objectMapper;

  public LearnStatsEventDispatcher(LearnStatsService projectionService, ObjectMapper objectMapper) {
    this.projectionService = projectionService;
    this.objectMapper = objectMapper;
  }

  @SneakyThrows
  public boolean dispatch(OutboxEvent e) {
    LearnStatsEventType eventType = LearnStatsEventType.from(e.getEventType());
    if (eventType == null) {
      return false;
    }

    switch (eventType) {
      case WEEKLY_RESET -> {
        MemberIdPayload payload = objectMapper.readValue(e.getPayload(), MemberIdPayload.class);
        projectionService.applyWeeklyReset(payload.memberId());
      }
      case QUESTION_SET_SOLVED -> {
        QuestionSetSolvedPayload payload =
            objectMapper.readValue(e.getPayload(), QuestionSetSolvedPayload.class);
        projectionService.applyQuestionSetSolved(payload.memberId(), payload.solvedQuestionCount());
      }
      default -> {
        return false;
      }
    }
    return true;
  }
}
