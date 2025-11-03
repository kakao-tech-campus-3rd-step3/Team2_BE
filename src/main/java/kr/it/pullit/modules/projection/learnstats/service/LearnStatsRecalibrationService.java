package kr.it.pullit.modules.projection.learnstats.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsRecalibrationPublicApi;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsRepository;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LearnStatsRecalibrationService implements LearnStatsRecalibrationPublicApi {

  private final MemberPublicApi memberPublicApi;
  private final QuestionSetPublicApi questionSetPublicApi;
  private final LearnStatsRepository learnStatsRepository;
  private final Clock clock;

  @Override
  public void recalibrateLearnStatsAllMembers() {
    log.info("모든 회원의 학습 통계(총 문제, 주간 문제, 연속 학습일) 보정 작업을 시작합니다.");

    Pageable pageable = PageRequest.of(0, 100);
    Page<Member> memberPage;

    do {
      memberPage = memberPublicApi.findAll(pageable);
      memberPage.getContent().forEach(member -> recalibrateMember(member.getId()));
      pageable = memberPage.nextPageable();
    } while (memberPage.hasNext());

    log.info("모든 회원의 학습 통계 보정 작업을 완료했습니다.");
  }

  private void recalibrateMember(Long memberId) {
    long totalSolvedCount = questionSetPublicApi.countCompletedQuestionsByMemberId(memberId);
    int weeklySolvedCount = calculateWeeklySolvedCount(memberId);
    List<LocalDateTime> completedDates = questionSetPublicApi.findCompletedDatesByMemberId(memberId);

    LearnStats stats =
        learnStatsRepository.findById(memberId).orElseGet(() -> LearnStats.newOf(memberId));

    stats.recalibrate(totalSolvedCount, weeklySolvedCount, completedDates);

    learnStatsRepository.save(stats);

    log.info(
        "{}번 회원의 학습 통계를 보정했습니다: 총 문제 수={}, 주간 문제 수={}, 연속 학습일={}",
        memberId,
        stats.getTotalSolvedQuestionCount(),
        stats.getWeeklySolvedQuestionCount(),
        stats.getConsecutiveLearningDays());
  }

  private int calculateWeeklySolvedCount(Long memberId) {
    LocalDateTime today = LocalDateTime.now(clock);
    LocalDateTime startOfWeek =
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
    LocalDateTime endOfWeek =
        today
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .toLocalDate()
            .atTime(23, 59, 59, 999999999);
    return (int)
        questionSetPublicApi.countCompletedQuestionsByMemberIdAndDateBetween(
            memberId, startOfWeek, endOfWeek);
  }
}
