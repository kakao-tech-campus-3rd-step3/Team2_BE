package kr.it.pullit.modules.projection.learnstats.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kr.it.pullit.modules.projection.learnstats.exception.InvalidSolvedQuestionCountException;
import kr.it.pullit.support.annotation.SpringUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@SpringUnitTest
@DisplayName("LearnStats 단위 테스트")
class LearnStatsTest {

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private Clock clock;

  private LearnStats projection;
  private LocalDate today;

  @BeforeEach
  void setUp() {
    projection = LearnStats.newOf(1L);
    today = LocalDate.now(clock);
  }

  @Test
  @DisplayName("newOf: 모든 통계치가 초기화된 객체를 생성한다")
  void newOf() {
    assertThat(projection.getMemberId()).isEqualTo(1L);
    assertThat(projection.getTotalSolvedQuestionSetCount()).isZero();
    assertThat(projection.getTotalSolvedQuestionCount()).isZero();
  }

  @Test
  @DisplayName("onWeeklyReset: 주간 통계를 초기화한다")
  void onWeeklyReset() {
    projection.onQuestionSetSolved(10, today);
    projection.onWeeklyReset();
    assertThat(projection.getWeeklySolvedQuestionCount()).isZero();
  }

  @Nested
  @DisplayName("onQuestionSetSolved 호출 시")
  class OnQuestionSetSolved {

    @Test
    @DisplayName("문제집 풀이 통계와 연속 학습일이 업데이트된다")
    void updatesStatsCorrectly() {
      projection.onQuestionSetSolved(15, today);

      assertThat(projection.getTotalSolvedQuestionSetCount()).isEqualTo(1);
      assertThat(projection.getTotalSolvedQuestionCount()).isEqualTo(15);
      assertThat(projection.getWeeklySolvedQuestionCount()).isEqualTo(15);
      assertThat(projection.getConsecutiveLearningDays()).isEqualTo(1);
      assertThat(projection.getLastLearningDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("0 이하의 문제 수로 호출하면 예외가 발생한다")
    void withZeroOrNegativeQuestions_throwsException() {
      assertThatThrownBy(() -> projection.onQuestionSetSolved(0, today))
          .isInstanceOf(InvalidSolvedQuestionCountException.class);
      assertThatThrownBy(() -> projection.onQuestionSetSolved(-1, today))
          .isInstanceOf(InvalidSolvedQuestionCountException.class);
    }

    @Test
    @DisplayName("같은 날 여러번 풀어도 연속 학습일은 1을 유지한다")
    void withMultipleSolvesOnSameDay_maintainsConsecutiveDays() {
      // when
      projection.onQuestionSetSolved(10, today);
      projection.onQuestionSetSolved(5, today);

      // then
      assertThat(projection.getConsecutiveLearningDays()).isEqualTo(1);
      assertThat(projection.getTotalSolvedQuestionSetCount()).isEqualTo(2);
      assertThat(projection.getTotalSolvedQuestionCount()).isEqualTo(15);
    }

    @Test
    @DisplayName("다음 날 풀면 연속 학습일이 증가한다")
    void withSolveOnNextDay_incrementsConsecutiveDays() {
      // given
      projection.onQuestionSetSolved(10, today);

      // when
      projection.onQuestionSetSolved(5, today.plusDays(1));

      // then
      assertThat(projection.getConsecutiveLearningDays()).isEqualTo(2);
    }

    @Test
    @DisplayName("이틀 이상 뒤에 풀면 연속 학습일이 1로 초기화된다")
    void withSolveAfterGap_resetsConsecutiveDays() {
      // given
      projection.onQuestionSetSolved(10, today);

      // when
      projection.onQuestionSetSolved(5, today.plusDays(3));

      // then
      assertThat(projection.getConsecutiveLearningDays()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("recalibrate 호출 시")
  class Recalibrate {

    @Test
    @DisplayName("모든 학습 통계가 전달된 값으로 재보정된다")
    void shouldRecalibrateAllLearnStats() {
      // given
      List<LocalDateTime> completedDates =
          List.of(
              today.minusDays(2).atStartOfDay(), // 2일 전
              today.minusDays(1).atStartOfDay(), // 1일 전 (연속)
              today.plusDays(1).atStartOfDay() // 다음 날 (1일 단절 후 다시 시작)
              );

      // when
      projection.recalibrate(100L, 20, completedDates);

      // then
      assertThat(projection.getTotalSolvedQuestionCount()).isEqualTo(100L);
      assertThat(projection.getWeeklySolvedQuestionCount()).isEqualTo(20);
      assertThat(projection.getConsecutiveLearningDays()).isEqualTo(1);
      assertThat(projection.getLastLearningDate()).isEqualTo(today.plusDays(1));
    }

    @Test
    @DisplayName("학습 기록이 없으면 연속 학습일과 마지막 학습일이 초기화된다")
    void shouldResetConsecutiveStatsWhenHistoryIsEmpty() {
      // given
      projection.onQuestionSetSolved(10, today); // 기존 기록 생성

      // when
      projection.recalibrate(10L, 5, List.of());

      // then
      assertThat(projection.getConsecutiveLearningDays()).isZero();
      assertThat(projection.getLastLearningDate()).isNull();
    }
  }
}
