package kr.it.pullit.modules.commonfolder.exception;

import org.springframework.http.HttpStatus;
import kr.it.pullit.shared.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonFolderErrorCode implements ErrorCode {
  CANNOT_DELETE_DEFAULT_FOLDER(HttpStatus.BAD_REQUEST, "CF_001", "기본 폴더는 삭제할 수 없습니다."),
  CANNOT_UPDATE_DEFAULT_FOLDER(HttpStatus.BAD_REQUEST, "CF_002", "기본 폴더명은 변경할 수 없습니다."),
  FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, "CF_003", "해당 폴더를 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
