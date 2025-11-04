package kr.it.pullit.shared.error.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TestErrorCode implements ErrorCode {
  TEST_ERROR(HttpStatus.BAD_REQUEST, "T001", "테스트 비즈니스 예외");
  private final HttpStatus status;
  private final String code;
  private final String message;
}
