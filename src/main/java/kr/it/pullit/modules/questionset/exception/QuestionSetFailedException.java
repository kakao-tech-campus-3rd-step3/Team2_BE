package kr.it.pullit.modules.questionset.exception;

import kr.it.pullit.shared.error.BusinessException;

public class QuestionSetFailedException extends BusinessException {

  private QuestionSetFailedException(Object... args) {
    super(QuestionSetErrorCode.QUESTION_SET_FAILED, args);
  }

  public static QuestionSetFailedException byId(long id) {
    return new QuestionSetFailedException("ID", id);
  }

  public static QuestionSetFailedException withMessage(String message) {
    return new QuestionSetFailedException("조건", message);
  }
}
