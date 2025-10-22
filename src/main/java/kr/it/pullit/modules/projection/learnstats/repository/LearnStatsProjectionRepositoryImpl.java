package kr.it.pullit.modules.projection.learnstats.repository;

import java.util.Optional;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LearnStatsProjectionRepositoryImpl implements LearnStatsProjectionRepository {

  private final LearnStatsProjectionJpaRepository jpaRepository;

  @Override
  public LearnStats save(LearnStats projection) {
    return jpaRepository.save(projection);
  }

  @Override
  public Optional<LearnStats> findById(Long memberId) {
    return jpaRepository.findById(memberId);
  }
}
