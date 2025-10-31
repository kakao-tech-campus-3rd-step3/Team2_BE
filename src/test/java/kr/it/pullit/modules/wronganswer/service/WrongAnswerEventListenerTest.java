package kr.it.pullit.modules.wronganswer.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import kr.it.pullit.modules.questionset.event.MarkingCompletedEvent;
import kr.it.pullit.modules.questionset.web.dto.response.MarkingResultDto;
import kr.it.pullit.modules.wronganswer.api.WrongAnswerPublicApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("WrongAnswerEventListener 단위 테스트")
class WrongAnswerEventListenerTest {

  private static final long MEMBER_ID = 55L;

  @Mock private WrongAnswerPublicApi wrongAnswerPublicApi;

  private WrongAnswerEventListener wrongAnswerEventListener;

  @BeforeEach
  void setUp() {
    wrongAnswerEventListener = new WrongAnswerEventListener(wrongAnswerPublicApi);
  }

  @Test
  @DisplayName("검토 중 정답으로 맞힌 문제는 정답 처리 API로 전달한다")
  void shouldMarkCorrectAnswersWhenReviewing() {
    MarkingCompletedEvent event =
        new MarkingCompletedEvent(
            MEMBER_ID,
            List.of(
                MarkingResultDto.of(1L, true),
                MarkingResultDto.of(2L, false),
                MarkingResultDto.of(3L, true)),
            true);

    wrongAnswerEventListener.handleMarkingCompletedEvent(event);

    verify(wrongAnswerPublicApi).markAsCorrectAnswers(MEMBER_ID, List.of(1L, 3L));
    verify(wrongAnswerPublicApi, never()).markAsWrongAnswers(anyLong(), anyList());
  }

  @Test
  @DisplayName("검토 중이 아니면 오답으로 제출된 문제만 오답 처리 API로 전달한다")
  void shouldMarkWrongAnswersWhenNotReviewing() {
    MarkingCompletedEvent event =
        new MarkingCompletedEvent(
            MEMBER_ID,
            List.of(
                MarkingResultDto.of(7L, true),
                MarkingResultDto.of(8L, false),
                MarkingResultDto.of(9L, false)),
            false);

    wrongAnswerEventListener.handleMarkingCompletedEvent(event);

    verify(wrongAnswerPublicApi).markAsWrongAnswers(MEMBER_ID, List.of(8L, 9L));
    verify(wrongAnswerPublicApi, never()).markAsCorrectAnswers(anyLong(), anyList());
  }
}
