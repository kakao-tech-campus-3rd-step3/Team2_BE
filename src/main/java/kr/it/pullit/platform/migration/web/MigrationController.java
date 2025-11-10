package kr.it.pullit.platform.migration.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.it.pullit.modules.auth.web.apidocs.AuthApiDocs;
import kr.it.pullit.platform.migration.api.MigrationPublicApi;
import kr.it.pullit.platform.migration.web.apidocs.RunLearnStatsRecalibrationApiDocs;
import kr.it.pullit.platform.migration.web.apidocs.RunSourceStatusMigrationApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Migration API", description = "데이터베이스 마이그레이션 API (내부용)")
@RestController
@RequestMapping("/api/admin/migrations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@AuthApiDocs
public class MigrationController {

  private final MigrationPublicApi migrationPublicApi;

  @PostMapping("/source-status-v1")
  @RunSourceStatusMigrationApiDocs
  public ResponseEntity<String> runSourceStatusMigration() {
    migrationPublicApi.runSourceStatusMigration();
    return ResponseEntity.ok("소스 상태 마이그레이션이 성공적으로 실행되었습니다.");
  }

  @PostMapping("/recalibrate/learn-stats")
  @RunLearnStatsRecalibrationApiDocs
  public ResponseEntity<String> runLearnStatsRecalibration() {
    migrationPublicApi.runLearnStatsRecalibration();
    return ResponseEntity.ok("모든 회원의 학습 통계(총 문제, 주간 문제, 연속 학습일) 보정 작업이 시작되었습니다.");
  }
}
