package kr.it.pullit.modules.projection.learnstats.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsPublicApi;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsRepository;
import kr.it.pullit.modules.questionset.api.MarkingResultPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LearnStatsService implements LearnStatsPublicApi {

  private final LearnStatsRepository repo;
  private final MarkingResultPublicApi markingResultPublicApi;
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

  public void recalculateCorrectQuestionCount(Long memberId) {
    long totalCorrectQuestionCount = markingResultPublicApi.countCorrectAnswersByMemberId(memberId);

    LearnStats p = repo.findById(memberId).orElseGet(() -> LearnStats.newOf(memberId));
    p.updateTotalCorrectQuestionCount(totalCorrectQuestionCount);
    repo.save(p);
  }

  @Override
  public void applyQuestionSetDeleted(Long memberId, long correctQuestionCount) {
    repo.findById(memberId)
        .ifPresent(
            learnStats -> {
              learnStats.onQuestionSetDeleted(correctQuestionCount);
              repo.save(learnStats);
            });
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<LearnStats> getLearnStats(Long memberId) {
    return repo.findById(memberId);
  }
}
