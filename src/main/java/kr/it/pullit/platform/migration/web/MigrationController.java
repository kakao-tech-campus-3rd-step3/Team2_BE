package kr.it.pullit.platform.migration.web;

import kr.it.pullit.platform.migration.api.MigrationPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/migrations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MigrationController {

  private final MigrationPublicApi migrationPublicApi;

  @PostMapping("/source-status-v1")
  public ResponseEntity<String> runSourceStatusMigration() {
    migrationPublicApi.runSourceStatusMigration();
    return ResponseEntity.ok("소스 상태 마이그레이션이 성공적으로 실행되었습니다.");
  }
}
