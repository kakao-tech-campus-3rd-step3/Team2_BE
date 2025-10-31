package kr.it.pullit.modules.wronganswer.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

//TODO: 서비스 단위테스트에서 에러코드를 검증하도록 변경 필요.
@DisplayName("WrongAnswerErrorCode 단위 테스트")
class WrongAnswerErrorCodeTest {

  @Test
  @DisplayName("WRONG_ANSWER_NOT_FOUND 코드는 404 상태와 메시지를 제공한다")
  void shouldExposeMetadataForWrongAnswerNotFound() {
    WrongAnswerErrorCode errorCode = WrongAnswerErrorCode.WRONG_ANSWER_NOT_FOUND;

    assertThat(errorCode.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(errorCode.getCode()).isEqualTo("WAE_001");
    assertThat(errorCode.getMessage()).isEqualTo("오답을 찾을 수 없습니다. (%s: %s)");
  }

  @Test
  @DisplayName("NO_WRONG_ANSWERS_TO_REVIEW 코드는 복습할 오답이 없음을 나타낸다")
  void shouldExposeMetadataForEmptyReview() {
    WrongAnswerErrorCode errorCode = WrongAnswerErrorCode.NO_WRONG_ANSWERS_TO_REVIEW;

    assertThat(errorCode.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(errorCode.getCode()).isEqualTo("WAN_002");
    assertThat(errorCode.getMessage()).isEqualTo("복습할 오답이 없습니다.");
  }
}
