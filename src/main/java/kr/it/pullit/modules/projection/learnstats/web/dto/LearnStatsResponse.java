package kr.it.pullit.modules.projection.learnstats.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 학습 통계 응답 DTO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LearnStatsResponse {

  @Schema(description = "총 문제집 수", example = "15")
  @PositiveOrZero
  private int totalQuestionSetCount; // 총 문제집 수

  @Schema(description = "완료한 문제집 수", example = "10")
  @PositiveOrZero
  private int totalSolvedQuestionSetCount; // 완료한 문제집 수

  @Schema(description = "전체 문제 수", example = "300")
  @PositiveOrZero
  private long totalQuestionCount; // 전체 문제 수

  @Schema(description = "총 시도한 문제 수", example = "250")
  @PositiveOrZero
  private long totalSolvedQuestionCount; // 총 시도한 문제 수

  @Schema(description = "총 맞은 문제 수", example = "200")
  @PositiveOrZero
  private long totalCorrectQuestionCount; // 총 맞은 문제 수

  @Schema(description = "이번 주 시도한 문제 수", example = "50")
  @PositiveOrZero
  private int weeklySolvedQuestionCount; // 이번 주 시도한 문제 수

  @Schema(description = "연속 학습일", example = "7")
  @PositiveOrZero
  private int consecutiveLearningDays; // 연속 학습일

  @Schema(description = "마지막 학습일", example = "2025-11-10")
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
