package kr.it.pullit.modules.projection.learnstats.api;

import java.util.Optional;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;

public interface LearnStatsPublicApi {

  void applyWeeklyReset(Long memberId);

  void applyQuestionSetSolved(Long memberId, int questionCount);

  Optional<LearnStats> getLearnStats(Long memberId);
}
