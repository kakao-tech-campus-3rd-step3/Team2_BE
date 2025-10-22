package kr.it.pullit.modules.projection.learnstats.repository;

import kr.it.pullit.modules.projection.learnstats.domain.LearnStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearnStatsProjectionJpaRepository
    extends JpaRepository<LearnStatsProjection, Long> {}
