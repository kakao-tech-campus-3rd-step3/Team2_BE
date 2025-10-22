package kr.it.pullit.modules.projection.learnstats.repository;

import java.time.LocalDate;

public interface LearnStatsDailyRepository {
  int upsertAdd(Long memberId, LocalDate activityDate, int questionsDelta, int questionSetsDelta);
}
