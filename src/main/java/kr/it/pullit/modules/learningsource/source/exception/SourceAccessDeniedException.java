package kr.it.pullit.modules.learningsource.source.exception;

import kr.it.pullit.shared.error.BusinessException;

public class SourceAccessDeniedException extends BusinessException {

  private SourceAccessDeniedException(Object... args) {
    super(SourceErrorCode.SOURCE_FORBIDDEN, args);
  }

  public static SourceAccessDeniedException byMember(Long memberId) {
    return new SourceAccessDeniedException(memberId);
  }
}
