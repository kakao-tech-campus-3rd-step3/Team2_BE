package kr.it.pullit.modules.auth.exception;

import kr.it.pullit.shared.error.BusinessException;

public class InvalidRefreshTokenException extends BusinessException {

  private InvalidRefreshTokenException() {
    super(AuthErrorCode.INVALID_REFRESH_TOKEN);
  }

  public static InvalidRefreshTokenException by() {
    return new InvalidRefreshTokenException();
  }
}
