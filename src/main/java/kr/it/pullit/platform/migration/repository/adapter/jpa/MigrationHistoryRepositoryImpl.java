package kr.it.pullit.platform.migration.repository.adapter.jpa;

import kr.it.pullit.platform.migration.domain.entity.MigrationHistory;
import kr.it.pullit.platform.migration.repository.MigrationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MigrationHistoryRepositoryImpl implements MigrationHistoryRepository {

  private final MigrationHistoryJpaRepository jpaRepository;

  @Override
  public boolean existsByMigrationName(String migrationName) {
    return jpaRepository.existsByMigrationName(migrationName);
  }

  @Override
  public void save(String migrationName) {
    MigrationHistory history = MigrationHistory.create(migrationName);
    jpaRepository.save(history);
  }
}
