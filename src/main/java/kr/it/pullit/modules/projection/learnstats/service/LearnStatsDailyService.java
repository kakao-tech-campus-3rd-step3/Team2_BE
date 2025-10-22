package kr.it.pullit.modules.projection.learnstats.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsDailyPublicApi;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStatsDaily;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsDailyJpaRepository;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsDailyRepository;
import kr.it.pullit.modules.projection.learnstats.web.dto.DailyStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LearnStatsDailyService implements LearnStatsDailyPublicApi {

  private final LearnStatsDailyRepository learnStatsDailyRepository;
  private final LearnStatsDailyJpaRepository learnStatsDailyJpaRepository;

  private final Clock clock;

  @Override
  public void addDailyStats(Long memberId, int questionsDelta, int questionSetsDelta) {
    learnStatsDailyRepository.upsertAdd(
        memberId, LocalDate.now(clock), questionsDelta, questionSetsDelta);
  }

  @Transactional(readOnly = true)
  @Override
  public List<DailyStatsResponse> findDailyStats(Long memberId, LocalDate from, LocalDate to) {
    List<LearnStatsDaily> dailyStats =
        learnStatsDailyJpaRepository.findByMemberIdAndActivityDateBetween(memberId, from, to);

    return dailyStats.stream()
        .map(stat -> new DailyStatsResponse(stat.getActivityDate(), stat.getSolvedQuestionCount()))
        .toList();
  }
}
