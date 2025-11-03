package kr.it.pullit.modules.questionset.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuestionSetNotCompleteExceptionTest {

  @Test
  @DisplayName("ID 기반 팩토리 메서드는 공통 에러 코드를 반환한다")
  void byIdUsesQuestionSetNotCompleteErrorCode() {
    QuestionSetNotCompleteException exception = QuestionSetNotCompleteException.byId(12L);

    assertThat(exception.getErrorCode()).isEqualTo(QuestionSetErrorCode.QUESTION_SET_NOT_COMPLETE);
    assertThat(exception.getMessage()).isEqualTo("문제집이 완료되지 않았습니다.");
  }

  @Test
  @DisplayName("메시지 기반 팩토리 메서드도 공통 메시지를 유지한다")
  void withMessageUsesSharedMessage() {
    QuestionSetNotCompleteException exception =
        QuestionSetNotCompleteException.withMessage("풀이가 모두 끝나지 않았습니다.");

    assertThat(exception.getErrorCode()).isEqualTo(QuestionSetErrorCode.QUESTION_SET_NOT_COMPLETE);
    assertThat(exception.getMessage()).isEqualTo("문제집이 완료되지 않았습니다.");
  }
}
