package kr.it.pullit.modules.projection.learnstats.batch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsEventPublicApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearnStatsBatchScheduler {

  private final MemberPublicApi memberPublicApi;
  private final LearnStatsEventPublicApi learnStatsEventPublicApi;

  /** 매주 월요일 오전 4시에 실행 */
  @Scheduled(cron = "0 0 4 * * MON")
  public void triggerWeeklyReset() {
    log.info("주간 학습 통계 초기화 작업을 시작합니다.");

    Pageable pageable = PageRequest.of(0, 100);
    Page<Member> memberPage;

    do {
      memberPage = memberPublicApi.findAll(pageable);
      memberPage
          .getContent()
          .forEach(
              member -> {
                log.info("{}번 회원의 주간 통계를 초기화합니다.", member.getId());
                learnStatsEventPublicApi.publishWeeklyReset(member.getId());
              });
      pageable = memberPage.nextPageable();
    } while (memberPage.hasNext());

    log.info("주간 학습 통계 초기화 작업을 완료했습니다.");
  }
}
