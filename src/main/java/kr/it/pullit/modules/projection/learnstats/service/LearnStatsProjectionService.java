package kr.it.pullit.modules.projection.learnstats.service;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsProjectionPublicApi;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsProjectionRepository;
import kr.it.pullit.modules.projection.learnstats.web.dto.LearnStatsResponse;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class LearnStatsProjectionService implements LearnStatsProjectionPublicApi {

  private final LearnStatsProjectionRepository repo;
  private final QuestionSetPublicApi questionSetPublicApi;
  private final Clock clock;

  @Override
  public void applyWeeklyReset(Long memberId) {
    LearnStats p = repo.findById(memberId).orElseGet(() -> LearnStats.newOf(memberId));
    p.onWeeklyReset();
    repo.save(p);
  }

  @Override
  public void applyQuestionSetSolved(Long memberId, int questionCount) {
    LearnStats p = repo.findById(memberId).orElseGet(() -> LearnStats.newOf(memberId));
    p.onQuestionSetSolved(questionCount, LocalDate.now(clock));
    repo.save(p);
  }

  @Override
  @Transactional(readOnly = true)
  public LearnStatsResponse getLearnStats(Long memberId) {
    LearnStats projection = repo.findById(memberId).orElseGet(() -> LearnStats.newOf(memberId));

    int totalQuestionSetCount = questionSetPublicApi.getMemberQuestionSets(memberId).size();

    return LearnStatsResponse.of(projection, totalQuestionSetCount);
  }
}
