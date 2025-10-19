package kr.it.pullit.modules.questionset.exception;

import kr.it.pullit.shared.error.BusinessException;

public class QuestionSetNotFoundException extends BusinessException {

  private QuestionSetNotFoundException(Object... args) {
    super(QuestionSetErrorCode.QUESTION_SET_NOT_FOUND, args);
  }

  public static QuestionSetNotFoundException byId(long id) {
    return new QuestionSetNotFoundException("ID", id);
  }

  public static QuestionSetNotFoundException withMessage(String message) {
    return new QuestionSetNotFoundException("조건", message);
  }
}
