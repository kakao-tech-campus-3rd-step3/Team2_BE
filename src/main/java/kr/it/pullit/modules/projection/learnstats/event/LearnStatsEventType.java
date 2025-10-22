package kr.it.pullit.modules.projection.learnstats.event;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LearnStatsEventType {
  WEEKLY_RESET("WEEKLY_RESET"),
  QUESTION_SET_SOLVED("QUESTION_SET_SOLVED"),
  QUESTION_SET_ASSIGNED("QUESTION_SET_ASSIGNED");

  private final String eventType;

  public static LearnStatsEventType from(String eventType) {
    return Arrays.stream(values())
        .filter(it -> it.eventType.equals(eventType))
        .findFirst()
        .orElse(null);
  }
}
