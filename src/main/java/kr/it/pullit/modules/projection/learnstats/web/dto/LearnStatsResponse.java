package kr.it.pullit.modules.projection.learnstats.web.dto;

import jakarta.validation.constraints.PositiveOrZero;
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

  @PositiveOrZero private int totalQuestionSetCount; // 총 문제집 수

  @PositiveOrZero private int totalSolvedQuestionSetCount; // 완료한 문제집 수

  @PositiveOrZero private long totalSolvedQuestionCount; // 총 푼 문제 수

  @PositiveOrZero private int weeklySolvedQuestionCount; // 이번 주 푼 문제 수

  @PositiveOrZero private int consecutiveLearningDays; // 연속 학습일

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

  public int calculateLearningProgress(long totalQuestionSetCount) {
    if (totalQuestionSetCount == 0) {
      return 0;
    }
    return (int) (((double) totalSolvedQuestionSetCount / totalQuestionSetCount) * 100);
  }
}
