package kr.it.pullit.platform.migration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.platform.migration.repository.MigrationHistoryRepository;
import kr.it.pullit.platform.migration.repository.adapter.jpa.MigrationHistoryJpaRepository;
import kr.it.pullit.support.annotation.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
@DisplayName("MigrationService 통합 테스트")
class MigrationServiceIntegrationTest {

  private static final String MIGRATION_NAME = "SOURCE_STATUS_MIGRATION_V1";

  @Autowired private MigrationService migrationService;

  @Autowired private MigrationHistoryRepository migrationHistoryRepository;

  @Autowired private MigrationHistoryJpaRepository jpaRepository;

  @MockitoBean private SourcePublicApi sourcePublicApi;

  @Test
  @DisplayName("마이그레이션이 이미 실행된 경우, 추가 작업을 수행하지 않는다")
  void shouldRunMigrationOnlyOnce() {
    // given: 마이그레이션 기록이 없는 상태
    assertThat(migrationHistoryRepository.existsByMigrationName(MIGRATION_NAME)).isFalse();

    // when: 첫 번째 마이그레이션 실행
    migrationService.runSourceStatusMigration();

    // then: 마이그레이션이 실행되고, 기록이 저장됨
    verify(sourcePublicApi, times(1)).migrateUploadedSourcesToReady();
    assertThat(migrationHistoryRepository.existsByMigrationName(MIGRATION_NAME)).isTrue();

    // when: 두 번째 마이그레이션 실행
    migrationService.runSourceStatusMigration();

    // then: 추가적인 마이그레이션 실행 없이, 총 실행 횟수는 1회를 유지
    verify(sourcePublicApi, times(1)).migrateUploadedSourcesToReady();
  }
}
