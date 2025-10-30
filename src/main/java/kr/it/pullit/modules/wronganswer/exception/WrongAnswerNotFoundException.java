package kr.it.pullit.modules.wronganswer.exception;

import kr.it.pullit.shared.error.BusinessException;

public class WrongAnswerNotFoundException extends BusinessException {

  private WrongAnswerNotFoundException(
      WrongAnswerErrorCode errorCode, Object... messageArguments) {
    super(errorCode, messageArguments);
  }

  public static WrongAnswerNotFoundException byMemberAndQuestion(long memberId, long questionId) {
    return new WrongAnswerNotFoundException(
        WrongAnswerErrorCode.WRONG_ANSWER_NOT_FOUND,
        "Member ID, Question ID",
        memberId + ", " + questionId);
  }

  public static WrongAnswerNotFoundException withMessage(String message) {
    return new WrongAnswerNotFoundException(
        WrongAnswerErrorCode.WRONG_ANSWER_NOT_FOUND, "조건", message);
  }

  public static WrongAnswerNotFoundException noWrongAnswersToReview() {
    return new WrongAnswerNotFoundException(
        WrongAnswerErrorCode.NO_WRONG_ANSWERS_TO_REVIEW);
  }
}
