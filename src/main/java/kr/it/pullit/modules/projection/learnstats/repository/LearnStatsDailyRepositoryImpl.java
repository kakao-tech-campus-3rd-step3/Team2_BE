package kr.it.pullit.modules.projection.learnstats.repository;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

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
}
