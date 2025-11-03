package kr.it.pullit.modules.projection.learnstats.domain;

import static java.time.temporal.ChronoUnit.DAYS;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
  private long totalSolvedQuestionCount; // 총 풀었던 문제 수

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

  public void recalibrate(
      long totalSolvedQuestionCount,
      int weeklySolvedQuestionCount,
      List<LocalDateTime> completedDates) {
    this.totalSolvedQuestionCount = totalSolvedQuestionCount;
    this.weeklySolvedQuestionCount = weeklySolvedQuestionCount;
    this.consecutiveLearningDays = calculateConsecutiveDaysFrom(completedDates);
    this.lastLearningDate = findLastLearningDateFrom(completedDates);
  }

  private int calculateConsecutiveDaysFrom(List<LocalDateTime> completedDates) {
    if (completedDates == null || completedDates.isEmpty()) {
      return 0;
    }

    List<LocalDateTime> sortedCompletedDates = completedDates.stream().sorted().toList();

    int consecutiveDays = 0;
    LocalDate previousDate = null;

    for (LocalDateTime completedDateTime : sortedCompletedDates) {
      LocalDate currentDate = completedDateTime.toLocalDate();
      consecutiveDays = updateConsecutiveCount(consecutiveDays, previousDate, currentDate);
      previousDate = currentDate;
    }
    return consecutiveDays;
  }

  private int updateConsecutiveCount(
      int currentConsecutiveDays, LocalDate previousDate, LocalDate currentDate) {
    if (previousDate == null) {
      return 1;
    }

    long daysBetween = ChronoUnit.DAYS.between(previousDate, currentDate);

    if (daysBetween == 1) {
      return currentConsecutiveDays + 1;
    }
    if (daysBetween > 1) {
      return 1;
    }
    return currentConsecutiveDays;
  }

  private LocalDate findLastLearningDateFrom(List<LocalDateTime> completedDates) {
    if (completedDates == null || completedDates.isEmpty()) {
      return null;
    }
    return completedDates.stream()
        .map(LocalDateTime::toLocalDate)
        .max(LocalDate::compareTo)
        .orElse(null);
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

  public void resetConsecutiveDaysIfMissed(LocalDate today) {
    if (lastLearningDate != null) {
      long daysBetween = DAYS.between(lastLearningDate, today);
      if (daysBetween > 1) {
        consecutiveLearningDays = 0;
      }
    }
  }
}
