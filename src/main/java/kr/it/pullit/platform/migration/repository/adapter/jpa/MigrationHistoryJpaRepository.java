package kr.it.pullit.platform.migration.repository.adapter.jpa;

import kr.it.pullit.platform.migration.domain.entity.MigrationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

interface MigrationHistoryJpaRepository extends JpaRepository<MigrationHistory, Long> {
  boolean existsByMigrationName(String migrationName);
}
