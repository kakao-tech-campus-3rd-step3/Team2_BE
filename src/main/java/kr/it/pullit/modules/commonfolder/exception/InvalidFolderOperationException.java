package kr.it.pullit.modules.commonfolder.exception;

import kr.it.pullit.shared.error.BusinessException;
import kr.it.pullit.shared.error.ErrorCode;

public class InvalidFolderOperationException extends BusinessException {

  public InvalidFolderOperationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public InvalidFolderOperationException(ErrorCode errorCode, Object... args) {
    super(errorCode, args);
  }
}
