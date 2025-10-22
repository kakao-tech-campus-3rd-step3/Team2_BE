package kr.it.pullit.modules.commonfolder.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonFolderErrorCode implements ErrorCode {
  CANNOT_DELETE_DEFAULT_FOLDER(HttpStatus.BAD_REQUEST, "CF_001", "기본 폴더는 삭제할 수 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
