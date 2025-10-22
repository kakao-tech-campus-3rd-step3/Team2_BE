package kr.it.pullit.modules.projection.learnstats.repository;

import java.time.LocalDate;
import java.util.List;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStatsDaily;

public interface LearnStatsDailyRepository {
  int upsertAdd(Long memberId, LocalDate activityDate, int questionsDelta, int questionSetsDelta);

  List<LearnStatsDaily> findByMemberIdAndActivityDateBetween(Long memberId, LocalDate from, LocalDate to);
}
