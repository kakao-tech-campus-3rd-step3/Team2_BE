package kr.it.pullit.modules.questionset.exception;

import kr.it.pullit.shared.error.BusinessException;

public class QuestionSetUnauthorizedException extends BusinessException {

  private QuestionSetUnauthorizedException(Object... args) {
    super(QuestionSetErrorCode.QUESTION_SET_UNAUTHORIZED, args);
  }

  public static QuestionSetUnauthorizedException byId(long id) {
    return new QuestionSetUnauthorizedException("ID", id);
  }
}
