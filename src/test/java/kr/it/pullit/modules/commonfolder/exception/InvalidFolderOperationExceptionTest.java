package kr.it.pullit.modules.commonfolder.exception;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.shared.error.ErrorCode;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@MockitoUnitTest
class InvalidFolderOperationExceptionTest {

  @Test
  @DisplayName("가변 인자를 받는 생성자는 메시지를 포맷팅한다")
  void constructorFormatsMessageWithArguments() {
    ErrorCode errorCode = new TestErrorCode();

    InvalidFolderOperationException exception =
        new InvalidFolderOperationException(errorCode, "name", 3);

    assertThat(exception.getErrorCode()).isEqualTo(errorCode);
    assertThat(exception).hasMessage("폴더 name 은(는) 3번 사용되었습니다.");
  }

  private static class TestErrorCode implements ErrorCode {

    @Override
    public HttpStatus getStatus() {
      return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getCode() {
      return "TST";
    }

    @Override
    public String getMessage() {
      return "폴더 %s 은(는) %d번 사용되었습니다.";
    }
  }
}
