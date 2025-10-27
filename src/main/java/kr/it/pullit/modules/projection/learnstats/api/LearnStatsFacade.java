package kr.it.pullit.modules.projection.learnstats.api;

import kr.it.pullit.modules.projection.learnstats.web.dto.LearnStatsResponse;

public interface LearnStatsFacade {
  LearnStatsResponse getLearnStats(Long memberId);
}
