package kr.it.pullit.modules.projection.learnstats.event;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LearnStatsEventType {
  WEEKLY_RESET("LEARN_STATS.WEEKLY_RESET"),
  QUESTION_SET_SOLVED("LEARN_STATS.QUESTION_SET_SOLVED"),
  CORRECT_ANSWER_COUNT_INCREASED("LEARN_STATS.CORRECT_ANSWER_COUNT_INCREASED");

  private final String eventType;

  public static LearnStatsEventType from(String eventType) {
    return Arrays.stream(values())
        .filter(it -> it.eventType.equals(eventType))
        .findFirst()
        .orElse(null);
  }
}
