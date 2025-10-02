package kr.it.pullit.modules.auth.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH006", "리프레시 토큰이 유효하지 않습니다."),
  INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH007", "액세스 토큰이 유효하지 않습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
