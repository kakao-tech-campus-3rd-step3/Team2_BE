package kr.it.pullit.modules.projection.learnstats.event.handler;

import static org.mockito.Mockito.verify;

import kr.it.pullit.modules.projection.learnstats.service.LearnStatsProjectionService;
import kr.it.pullit.modules.questionset.service.event.StudySessionFinishedEvent;
import kr.it.pullit.support.annotation.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@UnitTest
class LearnStatsEventHandlerTest {

  @InjectMocks private LearnStatsEventHandler sut;

  @Mock private LearnStatsProjectionService projectionService;

  @Test
  @DisplayName("학습 세션 완료 이벤트를 수신하면 학습 시간을 통계에 반영한다")
  void shouldApplyStudySessionTimeToStats() {
    // given
    var memberId = 1L;
    var durationSeconds = 3600L;
    var event = new StudySessionFinishedEvent(memberId, durationSeconds);

    // when
    sut.handleStudySessionFinished(event);

    // then
    verify(projectionService).applyStudySessionFinished(memberId, durationSeconds);
  }
}
