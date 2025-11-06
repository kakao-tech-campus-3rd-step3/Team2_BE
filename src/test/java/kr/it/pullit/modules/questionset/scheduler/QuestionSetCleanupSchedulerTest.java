package kr.it.pullit.modules.questionset.scheduler;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.support.annotation.SpringUnitTest;
import kr.it.pullit.support.config.MutableClockConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringUnitTest
@Import({QuestionSetCleanupScheduler.class, MutableClockConfig.class})
@DisplayName("QuestionSetCleanupScheduler 단위 테스트")
class QuestionSetCleanupSchedulerTest {

  @Autowired private QuestionSetCleanupScheduler questionSetCleanupScheduler;

  @MockitoBean private QuestionSetPublicApi questionSetPublicApi;

  @Autowired private Clock clock;

  @Test
  @DisplayName("60분 이상 PENDING 상태인 문제집이 있으면 FAILED로 변경한다")
  void cleanupStalePendingQuestionSets_whenStaleSetsExist_marksThemAsFailed() {
    // given
    QuestionSet staleSet1 = mock(QuestionSet.class);
    when(staleSet1.getId()).thenReturn(1L);

    QuestionSet staleSet2 = mock(QuestionSet.class);
    when(staleSet2.getId()).thenReturn(2L);

    List<QuestionSet> staleQuestionSets = List.of(staleSet1, staleSet2);

    LocalDateTime threshold = LocalDateTime.now(clock).minusMinutes(60);

    when(questionSetPublicApi.findStalePending(threshold)).thenReturn(staleQuestionSets);

    // when
    questionSetCleanupScheduler.cleanupStalePendingQuestionSets();

    // then
    verify(questionSetPublicApi).markAsFailed(1L);
    verify(questionSetPublicApi).markAsFailed(2L);
  }

  @Test
  @DisplayName("오래된 문제집이 없으면 아무 작업도 하지 않는다")
  void cleanupStalePendingQuestionSets_whenNoStaleSets_doesNothing() {
    // given
    LocalDateTime threshold = LocalDateTime.now(clock).minusMinutes(60);

    when(questionSetPublicApi.findStalePending(threshold)).thenReturn(Collections.emptyList());

    // when
    questionSetCleanupScheduler.cleanupStalePendingQuestionSets();

    // then
    verify(questionSetPublicApi, never()).markAsFailed(anyLong());
  }
}
