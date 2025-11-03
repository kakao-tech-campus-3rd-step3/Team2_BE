package kr.it.pullit.modules.projection.learnstats.api;

public interface LearnStatsRecalibrationPublicApi {

  void recalibrateLearnStatsAllMembers();

  void recalibrateTotalQuestionCountForMember(Long memberId);
}
