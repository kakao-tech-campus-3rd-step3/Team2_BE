package kr.it.pullit.modules.projection.learnstats.web;

import java.time.LocalDate;
import java.util.List;
import kr.it.pullit.modules.projection.learnstats.query.LearnStatsDailyQueryService;
import kr.it.pullit.modules.projection.learnstats.query.LearnStatsQueryService;
import kr.it.pullit.modules.projection.learnstats.web.dto.DailyStatsResponse;
import kr.it.pullit.modules.projection.learnstats.web.dto.LearnStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LearnStatsController {

  private final LearnStatsQueryService learnStatsQueryService;
  private final LearnStatsDailyQueryService learnStatsDailyQueryService;

  @GetMapping("/api/members/{memberId}/learn-stats")
  public ResponseEntity<LearnStatsResponse> getLearnStats(@PathVariable Long memberId) {
    return ResponseEntity.ok(learnStatsQueryService.getLearnStats(memberId));
  }

  @GetMapping("/api/members/{memberId}/daily-stats")
  public ResponseEntity<List<DailyStatsResponse>> getDailyStats(
      @PathVariable Long memberId,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate to) {
    return ResponseEntity.ok(learnStatsDailyQueryService.findDailyStats(memberId, from, to));
  }
}
