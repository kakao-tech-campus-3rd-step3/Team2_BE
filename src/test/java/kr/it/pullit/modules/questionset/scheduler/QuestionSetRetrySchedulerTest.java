package kr.it.pullit.modules.questionset.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.event.QuestionSetCreatedEvent;
import kr.it.pullit.shared.event.EventPublisher;
import kr.it.pullit.support.annotation.SpringUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringUnitTest
@ContextConfiguration(classes = {QuestionSetRetryScheduler.class})
@DisplayName("QuestionSetRetryScheduler 단위 테스트")
class QuestionSetRetrySchedulerTest {

  @Autowired private QuestionSetRetryScheduler questionSetRetryScheduler;

  @MockitoBean private QuestionSetPublicApi questionSetPublicApi;

  @MockitoBean private EventPublisher eventPublisher;

  @Test
  @DisplayName("재시도할 FAILED 상태의 문제집이 있으면, 해당 문제집 하나만 재시도하고 이벤트를 발행한다")
  void retryFailedQuestionSetGeneration_whenRetryableSetExists_republishesEventForOne() {
    // given
    QuestionSet retryableSet = mock(QuestionSet.class);

    when(questionSetPublicApi.claimOneForRetry(3)).thenReturn(Optional.of(retryableSet));

    // when
    questionSetRetryScheduler.retryFailedQuestionSetGeneration();

    // then
    verify(eventPublisher, times(1)).publish(any(QuestionSetCreatedEvent.class));
  }

  @Test
  @DisplayName("재시도할 문제집이 없으면 아무 작업도 하지 않는다")
  void retryFailedQuestionSetGeneration_whenNoRetryableSets_doesNothing() {
    // given
    when(questionSetPublicApi.claimOneForRetry(3)).thenReturn(Optional.empty());

    // when
    questionSetRetryScheduler.retryFailedQuestionSetGeneration();

    // then
    verify(eventPublisher, never()).publish(any(QuestionSetCreatedEvent.class));
  }
}
