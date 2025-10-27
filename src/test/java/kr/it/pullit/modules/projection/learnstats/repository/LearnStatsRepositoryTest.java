package kr.it.pullit.modules.projection.learnstats.repository;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.support.annotation.JpaSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@JpaSliceTest
@Import(LearnStatsRepositoryImpl.class)
class LearnStatsRepositoryTest {

  @Autowired private LearnStatsRepository learnStatsRepository;

  @DisplayName("학습 통계 프로젝션 저장")
  @Test
  void save() {
    // given
    var newProjection = LearnStats.newOf(1L);

    // when
    var savedProjection = learnStatsRepository.save(newProjection);

    // then
    assertThat(savedProjection.getMemberId()).isEqualTo(1L);
  }

  @DisplayName("학습 통계 프로젝션 조회")
  @Nested
  class Find {

    @DisplayName("ID로 조회 시, 해당 프로젝션을 반환한다.")
    @Test
    void findById_returnsProjection() {
      // given
      var saved = learnStatsRepository.save(LearnStats.newOf(1L));

      // when
      var found = learnStatsRepository.findById(saved.getMemberId()).orElseThrow();

      // then
      assertThat(found.getMemberId()).isEqualTo(saved.getMemberId());
    }

    @DisplayName("존재하지 않는 ID로 조회 시, 비어있는 Optional을 반환한다.")
    @Test
    void findById_withNonExistentId_returnsEmpty() {
      // when
      var found = learnStatsRepository.findById(999L);

      // then
      assertThat(found).isEmpty();
    }
  }
}
