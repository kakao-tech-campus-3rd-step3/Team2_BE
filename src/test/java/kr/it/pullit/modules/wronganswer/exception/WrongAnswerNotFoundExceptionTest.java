package kr.it.pullit.modules.wronganswer.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WrongAnswerNotFoundException 단위 테스트")
class WrongAnswerNotFoundExceptionTest {

  @Test
  @DisplayName("byMemberAndQuestion는 멤버와 문제 ID를 포함한 메시지와 오류 코드를 생성한다")
  void shouldCreateExceptionByMemberAndQuestion() {
    WrongAnswerNotFoundException exception =
        WrongAnswerNotFoundException.byMemberAndQuestion(1L, 2L);

    assertThat(exception.getErrorCode()).isEqualTo(WrongAnswerErrorCode.WRONG_ANSWER_NOT_FOUND);
    assertThat(exception.getMessage()).isEqualTo("오답을 찾을 수 없습니다. (Member ID, Question ID: 1, 2)");
  }

  @Test
  @DisplayName("withMessage는 전달된 메시지를 포함한 기본 오류 코드를 생성한다")
  void shouldCreateExceptionWithCustomMessage() {
    WrongAnswerNotFoundException exception = WrongAnswerNotFoundException.withMessage("특정 조건");

    assertThat(exception.getErrorCode()).isEqualTo(WrongAnswerErrorCode.WRONG_ANSWER_NOT_FOUND);
    assertThat(exception.getMessage()).isEqualTo("오답을 찾을 수 없습니다. (조건: 특정 조건)");
  }

  @Test
  @DisplayName("noWrongAnswersToReview는 복습할 오답이 없을 때의 오류 코드를 반환한다")
  void shouldCreateExceptionForEmptyReviewTargets() {
    WrongAnswerNotFoundException exception = WrongAnswerNotFoundException.noWrongAnswersToReview();

    assertThat(exception.getErrorCode()).isEqualTo(WrongAnswerErrorCode.NO_WRONG_ANSWERS_TO_REVIEW);
    assertThat(exception.getMessage()).isEqualTo("복습할 오답이 없습니다.");
  }
}
