package kr.it.pullit.modules.projection.learnstats.web.dto;

import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LearnStatsResponse {

  private int totalQuestionSetCount;
  private int totalSolvedQuestionSetCount;
  private long totalSolvedQuestionCount;
  private int weeklySolvedQuestionCount;
  private int consecutiveLearningDays;

  public static LearnStatsResponse of(LearnStats p, int totalQuestionSetCount) {
    if (p == null) {
      return new LearnStatsResponse();
    }
    return LearnStatsResponse.builder()
        .totalQuestionSetCount(totalQuestionSetCount)
        .totalSolvedQuestionSetCount(p.getTotalSolvedQuestionSetCount())
        .totalSolvedQuestionCount(p.getTotalSolvedQuestionCount())
        .weeklySolvedQuestionCount(p.getWeeklySolvedQuestionCount())
        .consecutiveLearningDays(p.getConsecutiveLearningDays())
        .build();
  }
}
