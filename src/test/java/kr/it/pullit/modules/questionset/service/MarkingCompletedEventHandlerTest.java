package kr.it.pullit.modules.questionset.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import kr.it.pullit.modules.projection.learnstats.events.publisher.LearnStatsEventPublisher;
import kr.it.pullit.modules.questionset.event.MarkingCompletedEvent;
import kr.it.pullit.modules.questionset.event.MarkingCompletedEventHandler;
import kr.it.pullit.modules.questionset.web.dto.response.MarkingResult;
import kr.it.pullit.support.annotation.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@UnitTest
class MarkingCompletedEventHandlerTest {

  @InjectMocks private MarkingCompletedEventHandler sut;

  @Mock private LearnStatsEventPublisher learnStatsEventPublisher;

  @Test
  @DisplayName("채점 완료 시 solvedQuestionCount가 0보다 크면 이벤트를 발행한다")
  void shouldPublishEventWhenResultExists() {
    // given
    var memberId = 1L;
    var results = List.of(new MarkingResult(1L, true));
    var event = new MarkingCompletedEvent(memberId, results, false);

    // when
    sut.handleMarkingCompletedEvent(event);

    // then
    verify(learnStatsEventPublisher).publishQuestionSetSolved(memberId, results.size());
  }

  @Test
  @DisplayName("재채점 시에는 이벤트를 발행하지 않는다")
  void shouldNotPublishEventWhenReviewing() {
    // given
    var memberId = 1L;
    var results = List.of(new MarkingResult(1L, true));
    var event = new MarkingCompletedEvent(memberId, results, true);

    // when
    sut.handleMarkingCompletedEvent(event);

    // then
    verify(learnStatsEventPublisher, never()).publishQuestionSetSolved(memberId, results.size());
  }

  @Test
  @DisplayName("채점 결과가 비어있으면 이벤트를 발행하지 않는다")
  void shouldNotPublishEventWhenResultsAreEmpty() {
    // given
    var memberId = 1L;
    var results = Collections.<MarkingResult>emptyList();
    var event = new MarkingCompletedEvent(memberId, results, false);

    // when
    sut.handleMarkingCompletedEvent(event);

    // then
    verify(learnStatsEventPublisher, never()).publishQuestionSetSolved(memberId, results.size());
  }
}
