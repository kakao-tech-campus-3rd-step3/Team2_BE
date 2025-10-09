package kr.it.pullit.modules.questionset.exception;

import kr.it.pullit.shared.error.BusinessException;
import kr.it.pullit.shared.error.ErrorCode;

public class InvalidQuestionException extends BusinessException {

  public InvalidQuestionException(ErrorCode errorCode) {
    super(errorCode);
  }
}
