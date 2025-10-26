package kr.it.pullit.modules.projection.learnstats.api;

import java.time.LocalDate;
import java.util.List;
import kr.it.pullit.modules.projection.learnstats.web.dto.DailyStatsResponse;
import org.springframework.transaction.annotation.Transactional;

public interface LearnStatsDailyPublicApi {

  void addDailyStats(Long memberId, int questionsDelta, int questionSetsDelta);

  @Transactional(readOnly = true)
  List<DailyStatsResponse> findDailyStats(Long memberId, LocalDate from, LocalDate to);
}
