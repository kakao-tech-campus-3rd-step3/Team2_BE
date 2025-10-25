package kr.it.pullit.modules.projection.learnstats.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import kr.it.pullit.modules.projection.learnstats.exception.InvalidSolvedQuestionCountException;
import kr.it.pullit.support.annotation.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@UnitTest
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
  }
}
