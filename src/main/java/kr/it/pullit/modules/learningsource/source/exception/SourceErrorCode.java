package kr.it.pullit.modules.learningsource.source.exception;

import org.springframework.http.HttpStatus;
import kr.it.pullit.shared.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SourceErrorCode implements ErrorCode {
  SOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "SE_001", "소스를 찾을 수 없습니다. (%s: %s)"),
  SOURCE_FORBIDDEN(HttpStatus.FORBIDDEN, "SE_002", "사용자 %s는 해당 소스를 삭제할 권한이 없습니다."),
  FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "SE_003", "파일 크기 제한을 초과했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
