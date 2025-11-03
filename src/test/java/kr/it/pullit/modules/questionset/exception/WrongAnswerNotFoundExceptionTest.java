package kr.it.pullit.modules.questionset.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WrongAnswerNotFoundExceptionTest {

  @Test
  @DisplayName("오답 미존재 예외는 미리 정의된 에러 코드를 포함한다")
  void wrongAnswerNotFoundExceptionContainsErrorCode() {
    WrongAnswerNotFoundException exception = new WrongAnswerNotFoundException();

    assertThat(exception.getErrorCode()).isEqualTo(QuestionSetErrorCode.WRONG_ANSWER_NOT_FOUND);
    assertThat(exception.getMessage()).isEqualTo("복습할 오답이 없습니다.");
  }
}
