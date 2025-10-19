package kr.it.pullit.platform.security.jwt.exception;

import kr.it.pullit.shared.error.BusinessException;
import kr.it.pullit.shared.error.ErrorCode;

public class TokenException extends BusinessException {

  public TokenException(ErrorCode errorCode) {
    super(errorCode);
  }

  public TokenException(ErrorCode errorCode, String message, Throwable cause) {
    super(errorCode, message, cause);
  }
}
