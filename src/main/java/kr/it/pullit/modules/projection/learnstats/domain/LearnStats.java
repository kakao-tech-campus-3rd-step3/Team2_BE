package kr.it.pullit.modules.projection.learnstats.domain;

import static java.time.temporal.ChronoUnit.DAYS;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import kr.it.pullit.modules.projection.learnstats.exception.InvalidSolvedQuestionCountException;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "learn_stats")
public class LearnStats extends BaseEntity {

  @Id
  @Column(nullable = false)
  private Long memberId;

  // 문제집
  @Column(nullable = false)
  private int totalSolvedQuestionSetCount; // 완료한 문제집 수

  // 문제
  @Column(nullable = false)
  private long totalSolvedQuestionCount; // 총 문제 수

  @Column(nullable = false)
  private int weeklySolvedQuestionCount; // 이번 주 푼 문제 수

  // 활동
  @Column(nullable = false)
  private int consecutiveLearningDays; // 연속 학습일

  private LocalDate lastLearningDate; // 마지막 학습일(처음은 null)

  @Builder(access = AccessLevel.PRIVATE)
  public LearnStats(Long memberId) {
    this.memberId = memberId;
  }

  public static LearnStats newOf(Long memberId) {
    return LearnStats.builder().memberId(memberId).build();
  }

  public void onWeeklyReset() {
    this.weeklySolvedQuestionCount = 0;
  }

  public void onQuestionSetSolved(int solvedQuestionCount, LocalDate today) {
    if (solvedQuestionCount <= 0) {
      throw new InvalidSolvedQuestionCountException();
    }
    this.totalSolvedQuestionSetCount++;
    this.totalSolvedQuestionCount += solvedQuestionCount;
    this.weeklySolvedQuestionCount += solvedQuestionCount;
    updateConsecutiveStreak(today);
  }

  public void updateTotalSolvedQuestionCount(long realCount) {
    this.totalSolvedQuestionCount = realCount;
  }

  private void updateConsecutiveStreak(LocalDate today) {
    if (lastLearningDate == null) {
      consecutiveLearningDays = 1;
      lastLearningDate = today;
      return;
    }

    int delta = (int) DAYS.between(lastLearningDate, today);

    if (isSameOrPastDay(delta)) {
      updateLastLearningDateIfNeeded(today);
      return;
    }

    updateConsecutiveDays(delta);
    lastLearningDate = today;
  }

  private void updateConsecutiveDays(int delta) {
    consecutiveLearningDays = (delta == 1) ? consecutiveLearningDays + 1 : 1;
  }

  private void updateLastLearningDateIfNeeded(LocalDate today) {
    if (today.isAfter(lastLearningDate)) {
      lastLearningDate = today;
    }
  }

  private boolean isSameOrPastDay(int delta) {
    return delta <= 0;
  }
}
