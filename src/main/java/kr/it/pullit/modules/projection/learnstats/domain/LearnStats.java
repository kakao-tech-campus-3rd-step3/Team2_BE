package kr.it.pullit.modules.projection.learnstats.domain;

import static java.time.temporal.ChronoUnit.DAYS;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import kr.it.pullit.modules.projection.learnstats.exception.InvalidSolvedQuestionCountException;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 학습 통계 도메인 */
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
  private long totalQuestionCount; // 전체 문제 수

  @Column(nullable = false)
  private long totalSolvedQuestionCount; // 총 풀었던 문제 수

  @Column(nullable = false)
  private long totalCorrectQuestionCount; // 총 맞은 문제 수

  @Column(nullable = false)
  private int weeklySolvedQuestionCount; // 이번 주 시도한 문제 수

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

  public int calculateLearningProgress() {
    if (this.totalQuestionCount == 0) {
      return 0;
    }
    return (int) (((double) this.totalCorrectQuestionCount / this.totalQuestionCount) * 100);
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
  }

  public void onQuestionSetDeleted(long correctQuestionsInSet) {
    this.totalCorrectQuestionCount =
        Math.max(0, this.totalCorrectQuestionCount - correctQuestionsInSet);
  }

  public void updateTotalQuestionCount(long totalQuestionCount) {
    this.totalQuestionCount = totalQuestionCount;
  }

  public void updateTotalSolvedQuestionCount(long realCount) {
    this.totalSolvedQuestionCount = realCount;
  }

  public void updateTotalCorrectQuestionCount(long totalCorrectQuestionCount) {
    this.totalCorrectQuestionCount = totalCorrectQuestionCount;
  }

  public void recalibrate(
      long totalAttemptedQuestionCount,
      long totalCorrectQuestionCount,
      int weeklySolvedQuestionCount,
      List<LocalDateTime> completedDates,
      LocalDate today) {
    this.totalSolvedQuestionCount = totalAttemptedQuestionCount;
    this.totalCorrectQuestionCount = totalCorrectQuestionCount;
    this.weeklySolvedQuestionCount = weeklySolvedQuestionCount;
    this.consecutiveLearningDays = calculateCurrentStreakFrom(completedDates, today);
    this.lastLearningDate = findLastLearningDateFrom(completedDates);
  }

  private int calculateCurrentStreakFrom(List<LocalDateTime> completedDates, LocalDate today) {
    if (completedDates == null || completedDates.isEmpty()) {
      return 0;
    }

    List<LocalDate> distinctSortedDates =
        completedDates.stream()
            .map(LocalDateTime::toLocalDate)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .toList();

    LocalDate lastDate = distinctSortedDates.get(0);

    if (DAYS.between(lastDate, today) > 1) {
      return 0;
    }

    int consecutiveDays = 1;
    LocalDate previousDate = lastDate;

    for (int i = 1; i < distinctSortedDates.size(); i++) {
      LocalDate currentDate = distinctSortedDates.get(i);
      if (DAYS.between(currentDate, previousDate) == 1) {
        consecutiveDays++;
        previousDate = currentDate;
      } else {
        break;
      }
    }
    return consecutiveDays;
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

  public void resetConsecutiveDaysIfMissed(LocalDate today) {
    if (lastLearningDate != null) {
      long daysBetween = DAYS.between(lastLearningDate, today);
      if (daysBetween > 1) {
        consecutiveLearningDays = 0;
      }
    }
  }
}
