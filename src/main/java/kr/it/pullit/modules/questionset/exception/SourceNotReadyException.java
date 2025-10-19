package kr.it.pullit.modules.questionset.exception;

import kr.it.pullit.shared.error.BusinessException;

public class SourceNotReadyException extends BusinessException {

  public SourceNotReadyException() {
    super(QuestionSetErrorCode.SOURCE_NOT_READY);
  }
}
