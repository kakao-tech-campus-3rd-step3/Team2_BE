package kr.it.pullit.modules.questionset.exception;

import kr.it.pullit.shared.error.BusinessException;

public class QuestionNotFoundException extends BusinessException {

  private QuestionNotFoundException(Object... args) {
    super(QuestionErrorCode.QUESTION_NOT_FOUND, args);
  }

  public static QuestionNotFoundException byId(long id) {
    return new QuestionNotFoundException("ID", id);
  }

  public static QuestionNotFoundException withMessage(String message) {
    return new QuestionNotFoundException("조건", message);
  }
}
