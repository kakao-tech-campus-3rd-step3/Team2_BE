package kr.it.pullit.shared.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {
  INVALID_CONFIGURATION(HttpStatus.INTERNAL_SERVER_ERROR, "C_001", "서버 설정이 올바르지 않습니다: %s"),
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C_002", "잘못된 입력 값입니다."),
  UNSUPPORTED_HTTP_METHOD(HttpStatus.METHOD_NOT_ALLOWED, "C004", "지원하지 않는 HTTP Method입니다."),
  UNHANDLED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "C005", "알 수 없는 서버 에러입니다."),
  DUPLICATE_REQUEST(HttpStatus.CONFLICT, "C006", "이미 처리된 요청입니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
