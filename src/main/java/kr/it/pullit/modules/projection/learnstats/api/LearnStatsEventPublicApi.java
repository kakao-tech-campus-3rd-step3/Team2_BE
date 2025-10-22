package kr.it.pullit.modules.projection.learnstats.api;

import lombok.SneakyThrows;

public interface LearnStatsEventPublicApi {

  void publishQuestionSetAssigned(Long memberId);

  void publishQuestionSetSolved(Long memberId, int solvedQuestionCount);

  @SneakyThrows
  void publishWeeklyReset(Long memberId);
}
