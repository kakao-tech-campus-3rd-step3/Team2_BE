package kr.it.pullit.modules.projection.learnstats.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;

public interface LearnStatsRepository {

  LearnStats save(LearnStats projection);

  Optional<LearnStats> findById(Long memberId);

  Page<LearnStats> findAll(Pageable pageable);
}
