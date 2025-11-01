package kr.it.pullit.platform.migration.repository;

public interface MigrationHistoryRepository {
  boolean existsByMigrationName(String migrationName);

  void save(String migrationName);
}
