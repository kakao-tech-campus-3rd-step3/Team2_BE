package kr.it.pullit.platform.migration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.platform.migration.repository.adapter.jpa.MigrationHistoryRepositoryImpl;
import kr.it.pullit.support.annotation.JpaSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@JpaSliceTest
@Import(MigrationHistoryRepositoryImpl.class)
@DisplayName("MigrationHistoryRepository 슬라이스 테스트")
class MigrationHistoryRepositoryTest {

  @Autowired private MigrationHistoryRepository migrationHistoryRepository;

  @Nested
  @DisplayName("existsByMigrationName 메서드는")
  class Describe_existsByMigrationName {

    @Test
    @DisplayName("저장된 마이그레이션 이름이 주어지면 true를 반환한다")
    void shouldReturnTrueForExistingMigration() {
      // given
      String migrationName = "V1__init_schema";
      migrationHistoryRepository.save(migrationName);

      // when
      boolean exists = migrationHistoryRepository.existsByMigrationName(migrationName);

      // then
      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("저장되지 않은 마이그레이션 이름이 주어지면 false를 반환한다")
    void shouldReturnFalseForNonExistingMigration() {
      // given
      String migrationName = "V2__non_existent";

      // when
      boolean exists = migrationHistoryRepository.existsByMigrationName(migrationName);

      // then
      assertThat(exists).isFalse();
    }
  }

  @Nested
  @DisplayName("save 메서드는")
  class Describe_save {

    @Test
    @DisplayName("주어진 이름으로 마이그레이션 기록을 성공적으로 저장한다")
    void shouldSaveHistorySuccessfully() {
      // given
      String migrationName = "V3__add_new_table";

      // when
      migrationHistoryRepository.save(migrationName);

      // then
      boolean exists = migrationHistoryRepository.existsByMigrationName(migrationName);
      assertThat(exists).isTrue();
    }
  }
}
