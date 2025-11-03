package kr.it.pullit.modules.projection.learnstats.service;

import java.time.Clock;
import java.time.LocalDate;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LearnStatsValidationService {

  private final LearnStatsRepository learnStatsRepository;
  private final Clock clock;

  public void validateAllMembersConsecutiveLearning() {
    log.info("모든 회원의 연속 학습일 검증을 시작합니다.");

    Pageable pageable = PageRequest.of(0, 100);
    var learnStatsPage = learnStatsRepository.findAll(pageable);

    do {
      learnStatsPage
          .getContent()
          .forEach(
              learnStats -> {
                learnStats.resetConsecutiveDaysIfMissed(LocalDate.now(clock));
              });
      pageable = learnStatsPage.nextPageable();
      learnStatsPage = learnStatsRepository.findAll(pageable);
    } while (learnStatsPage.hasNext());

    log.info("모든 회원의 연속 학습일 검증을 완료했습니다.");
  }
}
