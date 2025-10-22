package kr.it.pullit.modules.projection.learnstats.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.util.Optional;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStatsProjection;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsProjectionRepository;
import kr.it.pullit.support.annotation.UnitTest;
import kr.it.pullit.support.config.FixedClockConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@UnitTest
@ContextConfiguration(classes = {LearnStatsProjectionService.class, FixedClockConfig.class})
@DisplayName("LearnStatsProjectionService 단위 테스트")
class LearnStatsProjectionServiceTest {

  @Autowired private LearnStatsProjectionService sut;

  @MockitoBean private LearnStatsProjectionRepository learnStatsProjectionRepository;

  @Autowired private Clock clock;

  @Nested
  @DisplayName("주간 초기화 적용 시")
  class ApplyWeeklyReset {

    @Test
    @DisplayName("기존 통계가 있으면 해당 객체를 초기화하고 저장한다")
    void givenExistingProjectionThenResetsAndSaves() {
      // given
      Long memberId = 1L;
      LearnStatsProjection existingProjection = LearnStatsProjection.newOf(memberId);
      given(learnStatsProjectionRepository.findById(memberId))
          .willReturn(Optional.of(existingProjection));

      // when
      sut.applyWeeklyReset(memberId);

      // then
      verify(learnStatsProjectionRepository, times(1)).save(existingProjection);
    }

    @Test
    @DisplayName("기존 통계가 없으면 새 객체를 생성하여 초기화하고 저장한다")
    void givenNoProjectionThenCreatesResetsAndSaves() {
      // given
      Long memberId = 1L;
      given(learnStatsProjectionRepository.findById(memberId)).willReturn(Optional.empty());

      // when
      sut.applyWeeklyReset(memberId);

      // then
      verify(learnStatsProjectionRepository, times(1)).save(any(LearnStatsProjection.class));
    }
  }

  @Nested
  @DisplayName("문제집 풀이 완료 적용 시")
  class ApplyQuestionSetSolved {

    @Test
    @DisplayName("기존 통계가 있으면 해당 객체에 결과를 반영하고 저장한다")
    void givenExistingProjectionThenUpdatesAndSaves() {
      // given
      Long memberId = 1L;
      int questionCount = 10;
      LearnStatsProjection existingProjection = LearnStatsProjection.newOf(memberId);
      given(learnStatsProjectionRepository.findById(memberId))
          .willReturn(Optional.of(existingProjection));

      // when
      sut.applyQuestionSetSolved(memberId, questionCount);

      // then
      verify(learnStatsProjectionRepository, times(1)).save(existingProjection);
    }

    @Test
    @DisplayName("기존 통계가 없으면 새 객체를 생성하여 결과를 반영하고 저장한다")
    void givenNoProjectionthenCreatesUpdatesAndSaves() {
      // given
      Long memberId = 1L;
      int questionCount = 10;
      given(learnStatsProjectionRepository.findById(memberId)).willReturn(Optional.empty());

      // when
      sut.applyQuestionSetSolved(memberId, questionCount);

      // then
      verify(learnStatsProjectionRepository, times(1)).save(any(LearnStatsProjection.class));
    }
  }

  @Nested
  @DisplayName("문제집 할당 적용 시")
  class ApplyQuestionSetAssigned {

    @Test
    @DisplayName("기존 통계가 있으면 해당 객체에 결과를 반영하고 저장한다")
    void givenExistingProjectionThenUpdatesAndSaves() {
      // given
      Long memberId = 1L;
      LearnStatsProjection existingProjection = LearnStatsProjection.newOf(memberId);
      given(learnStatsProjectionRepository.findById(memberId))
          .willReturn(Optional.of(existingProjection));

      // when
      sut.applyQuestionSetAssigned(memberId);

      // then
      verify(learnStatsProjectionRepository, times(1)).save(existingProjection);
    }

    @Test
    @DisplayName("기존 통계가 없으면 새 객체를 생성하여 결과를 반영하고 저장한다")
    void givenNoProjectionThenCreatesUpdatesAndSaves() {
      // given
      Long memberId = 1L;
      given(learnStatsProjectionRepository.findById(memberId)).willReturn(Optional.empty());

      // when
      sut.applyQuestionSetAssigned(memberId);

      // then
      verify(learnStatsProjectionRepository, times(1)).save(any(LearnStatsProjection.class));
    }
  }

  @Nested
  @DisplayName("학습 세션 완료 적용 시")
  class ApplyStudySessionFinished {

    @Test
    @DisplayName("기존 통계가 없으면 새 객체를 생성하여 학습 시간을 반영하고 저장한다")
    void givenNoProjectionThenCreatesAndSaves() {
      // given
      Long memberId = 1L;
      long duration = 1800L;
      given(learnStatsProjectionRepository.findById(memberId)).willReturn(Optional.empty());

      // when
      sut.applyStudySessionFinished(memberId, duration);

      // then
      verify(learnStatsProjectionRepository, times(1)).save(any(LearnStatsProjection.class));
    }

    @Test
    @DisplayName("학습 시간이 0 또는 음수이면 통계에 반영하지 않고 저장 호출도 하지 않는다")
    void givenZeroOrNegativeDurationThenDoesNotSave() {
      // given
      Long memberId = 1L;
      LearnStatsProjection existingProjection = LearnStatsProjection.newOf(memberId);
      given(learnStatsProjectionRepository.findById(memberId))
          .willReturn(Optional.of(existingProjection));

      // when
      sut.applyStudySessionFinished(memberId, 0L);
      sut.applyStudySessionFinished(memberId, -100L);

      // then
      verify(learnStatsProjectionRepository, never()).save(any(LearnStatsProjection.class));
    }
  }
}
