package kr.it.pullit.platform.migration.service;

import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.platform.migration.api.MigrationPublicApi;
import kr.it.pullit.platform.migration.repository.MigrationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MigrationService implements MigrationPublicApi {

  private static final String SOURCE_STATUS_MIGRIGRATION_V1 = "SOURCE_STATUS_MIGRATION_V1";

  private final MigrationHistoryRepository migrationHistoryRepository;
  private final SourcePublicApi sourcePublicApi;

  @Override
  @Transactional
  public void runSourceStatusMigration() {
    if (migrationHistoryRepository.existsByMigrationName(SOURCE_STATUS_MIGRIGRATION_V1)) {
      log.info("마이그레이션 '{}'는 이미 실행되었습니다. 작업을 건너뜁니다.", SOURCE_STATUS_MIGRIGRATION_V1);
      return;
    }

    log.info("마이그레이션 '{}' 실행을 시작합니다.", SOURCE_STATUS_MIGRIGRATION_V1);
    sourcePublicApi.migrateUploadedSourcesToReady();

    migrationHistoryRepository.save(SOURCE_STATUS_MIGRIGRATION_V1);
    log.info("마이그레이션 '{}' 실행을 성공적으로 완료하고 실행 기록을 저장했습니다.", SOURCE_STATUS_MIGRIGRATION_V1);
  }
}
