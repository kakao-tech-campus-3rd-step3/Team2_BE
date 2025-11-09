package kr.it.pullit.modules.learningsource.source.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SourceErrorCode implements ErrorCode {
  SOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "SE_001", "소스를 찾을 수 없습니다. (%s: %s)"),
  SOURCE_FORBIDDEN(HttpStatus.FORBIDDEN, "SE_002", "사용자 %s는 해당 소스를 삭제할 권한이 없습니다."),
  FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "SE_003", "파일 크기 제한을 초과했습니다."),
  S3_FILE_NOT_FOUND(
      HttpStatus.NOT_FOUND, "SE_004", "S3에 파일이 존재하지 않습니다. (sourceId: %d, filePath: '%s')"),
  SOURCE_NOT_EXIST_ON_S3(
      HttpStatus.BAD_REQUEST, "SE_005", "S3에 파일이 존재하지 않아 문제집을 생성할 수 없습니다. (Source IDs: %s)"),
  SOURCE_PROCESSING_FAILED(
      HttpStatus.BAD_REQUEST,
      "SE_006",
      "소스 파일 처리 실패로 인해 문제집을 생성할 수 없습니다. 해당 소스를 삭제 후 다시 시도해주세요. (Source IDs: %s)");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
