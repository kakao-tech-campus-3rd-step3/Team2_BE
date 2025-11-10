package kr.it.pullit.modules.projection.learnstats.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsFacade;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsPublicApi;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.modules.projection.learnstats.web.dto.LearnStatsResponse;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.support.annotation.SpringUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringUnitTest
@ContextConfiguration(classes = LearnStatsFacadeImpl.class)
@DisplayName("LearnStatsFacadeImpl 단위 테스트")
class LearnStatsFacadeImplTest {

  @Autowired private LearnStatsFacade learnStatsFacade;

  @MockitoBean private LearnStatsPublicApi learnStatsPublicApi;
  @MockitoBean private QuestionSetPublicApi questionSetPublicApi;

  @Nested
  @DisplayName("학습 통계 조회 (getLearnStats)")
  class GetLearnStats {

    private final Long memberId = 1L;

    @Test
    @DisplayName("기존 학습 통계가 존재하면, 해당 통계와 문제집 수를 조합하여 반환한다")
    void givenExistingLearnStatsReturnsCombinedStats() {
      // given
      LearnStats existingStats = LearnStats.newOf(memberId);
      existingStats.updateTotalCorrectQuestionCount(10); // 임의의 통계 데이터 추가

      long totalQuestionSetCount = 20L;

      given(learnStatsPublicApi.getLearnStats(memberId)).willReturn(Optional.of(existingStats));
      given(questionSetPublicApi.countByMemberId(memberId)).willReturn(totalQuestionSetCount);

      // when
      LearnStatsResponse response = learnStatsFacade.getLearnStats(memberId);

      // then
      assertThat(response.getTotalCorrectQuestionCount()).isEqualTo(10);
      assertThat(response.getTotalQuestionSetCount()).isEqualTo((int) totalQuestionSetCount);
    }

    @Test
    @DisplayName("기존 학습 통계가 없으면, 새로운 통계 객체를 생성하고 문제집 수를 조합하여 반환한다")
    void givenNoLearnStatsReturnsNewCombinedStats() {
      // given
      long totalQuestionSetCount = 5L;

      given(learnStatsPublicApi.getLearnStats(memberId)).willReturn(Optional.empty());
      given(questionSetPublicApi.countByMemberId(memberId)).willReturn(totalQuestionSetCount);

      // when
      LearnStatsResponse response = learnStatsFacade.getLearnStats(memberId);

      // then
      assertThat(response.getTotalCorrectQuestionCount()).isZero();
      assertThat(response.getTotalSolvedQuestionCount()).isZero();
      assertThat(response.getConsecutiveLearningDays()).isZero();
      assertThat(response.getTotalQuestionSetCount()).isEqualTo((int) totalQuestionSetCount);
    }
  }
}
