package kr.it.pullit.modules.projection.learnstats.repository;

import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearnStatsJpaRepository extends JpaRepository<LearnStats, Long> {}
