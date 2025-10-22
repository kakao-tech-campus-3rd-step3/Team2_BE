package kr.it.pullit.modules.projection.learnstats.repository;

import java.util.Optional;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStatsProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LearnStatsProjectionRepositoryImpl implements LearnStatsProjectionRepository {

  private final LearnStatsProjectionJpaRepository jpaRepository;

  @Override
  public LearnStatsProjection save(LearnStatsProjection projection) {
    return jpaRepository.save(projection);
  }

  @Override
  public Optional<LearnStatsProjection> findById(Long memberId) {
    return jpaRepository.findById(memberId);
  }
}
