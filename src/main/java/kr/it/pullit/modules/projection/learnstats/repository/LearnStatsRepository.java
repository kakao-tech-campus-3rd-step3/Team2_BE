package kr.it.pullit.modules.projection.learnstats.repository;

import java.util.Optional;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;

public interface LearnStatsRepository {

  LearnStats save(LearnStats projection);

  Optional<LearnStats> findById(Long memberId);
}
