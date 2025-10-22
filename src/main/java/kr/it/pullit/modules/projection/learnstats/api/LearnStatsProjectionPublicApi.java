package kr.it.pullit.modules.projection.learnstats.api;

import kr.it.pullit.modules.projection.learnstats.web.dto.LearnStatsResponse;

public interface LearnStatsProjectionPublicApi {

  void applyWeeklyReset(Long memberId);

  void applyQuestionSetSolved(Long memberId, int questionCount);

  LearnStatsResponse getLearnStats(Long memberId);
}
