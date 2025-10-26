package kr.it.pullit.modules.projection.learnstats.api;

public interface LearnStatsEventPublicApi {

  void publishWeeklyReset(Long memberId);

  void publishQuestionSetSolved(Long memberId, int solvedQuestionCount);
}
