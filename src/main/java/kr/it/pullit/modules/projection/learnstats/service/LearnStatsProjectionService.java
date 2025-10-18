package kr.it.pullit.modules.projection.learnstats.service;

import java.time.Clock;
import java.time.LocalDate;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsProjectionPublicApi;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStatsProjection;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsProjectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LearnStatsProjectionService implements LearnStatsProjectionPublicApi {

  private final LearnStatsProjectionRepository repo;
  private final Clock clock;

  public void applyWeeklyReset(Long memberId) {
    LearnStatsProjection p =
        repo.findById(memberId).orElseGet(() -> LearnStatsProjection.newOf(memberId));
    p.onWeeklyReset();
    repo.save(p);
  }

  public void applyQuestionSetSolved(Long memberId, int questionCount) {
    LearnStatsProjection p =
        repo.findById(memberId).orElseGet(() -> LearnStatsProjection.newOf(memberId));
    p.onQuestionSetSolved(questionCount, LocalDate.now(clock));
    repo.save(p);
  }

  @Transactional
  public void applyQuestionSetAssigned(Long memberId) {
    LearnStatsProjection p =
        repo.findById(memberId).orElseGet(() -> LearnStatsProjection.newOf(memberId));
    p.onQuestionSetAssigned();
    repo.save(p);
  }
}
