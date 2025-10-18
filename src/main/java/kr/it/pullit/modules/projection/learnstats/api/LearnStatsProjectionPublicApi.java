package kr.it.pullit.modules.projection.learnstats.api;

public interface LearnStatsProjectionPublicApi {

  void applyWeeklyReset(Long memberId);

  void applyQuestionSetSolved(Long memberId, int questionCount);

  void applyQuestionSetAssigned(Long memberId);
}
