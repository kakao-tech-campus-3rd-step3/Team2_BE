package kr.it.pullit.modules.projection.learnstats.query;

import java.time.LocalDate;
import java.util.List;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStatsDaily;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsDailyJpaRepository;
import kr.it.pullit.modules.projection.learnstats.web.dto.DailyStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LearnStatsDailyQueryService {

  private final LearnStatsDailyJpaRepository learnStatsDailyJpaRepository;

  public List<DailyStatsResponse> findDailyStats(Long memberId, LocalDate from, LocalDate to) {
    List<LearnStatsDaily> dailyStats =
        learnStatsDailyJpaRepository.findByMemberIdAndActivityDateBetween(memberId, from, to);

    return dailyStats.stream()
        .map(stat -> new DailyStatsResponse(stat.getActivityDate(), stat.getSolvedQuestionCount()))
        .toList();
  }
}
