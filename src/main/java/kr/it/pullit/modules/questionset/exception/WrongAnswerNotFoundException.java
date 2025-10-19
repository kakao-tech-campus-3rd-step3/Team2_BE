package kr.it.pullit.modules.questionset.exception;

import kr.it.pullit.shared.error.BusinessException;

public class WrongAnswerNotFoundException extends BusinessException {

  public WrongAnswerNotFoundException() {
    super(QuestionSetErrorCode.WRONG_ANSWER_NOT_FOUND);
  }
}
