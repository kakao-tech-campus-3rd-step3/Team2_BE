package kr.it.pullit.modules.projection.learnstats.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStatsDaily;
import lombok.RequiredArgsConstructor;

@Profile("!test")
@Repository
@RequiredArgsConstructor
public class LearnStatsDailyRepositoryImpl implements LearnStatsDailyRepository {

  private final LearnStatsDailyJpaRepository jpaRepository;

  @Override
  public int upsertAdd(
      Long memberId, LocalDate activityDate, int questionsDelta, int questionSetsDelta) {
    return jpaRepository.upsertAdd(memberId, activityDate, questionsDelta, questionSetsDelta);
  }

  @Override
  public List<LearnStatsDaily> findByMemberIdAndActivityDateBetween(Long memberId, LocalDate from,
      LocalDate to) {
    return jpaRepository.findByMemberIdAndActivityDateBetween(memberId, from, to);
  }
}
