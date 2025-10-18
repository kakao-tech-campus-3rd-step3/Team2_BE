package kr.it.pullit.modules.projection.learnStats.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "learn_stats_projection")
public class LearnStatsProjection extends BaseEntity {

  @Id
  @Column(nullable = false)
  private Long memberId;

  private int totalQuestionSetCount;
  private int totalSolvedQuestionSetCount;
  private long totalSolvedQuestionCount;
  private int weeklySolvedQuestionCount;
  private int consecutiveLearningDays;
  private LocalDate lastLearningDate;

  @Builder(access = AccessLevel.PRIVATE)
  public LearnStatsProjection(Long memberId) {
    this.memberId = memberId;
  }

  public static LearnStatsProjection newOf(Long memberId) {
    return LearnStatsProjection.builder()
        .memberId(memberId)
        .build();
  }

  public void onWeeklyReset() {
    weeklySolvedQuestionCount = 0;
  }

  public void onQuestionSetSolved(LocalDate today) {
    totalSolvedQuestionSetCount++;
    weeklySolvedQuestionCount++;
    if (lastLearningDate == null) {
      consecutiveLearningDays = 1;
    } else if (lastLearningDate.equals(today.minusDays(1))) {
      consecutiveLearningDays++;
    } else if (!lastLearningDate.equals(today)) {
      consecutiveLearningDays = 1;
    }
    lastLearningDate = today;
  }

}
