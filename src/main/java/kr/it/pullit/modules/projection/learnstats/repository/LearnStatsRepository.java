package kr.it.pullit.modules.projection.learnstats.repository;

import java.util.Optional;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LearnStatsRepository {

  LearnStats save(LearnStats projection);

  Optional<LearnStats> findById(Long memberId);

  Page<LearnStats> findAll(Pageable pageable);
}
