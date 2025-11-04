package kr.it.pullit.modules.projection.learnstats.service;

import static kr.it.pullit.support.fixture.MemberFixtures.basicUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsRepository;
import kr.it.pullit.modules.questionset.api.MarkingResultPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.support.annotation.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
@DisplayName("LearnStatsRecalibrationService 통합 테스트")
class LearnStatsRecalibrationServiceTest {

  @Autowired private LearnStatsRecalibrationService learnStatsRecalibrationService;
  @Autowired private LearnStatsRepository learnStatsRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private Clock clock;

  @MockitoBean private QuestionSetPublicApi questionSetPublicApi;

  @MockitoBean private MarkingResultPublicApi markingResultPublicApi;

  @Test
  @DisplayName("모든 회원의 학습 통계를 성공적으로 재보정한다")
  void shouldRecalibrateLearnStatsForAllMembers() {
    // given
    Member member = memberRepository.save(basicUser());
    LocalDateTime today = LocalDateTime.now(clock);
    List<LocalDateTime> completedDates =
        List.of(today.minusDays(1), today.minusDays(2), today.minusDays(4));

    given(markingResultPublicApi.countTotalCorrectQuestionsByMemberId(member.getId()))
        .willReturn(120L);
    given(markingResultPublicApi.countTotalAttemptedQuestionsByMemberId(member.getId()))
        .willReturn(150L);
    given(questionSetPublicApi.countByQuestionSetOwnerId(member.getId())).willReturn(200L);
    given(
            questionSetPublicApi.countCompletedQuestionsByMemberIdAndDateBetween(
                eq(member.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
        .willReturn(35L);
    given(questionSetPublicApi.findCompletedDatesByMemberId(member.getId()))
        .willReturn(completedDates);

    // when
    learnStatsRecalibrationService.recalibrateLearnStatsAllMembers();

    // then
    LearnStats stats = learnStatsRepository.findById(member.getId()).get();

    assertThat(stats.getTotalQuestionCount()).isEqualTo(200L);
    assertThat(stats.getTotalSolvedQuestionCount()).isEqualTo(150L);
    assertThat(stats.getTotalCorrectQuestionCount()).isEqualTo(120L);
    assertThat(stats.getWeeklySolvedQuestionCount()).isEqualTo(35);
    assertThat(stats.getConsecutiveLearningDays()).isEqualTo(2);
    assertThat(stats.getLastLearningDate()).isEqualTo(today.minusDays(1).toLocalDate());
  }
}
