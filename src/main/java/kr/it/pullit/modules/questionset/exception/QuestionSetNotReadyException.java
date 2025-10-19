package kr.it.pullit.modules.questionset.exception;

import kr.it.pullit.shared.error.BusinessException;

public class QuestionSetNotReadyException extends BusinessException {

  private QuestionSetNotReadyException(Object... args) {
    super(QuestionSetErrorCode.QUESTION_SET_NOT_READY, args);
  }

  public static QuestionSetNotReadyException byId(long id) {
    return new QuestionSetNotReadyException("ID", id);
  }

  public static QuestionSetNotReadyException withMessage(String message) {
    return new QuestionSetNotReadyException("조건", message);
  }
}
