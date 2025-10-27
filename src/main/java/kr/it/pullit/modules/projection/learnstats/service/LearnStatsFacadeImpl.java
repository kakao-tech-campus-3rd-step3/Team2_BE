package kr.it.pullit.modules.projection.learnstats.service;

import kr.it.pullit.modules.projection.learnstats.api.LearnStatsFacade;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsPublicApi;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.modules.projection.learnstats.web.dto.LearnStatsResponse;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearnStatsFacadeImpl implements LearnStatsFacade {

  private final LearnStatsPublicApi learnStatsPublicApi;
  private final QuestionSetPublicApi questionSetPublicApi;

  @Override
  public LearnStatsResponse getLearnStats(Long memberId) {
    LearnStats learnStats =
        learnStatsPublicApi.getLearnStats(memberId).orElseGet(() -> LearnStats.newOf(memberId));
    long totalCount = questionSetPublicApi.countByMemberId(memberId);

    return LearnStatsResponse.of(learnStats, (int) totalCount);
  }
}
