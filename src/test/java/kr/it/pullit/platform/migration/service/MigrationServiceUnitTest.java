package kr.it.pullit.platform.migration.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.platform.migration.repository.MigrationHistoryRepository;
import kr.it.pullit.support.annotation.MockitoUnitTest;

@MockitoUnitTest
@DisplayName("MigrationService 단위 테스트")
class MigrationServiceUnitTest {

  @InjectMocks private MigrationService migrationService;

  @Mock private MigrationHistoryRepository migrationHistoryRepository;

  @Mock private SourcePublicApi sourcePublicApi;

  @Test
  @DisplayName("마이그레이션이 이미 실행된 경우, 추가 작업을 수행하지 않는다")
  void shouldDoNothingWhenMigrationAlreadyExecuted() {
    // given
    String migrationName = "SOURCE_STATUS_MIGRATION_V1";
    when(migrationHistoryRepository.existsByMigrationName(migrationName)).thenReturn(true);

    // when
    migrationService.runSourceStatusMigration();

    // then
    verify(sourcePublicApi, never()).migrateUploadedSourcesToReady();
    verify(migrationHistoryRepository, never()).save(migrationName);
  }

  @Test
  @DisplayName("마이그레이션이 실행된 적 없는 경우, 마이그레이션을 수행하고 실행 기록을 저장한다")
  void shouldRunMigrationAndSaveHistoryWhenNotExecuted() {
    // given
    String migrationName = "SOURCE_STATUS_MIGRATION_V1";
    when(migrationHistoryRepository.existsByMigrationName(migrationName)).thenReturn(false);

    // when
    migrationService.runSourceStatusMigration();

    // then
    verify(sourcePublicApi).migrateUploadedSourcesToReady();
    verify(migrationHistoryRepository).save(migrationName);
  }
}
