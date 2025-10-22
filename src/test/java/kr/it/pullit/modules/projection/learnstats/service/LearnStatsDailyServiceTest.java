package kr.it.pullit.modules.projection.learnstats.service;

import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.LocalDate;
import kr.it.pullit.modules.projection.learnstats.repository.LearnStatsDailyRepository;
import kr.it.pullit.support.annotation.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@UnitTest
@DisplayName("LearnStatsDailyService 단위 테스트")
@ContextConfiguration(classes = {LearnStatsDailyService.class})
class LearnStatsDailyServiceTest {

  @Autowired private LearnStatsDailyService service;

  @MockitoBean private LearnStatsDailyRepository learnStatsDailyRepository;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private Clock clock;

  @Test
  @DisplayName("일일 통계 추가 시, 오늘 날짜와 함께 리포지토리를 호출한다")
  void addDailyStats_callsRepositoryWithTodayDate() {
    // given
    Long memberId = 1L;
    int questionsDelta = 5;
    int questionSetsDelta = 1;
    LocalDate today = LocalDate.now(clock);

    // when
    service.addDailyStats(memberId, questionsDelta, questionSetsDelta);

    // then
    verify(learnStatsDailyRepository).upsertAdd(memberId, today, questionsDelta, questionSetsDelta);
  }
}
