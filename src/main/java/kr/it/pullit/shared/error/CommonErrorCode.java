package kr.it.pullit.shared.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {
  INVALID_CONFIGURATION(HttpStatus.INTERNAL_SERVER_ERROR, "C_001", "서버 설정이 올바르지 않습니다: %s");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
