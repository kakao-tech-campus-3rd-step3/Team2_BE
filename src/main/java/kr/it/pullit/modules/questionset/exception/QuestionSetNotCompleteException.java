package kr.it.pullit.modules.questionset.exception;

import kr.it.pullit.shared.error.BusinessException;

public class QuestionSetNotCompleteException extends BusinessException {

  @SuppressWarnings("suppressLintsFor")
  private QuestionSetNotCompleteException(Object... args) {
    super(QuestionSetErrorCode.QUESTION_SET_NOT_COMPLETE, args);
  }

  public static QuestionSetNotCompleteException byId(long id) {
    return new QuestionSetNotCompleteException("ID", id);
  }

  public static QuestionSetNotCompleteException withMessage(String message) {
    return new QuestionSetNotCompleteException("조건", message);
  }
}
