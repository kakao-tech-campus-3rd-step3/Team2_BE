package kr.it.pullit.modules.projection.learnstats.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import kr.it.pullit.support.annotation.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
@DisplayName("LearnStatsDaily 단위 테스트")
class LearnStatsDailyTest {

  @Test
  @DisplayName("add: 문제와 문제집 풀이 수가 누적된다")
  void add() {
    // given
    LearnStatsDaily dailyStat = LearnStatsDaily.of(1L, LocalDate.of(2025, 1, 1));

    // when
    dailyStat.add(5, 2);

    // then
    assertThat(dailyStat.getSolvedQuestionCount()).isEqualTo(5);
    assertThat(dailyStat.getSolvedQuestionSetCount()).isEqualTo(2);

    // when
    dailyStat.add(3, 1);

    // then
    assertThat(dailyStat.getSolvedQuestionCount()).isEqualTo(8);
    assertThat(dailyStat.getSolvedQuestionSetCount()).isEqualTo(3);
  }
}
