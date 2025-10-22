package kr.it.pullit.modules.projection.learnstats.repository;

import java.util.Optional;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStatsProjection;

public interface LearnStatsProjectionRepository {

  LearnStatsProjection save(LearnStatsProjection projection);

  Optional<LearnStatsProjection> findById(Long memberId);
}
