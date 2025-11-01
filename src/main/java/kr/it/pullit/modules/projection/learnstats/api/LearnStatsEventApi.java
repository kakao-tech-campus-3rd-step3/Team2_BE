package kr.it.pullit.modules.projection.learnstats.api;

public interface LearnStatsEventApi {

  void publishWeeklyReset(Long memberId);

  void publishQuestionSetSolved(Long memberId, int solvedQuestionCount);
}
