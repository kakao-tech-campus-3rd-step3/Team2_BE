package kr.it.pullit.modules.auth.exception;

import kr.it.pullit.shared.error.BusinessException;

public class InvalidAccessTokenException extends BusinessException {

  private InvalidAccessTokenException() {
    super(AuthErrorCode.INVALID_ACCESS_TOKEN);
  }

  public static InvalidAccessTokenException by() {
    return new InvalidAccessTokenException();
  }
}
