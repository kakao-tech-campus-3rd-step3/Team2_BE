package kr.it.pullit.modules.projection.learnstats.service;

import static kr.it.pullit.support.fixture.MemberFixtures.basicUser;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsRepository;
import kr.it.pullit.support.annotation.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@DisplayName("LearnStatsValidationService 통합 테스트")
class LearnStatsValidationServiceTest {

  @Autowired private LearnStatsValidationService learnStatsValidationService;
  @Autowired private LearnStatsRepository learnStatsRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private Clock clock;

  @Test
  @DisplayName("어제 학습 후 오늘 학습하지 않은 회원의 연속 학습일을 0으로 초기화한다")
  void shouldResetConsecutiveDaysWhenMissed() {
    // given
    Member member = memberRepository.save(basicUser());
    LearnStats stats = LearnStats.newOf(member.getId());
    // 이틀 전이 마지막 학습일이라고 가정 (어제 학습 누락)

    stats.recalibrate(
        10L,
        8L,
        5,
        List.of(LocalDate.now(clock).minusDays(2).atStartOfDay()),
        LocalDate.now(clock));
    learnStatsRepository.save(stats);

    // when
    learnStatsValidationService.validateAllMembersConsecutiveLearning();

    // then
    LearnStats validatedStats = learnStatsRepository.findById(member.getId()).get();
    assertThat(validatedStats.getConsecutiveLearningDays()).isZero();
  }
}
