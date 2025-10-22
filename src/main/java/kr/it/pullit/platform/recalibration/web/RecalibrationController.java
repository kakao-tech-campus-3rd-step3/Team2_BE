package kr.it.pullit.platform.recalibration.web;

import kr.it.pullit.modules.projection.learnstats.api.LearnStatsRecalibrationPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/recalibrations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RecalibrationController {

  private final LearnStatsRecalibrationPublicApi recalibrationApi;

  @PostMapping("/learn-stats/total-solved-questions")
  public ResponseEntity<String> runRecalibration() {
    // 비동기 실행을 고려할 수 있으나, 우선 동기 실행으로 구현
    recalibrationApi.recalibrateAllMembers();
    return ResponseEntity.ok("모든 회원의 총 푼 문제 수 보정 작업이 시작되었습니다.");
  }
}
