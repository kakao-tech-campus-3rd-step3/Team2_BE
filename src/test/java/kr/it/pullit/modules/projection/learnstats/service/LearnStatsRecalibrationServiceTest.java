package kr.it.pullit.modules.projection.learnstats.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsRepository;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.support.annotation.SpringUnitTest;
import kr.it.pullit.support.fixture.QuestionSetFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringUnitTest
@ContextConfiguration(classes = {LearnStatsRecalibrationService.class})
@DisplayName("LearnStatsRecalibrationService 단위 테스트")
class LearnStatsRecalibrationServiceTest {

  @Autowired private LearnStatsRecalibrationService sut;

  @MockitoBean private MemberPublicApi memberPublicApi;
  @MockitoBean private QuestionSetPublicApi questionSetPublicApi;
  @MockitoBean private LearnStatsRepository learnStatsRepository;

  @Test
  @DisplayName("모든 회원의 통계를 보정한다")
  void shouldRecalibrateAllMembersStats() {
    // given
    Member member1 = mock(Member.class);
    when(member1.getId()).thenReturn(1L);

    Member member2 = mock(Member.class);
    when(member2.getId()).thenReturn(2L);

    PageImpl<Member> memberPage = new PageImpl<>(List.of(member1, member2));

    QuestionSet qs1 =
        QuestionSetFixtures.withQuestions(List.of(mock(Question.class), mock(Question.class)));
    QuestionSet qs2 = QuestionSetFixtures.withQuestions(List.of(mock(Question.class)));

    LearnStats stats1 = LearnStats.newOf(1L);

    given(memberPublicApi.findAll(any(PageRequest.class))).willReturn(memberPage);
    given(questionSetPublicApi.findCompletedEntitiesByMemberId(1L)).willReturn(List.of(qs1, qs2));
    given(questionSetPublicApi.findCompletedEntitiesByMemberId(2L)).willReturn(List.of(qs2));
    given(learnStatsRepository.findById(1L)).willReturn(Optional.of(stats1));
    given(learnStatsRepository.findById(2L)).willReturn(Optional.empty());

    // when
    sut.recalibrateAllMembers();

    // then
    ArgumentCaptor<LearnStats> captor = ArgumentCaptor.forClass(LearnStats.class);
    verify(learnStatsRepository, times(2)).save(captor.capture());

    LearnStats savedStats1 =
        captor.getAllValues().stream()
            .filter(s -> s.getMemberId().equals(1L))
            .findFirst()
            .orElseThrow();
    assertThat(savedStats1.getTotalSolvedQuestionCount()).isEqualTo(3); // qs1(2) + qs2(1)

    LearnStats savedStats2 =
        captor.getAllValues().stream()
            .filter(s -> s.getMemberId().equals(2L))
            .findFirst()
            .orElseThrow();
    assertThat(savedStats2.getTotalSolvedQuestionCount()).isEqualTo(1); // qs2(1)
  }
}
