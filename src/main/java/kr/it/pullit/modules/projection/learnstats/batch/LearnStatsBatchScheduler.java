package kr.it.pullit.modules.projection.learnstats.batch;

import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsEventPublicApi;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsRecalibrationPublicApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearnStatsBatchScheduler {

  private final MemberPublicApi memberPublicApi;
  private final LearnStatsEventPublicApi learnStatsEventPublicApi;
  private final LearnStatsRecalibrationPublicApi recalibrationApi;

  /** 매주 월요일 오전 4시에 실행 */
  @Scheduled(cron = "0 0 4 * * MON", zone = "Asia/Seoul")
  public void triggerWeeklyResetAndRecalibration() {
    log.info("주간 학습 통계 초기화 및 보정 작업을 시작합니다.");

    Pageable pageable = PageRequest.of(0, 100);
    Page<Member> memberPage;

    do {
      memberPage = memberPublicApi.findAll(pageable);
      memberPage
          .getContent()
          .forEach(
              member -> {
                log.info("{}번 회원의 주간 통계를 초기화하고, 총 푼 문제 수를 보정합니다.", member.getId());
                learnStatsEventPublicApi.publishWeeklyReset(member.getId());
              });
      pageable = memberPage.nextPageable();
    } while (memberPage.hasNext());

    log.info("주간 학습 통계 초기화 및 보정 작업을 완료했습니다.");

    log.info("총 푼 문제 수 보정 작업을 시작합니다.");
    recalibrationApi.recalibrateAllMembers();
    log.info("총 푼 문제 수 보정 작업을 완료했습니다.");
  }
}
