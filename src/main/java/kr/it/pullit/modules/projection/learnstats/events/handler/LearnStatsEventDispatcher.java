package kr.it.pullit.modules.projection.learnstats.events.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.it.pullit.modules.projection.learnstats.events.dto.MemberIdPayload;
import kr.it.pullit.modules.projection.learnstats.events.dto.QuestionSetSolvedPayload;
import kr.it.pullit.modules.projection.learnstats.service.LearnStatsProjectionService;
import kr.it.pullit.modules.projection.outbox.domain.OutboxEvent;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class LearnStatsEventDispatcher {

  private final LearnStatsProjectionService projectionService;
  private final ObjectMapper objectMapper;

  public LearnStatsEventDispatcher(
      LearnStatsProjectionService projectionService, ObjectMapper objectMapper) {
    this.projectionService = projectionService;
    this.objectMapper = objectMapper;
  }

  @SneakyThrows
  public boolean dispatch(OutboxEvent e) {
    String type = e.getEventType();
    if (type == null || !type.startsWith("LEARN_STATS.")) {
      return false;
    }
    switch (type) {
      case "LEARN_STATS.WEEKLY_RESET" -> {
        MemberIdPayload payload = objectMapper.readValue(e.getPayload(), MemberIdPayload.class);
        projectionService.applyWeeklyReset(payload.memberId());
      }
      case "LEARN_STATS.QUESTION_SET_SOLVED" -> {
        QuestionSetSolvedPayload payload =
            objectMapper.readValue(e.getPayload(), QuestionSetSolvedPayload.class);
        projectionService.applyQuestionSetSolved(payload.memberId(), payload.solvedQuestionCount());
      }
      case "LEARN_STATS.QUESTION_SET_ASSIGNED" -> {
        MemberIdPayload payload = objectMapper.readValue(e.getPayload(), MemberIdPayload.class);
        projectionService.applyQuestionSetAssigned(payload.memberId());
      }
      default -> {
        return false;
      }
    }
    return true;
  }
}
