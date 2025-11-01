package kr.it.pullit.platform.security.jwt.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class JwtAuthenticationException extends AuthenticationException {
  private final ErrorCode errorCode;

  public JwtAuthenticationException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public JwtAuthenticationException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public static JwtAuthenticationException from(ErrorCode errorCode) {
    return new JwtAuthenticationException(errorCode);
  }

  public static JwtAuthenticationException withMessage(ErrorCode errorCode, String message) {
    return new JwtAuthenticationException(errorCode, message);
  }
}
