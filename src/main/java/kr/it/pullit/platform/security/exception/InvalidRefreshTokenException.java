package kr.it.pullit.platform.security.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidRefreshTokenException extends RuntimeException {

  private final ErrorCode errorCode;

  public InvalidRefreshTokenException() {
    this.errorCode = ErrorCode.INVALID_REFRESH_TOKEN;
  }
}
