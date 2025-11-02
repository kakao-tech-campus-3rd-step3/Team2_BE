package kr.it.pullit.modules.projection.learnstats.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "learn_stats_daily",
    uniqueConstraints =
        @UniqueConstraint(
            name = "learn_stats_daily_unique_idx",
            columnNames = {"member_id", "activity_date"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearnStatsDaily {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @Column(nullable = false)
  private LocalDate activityDate;

  @Column(nullable = false)
  private int solvedQuestionCount;

  @Column(nullable = false)
  private int solvedQuestionSetCount;

  @Builder(access = AccessLevel.PRIVATE)
  public LearnStatsDaily(Long memberId, LocalDate activityDate) {
    this.memberId = memberId;
    this.activityDate = activityDate;
  }

  public static LearnStatsDaily of(Long memberId, LocalDate activityDate) {
    return LearnStatsDaily.builder().memberId(memberId).activityDate(activityDate).build();
  }

  public void add(int q, int s) {
    if (q > 0) {
      solvedQuestionCount += q;
    }
    if (s > 0) {
      solvedQuestionSetCount += s;
    }
  }
}
