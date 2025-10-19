package kr.it.pullit.modules.learningsource.source.exception;

import kr.it.pullit.shared.error.BusinessException;

public class SourceNotFoundException extends BusinessException {

  private SourceNotFoundException(Object... args) {
    super(SourceErrorCode.SOURCE_NOT_FOUND, args);
  }

  public static SourceNotFoundException byId(long id) {
    return new SourceNotFoundException("ID", id);
  }

  public static SourceNotFoundException withMessage(String message) {
    return new SourceNotFoundException("조건", message);
  }
}
