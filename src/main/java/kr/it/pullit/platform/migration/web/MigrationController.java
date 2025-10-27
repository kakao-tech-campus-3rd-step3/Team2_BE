package kr.it.pullit.platform.migration.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsRecalibrationPublicApi;
import kr.it.pullit.platform.migration.api.MigrationPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Migration API", description = "데이터베이스 마이그레이션 API (내부용)")
@RestController
@RequestMapping("/data/migration")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MigrationController {

  private final MigrationPublicApi migrationPublicApi;
  private final LearnStatsRecalibrationPublicApi recalibrationApi;

  @PostMapping("/source-status-v1")
  public ResponseEntity<String> runSourceStatusMigration() {
    migrationPublicApi.runSourceStatusMigration();
    return ResponseEntity.ok("소스 상태 마이그레이션이 성공적으로 실행되었습니다.");
  }

  @PostMapping("/recalibrate/learn-stats")
  public ResponseEntity<String> runLearnStatsRecalibration() {
    recalibrationApi.recalibrateAllMembers();
    return ResponseEntity.ok("모든 회원의 총 푼 문제 수 보정 작업이 시작되었습니다.");
  }
}
