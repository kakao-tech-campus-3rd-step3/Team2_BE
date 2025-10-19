package kr.it.pullit.modules.learningsource.source.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SourceErrorCode implements ErrorCode {
  SOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "SE_001", "소스를 찾을 수 없습니다. (%s: %s)");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
