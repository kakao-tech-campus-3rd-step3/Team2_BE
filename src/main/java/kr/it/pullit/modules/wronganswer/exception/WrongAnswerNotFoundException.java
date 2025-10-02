package kr.it.pullit.modules.wronganswer.exception;

import kr.it.pullit.shared.error.BusinessException;

public class WrongAnswerNotFoundException extends BusinessException {

  private WrongAnswerNotFoundException(Object... args) {
    super(WrongAnswerErrorCode.WRONG_ANSWER_NOT_FOUND, args);
  }

  public static WrongAnswerNotFoundException byMemberAndQuestion(long memberId, long questionId) {
    return new WrongAnswerNotFoundException("Member ID, Question ID", memberId + ", " + questionId);
  }

  public static WrongAnswerNotFoundException withMessage(String message) {
    return new WrongAnswerNotFoundException("조건", message);
  }
}
