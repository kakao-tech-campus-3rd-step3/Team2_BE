package kr.it.pullit.modules.projection.learnstats.web.dto;

import java.time.LocalDate;
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

  @PositiveOrZero private long totalQuestionCount; // 전체 문제 수
  @PositiveOrZero private long totalSolvedQuestionCount; // 총 시도한 문제 수
  @PositiveOrZero private long totalCorrectQuestionCount; // 총 맞은 문제 수

  @PositiveOrZero private int weeklySolvedQuestionCount; // 이번 주 시도한 문제 수

  @PositiveOrZero private int consecutiveLearningDays; // 연속 학습일

  private LocalDate lastLearningDate; // 마지막 학습일(처음은 null)

  public static LearnStatsResponse of(LearnStats p, int totalQuestionSetCount) {
    if (p == null) {
      return new LearnStatsResponse();
    }
    return LearnStatsResponse.builder()
        .totalQuestionSetCount(totalQuestionSetCount)
        .totalSolvedQuestionSetCount(p.getTotalSolvedQuestionSetCount())
        .totalQuestionCount(p.getTotalQuestionCount())
        .totalSolvedQuestionCount(p.getTotalSolvedQuestionCount())
        .totalCorrectQuestionCount(p.getTotalCorrectQuestionCount())
        .weeklySolvedQuestionCount(p.getWeeklySolvedQuestionCount())
        .consecutiveLearningDays(p.getConsecutiveLearningDays())
        .lastLearningDate(p.getLastLearningDate())
        .build();
  }

  public int calculateLearningProgress(long totalQuestionSetCount) {
    if (totalQuestionSetCount == 0) {
      return 0;
    }
    return (int) (((double) totalSolvedQuestionSetCount / totalQuestionSetCount) * 100);
  }
}
