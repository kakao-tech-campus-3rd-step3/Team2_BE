package kr.it.pullit.modules.projection.learnstats.service;

import java.time.Clock;
import java.time.LocalDate;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsDailyPublicApi;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LearnStatsDailyService implements LearnStatsDailyPublicApi {

  private final LearnStatsDailyRepository learnStatsDailyRepository;
  private final Clock clock;

  @Override
  public void addDailyStats(Long memberId, int questionsDelta, int questionSetsDelta) {
    learnStatsDailyRepository.upsertAdd(
        memberId, LocalDate.now(clock), questionsDelta, questionSetsDelta);
  }
}
