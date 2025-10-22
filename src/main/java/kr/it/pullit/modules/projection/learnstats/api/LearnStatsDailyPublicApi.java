package kr.it.pullit.modules.projection.learnstats.api;

public interface LearnStatsDailyPublicApi {

  void addDailyStats(Long memberId, int questionsDelta, int questionSetsDelta);
}
