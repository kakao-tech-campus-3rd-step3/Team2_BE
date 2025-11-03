package kr.it.pullit.modules.projection.learnstats.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LearnStatsRepositoryImpl implements LearnStatsRepository {

  private final LearnStatsJpaRepository jpaRepository;

  @Override
  public LearnStats save(LearnStats projection) {
    return jpaRepository.save(projection);
  }

  @Override
  public Optional<LearnStats> findById(Long memberId) {
    return jpaRepository.findById(memberId);
  }

  @Override
  public Page<LearnStats> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable);
  }
}
