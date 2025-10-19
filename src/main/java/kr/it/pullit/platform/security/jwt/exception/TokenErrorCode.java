package kr.it.pullit.platform.security.jwt.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TokenErrorCode implements ErrorCode {
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOK_001", "토큰이 만료되었습니다."),
  TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TOK_002", "유효하지 않은 토큰입니다."),
  TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "TOK_003", "토큰을 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
