package kr.it.pullit.modules.questionset.exception;

import kr.it.pullit.shared.error.BusinessException;

public class QuestionSetUnprocessableException extends BusinessException {

  private QuestionSetUnprocessableException(Object... args) {
    super(QuestionSetErrorCode.QUESTION_SET_UNPROCESSABLE, args);
  }

  public static QuestionSetUnprocessableException byId(long id) {
    return new QuestionSetUnprocessableException("ID", id);
  }
}
